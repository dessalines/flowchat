import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import {Comment, CommentRole, Discussion} from '../../shared';
import {UserService, ThreadedChatService} from '../../services';
import * as moment from 'moment';

@Component({
  selector: 'app-comment',
  templateUrl: './comment.component.html',
  styleUrls: ['./comment.component.scss'],
})
export class CommentComponent implements OnInit {

  @Input() comment: any; // Couldn't get strict typing of this to work for recursive templates

  // Have to pass in the discussion to get the moderators and comment roles
  @Input() discussion: Discussion;

  // This emits an event to the higher level chat component, because you only
  // want to focus on new comments when not replying
  @Output() replyingEvent = new EventEmitter();

  private replyText: string;

  private showReply: boolean = false;

  private editText: string;

  private showEdit: boolean = false;

  private collapsed: boolean = false;

  private showVoteSlider: boolean = false;

  private rank: number;

  private editable: boolean = false;

  private deleteable: boolean = false;

  private commentRole: CommentRole;

  constructor(private threadedChatService: ThreadedChatService,
    private userService: UserService) { }

  toggleShowReply() {
    this.showReply = !this.showReply;
    this.replyingEvent.emit(this.showReply);
  }

  toggleShowEdit() {
    this.showEdit = !this.showEdit;
  }

  toggleShowVoteSlider() {
    this.showVoteSlider = !this.showVoteSlider;
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
    this.setEditable();
    this.setDeleteable();
    this.setCommentRole();
    this.setRank();
  }

  sendMessage() {

    this.threadedChatService.send(this.replyData());

    this.showReply = false;
    this.replyingEvent.emit(this.showReply);
    this.replyText = "";
  }

  deleteComment() {
    this.threadedChatService.send(this.deleteData());
  }

  editMessage() {
    this.threadedChatService.send(this.editData());
    this.showEdit = false;
    this.replyingEvent.emit(this.showEdit);
  }

  setRank() {
    if (this.comment.userRank) {
      this.rank = this.comment.userRank;
    }
  }

  updateRank($event) {
    this.rank = $event;
  }

  saveRank($event) {
    this.rank = $event;
    this.showVoteSlider = false;
    this.threadedChatService.send(this.commentRankData());
  }

  private replyData(): ReplyData {
    return {
      parentId: this.comment.id,
      reply: this.replyText
    }
  }

  private editData(): EditData {
    return {
      id: this.comment.id,
      edit: this.editText,
    }
  }

  private deleteData(): DeleteData {
    return {
      deleteId: this.comment.id
    }
  }

  private commentRankData(): CommentRankData {
    return {
      rank: this.rank,
      commentId: this.comment.id
    }

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
      this.comment.user.id == this.userService.getUser().id) {
      this.editable = true;
    }
  }

  setDeleteable() {
    if (this.discussion.community.creator.id === this.userService.getUser().id) {
      this.deleteable = true;
    } else {
      let m = this.discussion.community.moderators.filter(m => m.id == this.userService.getUser().id)[0];
      if (m !== undefined) {
        this.deleteable = true;
      } else {
        this.deleteable = false;
      }
    }
  }

  setCommentRole() {
    if (this.comment.user.id == this.discussion.creator.id) {
      this.commentRole = CommentRole.DiscussionCreator;
    } else if (this.comment.user.id == this.discussion.community.creator.id) {
      this.commentRole = CommentRole.CommunityCreator;
    } else if (this.discussion.community.moderators.filter(m => m.id == this.comment.user.id)[0] !== undefined) {
      this.commentRole = CommentRole.CommunityModerator;
    } else {
      this.commentRole = CommentRole.User;
    }
  }

  isCommunityCreator() {
    return this.commentRole == CommentRole.CommunityCreator;
  }

  isCommunityModerator() {
    return this.commentRole == CommentRole.CommunityModerator;
  }

  isDiscussionCreator() {
    return this.commentRole == CommentRole.DiscussionCreator;
  }

  isDiscussionUser() {
    return this.commentRole == CommentRole.User;
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

interface DeleteData {
  deleteId: number;
}

interface CommentRankData {
  rank: number;
  commentId: number;
}

