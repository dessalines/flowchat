import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
import { Http, Response } from '@angular/http';
import { Headers, RequestOptions } from '@angular/http';
import {UserService} from './user.service';

@Injectable()
export class DiscussionService {

  private getDiscussionUrl: string = 'http://localhost:4567/get_discussion/';
  private saveRankUrl: string = 'http://localhost:4567/save_discussion_rank/';

  private getDiscussionsUrl(limit: number, page: number, tagId: string,
    orderBy: string): string {
    return 'http://localhost:4567/get_discussions/' + tagId + '/' +
      limit + '/' + page + '/' + orderBy;
  }

  headers: Headers;
  options: RequestOptions;

  constructor(private http: Http,
    private userService: UserService) {
    this.headers = new Headers(
      {
        'Content-Type': 'application/json',
        'user': JSON.stringify(this.userService.getUser())
      });
    this.options = new RequestOptions({ headers: this.headers });
  }

  getDiscussion(id: number) {
    return this.http.get(this.getDiscussionUrl + id, this.options)
      .map(this.extractData)
      .catch(this.handleError);
  }

  getDiscussions(limit: number = 10, page: number = 1, tagId: string = 'all',
    orderBy: string = 'created desc') {
    return this.http.get(this.getDiscussionsUrl(limit, page, tagId, orderBy), this.options)
      .map(this.extractData)
      .catch(this.handleError);
  }

  saveRank(id: number, rank: number) {
    return this.http.post(this.saveRankUrl + id + '/' + rank, null, this.options)
      .map(this.extractData)
      .catch(this.handleError);
  }

  private handleError(error: any) {
    // We'd also dig deeper into the error to get a better message
    let errMsg = error;
    return Observable.throw(errMsg);
  }

  private extractData(res: Response) {
    let body = res.json();
    console.log(body);
    return body || {};
  }

}
