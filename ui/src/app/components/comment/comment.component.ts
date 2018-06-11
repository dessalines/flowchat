import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Comment, CommentRole, Discussion, Tools } from '../../shared';
import { UserService, ThreadedChatService } from '../../services';
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

  public replyText: string;

  public showReply: boolean = false;

  public editText: string;

  public showEdit: boolean = false;

  public collapsed: boolean = false;

  public showVoteSlider: boolean = false;

  public rank: number;

  public isCreator: boolean = false;

  public isModerator: boolean = false;

  public commentRole: CommentRole;

  constructor(private threadedChatService: ThreadedChatService,
    private userService: UserService) {
  }

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
    this.setIsCreator();
    this.setIsModerator();
    this.setCommentRole();
    this.setRank();
  }

  ngAfterViewInit() {
    Tools.zooming.listen('.img-zoomable');
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
    if (this.comment.userRank !== undefined) {
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

  toggleStickyComment() {
    this.comment.stickied = !this.comment.stickied;
    this.threadedChatService.send(this.stickyData());
  }



  replyData(): ReplyData {
    return {
      parentId: this.comment.id,
      reply: this.replyText
    }
  }

  editData(): EditData {
    return {
      id: this.comment.id,
      edit: this.editText,
    }
  }

  stickyData(): StickyData {
    return {
      id: this.comment.id,
      sticky: this.comment.stickied,
    }
  }

  deleteData(): DeleteData {
    return {
      deleteId: this.comment.id
    }
  }

  commentRankData(): CommentRankData {
    return {
      rank: this.rank,
      commentId: this.comment.id
    }

  }

  collapseText(): string {
    return (this.collapsed) ? "[+]" : "[-]";
  }

  isCommentNew(): boolean {
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

  setIsCreator() {
    if (this.userService.getUser() != null &&
      this.comment.user.id == this.userService.getUser().id) {
      this.isCreator = true;
    }
  }

  setIsModerator() {
    if (this.discussion.community.creator.id === this.userService.getUser().id) {
      this.isModerator = true;
    } else {
      let m = this.discussion.community.moderators.filter(m => m.id == this.userService.getUser().id)[0];
      if (m !== undefined) {
        this.isModerator = true;
      } else {
        this.isModerator = false;
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

  voteAvg(): string {
		return (this.comment.avgRank !== undefined) ? (this.comment.avgRank).toFixed(0).toString() : 'none';
	}

	voteCount(): string {
		return (this.comment.voteCount) ? this.comment.voteCount.toString() : '0';
  }

  voteExists(): boolean {
    return this.rank !== undefined && this.rank !== null;
  }

  upvote() {
    let newVote: number = (this.rank !== 100) ? 100 : null;
    this.saveRank(newVote);
  }

  downvote() {
    let newVote: number = (this.rank !== 0) ? 0 : null;
    this.saveRank(newVote);
  }

  colors = ['transparent', 'red', 'blue', 'green', 'yellow', 'purple', 'cyan','orange','white'];
  commentBorderColor(): string {
    return this.colors[(this.comment.pathLength % (this.colors.length))];
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

interface StickyData {
  id: number;
  sticky: boolean;
}

interface DeleteData {
  deleteId: number;
}

interface CommentRankData {
  rank: number;
  commentId: number;
}

