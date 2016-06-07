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
    updateThreadedChat(someData) {
        let data = JSON.parse(someData);
        console.log(data);

        if (data.comments) {
            this.comments = data.comments; 
        }

        if (data.users) {
            this.users = data.users;
        }

        if (data.newCommentId) {
            this.newCommentId = data.newCommentId;
        }

    }

}
