import {Component, Input, provide, OnInit} from '@angular/core';
import { HTTP_PROVIDERS }    from '@angular/http';
import {ThreadedChatService} from '../services/threaded-chat.service';
import {Comment, User} from '../shared';
import {CommentComponent} from '../comment';

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

  private user: User;

  constructor(private threadedChatService: ThreadedChatService) {

    this.threadedChatService.ws.getDataStream().subscribe(res => {
      this.updateThreadedChat(res.data);
    });

  }

  ngOnInit() { }


  // TODO only send specifically added comment object
  // alter the comments by searching through them, and only changing the specific one
  // use push, set,etc. Don't replace all the comments

  // The replying mode should be event emitters pushed up to this top component,
  // not the specific one
  updateThreadedChat(someData) {
    let data = JSON.parse(someData);
    console.log(data);

    if (data.comments) {
      this.comments = data.comments;
    }

    if (data.users) {
      this.users = data.users;
    }

    // For only new comments
    if (data.numOfParents) {
      let newComment: Comment = data;

      // Gotta basically place this in the correct location in the array of comments
      this.addNewComment(newComment);
    }

  }

  private addNewComment(newComment: Comment) {

    // If its the top level, stop and return 
    if (newComment.parentId == null) {
      this.comments.push(newComment);
      return;
    }

    // filter to find correct top level
    let topLevel: Comment = this.comments.filter(item => item.id == newComment.topParentId)[0];

    // Do a recursive loop to find and push the new comment
    this.recursiveComment(newComment, topLevel);
    this.recursiveCommentStopper = false;

    // Focus on the new comment if not replying
    if (!this.isReplying) {
      setTimeout(() => { location.href = "#comment_" + newComment.id; }, 50);
    }
  }

  private recursiveComment(newComment: Comment, parent: Comment) {

    if (parent.id == newComment.parentId) {
      parent.embedded.push(newComment);
      this.recursiveCommentStopper = true;
    } else {
      for (let emb of parent.embedded) {
        if (!this.recursiveCommentStopper) {
          this.recursiveComment(newComment, emb);
        }
      }
    }
  }

  setIsReplying($event) {
    this.isReplying = $event;
  }

}
