import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule, } from '@angular/core';
import {
  FormsModule,
  ReactiveFormsModule
} from '@angular/forms';
import { HttpModule } from '@angular/http';
import { Location, LocationStrategy, HashLocationStrategy } from '@angular/common';
import { AppComponent } from './app.component';
import {
  HomeComponent,
  TagComponent,
  CommunityComponent,
  CommunityModlogComponent,
  DiscussionComponent,
  UserComponent,
  DiscussionCardComponent,
  CommunityCardComponent,
  CommentComponent,
  DiscussionCardSortSelectComponent,
  DiscussionCardViewTypeSelectComponent,
  NavbarComponent,
  SidebarComponent,
  FooterComponent,
  MarkdownEditComponent,
  OnboardAlertComponent,
  LoginModalComponent,
  NewUserModalComponent
} from './components';
import {
  MomentPipe,
  MarkdownPipe
} from './pipes';
import {
  ToasterModule,
  ToasterService
} from 'angular2-toaster';
import {
  TooltipModule,
  BsDropdownModule,
  ModalModule,
  TabsModule,
  TypeaheadModule,
  AlertModule
} from 'ngx-bootstrap';

import { Title } from '@angular/platform-browser'

import { AppRoutingModule } from './app.routing';

import { NgxMasonryModule } from 'ngx-masonry';

window['imagesLoaded'] = require('imagesloaded');

@NgModule({
  declarations: [

    // Components
    AppComponent,
    HomeComponent,
    TagComponent,
    CommunityComponent,
    CommunityModlogComponent,
    DiscussionComponent,
    UserComponent,
    DiscussionCardComponent,
    CommunityCardComponent,
    CommentComponent,
    DiscussionCardSortSelectComponent,
    DiscussionCardViewTypeSelectComponent,
    NavbarComponent,
    SidebarComponent,
    FooterComponent,
    MarkdownEditComponent,
    OnboardAlertComponent,
    LoginModalComponent,
    NewUserModalComponent,

    // Pipes
    MomentPipe,
    MarkdownPipe,
    OnboardAlertComponent,

  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    ReactiveFormsModule,
    HttpModule,
    ToasterModule,
    AppRoutingModule,

    // ng2-bootstrap modules
    TooltipModule.forRoot(),
    BsDropdownModule.forRoot(),
    ModalModule.forRoot(),
    TabsModule.forRoot(),
    TypeaheadModule.forRoot(),
    AlertModule.forRoot(),

    NgxMasonryModule
  ],
  providers: [Title],
  bootstrap: [AppComponent]
})
export class AppModule { }
