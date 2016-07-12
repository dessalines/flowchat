import {Component, Input, provide, OnInit, ViewChild} from '@angular/core';
import { HTTP_PROVIDERS }    from '@angular/http';
import {ThreadedChatService} from '../../services/threaded-chat.service';
import {Comment, User, Discussion, Tag} from '../../shared';
import {CommentComponent} from '../comment/index';
import {DiscussionCardComponent} from '../discussion-card/index';
import {UserService} from '../../services/user.service';
import {DiscussionService} from '../../services/discussion.service';
import {Subscription} from 'rxjs/Subscription';
import { RouteConfig, ROUTER_DIRECTIVES, Router, RouteParams} from '@angular/router-deprecated';
import {RouteParamService} from '../../services/route-param.service';
import {MarkdownEditComponent} from '../markdown-edit/index';
import {ToasterService} from 'angular2-toaster/angular2-toaster';
import {MODAL_DIRECTVES, BS_VIEW_PROVIDERS} from 'ng2-bootstrap/ng2-bootstrap';


@Component({
  moduleId: module.id,
  selector: 'app-chat',
  templateUrl: 'chat.component.html',
  styleUrls: ['chat.component.css'],
  providers: [HTTP_PROVIDERS, ThreadedChatService],
  directives: [CommentComponent, MarkdownEditComponent, DiscussionCardComponent,MODAL_DIRECTVES],
  viewProviders: [BS_VIEW_PROVIDERS]
})
export class ChatComponent implements OnInit {

  private comments: Array<Comment>;
  private scrollDebounce: number = 0;
  private currentTopLimit: number = 20;
  private currentCommentBatchSize: number = 20;
  private maxDepth: number = 20;

  private users: Array<User>;
  private newCommentId: number;

  private recursiveCommentStopper: boolean = false;

  private isReplying: boolean = false;

  private userServiceWatcher: Subscription;
  private threadedChatSubscription: Subscription;
  private discussionSubscription: Subscription;

  private discussionId: number = null;
  private topParentId: number = null;

  private discussion: Discussion;

  private topReply: string;
  private clearTopReply: boolean = false;

  // This is set to true on ngOnDestroy, to not do an alert for reconnect
  private websocketSoftClose: boolean = false;

  @ViewChild('reconnectModal') reconnectModal;

  constructor(private threadedChatService: ThreadedChatService,
    private userService: UserService,
    private router: Router,
    private routeParams: RouteParams,
    private routeParamService: RouteParamService,
    private discussionService: DiscussionService,
    private toasterService: ToasterService) {


  }

  ngOnInit() {
    // console.log(this.routeParams);
    // console.log(this.routeParamService.params());
    this.discussionId = Number(this.routeParamService.params().get("discussionId"));
    if (this.routeParams.get("commentId") != null) {
      this.topParentId = Number(this.routeParams.get("commentId"));
    }
    this.threadedChatService.connect(this.discussionId, this.topParentId);
    
    this.subscribeToChat();
    this.subscribeToUserServiceWatcher();
    this.websocketCloseWatcher();

    this.subscribeToDiscussion();

  }

  editMode(): Boolean {
    return Boolean(this.routeParamService.params().get("editMode"));
  }

  ngOnDestroy() {
    this.websocketSoftClose = true;
    if (this.userServiceWatcher != null) {
      this.threadedChatService.ws.close(true);
      console.log('Destroying chat component');
      this.userServiceWatcher.unsubscribe();
      this.threadedChatSubscription.unsubscribe();
      this.threadedChatService = null;
      this.discussionSubscription.unsubscribe();
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
      },
      error => {
        this.toasterService.pop("error", "Error", error);
        this.router.navigate(['/Home']);
      })
  }

  websocketCloseWatcher() {
    this.threadedChatService.ws.onClose(cb => {

      if (!this.websocketSoftClose) {
        console.log('ws connection closed');

        this.reconnectModal.show();
      }
    });
  }


  websocketReconnect() {
    this.threadedChatService.connect(this.discussionId, this.topParentId);
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
    console.log(data);

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
      // If the user isn't logged in, set the cookies to this anonymous user
      if (this.userService.getUser() == null) {
        this.userService.setUser(data.user);
      }
    }

    if (data.discussion) {
      this.userService.updateFavoriteDiscussions(data.discussion);
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

    if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight) {
      if (this.scrollDebounce == 0) {
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
      setTimeout(() => { document.getElementById("comment_" + newComment.id).scrollIntoView();},50);
      return;
    }

    // Do a recursive loop to find and push the new comment
    this.recursiveComment(newComment, this.comments, true);
    this.recursiveCommentStopper = false;

    // Focus on the new comment if not replying
    if (!this.isReplying) {
      setTimeout(() => { document.getElementById("comment_" + newComment.id).scrollIntoView();},50);
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
    comments.sort((a, b) => {
      return b.avgRank - a.avgRank;
    });


    this.recursiveCommentStopper = true;
  }


  setIsReplying($event) {
    this.isReplying = $event;
  }


}

interface TopReplyData {
  topReply: string;
}

interface NextPageData {
  topLimit: number;
  maxDepth: number;
}
