import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import {Comment} from '../shared/comment.interface';
import {ThreadedChatService} from '../services/threaded-chat.service';
import {UserService} from '../services/user.service';
import { MomentPipe } from '../pipes/moment.pipe';
import {MarkdownEditComponent} from '../markdown-edit';
import * as moment from 'moment';
import {MarkdownPipe} from '../pipes/markdown.pipe';
import { Router, ROUTER_DIRECTIVES } from '@angular/router-deprecated';


@Component({
  moduleId: module.id,
  selector: 'app-comment',
  templateUrl: 'comment.component.html',
  styleUrls: ['comment.component.css'],
  styles: [':host >>> p { margin-bottom: .2rem; }'],
  directives: [CommentComponent, MarkdownEditComponent, ROUTER_DIRECTIVES],
  pipes: [MomentPipe, MarkdownPipe]
})

export class CommentComponent implements OnInit {

  @Input() comment: any; // Couldn't get strict typing of this to work for recursive templates

  // This emits an event to the higher level chat component, because you only
  // want to focus on new comments when not replying
  @Output() replyingEvent = new EventEmitter();

  private replyText: string;

  private showReply: boolean = false;

  private editText: string;

  private showEdit: boolean = false;

  private collapsed: boolean = false;

  private highlight: boolean = false;

  private editable: boolean = false;

  constructor(private threadedChatService: ThreadedChatService,
    private userService: UserService) { }

  toggleShowReply() {
    this.showReply = !this.showReply;
    this.replyingEvent.emit(this.showReply);
  }

  toggleShowEdit() {
    this.showEdit = !this.showEdit;
  }

  hideReply() {
    this.showReply = false;
    this.replyingEvent.emit(this.showReply);
  }

  hideEdit() {
    this.showEdit = false;
    this.replyingEvent.emit(this.showEdit);
  }

  // This sends the event up the chain
  setIsReplying($event) {
    this.replyingEvent.emit($event);
  }

  ngOnInit() {
    this.highlight = this.isCommentNew();
    this.setEditable();
  }

  sendMessage() {
    try {
      this.threadedChatService.ws.send(this.replyData());
    } catch (e) {
      console.log(e);
    }
    this.showReply = false;
    this.replyingEvent.emit(this.showReply);
    this.replyText = "";

  }

  editMessage() {
    this.threadedChatService.ws.send(this.editData());
    this.showEdit = false;
    this.replyingEvent.emit(this.showEdit);
  }

  private replyData(): ReplyData {
    let replyData = {
      parentId: this.comment.id,
      reply: this.replyText
    }

    return replyData;
    // return JSON.stringify({ parentId: this.comment.id, reply: this.reply });
  }

  private editData(): EditData {
    let editData = {
      id: this.comment.id,
      edit: this.editText
    }
    return editData;
  }

  private collapseText(): string {
    return (this.collapsed) ? "[+]" : "[-]";
  }

  private isCommentNew(): boolean {
    let now = moment().subtract(2, 'minutes');
    let then = (this.comment.modified != null) ? moment(this.comment.modified) :
      moment(this.comment.created);
    return now.isBefore(then);
  }

  setReply($event) {
    this.replyText = $event;
  }

  setEdit($event) {
    this.editText = $event;
  }

  setEditable() {
    if (this.userService.getUser() != null &&
      this.comment.userId == this.userService.getUser().id) {
      this.editable = true;
    }
  }
}

interface ReplyData {
  parentId: number;
  reply: string;
}

interface EditData {
  id: number;
  edit: string;
}

