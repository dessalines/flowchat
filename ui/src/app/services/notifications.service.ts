import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
import 'rxjs/add/observable/interval';
import 'rxjs/add/operator/startWith';
import 'rxjs/add/operator/mergeMap';
import { Http, Response } from '@angular/http';
import { Headers, RequestOptions } from '@angular/http';
import {UserService} from './user.service';
import {Comment} from '../shared/comment.interface';
import {environment} from '../../environments/environment';

@Injectable()
export class NotificationsService {

  private getUnreadUrl: string = environment.endpoint + 'unread_replies';
  private markAsReadUrl: string = environment.endpoint + 'mark_reply_as_read/';
  private markAllAsReadUrl: string = environment.endpoint + 'mark_all_replies_as_read';

  private fetchInterval: number = 15000;

  constructor(private http: Http,
    private userService: UserService) {
  }

  getUnreadMessages(): any {
    return Observable.interval(this.fetchInterval).startWith(0).flatMap(() => {
      return this.http.get(this.getUnreadUrl, this.userService.getOptions())
        .map(this.extractData)
        .catch(this.handleError);
    });
  }

  markMessageAsRead(id: number) {
    return this.http.post(this.markAsReadUrl + id, null, this.userService.getOptions())
      .map(this.extractData)
      .catch(this.handleError);
  }

  markAllAsRead() {
    return this.http.post(this.markAllAsReadUrl, null, this.userService.getOptions())
      .map(this.extractData)
      .catch(this.handleError);
  }

  private handleError(error: any) {
    // We'd also dig deeper into the error to get a better message
    let errMsg = error._body;

    return Observable.throw(errMsg);
  }

  private extractData(res: Response) {
    let body = res.json();
    return body || {};
  }

}
