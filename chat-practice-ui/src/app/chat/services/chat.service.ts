import {Injectable}    from '@angular/core';
import {$WebSocket}    from './ng2-websocket';

@Injectable() 
export class ChatService {
  private _messagesUrl = 'ws://localhost:4567/chat';

  public ws: $WebSocket;

  constructor() { 
    this.ws = new $WebSocket(this._messagesUrl);
    this.ws.connect();
    // this.ws.send("Hello");
  };

  


}