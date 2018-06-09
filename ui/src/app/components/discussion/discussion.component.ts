import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Subscription } from 'rxjs/Subscription';
import { Router, ActivatedRoute } from '@angular/router';

import { ThreadedChatService, DiscussionService, UserService } from '../../services';
import { Comment, User, Discussion, Tag, CommentRole } from '../../shared';
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
  public sortType: string = 'new';

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

  constructor(private threadedChatService: ThreadedChatService,
    private userService: UserService,
    private route: ActivatedRoute,
    private router: Router,
    private titleService: Title,
    private discussionService: DiscussionService,
    private toasterService: ToasterService) {
  }

  ngOnInit() {


    this.sub = this.route.params.subscribe(params => {
      this.discussionId = +params["discussionId"];
      this.editing = Boolean(this.route.snapshot.params["editMode"]);

      if (+params["commentId"] != null) {
        this.topParentId = +params["commentId"];
      }

      if (this.threadedChatService.ws != null) {
        this.unloadSubscriptions();
      }

      this.threadedChatService.connect(this.discussionId, this.topParentId, this.sortType);


      this.subscribeToChat();
      this.subscribeToUserServiceWatcher();
      this.websocketCloseWatcher();

      this.subscribeToDiscussion();


    });

  }

  ngOnDestroy() {
    this.unloadSubscriptions();
    this.sub.unsubscribe();
  }

  editingChanged($event) {
    this.editing = $event;
  }

  unloadSubscriptions() {
    this.websocketSoftClose = true;
    if (this.userServiceWatcher != null) {
      this.scrollDebounce = 0;
      this.currentTopLimit = 20;
      this.threadedChatService.ws.close(true);
      this.userServiceWatcher.unsubscribe();
      this.threadedChatSubscription.unsubscribe();
      // this.threadedChatService = null;
      this.discussionSubscription.unsubscribe();
      // this.sub.unsubscribe();
    }
  }

  subscribeToUserServiceWatcher() {
    this.userServiceWatcher = this.userService.userObservable.subscribe(res => {
      if (res != null) {
        this.threadedChatService.ws.close(true);
        this.threadedChatSubscription.unsubscribe();
        this.threadedChatService.reconnect();
        this.subscribeToChat();
      }
    });
  }

  subscribeToChat() {
    this.threadedChatSubscription = this.threadedChatService.ws.getDataStream().
      subscribe(res => {
        this.updateThreadedChat(res.data);
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

  websocketCloseWatcher() {
    // check every 5 seconds for websocket disconnect status
    setInterval(() => {

      if (this.threadedChatService.ws.getReadyState() != 1) {
        this.websocketReconnect();
      }
    }, 5000);

  }

  websocketReconnect() {
    this.threadedChatService.connect(this.discussionId, this.topParentId, this.sortType);
    this.subscribeToChat();
    this.reconnectModal.hide();
  }


  // TODO only send specifically added comment object
  // alter the comments by searching through them, and only changing the specific one
  // use push, set,etc. Don't replace all the comments

  // The replying mode should be event emitters pushed up to this top component,
  // not the specific one
  updateThreadedChat(someData: string) {
    let data = JSON.parse(someData);

    if (data.comments) {
      this.comments = data.comments;
    }

    if (data.users) {
      this.users = data.users;
    }

    // For only new comments
    if (data.reply) {
      let newComment: Comment = data.reply;

      // Gotta place this new comment in the correct location in the array of comments
      this.addNewComment(newComment);
    }

    if (data.edit) {
      let editedComment: Comment = data.edit;
      this.editComment(editedComment);
    }

    if (data.user) {
      if (this.userService.getUser() == null) {
        alert("no user set");
      }
    }

    if (data.discussion) {
      this.userService.pushToFavoriteDiscussions(data.discussion);
    }

  }

  setTopReply($event) {
    this.topReply = $event;
  }

  sendTopReply() {
    let reply: TopReplyData = {
      topReply: this.topReply
    }
    this.threadedChatService.send(reply);

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

    this.threadedChatService.send(nextPageData);
  }

  private editComment(editedComment: Comment) {

    // If its the top level, stop and return
    if (editedComment.parentId == null) {
      this.replaceEditedComment(this.comments, editedComment);

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
    if (this.sortType == 'top') {
      comments.sort((a, b) => {
        return b.avgRank - a.avgRank;
      });
    } else if (this.sortType == 'new') {
      comments.sort((a, b) => {
        return b.created - a.created;
      });
    }
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
    this.sortType = $event;
    this.unloadSubscriptions();
    this.comments = undefined;
    this.websocketReconnect();
  }

}

interface TopReplyData {
  topReply: string;
}

interface NextPageData {
  topLimit: number;
  maxDepth: number;
}
