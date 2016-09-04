import { BrowserModule } from '@angular/platform-browser';
import { NgModule, } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import {Location, LocationStrategy, HashLocationStrategy} from '@angular/common';
import { AppComponent } from './app.component';
import { HomeComponent, TagComponent, CommunityComponent, CommunityModlogComponent, DiscussionComponent } from './components';
import { routing,
  appRoutingProviders } from './app.routing';
import { Title } from '@angular/platform-browser';
import {SeoService} from './services/seo.service';
import {HTTP_PROVIDERS} from '@angular/http';


@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    TagComponent,
    // CommunityComponent,
    CommunityModlogComponent,
    DiscussionComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    routing
  ],
  providers: [appRoutingProviders,
    Title,
    SeoService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
