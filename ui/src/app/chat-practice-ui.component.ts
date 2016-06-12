import { Component, ViewContainerRef} from '@angular/core';
import { RouteConfig, ROUTER_DIRECTIVES, Router } from '@angular/router-deprecated';
import { ChatComponent } from './chat';
import { ChatListComponent } from './chat-list';
import { NavbarComponent } from './navbar';
import {HomeComponent} from './home';
import {UserService} from './services/user.service';

@Component({
  moduleId: module.id,
  selector: 'chat-practice-ui-app',
  templateUrl: 'chat-practice-ui.component.html',
  styleUrls: ['chat-practice-ui.component.css'],
  directives: [NavbarComponent, ROUTER_DIRECTIVES],
  providers: [UserService]
})
@RouteConfig([
    // { path: '/**', redirectTo: ['Home'] },
    { path: '/', name: 'Home', component: HomeComponent, useAsDefault: true },
    { path: '/discussion/:discussionId/...', name: 'Discussion', component: ChatListComponent }
])
export class ChatPracticeUiAppComponent {
  public title = 'derp';

  private viewContainerRef: ViewContainerRef;
  public constructor(viewContainerRef: ViewContainerRef,
    private router: Router) {
    // You need this small hack in order to catch application root view container ref
    this.viewContainerRef = viewContainerRef;
  }

}
