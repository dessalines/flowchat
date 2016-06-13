import {Component, Input, provide, OnInit} from '@angular/core';
import { HTTP_PROVIDERS }    from '@angular/http';
import {ThreadedChatService} from '../services/threaded-chat.service';
import {Comment, User} from '../shared';
import {CommentComponent} from '../comment';
import {UserService} from '../services/user.service';
import {Subscription} from 'rxjs/Subscription';
import { RouteConfig, ROUTER_DIRECTIVES, Router, RouteParams} from '@angular/router-deprecated';
import {RouteParamService} from '../services/route-param.service';

@Component({
  moduleId: module.id,
  selector: 'app-chat',
  templateUrl: 'chat.component.html',
  styleUrls: ['chat.component.css'],
  providers: [HTTP_PROVIDERS, ThreadedChatService],
  directives: [CommentComponent]
})
export class ChatComponent implements OnInit {

  private comments: Array<Comment>;
  private users: Array<User>;
  private newCommentId: number;

  private recursiveCommentStopper: boolean = false;

  private isReplying: boolean = false;

  private userServiceWatcher: Subscription;
  private threadedChatSubscription: Subscription;

  private discussionId: number = 1;
  private topParentId: number = null;

  constructor(private threadedChatService: ThreadedChatService,
    private userService: UserService,
    private router: Router,
    private routeParams: RouteParams,
    private routeParamService: RouteParamService) {

  }

  ngOnInit() {
    console.log(this.routeParams);
    console.log(this.routeParamService.params());
    this.discussionId = Number(this.routeParamService.params().get("discussionId"));
    if (this.routeParams.get("commentId") != null) {
      this.topParentId = Number(this.routeParams.get("commentId"));
    }
    // this.topParentId = null;
    this.threadedChatService.connect(this.discussionId, this.topParentId);
    this.subscribeToChat();
    this.subscribeToUserServiceWatcher();
  }

  ngOnDestroy() {

    if (this.userServiceWatcher != null) {
      this.threadedChatService.ws.close(true);
      console.log('Destroying chat component');
      this.userServiceWatcher.unsubscribe();
      this.threadedChatSubscription.unsubscribe();
      this.threadedChatService = null;
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

  }

  private editComment(editedComment: Comment) {

    // Do a recursive loop to find and push the new comment
    this.recursiveComment(editedComment, this.comments, false);
    this.recursiveCommentStopper = false;

    // Focus on the new comment if not replying
    if (!this.isReplying) {
      setTimeout(() => { location.hash = "#comment_" + editedComment.id; }, 50);
    }
  }

  private addNewComment(newComment: Comment) {

    // If its the top level, stop and return 
    if (newComment.parentId == null) {
      this.comments.push(newComment);
      return;
    }

    // Do a recursive loop to find and push the new comment
    this.recursiveComment(newComment, this.comments, true);
    this.recursiveCommentStopper = false;

    // Focus on the new comment if not replying
    if (!this.isReplying) {
      setTimeout(() => { location.hash = "#comment_" + newComment.id; }, 50);
    }
  }

  private recursiveComment(newComment: Comment, comments: Array<Comment>, isNew: boolean) {


    // isNew is for edited comments
    for (let parent of comments) {
      if (!this.recursiveCommentStopper) {

        // For new comments
        if (parent.id == newComment.parentId) {
          if (isNew) {
            parent.embedded.push(newComment);
            this.recursiveCommentStopper = true;
          } else {
            let child = parent.embedded.filter(item => item.id == newComment.id)[0];
            let index = parent.embedded.indexOf(child);

            parent.embedded[index] = newComment;
            this.recursiveCommentStopper = true;
          }
        } else {
          this.recursiveComment(newComment, parent.embedded, isNew);
        }
      }
    }
  }


  setIsReplying($event) {
    this.isReplying = $event;
  }

}
