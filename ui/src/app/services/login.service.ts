import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { Http, Response } from '@angular/http';
import { Headers, RequestOptions } from '@angular/http';
import { User } from '../shared';
import { UserService } from './user.service';
import { environment } from '../../environments/environment';


@Injectable()
export class LoginService {

  private getOrCreateUrl: string = environment.endpoint + 'user';
  private loginUrl: string = environment.endpoint + 'login';
  private signupUrl: string = environment.endpoint + 'signup';

  constructor(private http: Http,
    private userService: UserService) {
  }

  login(usernameOrEmail: string, password: string): Observable<string> {
    let reqBody: string = JSON.stringify({ usernameOrEmail, password });
    return this.http.post(this.loginUrl, reqBody)
      .map(r => r.text())
      .catch(this.handleError);
  }

  signup(username: string, password: string, verifyPassword: string, email: string): Observable<string> {
    let options = (this.userService.getUser()) ? this.userService.getOptions() : null;
    console.log(options);
    let reqBody: string = JSON.stringify({ username, password, verifyPassword, email });
    return this.http.post(this.signupUrl, reqBody, options)
      .map(r => r.text())
      .catch(this.handleError);
  }

  private handleError(error: any) {
    // We'd also dig deeper into the error to get a better message
    console.log(error);
    let errMsg = error._body;
    return Observable.throw(errMsg);
  }

}
