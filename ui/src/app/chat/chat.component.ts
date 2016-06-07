import {Component, Input, provide, OnInit} from '@angular/core';
import { HTTP_PROVIDERS }    from '@angular/http';
import {ThreadedChatService} from '../services/threaded-chat.service';
import {Comment} from '../shared';
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
    private users: Array<string> = [];
    private newCommentId: number;

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

        if (data.id) {
            console.log('its only new comment');

            let newComment: Comment = data;

            // Gotta basically place this in the correct location in the array of comments
            this.addNewComment(newComment);
        }

    }


    // sample:
    // Object { id: 44, userId: 197, discussionId: 1, parentId: 43, topParentId: 1, pathLength: 6, 
    // numOfParents: 6, numOfChildren: 0, userName: "user_197", text: "fdszxcv", topParentId: 1,
    // userId: 197, userName: "user_197"}

    private addNewComment(newComment: Comment) {

        // First test adding comment as a top level(to see if it rerenders everything :(
        // this.comments.push(newComment); // checks out, it didn't rerender

        // Next test adding it as one down
        // this.comments[0].embedded.push(newComment); // checks out, it didn't render


        // If its the top level, stop and return 
        if (newComment.parentId == null) {
            this.comments.push(newComment);
            return;
        }


        // loop to find correct top level
        let topLevel: Comment = this.comments.filter(item => item.id == newComment.topParentId)[0];

        console.log("new comment id: " + newComment.id);
        
        let parent = topLevel; 

        this.recursiveComment(newComment, parent);

        // break and continue
        // http://stackoverflow.com/a/2549333/1655478



        // recursive loop to find immediate parent



        // add the comment to the immediate parent

        // force a reload of that immediate parent?

    }

  private recursiveComment(newComment: Comment, parent: Comment) {
      for (let ) {


          console.log("parent:");
          console.log(parent);

            if (parent.id == newComment.id) {
              console.log("found the correct parent id");
              parent.embedded.push(newComment);
              break;
          } else {
              // loop over the embedded array
              let level: Comment = parent.embedded.filter(item => item.id == newComment.parentId)[0];
              console.log(level);
              if (level == null) {

              }
          }



  }
  }

}
