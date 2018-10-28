import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs/Rx';

import { Http, Response } from '@angular/http';
import { Headers, RequestOptions } from '@angular/http';
import { UserService } from './user.service';
import { Community, Communities } from '../shared';
import { environment } from '../../environments/environment';

@Injectable()
export class CommunityService {

  private getCommunityUrl: string = environment.endpoint + 'community/';
  private queryCommunityUrl: string = environment.endpoint + 'community_search/';
  private saveRankUrl: string = environment.endpoint + 'community_rank/';
  private createCommunityUrl: string = environment.endpoint + 'community';
  private saveCommunityUrl: string = environment.endpoint + 'community';
  private getCommunityModlogUrl: string = environment.endpoint + 'community_modlog/';

  private getCommunitiesUrl(page: number, limit: number, tagId: string,
    orderBy: string): string {
    return environment.endpoint + 'communities/' + tagId + '/' +
      limit + '/' + page + '/' + orderBy;
  }

  constructor(private http: Http,
    private userService: UserService) {
  }

  getCommunity(id: number): Observable<Community> {
    return this.http.get(this.getCommunityUrl + id, this.userService.getOptions())
      .map(r => r.json())
      .catch(this.handleError);
  }

  getCommunities(page: number = 1, limit: number = 12, tagId: string = 'all',
    orderBy: string = 'time-86400'): Observable<Communities> {
    return this.http.get(this.getCommunitiesUrl(page, limit, tagId, orderBy), this.userService.getOptions())
      .map(r => r.json())
      .catch(this.handleError);
  }

  searchCommunities(query: string): Observable<Communities> {
    return this.http.get(this.queryCommunityUrl + query)
      .map(r => r.json())
      .catch(this.handleError);
  }

  saveRank(id: number, rank: number) {
    return this.http.post(this.saveRankUrl + id + '/' + rank, null, this.userService.getOptions())
      .map(r => r.json())
      .catch(this.handleError);
  }

  createCommunity(): Observable<Community> {
    return this.http.post(this.createCommunityUrl, null, this.userService.getOptions())
      .map(r => r.json())
      .catch(this.handleError);
  }

  saveCommunity(community: Community): Observable<Community> {
    return this.http.put(this.saveCommunityUrl, community, this.userService.getOptions())
      .map(r => r.json())
      .catch(this.handleError);
  }

  getCommunityModlog(id: number) {
    return this.http.get(this.getCommunityModlogUrl + id, this.userService.getOptions())
      .map(r => r.json())
      .catch(this.handleError);
  }

  private handleError(error: any) {
    // We'd also dig deeper into the error to get a better message
    let errMsg = error._body;

    return Observable.throw(errMsg);
  }

}
