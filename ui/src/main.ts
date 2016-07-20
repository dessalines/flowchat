import { bootstrap } from '@angular/platform-browser-dynamic';
import { Title } from '@angular/platform-browser';
import { enableProdMode } from '@angular/core';
import { AppComponent, environment } from './app/';
import { appRouterProviders } from './app/app.routes';
import {HTTP_PROVIDERS} from '@angular/http';
import {Location,LocationStrategy,HashLocationStrategy} from '@angular/common';
import {SeoService} from './app/services/seo.service';
import {FORM_PROVIDERS} from '@angular/forms';

if (environment.production) {
  enableProdMode();
}

bootstrap(AppComponent, [
  appRouterProviders,
  HTTP_PROVIDERS, 
  FORM_PROVIDERS,
  {provide: LocationStrategy, useClass: HashLocationStrategy},
  Title,
  SeoService
]);

