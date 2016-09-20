import { Component, ViewContainerRef} from '@angular/core';
import { Router } from '@angular/router';
import { DiscussionComponent } from './components/discussion/index';
import { SidebarComponent } from './components/sidebar/index';
import { NavbarComponent } from './components/navbar/index';
import {HomeComponent} from './components/home/index';
import {TagComponent} from './components/tag/index';
import {UserService} from './services/user.service';
import {DiscussionService} from './services/discussion.service';
import {CommunityService} from './services/community.service';
import {LoginService} from './services/login.service';
import {SeoService} from './services/seo.service';
import {TagService} from './services/tag.service';
import {NotificationsService} from './services/notifications.service';
import {ToasterContainerComponent, ToasterService, ToasterConfig} from 'angular2-toaster/angular2-toaster';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.css'],
  providers: [UserService, ToasterService, DiscussionService, CommunityService, TagService, NotificationsService, LoginService]
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
    private seoService: SeoService
    ) {
    // You need this small hack in order to catch application root view container ref
    this.viewContainerRef = viewContainerRef;

    seoService.setTitle('FlowChat');
    seoService.setMetaDescription('An open-source, live updating, threaded chat platform with voting.');
    seoService.setMetaRobots('Index, Follow');
  }

  // Prevent backspace navigation
  ngAfterViewInit() {
    console.log('got here');
    window.addEventListener('keydown', (e: any) => {
      if (e.which === 8 && e.target.tagName == 'input') {
        e.preventDefault();
      }
    });
  }

}
