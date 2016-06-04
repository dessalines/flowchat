import { Component } from '@angular/core';
import { ChatComponent } from './chat';
import {CommentComponent} from './comment';

@Component({
  moduleId: module.id,
  selector: 'chat-practice-ui-app',
  templateUrl: 'chat-practice-ui.component.html',
  styleUrls: ['chat-practice-ui.component.css'],
  directives: [ChatComponent, CommentComponent]
})
export class ChatPracticeUiAppComponent {
    public title = 'derp';
}
