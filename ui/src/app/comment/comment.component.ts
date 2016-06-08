import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
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

  // This emits an event to the higher level chat component, because you only
  // want to focus on new comments when not replying
  @Output() replyingEvent = new EventEmitter();

  private reply: string;

  private showReply: boolean = false;

  private collapsed: boolean = false;

  private highlight: boolean = false;

  private editable: boolean = false;



  constructor(private threadedChatService: ThreadedChatService) {}

  toggleShowReply() {
    this.showReply = !this.showReply;
    this.replyingEvent.emit(this.showReply);
  }

  hideReply() {
    this.showReply = false;
    this.replyingEvent.emit(this.showReply);
  }

  // This sends the event up the chain
  setIsReplying($event) {
    this.replyingEvent.emit($event);
  }

  ngOnInit() { 
    this.highlight = this.isCommentNew();
    // this.focusToNewComment();
  }

  sendMessage() {
      try {
        this.threadedChatService.ws.send(this.replyData());
      } catch(e) {
          this.threadedChatService.ws.reconnect();
          this.threadedChatService.ws.send(this.replyData());
      }
      this.showReply = false;
      this.reply = "";
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
