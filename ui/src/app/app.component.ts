import { Component, ViewContainerRef} from '@angular/core';
import { ROUTER_DIRECTIVES, Router } from '@angular/router';
import { ChatComponent } from './components/chat/index';
import { ChatListComponent } from './components/chat-list/index';
import { NavbarComponent } from './components/navbar/index';
import {HomeComponent} from './components/home/index';
import {TagComponent} from './components/tag/index';
import {UserService} from './services/user.service';
import {DiscussionService} from './services/discussion.service';
import {LoginService} from './services/login.service';
import {SeoService} from './services/seo.service';
import {TagService} from './services/tag.service';
import {NotificationsService} from './services/notifications.service';
import {ToasterContainerComponent, ToasterService, ToasterConfig} from 'angular2-toaster/angular2-toaster';

@Component({
  moduleId: module.id,
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.css'],
  directives: [NavbarComponent, ROUTER_DIRECTIVES, ToasterContainerComponent],
  providers: [UserService, ToasterService, DiscussionService, TagService, NotificationsService, LoginService]
})
export class AppComponent {
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
    private toasterService: ToasterService,
    private seoService: SeoService) {
    // You need this small hack in order to catch application root view container ref
    this.viewContainerRef = viewContainerRef;

    seoService.setTitle('FlowChat');
    seoService.setMetaDescription('An open-source, live updating, threaded chat platform with voting.');
    seoService.setMetaRobots('Index, Follow');
  }

}
