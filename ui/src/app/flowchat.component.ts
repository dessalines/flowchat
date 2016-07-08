import { Component, ViewContainerRef} from '@angular/core';
import { RouteConfig, ROUTER_DIRECTIVES, Router } from '@angular/router-deprecated';
import { ChatComponent } from './components/chat/index';
import { ChatListComponent } from './components/chat-list/index';
import { NavbarComponent } from './components/navbar/index';
import {HomeComponent} from './components/home/index';
import {TagComponent} from './components/tag/index';
import {UserService} from './services/user.service';
import {DiscussionService} from './services/discussion.service';
import {TagService} from './services/tag.service';
import {NotificationsService} from './services/notifications.service';
import {ToasterContainerComponent, ToasterService, ToasterConfig} from 'angular2-toaster/angular2-toaster';

@Component({
  moduleId: module.id,
  selector: 'flowchat-app',
  templateUrl: 'flowchat.component.html',
  styleUrls: ['flowchat.component.css'],
  directives: [NavbarComponent, ROUTER_DIRECTIVES, ToasterContainerComponent],
  providers: [UserService, ToasterService, DiscussionService, TagService, NotificationsService]
})
@RouteConfig([
    // { path: '/**', redirectTo: ['Home'] },
    { path: '/', name: 'Home', component: HomeComponent, useAsDefault: true },
    { path: '/tag/:tagId', name: 'Tag', component: TagComponent},
    { path: '/discussion/:discussionId/...', name: 'Discussion', component: ChatListComponent }
])
export class FlowChatAppComponent {
  public title = 'derp';


  public toasterconfig: ToasterConfig =
  new ToasterConfig({
    showCloseButton: true,
    tapToDismiss: false,
    timeout: 3000,
  });


  private viewContainerRef: ViewContainerRef;


  public constructor(viewContainerRef: ViewContainerRef,
    private router: Router,
    private toasterService: ToasterService) {
    // You need this small hack in order to catch application root view container ref
    this.viewContainerRef = viewContainerRef;
  }

}
