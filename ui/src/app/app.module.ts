import { BrowserModule } from '@angular/platform-browser';
import { NgModule, } from '@angular/core';
import { FormsModule,
  ReactiveFormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { routing,
  appRoutingProviders } from './app.routing';
import {Location, LocationStrategy, HashLocationStrategy} from '@angular/common';
import { AppComponent } from './app.component';
import { HomeComponent,
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
  OnboardAlertComponent } from './components';
import { Title } from '@angular/platform-browser';
import {SeoService} from './services';
import { MomentPipe,
  MarkdownPipe } from './pipes';
import {ToasterModule,
  ToasterService} from 'angular2-toaster/angular2-toaster';
import { TooltipModule,
  DropdownModule,
  ModalModule,
  TabsModule,
  TypeaheadModule,
  AlertModule } from 'ng2-bootstrap/ng2-bootstrap';

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

    // Pipes
    MomentPipe,
    MarkdownPipe,
    OnboardAlertComponent,
    
  ],
  imports: [
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpModule,
    ToasterModule,

    // ng2-bootstrap modules
    TooltipModule,
    DropdownModule,
    ModalModule,
    TabsModule,
    TypeaheadModule,
    AlertModule,

    routing
  ],
  providers: [appRoutingProviders,
    Title,
    SeoService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
