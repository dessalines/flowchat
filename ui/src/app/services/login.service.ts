import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
import { Http, Response } from '@angular/http';
import { Headers, RequestOptions } from '@angular/http';
import {User} from '../shared';
import {UserService} from './user.service';


@Injectable()
export class LoginService {

  private getOrCreateUrl: string = 'http://localhost:4567/get_user';
  private loginUrl: string = 'http://localhost:4567/login';
  private signupUrl: string = 'http://localhost:4567/signup';

  constructor(private http: Http,
    private userService: UserService) { 
  }

  getOrCreateUser(): Observable<User> {
    return this.http.get(this.getOrCreateUrl, this.userService.getOptions())
      .map(this.extractData)
      .catch(this.handleError);
  }

  login(usernameOrEmail: string, password: string): Observable<User> {
    let reqBody: string = JSON.stringify({ usernameOrEmail, password });
    return this.http.post(this.loginUrl, reqBody)
      .map(this.extractData)
      .catch(this.handleError);
  }

  signup(username: string, password: string, email: string): Observable<User> {
    let reqBody: string = JSON.stringify({ username, password, email });
    return this.http.post(this.signupUrl, reqBody)
      .map(this.extractData)
      .catch(this.handleError);
  }

  private handleError(error: any) {
    // We'd also dig deeper into the error to get a better message
    let errMsg = error.json().message;
    return Observable.throw(errMsg);
  }

  private extractData(res: Response) {
    let body = res.json();
    // console.log(body);
    return body || {};
  }



}
