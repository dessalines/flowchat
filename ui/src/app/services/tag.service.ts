import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
import { Http, Response } from '@angular/http';

@Injectable()
export class TagService {

  private getTagUrl: string = 'http://localhost:4567/get_tag/';

  private getDiscussionsUrl(limit: number, page: number, tagId: string,
    orderBy: string): string {
    return 'http://localhost:4567/get_discussions/' + tagId + '/' +
      limit + '/' + page + '/' + orderBy;
  }


  constructor(private http: Http) {}

  getTag(id: string) {
    return this.http.get(this.getTagUrl + id)
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
