import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
import { Http, Response } from '@angular/http';
import { Headers, RequestOptions } from '@angular/http';
import {UserService} from './user.service';
import {Discussion} from '../shared/discussion.interface';

@Injectable()
export class DiscussionService {

  private getDiscussionUrl: string = 'http://localhost:4567/get_discussion/';
  private queryDiscussionsUrl: string = 'http://localhost:4567/discussion_search/';
  private saveRankUrl: string = 'http://localhost:4567/save_discussion_rank/';
  private createDiscussionUrl: string = "http://localhost:4567/create_discussion";
  private saveDiscussionUrl: string = "http://localhost:4567/save_discussion";


  private getDiscussionsUrl(limit: number, page: number, tagId: string,
    orderBy: string): string {
    return 'http://localhost:4567/get_discussions/' + tagId + '/' +
      limit + '/' + page + '/' + orderBy;
  }

  constructor(private http: Http,
    private userService: UserService) {
  }

  getDiscussion(id: number) {
    return this.http.get(this.getDiscussionUrl + id, this.userService.getOptions())
      .map(this.extractData)
      .catch(this.handleError);
  }

  getDiscussions(limit: number = 10, page: number = 1, tagId: string = 'all',
    orderBy: string = 'custom') {
    return this.http.get(this.getDiscussionsUrl(limit, page, tagId, orderBy), this.userService.getOptions())
      .map(this.extractData)
      .catch(this.handleError);
  }

  searchDiscussions(query: string) {
    return this.http.get(this.queryDiscussionsUrl + query)
      .map(this.extractData)
      .catch(this.handleError);
  }

  saveRank(id: number, rank: number) {
    return this.http.post(this.saveRankUrl + id + '/' + rank, null, this.userService.getOptions())
      .map(this.extractData)
      .catch(this.handleError);
  }

  createDiscussion() {
    return this.http.post(this.createDiscussionUrl, null, this.userService.getOptions())
      .map(this.extractData)
      .catch(this.handleError);
  }

  saveDiscussion(discussion: Discussion) {
    return this.http.post(this.saveDiscussionUrl, discussion, this.userService.getOptions())
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
    return body || {};
  }

}
