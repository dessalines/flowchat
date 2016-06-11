import { Component, ViewContainerRef} from '@angular/core';
import { ChatComponent } from './chat';
import { NavbarComponent } from './navbar';
import {UserService} from './services/user.service';

@Component({
  moduleId: module.id,
  selector: 'chat-practice-ui-app',
  templateUrl: 'chat-practice-ui.component.html',
  styleUrls: ['chat-practice-ui.component.css'],
  directives: [ChatComponent, NavbarComponent],
  providers: [UserService]
})
export class ChatPracticeUiAppComponent {
  public title = 'derp';

  private viewContainerRef: ViewContainerRef;
  public constructor(viewContainerRef: ViewContainerRef) {
    // You need this small hack in order to catch application root view container ref
    this.viewContainerRef = viewContainerRef;
  }
}
