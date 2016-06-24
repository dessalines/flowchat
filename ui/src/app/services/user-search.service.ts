import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
import { Http, Response } from '@angular/http';

@Injectable()
export class UserSearchService {

  private queryUsersUrl: string = 'http://localhost:4567/user_search/';

  constructor(private http: Http) { }

  searchUsers(query: string) {
    return this.http.get(this.queryUsersUrl + query)
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
