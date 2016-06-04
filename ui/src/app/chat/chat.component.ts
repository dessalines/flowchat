import {Component, Input, provide, OnInit} from '@angular/core';
import { HTTP_PROVIDERS }    from '@angular/http';
import {ChatService}       from './services/chat.service';
import {TempService} from './services/temp.service';
import {Comment} from '../shared/comment.interface';
import {CommentComponent} from '../comment';



@Component({
  moduleId: module.id,
  selector: 'app-chat',
  templateUrl: 'chat.component.html',
  styleUrls: ['chat.component.css'],
  providers: [HTTP_PROVIDERS, ChatService, TempService],
  directives: [CommentComponent]
})
export class ChatComponent implements OnInit {

    errorMessage: string;
    private message: string;
    private messages: Array<string> = [];
    private userList: Array<string> = [];

    private tempObj: Array<Comment>;


    constructor(private chatService: ChatService, private tempService: TempService) {
        this.chatService.ws.getDataStream().subscribe(res => {
            this.updateChat(res.data);
        });

        this.tempService.getData().subscribe(res => {
            this.tempObj = res;
            console.log(this.tempObj);
        });
       

        
    }

    ngOnInit() {

    }


    updateChat(msg) {
        let data = JSON.parse(msg);
        console.log(data);
        this.messages.push(data.userMessage);
        this.userList = data.userList;


    }

    sendMessage() {
        this.chatService.ws.send(this.message);
        // this.messages.push(this.message);
    }


}
