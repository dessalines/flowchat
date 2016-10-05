import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
import { Http, Response } from '@angular/http';
import {environment} from '../../environments/environment';
import {Tag} from '../shared';

@Injectable()
export class TagService {

  private getTagUrl: string = environment.endpoint + 'tag/';
  private queryTagsUrl: string = environment.endpoint + 'tag_search/';
  private createTagUrl: string = environment.endpoint + 'tag';

  private getPopularTagsUrl(limit: number, page: number, orderBy: string): string {
    return environment.endpoint + 'tags/' +
      limit + '/' + page + '/' + orderBy;
  }

  constructor(private http: Http) {}

  getTag(id: number): Observable<Tag> {
    return this.http.get(this.getTagUrl + id)
      .map(r => r.json())
      .catch(this.handleError);
  }

  searchTags(query: string): Observable<Array<Tag>> {
    return this.http.get(this.queryTagsUrl + query)
      .map(r => r.json().tags)
      .catch(this.handleError);
  }

  createTag(name: string): Observable<Tag> {
    let tagInfo = JSON.stringify({ name: name });

    return this.http.post(this.createTagUrl, tagInfo)
      .map(r => r.json())
      .catch(this.handleError);

  }

  getPopularTags(limit: number = 10, page: number = 1,
    orderBy: string = 'time-86400'): Observable<Array<Tag>>{
    return this.http.get(this.getPopularTagsUrl(limit, page, orderBy))
      .map(r => r.json().tags)
      .catch(this.handleError);
  }


  private handleError(error: any) {
    // We'd also dig deeper into the error to get a better message
    let errMsg = error._body;
    return Observable.throw(errMsg);
  }

}
