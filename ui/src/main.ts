import { bootstrap } from '@angular/platform-browser-dynamic';
import { enableProdMode } from '@angular/core';
import { ChatPracticeUiAppComponent, environment } from './app/';
import {HTTP_PROVIDERS} from '@angular/http';


if (environment.production) {
  enableProdMode();
}

bootstrap(ChatPracticeUiAppComponent, [HTTP_PROVIDERS]);
