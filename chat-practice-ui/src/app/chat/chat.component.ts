import {Component, Input, provide, OnInit} from '@angular/core';
import { HTTP_PROVIDERS }    from '@angular/http';
import {ChatService}       from './services/chat.service';

@Component({
  moduleId: module.id,
  selector: 'app-chat',
  templateUrl: 'chat.component.html',
  styleUrls: ['chat.component.css'],
  providers: [HTTP_PROVIDERS, ChatService]
})
export class ChatComponent implements OnInit {

    errorMessage: string;
    private message: string;
    private messages: Array<string> = [];
    private userList: Array<string> = [];


    constructor(private _chatService: ChatService) {
        this._chatService.ws.getDataStream().subscribe(res => {
            this.updateChat(res.data);
        });
    }

    ngOnInit() {

    }


    updateChat(msg) {
        var data = JSON.parse(msg);
        console.log(data);
        this.messages.push(data.userMessage);
        this.userList = data.userList;


    }

    sendMessage() {
        this._chatService.ws.send(this.message);
        // this.messages.push(this.message);
    }


}
