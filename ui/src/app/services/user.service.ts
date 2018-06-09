import { Injectable } from '@angular/core';
import {User, UserSettings, Discussion, Discussions, Tools, Community, Communities} from '../shared';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';
import { Headers, RequestOptions, Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
import {environment} from '../../environments/environment';
import { JwtHelperService } from '@auth0/angular-jwt';
import { ToasterService } from 'angular2-toaster';

@Injectable()
export class UserService {

  private user: User;
  private jwtHelper: JwtHelperService = new JwtHelperService();


  private favoriteDiscussions: Array<Discussion> = [];
  private favoriteCommunities: Array<Community> = [];

  private userSource = new BehaviorSubject<User>(this.user);

  public userObservable = this.userSource.asObservable();

  private userSearchUrl: string = environment.endpoint + 'user_search/';

  private favoriteDiscussionUrl: string = environment.endpoint + 'favorite_discussion';
  private favoriteDiscussionsUrl: string = environment.endpoint + 'favorite_discussions';

  private favoriteCommunityUrl: string = environment.endpoint + 'favorite_community';
  private favoriteCommunitiesUrl: string = environment.endpoint + 'favorite_communities';

  private userUrl: string = environment.endpoint + 'user';
  private userSettingUrl: string = environment.endpoint + 'user_setting';
  private getUserLogUrl: string = environment.endpoint + 'user_log';


  private defaultSettings: UserSettings = {
    defaultViewTypeRadioValue: 'card',
    defaultSortTypeRadioValue: 'time-86400',
    readOnboardAlert: false
  }

  constructor(private http: Http) {
    this.setUserFromCookie();
  }

	public setUserFromCookie() {
		let jwt = Tools.readCookie('jwt');
		if (jwt) {
      this.setUser(jwt);
		}
	}

	private setUser(jwt: string) {
		let dJWT = this.jwtHelper.decodeToken(jwt);
		this.user = {
			id: dJWT.user_id,
			name: dJWT.user_name,
      jwt: jwt,
      fullUser: dJWT.full_user
    };
    this.fetchFavoriteDiscussions();
    this.fetchFavoriteCommunities();
    this.fetchUserSettings();

  }
  
  public setUserAndCookie(jwt: string) {
    
  }

	public getUser(): User {
		return this.user;
	}

	createNewUser(nameStr: string): Observable<string> {
		let name = JSON.stringify({ name: nameStr });

		return this.http.post(this.userUrl, name)
			.map(r => r.text())
			.catch(this.handleError);
  }

  logout() {
    // Log out
    this.user = {
      id: null,
      name: null,
      jwt: null,
      settings: this.defaultSettings
    };

    this.favoriteDiscussions = [];
    this.clearCookies();

  }

  public getFavoriteDiscussions(): Array<Discussion> {
    return this.favoriteDiscussions;
  }

  public getFavoriteCommunities(): Array<Community> {
    return this.favoriteCommunities;
  }

  getUserSettings(): UserSettings {
    if (this.user === undefined || this.user.settings === undefined) {
      return this.defaultSettings;
    } else {
      return this.user.settings;
    }
  }

  sendLoginEvent() {
    this.userSource.next(this.user);
  }

  clearCookies() {
    Tools.eraseCookie("jwt");
  }

	getOptions(): RequestOptions {
		let headers = new Headers({
			// 'Content-Type': 'application/json',
			'token': this.getUser().jwt
		});
		return new RequestOptions({ headers: headers });
	}


  searchUsers(query: string): Observable<Array<User>> {
    return this.http.get(this.userSearchUrl + query)
      .map(r => r.json().users)
      .catch(this.handleError);
  }

  fetchFavoriteDiscussions() {
    this.fetchFavoriteDiscussionsObs().subscribe(d => {
      this.favoriteDiscussions = d.discussions;
    },
      error => console.error(error));
  }


  private fetchFavoriteDiscussionsObs(): Observable<Discussions> {
    return this.http.get(this.favoriteDiscussionsUrl, this.getOptions())
      .map(r => r.json())
      .catch(this.handleError);
  }

  removeFavoriteDiscussion(discussionId: number) {
    // Remove it from the list
    let discussion = this.favoriteDiscussions.filter(discussion => discussion.id == discussionId)[0];
    let index = this.favoriteDiscussions.indexOf(discussion);

    this.favoriteDiscussions.splice(index, 1);

    this.removeFavoriteDiscussionObjs(discussionId).subscribe(null,
      error => console.error(error));

  }

  private removeFavoriteDiscussionObjs(discussionId: number) {
    return this.http.delete(this.favoriteDiscussionUrl + '/' + discussionId, this.getOptions())
      .map(this.extractData)
      .catch(this.handleError);
  }

  // This is different, because it doesn't actually do an http fetch
  pushToFavoriteDiscussions(discussion: Discussion) {

    if (this.favoriteDiscussions === undefined) {
      this.favoriteDiscussions = [];
    }

    this.favoriteDiscussions.push(discussion);
  }


  fetchFavoriteCommunities(): Subscription {
    return this.fetchFavoriteCommunitiesObs().subscribe(d => {
      this.favoriteCommunities = d.communities;
    },
      error => console.error(error));
  }

  private fetchFavoriteCommunitiesObs(): Observable<Communities> {
    return this.http.get(this.favoriteCommunitiesUrl, this.getOptions())
      .map(r => r.json())
      .catch(this.handleError);
  }

  removeFavoriteCommunity(communityId: number) {
    // Remove it from the list
    let community = this.favoriteCommunities.filter(community => community.id == communityId)[0];
    let index = this.favoriteCommunities.indexOf(community);

    this.favoriteCommunities.splice(index, 1);

    this.removeFavoriteCommunityObjs(communityId).subscribe(null,
      error => console.error(error));

  }

  private removeFavoriteCommunityObjs(communityId: number) {
    return this.http.delete(this.favoriteCommunityUrl +  '/' + communityId, this.getOptions())
      .map(this.extractData)
      .catch(this.handleError);
  }

  saveFavoriteCommunity(community: Community) {
    if (this.favoriteCommunities === undefined) {
      this.favoriteCommunities = [];
    }

    this.favoriteCommunities.push(community);
    this.saveFavoriteCommunityObjs(community.id).subscribe(null,
      error => console.error(error));
  }

  private saveFavoriteCommunityObjs(communityId: number) {
    return this.http.post(this.favoriteCommunityUrl +  '/' +  communityId, null, this.getOptions())
      .map(this.extractData)
      .catch(this.handleError);
  }

  hasFavoriteCommunity(community: Community): boolean {
    return (this.favoriteCommunities !== undefined &&
      this.favoriteCommunities.filter(c => community.id == c.id)[0] !== undefined);
  }

  fetchUserSettings() {
    this.fetchUserSettingsObs().subscribe(d => {
      
      this.user.settings = {
        defaultSortTypeRadioValue: d["defaultSortTypeRadioValue"],
        defaultViewTypeRadioValue: d["defaultViewTypeRadioValue"],
        readOnboardAlert: d["readOnboardAlert"]
      }
      this.sendLoginEvent();
    },
      error => console.error(error));
  }

  private fetchUserSettingsObs(): Observable<UserSettings> {
    return this.http.get(this.userSettingUrl, this.getOptions())
      .map(r => r.json())
      .catch(this.handleError);
  }

  saveUserSettings() {
    this.saveUserSettingsObs().subscribe(d => {
      this.sendLoginEvent();
    },
      error => console.error(error));
  }

  private saveUserSettingsObs() {
    return this.http.put(this.userSettingUrl, this.user.settings, this.getOptions())
      .map(this.extractData)
      .catch(this.handleError);
  }

  getUserLog(id: number) {
    return this.http.get(this.getUserLogUrl + id, this.getOptions())
      .map(this.extractData)
      .catch(this.handleError);
  }


  private handleError(error: any) {
    // We'd also dig deeper into the error to get a better message
    // let errMsg = error.json().message;
    let errMsg = error;
    return Observable.throw(errMsg);
  }

  private extractData(res: Response) {
    let body = res.json();
    return body || {};
  }

}





