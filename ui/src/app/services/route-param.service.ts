import {Injectable} from '@angular/core';
import { RouteParams} from '@angular/router-deprecated';

@Injectable()
export class RouteParamService {
  constructor(private routeParams: RouteParams) {}

  params() {
    return this.routeParams;
  }
}