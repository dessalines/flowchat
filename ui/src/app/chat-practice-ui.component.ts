import { Component, ViewContainerRef} from '@angular/core';
import { RouteConfig, ROUTER_DIRECTIVES, Router } from '@angular/router-deprecated';
import { ChatComponent } from './components/chat/index';
import { ChatListComponent } from './components/chat-list/index';
import { NavbarComponent } from './components/navbar/index';
import {HomeComponent} from './components/home/index';
import {UserService} from './services/user.service';
import {ToasterContainerComponent, ToasterService, ToasterConfig} from 'angular2-toaster/angular2-toaster';

@Component({
  moduleId: module.id,
  selector: 'chat-practice-ui-app',
  templateUrl: 'chat-practice-ui.component.html',
  styleUrls: ['chat-practice-ui.component.css'],
  directives: [NavbarComponent, ROUTER_DIRECTIVES, ToasterContainerComponent],
  providers: [UserService, ToasterService]
})
@RouteConfig([
    // { path: '/**', redirectTo: ['Home'] },
    { path: '/', name: 'Home', component: HomeComponent, useAsDefault: true },
    { path: '/discussion/:discussionId/...', name: 'Discussion', component: ChatListComponent }
])
export class ChatPracticeUiAppComponent {
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
