import { BrowserModule } from '@angular/platform-browser';
import { NgModule, } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import {Location, LocationStrategy, HashLocationStrategy} from '@angular/common';
import { AppComponent } from './app.component';
import { HomeComponent, 
  TagComponent, 
  CommunityComponent, 
  CommunityModlogComponent, 
  DiscussionComponent, 
  UserComponent, 
  DiscussionCardSelectComponent } from './components';
import { routing,
  appRoutingProviders } from './app.routing';
import { Title } from '@angular/platform-browser';
import {SeoService} from './services/seo.service';
import { MomentPipe, MarkdownPipe } from './pipes';


import {ToasterModule, ToasterService} from 'angular2-toaster/angular2-toaster';
import { TooltipModule, DropdownModule, ModalModule, TabsModule, TypeaheadModule } from 'ng2-bootstrap/ng2-bootstrap';


@NgModule({
  declarations: [

    // Components
    AppComponent,
    HomeComponent,
    TagComponent,
    // CommunityComponent,
    CommunityModlogComponent,
    DiscussionComponent,
    UserComponent,
    DiscussionCardSelectComponent,

    // Pipes
    MomentPipe,
    MarkdownPipe,


    // ng2-bootstrap modules
    TooltipModule,
    DropdownModule,
    ModalModule,
    TabsModule,
    TypeaheadModule
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    ToasterModule,

    routing
  ],
  providers: [appRoutingProviders,
    Title,
    SeoService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
