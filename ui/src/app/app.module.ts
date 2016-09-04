import { BrowserModule } from '@angular/platform-browser';
import { NgModule, } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import {Location,LocationStrategy,HashLocationStrategy} from '@angular/common';
import { AppComponent } from './app.component';
import { routing,
  appRoutingProviders } from './app.routing';
import { Title } from '@angular/platform-browser';
import {SeoService} from './services/seo.service';
import {HTTP_PROVIDERS} from '@angular/http';


@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    routing
  ],
  providers: [appRoutingProviders,
    HTTP_PROVIDERS,
    // disableDeprecatedForms(),
    // provideForms(),
    // { provide: LocationStrategy, useClass: HashLocationStrategy },
    Title,
    SeoService
    ],
  bootstrap: [AppComponent]
})
export class AppModule { }
