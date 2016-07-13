import { bootstrap } from '@angular/platform-browser-dynamic';
import { enableProdMode } from '@angular/core';
import { AppComponent, environment } from './app/';
import { appRouterProviders } from './app/app.routes';
import {HTTP_PROVIDERS} from '@angular/http';
import {Location,LocationStrategy,HashLocationStrategy} from '@angular/common';

if (environment.production) {
  enableProdMode();
}

bootstrap(AppComponent, [
  appRouterProviders,
  HTTP_PROVIDERS, {provide: LocationStrategy, useClass: HashLocationStrategy}
]);

