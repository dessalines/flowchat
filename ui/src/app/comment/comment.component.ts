import { Component, OnInit, Input } from '@angular/core';
import {Comment} from '../shared/comment.interface';
import {ThreadedChatService} from '../services/threaded-chat.service';
import { MomentPipe } from '../pipes/moment.pipe';

@Component({
  moduleId: module.id,
  selector: 'app-comment',
  templateUrl: 'comment.component.html',
  styleUrls: ['comment.component.css'],
  directives: [CommentComponent],
  pipes: [MomentPipe]
})

export class CommentComponent implements OnInit {

	@Input() comment: any; // Couldn't get strict typing of this to work for recursive templates

	private reply: string;

  private showReply: boolean = false;

  private collapsed: boolean = false;

  constructor(private threadedChatService: ThreadedChatService) { }

  ngOnInit() {}

	sendMessage() {
		this.threadedChatService.ws.send(this.replyData());
	}

  private replyData(): string {
    return JSON.stringify({ parentId: this.comment.id, reply: this.reply });
  }

  private collapseText(): string {
      return (this.collapsed) ? "[+]" : "[-]";
  }

}
