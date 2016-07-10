import { Injectable } from '@angular/core';
import {User, Discussion, Tools} from '../shared';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';
import { Headers, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
import { Http, Response } from '@angular/http';
import {environment} from '../environment';

@Injectable()
export class UserService {

  private user: User;

  private favoriteDiscussions: Array<Discussion> = [];

  private userSource = new BehaviorSubject<User>(this.user);

  public userObservable = this.userSource.asObservable();

  private queryUsersUrl: string = environment.endpoint + '/user_search/';

  private fetchFavoriteDiscussionsUrl: string = environment.endpoint + 'get_favorite_discussions';
  private removeFavoriteDiscussionUrl: string = environment.endpoint + 'remove_favorite_discussion/';


  constructor(private http: Http) {
    this.setUserFromCookie();
    this.fetchFavoriteDiscussions();
  }

  public getUser(): User {
    return this.user;
  }

  public isAnonymousUser(): boolean {
    return this.user != null &&
      (this.user.auth === undefined || this.user.auth == 'undefined');
  }

  public isFullUser() {
    return this.user != null &&
      !(this.user.auth === undefined || this.user.auth == 'undefined');
  }

  public setUser(user: User) {
    this.user = user;
    this.setCookies(this.user);
  }

  setUserFromCookie() {
    if (Tools.readCookie("uid") != null) {
      this.user = {
        id: Number(Tools.readCookie("uid")),
        name: Tools.readCookie("name"),
        auth: Tools.readCookie("auth")
      }
    }
    console.log(this.user);
  }

  logout() {
    // Log out
    this.user = {
      id: null,
      name: null,
      auth: null
    }

    this.clearCookies();

  }

  sendLoginEvent(user: User) {
    this.userSource.next(user);
  }


  setCookies(user: User) {
    Tools.createCookie("uid", user.id, user.expire_time);
    Tools.createCookie("auth", user.auth, user.expire_time);
    Tools.createCookie("name", user.name, user.expire_time);
  }

  clearCookies() {
    Tools.eraseCookie("uid");
    Tools.eraseCookie("auth");
    Tools.eraseCookie("name");
  }


  getOptions(): RequestOptions {
    let headers = new Headers(
      {
        'Content-Type': 'application/json',
        'user': JSON.stringify(this.getUser())
      });
    return new RequestOptions({ headers: headers });
  }

  searchUsers(query: string) {
    return this.http.get(this.queryUsersUrl + query)
      .map(this.extractData)
      .catch(this.handleError);
  }

  private fetchFavoriteDiscussionsObs() {
    return this.http.get(this.fetchFavoriteDiscussionsUrl, this.getOptions())
      .map(this.extractData)
      .catch(this.handleError);
  }

  fetchFavoriteDiscussions() {
    this.fetchFavoriteDiscussionsObs().subscribe(d => {
      this.favoriteDiscussions = d.discussions;
      console.log(this.favoriteDiscussions);
    },
      error => console.log(error));
  }

  removeFavoriteDiscussion(discussionId: number) {
    // Remove it from the list
    let discussion = this.favoriteDiscussions.filter(discussion => discussion.id == discussionId)[0];
    let index = this.favoriteDiscussions.indexOf(discussion);

    this.favoriteDiscussions.splice(index, 1);

    this.removeFavoriteDiscussionObjs(discussionId).subscribe(null,
      error => console.log(error));

  }

  private removeFavoriteDiscussionObjs(discussionId: number) {
    return this.http.post(this.removeFavoriteDiscussionUrl + '/' + discussionId, null, this.getOptions())
      .map(this.extractData)
      .catch(this.handleError);
  }

  getFavoriteDiscussions(): Array<Discussion> {
    return this.favoriteDiscussions;
  }

  // This is different, because it doesn't actually do an http fetch
  updateFavoriteDiscussions(discussion: Discussion) {

    if (this.favoriteDiscussions === undefined) {
      this.favoriteDiscussions = [];
    }

    this.favoriteDiscussions.push(discussion);
    console.log(this.favoriteDiscussions);
    // // Find it
    // discussion = this.favoriteDiscussions.filter(discussion => discussion.id == discussionId)[0];

    // console.log("discussion is");
    // console.log(discussion);


    // if (discussion === undefined) {
    //   // then you have to refetch(for the name anyway)
    //   this.fetchFavoriteDiscussions();
    // }
  }

  private handleError(error: any) {
    // We'd also dig deeper into the error to get a better message
    // let errMsg = error.json().message;
    let errMsg = error;
    return Observable.throw(errMsg);
  }

  private extractData(res: Response) {
    let body = res.json();
    console.log(body);
    return body || {};
  }

}
