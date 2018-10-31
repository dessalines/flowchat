import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { Http, Response } from '@angular/http';
import { Headers, RequestOptions } from '@angular/http';
import { UserService } from './user.service';
import { Discussion, Discussions, EmptyDiscussion } from '../shared';
import { environment } from '../../environments/environment';

@Injectable()
export class DiscussionService {

  private getDiscussionUrl: string = environment.endpoint + 'discussion/';
  private queryDiscussionsUrl: string = environment.endpoint + 'discussion_search/';
  private saveRankUrl: string = environment.endpoint + 'discussion_rank/';
  private createDiscussionBlankUrl: string = environment.endpoint + 'discussion_blank';
  private createDiscussionUrl: string = environment.endpoint + 'discussion';
  private saveDiscussionUrl: string = environment.endpoint + 'discussion';

  private getDiscussionsUrl(page: number,
    limit: number,
    tagId: string,
    communityId: string,
    orderBy: string): string {
    return environment.endpoint + 'discussions/' + tagId + '/' + communityId + '/' + limit + '/' + page + '/' + orderBy;
  }

  constructor(private http: Http,
    private userService: UserService) {
  }

  getDiscussion(id: number): Observable<Discussion> {
    return this.http.get(this.getDiscussionUrl + id, this.userService.getOptions())
      .map(r => r.json())
      .catch(this.handleError);
  }

  getDiscussions(page: number = 1,
    limit: number = 12,
    tagId: string = 'all',
    communityId: string = 'all',
    orderBy: string = 'time-86400'): Observable<Discussions> {
    return this.http.get(this.getDiscussionsUrl(page, limit, tagId, communityId, orderBy), this.userService.getOptions())
      .map(r => r.json())
      .catch(this.handleError);
  }

  searchDiscussions(query: string): Observable<Discussions> {
    return this.http.get(this.queryDiscussionsUrl + query)
      .map(r => r.json())
      .catch(this.handleError);
  }

  saveRank(id: number, rank: number) {
    return this.http.post(this.saveRankUrl + id + '/' + rank, null, this.userService.getOptions())
      .map(r => r.json())
      .catch(this.handleError);
  }

  createDiscussionBlank(): Observable<Discussion> {
    return this.http.post(this.createDiscussionBlankUrl, null, this.userService.getOptions())
      .map(r => r.json())
      .catch(this.handleError);
  }

  createDiscussion(discussion: EmptyDiscussion): Observable<Discussion> {
    return this.http.post(this.createDiscussionUrl, discussion, this.userService.getOptions())
      .map(r => r.json())
      .catch(this.handleError);
  }

  saveDiscussion(discussion: Discussion): Observable<Discussion> {
    return this.http.put(this.saveDiscussionUrl, discussion, this.userService.getOptions())
      .map(r => r.json())
      .catch(this.handleError);
  }

  private handleError(error: any) {
    // We'd also dig deeper into the error to get a better message
    let errMsg = error._body;

    return Observable.throw(errMsg);
  }


}
