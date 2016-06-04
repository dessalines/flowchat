import { Injectable } from '@angular/core';
import { Observable }     from 'rxjs/Observable';
import { Http, Response } from '@angular/http';
import {Comment} from '../../shared/comment.interface';
import 'rxjs/add/operator/map';


@Injectable()
export class TempService {

	private url = 'http://localhost:4567/temp';

  constructor(private http: Http) {}

  getData(): Observable<Array<Comment>> {
		return this.http.get(this.url).map(res => res.json());
  }

}
