import { bootstrap } from '@angular/platform-browser-dynamic';
import { enableProdMode } from '@angular/core';
import { FlowChatAppComponent, environment } from './app/';
import {HTTP_PROVIDERS} from '@angular/http';
import { ROUTER_PROVIDERS } from '@angular/router-deprecated';
import {Location,LocationStrategy,HashLocationStrategy} from '@angular/common';


if (environment.production) {
  enableProdMode();
}

bootstrap(FlowChatAppComponent, 
	[HTTP_PROVIDERS, ROUTER_PROVIDERS, {provide: LocationStrategy, useClass: HashLocationStrategy}]);
