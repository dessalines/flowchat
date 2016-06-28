import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
import { Http, Response } from '@angular/http';

@Injectable()
export class TagService {

  private getTagUrl: string = 'http://localhost:4567/get_tag/';
  private queryTagsUrl: string = 'http://localhost:4567/tag_search/';
  private createTagUrl: string = 'http://localhost:4567/create_tag';

  private getPopularTagsUrl(limit: number, page: number, orderBy: string): string {
    return 'http://localhost:4567/get_popular_tags/' + 
      limit + '/' + page + '/' + orderBy;
  }

  constructor(private http: Http) {}

  getTag(id: string) {
    return this.http.get(this.getTagUrl + id)
      .map(this.extractData)
      .catch(this.handleError);
  }

  searchTags(query: string) {
    return this.http.get(this.queryTagsUrl + query)
      .map(this.extractData)
      .catch(this.handleError);
  }

  createTag(name: string) {
    let tagInfo = JSON.stringify({ name: name });

    return this.http.post(this.createTagUrl, tagInfo)
      .map(this.extractData)
      .catch(this.handleError);

  }

  getPopularTags(limit: number = 10, page: number = 1,
    orderBy: string = 'custom') {
    return this.http.get(this.getPopularTagsUrl(limit, page, orderBy))
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
    console.log(body);
    return body || {};
  }

}
