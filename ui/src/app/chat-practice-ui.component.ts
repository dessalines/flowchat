import { Component } from '@angular/core';
import { ChatComponent } from './chat';

@Component({
  moduleId: module.id,
  selector: 'chat-practice-ui-app',
  templateUrl: 'chat-practice-ui.component.html',
  styleUrls: ['chat-practice-ui.component.css'],
  directives: [ChatComponent]
})
export class ChatPracticeUiAppComponent {
    public title = 'derp';
}
