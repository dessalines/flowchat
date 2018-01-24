import { Injectable } from '@angular/core';
import {User, UserSettings, Discussion, Discussions, Tools, Community, Communities} from '../shared';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';
import { Headers, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
import { Http, Response } from '@angular/http';
import {environment} from '../../environments/environment';

@Injectable()
export class UserService {

  private user: User;

  private favoriteDiscussions: Array<Discussion> = [];
  private favoriteCommunities: Array<Community> = [];

  private userSource = new BehaviorSubject<User>(this.user);

  public userObservable = this.userSource.asObservable();

  private queryUsersUrl: string = environment.endpoint + 'user_search/';

  private fetchFavoriteDiscussionsUrl: string = environment.endpoint + 'favorite_discussions';
  private removeFavoriteDiscussionUrl: string = environment.endpoint + 'favorite_discussion/';

  private saveFavoriteCommunityUrl: string = environment.endpoint + 'favorite_community/';
  private fetchFavoriteCommunitiesUrl: string = environment.endpoint + 'favorite_communities';
  private removeFavoriteCommunityUrl: string = environment.endpoint + 'favorite_community/';

  private getUserLogUrl: string = environment.endpoint + 'user_log/';

  private saveUserUrl: string = environment.endpoint + 'user';

  private defaultSettings: UserSettings = {
    defaultViewTypeRadioValue: 'card',
    defaultSortTypeRadioValue: 'time-86400',
    readOnboardAlert: false
  }

  constructor(private http: Http) {
    this.setUserFromCookie();
    this.fetchFavoriteDiscussions();
    this.fetchFavoriteCommunities();
  }

  public getUser(): User {
    return this.user;
  }

  public getFavoriteDiscussions(): Array<Discussion> {
    return this.favoriteDiscussions;
  }

  public getFavoriteCommunities(): Array<Community> {
    return this.favoriteCommunities;
  }

  public isAnonymousUser(): boolean {
    return this.user != null &&
      (this.user.auth === undefined || this.user.auth == 'undefined' || this.user.auth == null);
  }

  public isFullUser() {
    return this.user != null &&
      !(this.user.auth === undefined || this.user.auth == 'undefined' || this.user.auth == null);
  }

  getUserSettings(): UserSettings {
    if (this.user == undefined) {
      return this.defaultSettings;
    } else {
      return this.user.settings;
    }
  }

  setUser(user: User) {
    this.user = user;
    this.setCookies(user);
    this.fetchFavoriteDiscussions();
    this.fetchFavoriteCommunities();
  }

  setUserSettings(settings: UserSettings) {
    this.user.settings = settings;
    this.setCookies(this.user);
  }

  setUserFromCookie() {
    if (Tools.readCookie("user") != null) {
      this.user = JSON.parse(Tools.readCookie("user"));
    }
  }

  logout() {
    // Log out
    this.user = {
      id: null,
      name: null,
      auth: null,
      settings: this.defaultSettings
    };

    this.favoriteDiscussions = [];
    this.clearCookies();

  }

  sendLoginEvent(user: User) {
    this.userSource.next(user);
  }


  public saveUser(): Observable<User> {
    return this.http.put(this.saveUserUrl, this.user)
      .map(r => r.json())
      .catch(this.handleError);
  }


  setCookies(user: User) {
    this.clearCookies();
    Tools.createCookie("user", JSON.stringify(user), user.expire_time);
  }

  clearCookies() {
    Tools.eraseCookie("user");
  }


  getOptions(): RequestOptions {
    let headers = new Headers({
      // 'Content-Type': 'application/json',
      'user': JSON.stringify(this.getUser())
    });
    return new RequestOptions({ headers: headers });
  }

  searchUsers(query: string): Observable<Array<User>> {
    return this.http.get(this.queryUsersUrl + query)
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
    return this.http.get(this.fetchFavoriteDiscussionsUrl, this.getOptions())
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
    return this.http.delete(this.removeFavoriteDiscussionUrl + discussionId, this.getOptions())
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


  fetchFavoriteCommunities() {
    this.fetchFavoriteCommunitiesObs().subscribe(d => {
      this.favoriteCommunities = d.communities;
    },
      error => console.error(error));
  }

  private fetchFavoriteCommunitiesObs(): Observable<Communities> {
    return this.http.get(this.fetchFavoriteCommunitiesUrl, this.getOptions())
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
    return this.http.delete(this.removeFavoriteCommunityUrl + communityId, this.getOptions())
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
    return this.http.post(this.saveFavoriteCommunityUrl + communityId, null, this.getOptions())
      .map(this.extractData)
      .catch(this.handleError);
  }

  hasFavoriteCommunity(community: Community): boolean {
    return (this.favoriteCommunities !== undefined &&
      this.favoriteCommunities.filter(c => community.id == c.id)[0] !== undefined);
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
