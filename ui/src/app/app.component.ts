import { Component, ViewContainerRef} from '@angular/core';
import { Router } from '@angular/router';
import { DiscussionComponent,
  SidebarComponent,
  NavbarComponent,
  HomeComponent,
  TagComponent } from './components';
import { UserService,
  DiscussionService,
  CommunityService,
  LoginService,
  TagService,
  NotificationsService } from './services';
import { ToasterContainerComponent,
  ToasterService,
  ToasterConfig } from 'angular2-toaster/angular2-toaster';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
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
    ) {
    // You need this small hack in order to catch application root view container ref
    this.viewContainerRef = viewContainerRef;
  }

  // Prevent backspace navigation
  ngAfterViewInit() {
    window.addEventListener('keydown', (e: any) => {
      if (e.which === 8 && e.target.tagName == 'input') {
        e.preventDefault();
      }
    });
  }

}
