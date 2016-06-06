import {Component, Input, provide, OnInit} from '@angular/core';
import { HTTP_PROVIDERS }    from '@angular/http';
import {ChatService}       from '../services/chat.service';
import {ThreadedChatService} from '../services/threaded-chat.service';
import {Comment} from '../shared';
import {CommentComponent} from '../comment';



@Component({
  moduleId: module.id,
  selector: 'app-chat',
  templateUrl: 'chat.component.html',
  styleUrls: ['chat.component.css'],
  providers: [HTTP_PROVIDERS, ChatService, ThreadedChatService],
  directives: [CommentComponent]
})
export class ChatComponent implements OnInit {

    errorMessage: string;
    private message: string;
    private messages: Array<string> = [];
    private userList: Array<string> = [];

    private comments: Array<Comment>;
    private users: Array<string> = [];


    constructor(private chatService: ChatService, private threadedChatService: ThreadedChatService) {
        this.chatService.ws.getDataStream().subscribe(res => {
            this.updateChat(res.data);
        });

        this.threadedChatService.ws.getDataStream().subscribe(res => {
            this.updateThreadedChat(res.data);
        });
       

        
    }

    ngOnInit() {}


    updateChat(msg) {
        let data = JSON.parse(msg);
        console.log(data);
        this.messages.push(data.userMessage);
        this.userList = data.userList;


    }

    updateThreadedChat(msg) {
            let data = JSON.parse(msg);
            console.log(data);
            this.comments = data.comments;
            this.users = data.users;
    }

    sendMessage() {
        this.chatService.ws.send(this.message);
    }


}
