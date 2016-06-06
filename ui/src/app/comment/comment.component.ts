import { Component, OnInit, Input } from '@angular/core';
import {Comment} from '../shared/comment.interface';
import {ThreadedChatService} from '../services/threaded-chat.service';


@Component({
  moduleId: module.id,
  selector: 'app-comment',
  templateUrl: 'comment.component.html',
  styleUrls: ['comment.component.css'],
  directives: [CommentComponent]
})

export class CommentComponent implements OnInit {

	@Input() comment: any; // Couldn't get strict typing of this to work for recursive templates

	private reply: string;

  constructor(private threadedChatService: ThreadedChatService) { }

  ngOnInit() {}

	sendMessage() {
		this.threadedChatService.ws.send(this.replyData());
	}

  private replyData(): string {
    return JSON.stringify({ parentId: this.comment.id, reply: this.reply });
  }

}
