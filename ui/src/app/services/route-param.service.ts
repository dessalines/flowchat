import {Injectable} from '@angular/core';
import { RouteParams, RouteData} from '@angular/router-deprecated';

@Injectable()
export class RouteParamService {
  constructor(private routeParams: RouteParams,
  	private routeData: RouteData) {}

  params() {
    return this.routeParams;
  }

  data() {
  	return this.routeData.data;
  }
}