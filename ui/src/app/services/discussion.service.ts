import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
import { Http, Response } from '@angular/http';
import { Headers, RequestOptions } from '@angular/http';
import {UserService} from './user.service';
import {Discussion} from '../shared/discussion.interface';
import {environment} from '../environment';

@Injectable()
export class DiscussionService {

  private getDiscussionUrl: string = environment.endpoint + 'get_discussion/';
  private queryDiscussionsUrl: string = environment.endpoint + 'discussion_search/';
  private saveRankUrl: string = environment.endpoint + 'save_discussion_rank/';
  private createDiscussionUrl: string = environment.endpoint + 'create_discussion';
  private saveDiscussionUrl: string = environment.endpoint + 'save_discussion';

  private getDiscussionsUrl(page: number, limit: number, tagId: string,
    orderBy: string): string {
    return environment.endpoint + 'get_discussions/' + tagId + '/' +
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

  getDiscussions(page: number = 1, limit: number = 12, tagId: string = 'all',
    orderBy: string = 'custom') {
    return this.http.get(this.getDiscussionsUrl(page, limit, tagId, orderBy), this.userService.getOptions())
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
    let errMsg = error._body;

    return Observable.throw(errMsg);
  }

  private extractData(res: Response) {
    let body = res.json();
    return body || {};
  }

}
