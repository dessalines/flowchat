import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Subscription } from 'rxjs/Rx';
import { Router, ActivatedRoute } from '@angular/router';

import { ThreadedChatService, DiscussionService, UserService } from '../../services';
import { Comment, User, Discussion, Tag, CommentRole, MessageType, Tools } from '../../shared';
import { MarkdownEditComponent } from '../markdown-edit/index';
import { ToasterService } from 'angular2-toaster/angular2-toaster';

@Component({
  selector: 'app-discussion',
  templateUrl: './discussion.component.html',
  styleUrls: ['./discussion.component.scss'],
  providers: [ThreadedChatService],
})
export class DiscussionComponent implements OnInit {

  public comments: Array<Comment>;
  public scrollDebounce: number = 0;
  public currentTopLimit: number = 20;
  public currentCommentBatchSize: number = 20;
  public maxDepth: number = 20;

  public users: Array<User>;
  public newCommentId: number;

  public recursiveCommentStopper: boolean = false;

  public isReplying: boolean = false;

  public userServiceWatcher: Subscription;
  public threadedChatSubscription: Subscription;
  public discussionSubscription: Subscription;

  public discussionId: number = null;
  public topParentId: number = null;

  public discussion: Discussion;

  public topReply: string;
  public clearTopReply: boolean = false;

  // This is set to true on ngOnDestroy, to not do an alert for reconnect
  public websocketSoftClose: boolean = false;

  public sub: any;

  public isModerator: boolean = false;

  public usersCollapsed: boolean = false;

  public editing: boolean = false;

  @ViewChild('reconnectModal') reconnectModal;

  constructor(public threadedChatService: ThreadedChatService,
    public userService: UserService,
    public route: ActivatedRoute,
    public router: Router,
    public titleService: Title,
    public discussionService: DiscussionService,
    public toasterService: ToasterService) {
  }

  ngOnInit() {

    this.sub = this.route.params.subscribe(params => {

      this.userService.userObservable.subscribe(user => {
        if (user) {
          this.discussionId = +params["discussionId"];
          this.editing = Boolean(this.route.snapshot.params["editMode"]);

          if (+params["commentId"] != null) {
            this.topParentId = +params["commentId"];
          }

          if (this.threadedChatService.ws != null) {
            this.unloadSubscriptions();
          }

          this.threadedChatService.connect(this.discussionId, this.topParentId, this.userService.getUserSettings().defaultCommentSortTypeRadioValue);


          this.subscribeToChat();
          this.subscribeToDiscussion();
        }
      });

    });

  }

  ngOnDestroy() {
    this.unloadSubscriptions();
  }

  editingChanged($event) {
    this.editing = $event;
    if (this.editing == false) {
      // Removing editing from the window location hash
      window.history.pushState('', 'title', window.location.hash.split(";")[0]);
    }
  }

  unloadSubscriptions() {
    // this.websocketSoftClose = true;
    this.scrollDebounce = 0;
    this.currentTopLimit = 20;
    this.threadedChatService.ws.close(true);
    // this.userServiceWatcher.unsubscribe();
    this.threadedChatSubscription.unsubscribe();

    this.discussionSubscription.unsubscribe();
    this.sub.unsubscribe();
    this.threadedChatService.ws.close(true);
    // this.threadedChatService = null;
    clearInterval(this.websocketCloseWatcher);

  }

  subscribeToChat() {
    this.threadedChatSubscription = this.threadedChatService.ws.getDataStream().
      subscribe(res => {
        this.update(res.data);
      });
  }

  // TODO this may need to have a subscribtion object as well
  subscribeToDiscussion() {
    this.discussionSubscription = this.discussionService.getDiscussion(this.discussionId).
      subscribe(d => {
        this.discussion = d;
        this.titleService.setTitle(d.title + ' - FlowChat');
        this.setIsModerator();
      },
        error => {
          this.toasterService.pop("error", "Error", error);
          this.router.navigate(['/']);
        });
  }

  websocketCloseWatcher = setInterval(() => {
    // check every 5 seconds for websocket disconnect 
    if (this.threadedChatService.ws.getReadyState() != 1) {
      this.websocketReconnect();
    }
  }, 5000);

  websocketReconnect() {
    this.threadedChatService.connect(this.discussionId, this.topParentId, this.userService.getUserSettings().defaultCommentSortTypeRadioValue);
    this.subscribeToChat();
    this.reconnectModal.hide();
  }


  // TODO only send specifically added comment object
  // alter the comments by searching through them, and only changing the specific one
  // use push, set,etc. Don't replace all the comments

  // The replying mode should be event emitters pushed up to this top component,
  // not the specific one

  update(dataStr: string) {
    let msg = JSON.parse(dataStr);

    switch (msg.message_type) {
      case MessageType.Comments:
        this.setComments(msg.data.comments);
        break;
      case MessageType.Users:
        this.setUsers(msg.data.users);
        break;
      case MessageType.Reply:
        this.addNewComment(msg.data);
        break;
      case MessageType.TopReply:
        this.addNewComment(msg.data);
        break;
      case MessageType.Edit:
        this.editComment(msg.data);
        break;
      case MessageType.SaveFavoriteDiscussion:
        this.userService.pushToFavoriteDiscussions(msg.data);
        break;
      case MessageType.Ping:
        this.sendPong();
        break;
      default:
        alert('wrong message: ' + dataStr);
    }

  }

  setComments(comments: Array<Comment>) {
    this.comments = comments;
  }

  setUsers(users: Array<User>) {
    this.users = users;
  }

  setTopReply($event) {
    this.topReply = $event;
  }

  sendTopReply() {
    let reply: TopReplyData = {
      topReply: this.topReply
    }
    this.threadedChatService.send(Tools.messageWrapper(MessageType.TopReply, reply));

    this.topReply = "";

    // necessary to reload the top replier
    this.clearTopReply = true;
    setTimeout(() => { this.clearTopReply = false }, 0);

  }

  onScroll(event) {

    if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight - 100) {
      if (this.scrollDebounce == 0 && this.isReplying == false) {
        this.scrollDebounce = 1;
        // you're at the bottom of the page
        this.currentTopLimit += this.currentCommentBatchSize;
        this.sendNextPage(this.currentTopLimit);
        setTimeout(() => this.scrollDebounce = 0, 1000);
      }
    }
  }

  sendNextPage(limit: number) {
    let nextPageData: NextPageData = {
      topLimit: limit,
      maxDepth: this.maxDepth
    }

    this.threadedChatService.send(Tools.messageWrapper(MessageType.NextPage, nextPageData));
  }

  private editComment(editedComment: Comment) {

    // If its the top level, stop and return
    if (editedComment.parentId == null) {
      this.replaceEditedComment(this.comments, editedComment);

      // could be a sticky change
      this.resort(this.comments);

      // setTimeout(() => { location.hash = "#comment_" + editedComment.id; }, 50);
      return;
    }


    // Do a recursive loop to find and push the new comment
    this.recursiveComment(editedComment, this.comments, false);
    this.recursiveCommentStopper = false;

    // Focus on the new comment if not replying
    if (!this.isReplying) {
      // setTimeout(() => { location.hash = "#comment_" + editedComment.id; }, 50);
    }

  }

  private addNewComment(newComment: Comment) {

    // If its the top level, stop and return
    if (newComment.parentId == null) {
      this.comments.unshift(newComment);
      setTimeout(() => { document.getElementById("comment_" + newComment.id).scrollIntoView(); }, 100);
      this.resort(this.comments);
      return;
    }

    // Do a recursive loop to find and push the new comment
    this.recursiveComment(newComment, this.comments, true);
    this.recursiveCommentStopper = false;

    // Focus on the new comment if not replying
    if (!this.isReplying) {
      setTimeout(() => { document.getElementById("comment_" + newComment.id).scrollIntoView(); }, 100);
    }

  }

  private recursiveComment(newComment: Comment, comments: Array<Comment>, isNew: boolean) {


    // isNew is for edited comments
    for (let parent of comments) {
      if (!this.recursiveCommentStopper) {

        // For new comments
        if (parent.id == newComment.parentId) {
          if (isNew) {
            parent.embedded.unshift(newComment);
            this.recursiveCommentStopper = true;
          } else {
            this.replaceEditedComment(parent.embedded, newComment);
          }
        }
        // Top level comments
        else {
          this.recursiveComment(newComment, parent.embedded, isNew);
        }
      }
    }
  }

  private replaceEditedComment(comments: Array<Comment>, editedComment: Comment) {
    let child = comments.filter(item => item.id == editedComment.id)[0];
    let index = comments.indexOf(child);

    comments[index].text = editedComment.text;
    comments[index].modified = editedComment.modified;
    comments[index].avgRank = editedComment.avgRank;
    comments[index].numberOfVotes = editedComment.numberOfVotes;
    comments[index].deleted = editedComment.deleted;

    // do a sort at that level:
    this.resort(comments);

    this.recursiveCommentStopper = true;
  }


  private resort(comments) {

    comments.sort((a: Comment, b: Comment) => {
      // sort the stickies first

      let stickyComp: number = (b.stickied === a.stickied) ? 0 : a.stickied ? -1 : 1;

      if (stickyComp != 0) {
        return stickyComp;
      }
      if (this.userService.getUserSettings().defaultCommentSortTypeRadioValue == 'top') {
        return b.avgRank - a.avgRank;
      } else if (this.userService.getUserSettings().defaultCommentSortTypeRadioValue == 'new') {
        return b.created - a.created;
      } else {
        return 0;
      }
    });
  }



  setIsReplying($event) {
    this.isReplying = $event;
  }

  setIsModerator() {
    let m = this.discussion.community.moderators.filter(m => m.id == this.userService.getUser().id)[0];
    if (m !== undefined) {
      this.isModerator = true;
    } else {
      this.isModerator = false;
    }
  }

  toggleUsersCollapsed() {
    this.usersCollapsed = !this.usersCollapsed;
  }

  getCommentRole(commentUserId: number): CommentRole {

    if (commentUserId == this.discussion.creator.id) {
      return CommentRole.DiscussionCreator;
    } else if (commentUserId == this.discussion.community.creator.id) {
      return CommentRole.CommunityCreator;
    } else if (this.discussion.community.moderators.filter(m => m.id == commentUserId)[0] !== undefined) {
      return CommentRole.CommunityModerator;
    } else {
      return CommentRole.User;
    }
  }

  changeSortType($event) {
    this.userService.getUserSettings().defaultCommentSortTypeRadioValue = $event;
    this.userService.saveUserSettings();
    this.unloadSubscriptions();
    this.comments = undefined;
    this.websocketReconnect();
  }

  sendPong() {
    console.debug("Received ping, sending pong");
    let pong = {
      pong: "pong"
    }
    this.threadedChatService.send(Tools.messageWrapper(MessageType.Pong, pong));
  }

}

interface TopReplyData {
  topReply: string;
}

interface NextPageData {
  topLimit: number;
  maxDepth: number;
}
