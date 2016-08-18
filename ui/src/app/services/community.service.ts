import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
import { Http, Response } from '@angular/http';
import { Headers, RequestOptions } from '@angular/http';
import {UserService} from './user.service';
import {Community} from '../shared/community.interface';
import {environment} from '../environment';


@Injectable()
export class CommunityService {

  private getCommunityUrl: string = environment.endpoint + 'community/';
  private queryCommunityUrl: string = environment.endpoint + 'community_search/';  
  private createCommunityUrl: string = environment.endpoint + 'community';
  private saveCommunityUrl: string = environment.endpoint + 'community';


  private getCommunitiesUrl(page: number, limit: number, tagId: string,
    orderBy: string): string {
    return environment.endpoint + 'communities/' + tagId + '/' +
      limit + '/' + page + '/' + orderBy;
  }

  constructor(private http: Http,
    private userService: UserService) {
  }

  getCommunity(id: number) {
    return this.http.get(this.getCommunityUrl + id, this.userService.getOptions())
      .map(this.extractData)
      .catch(this.handleError);
  }

  getCommunities(page: number = 1, limit: number = 12, tagId: string = 'all',
    orderBy: string = 'time-86400') {
    return this.http.get(this.getCommunitiesUrl(page, limit, tagId, orderBy), this.userService.getOptions())
      .map(this.extractData)
      .catch(this.handleError);
  }

  searchCommunities(query: string) {
    return this.http.get(this.queryCommunityUrl + query)
      .map(this.extractData)
      .catch(this.handleError);
  }

  createCommunity() {
    return this.http.post(this.createCommunityUrl, null, this.userService.getOptions())
      .map(this.extractData)
      .catch(this.handleError);
  }

  saveCommunity(community: Community) {
    return this.http.put(this.saveCommunityUrl, community, this.userService.getOptions())
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
