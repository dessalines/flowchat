import { Component, OnInit, Input } from '@angular/core';
import {Comment} from '../shared/comment.interface';
import {ThreadedChatService} from '../services/threaded-chat.service';
import { MomentPipe } from '../pipes/moment.pipe';
import * as moment from 'moment';


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

  private highlight: boolean = false;

  constructor(private threadedChatService: ThreadedChatService) {
  }



  ngOnInit() { 
    this.highlight = this.isCommentNew();
  }

  sendMessage() {
      try {
        this.threadedChatService.ws.send(this.replyData());
      } catch(e) {
          this.threadedChatService.ws.reconnect();
          this.threadedChatService.ws.send(this.replyData());
      }
  }

  private replyData(): string {
    return JSON.stringify({ parentId: this.comment.id, reply: this.reply });
  }

  private collapseText(): string {
    return (this.collapsed) ? "[+]" : "[-]";
  }

  private isCommentNew(): boolean {
    let now = moment().subtract(2, 'minutes');
    let then = moment(this.comment.created);

    return now.isBefore(then);
  }

}
