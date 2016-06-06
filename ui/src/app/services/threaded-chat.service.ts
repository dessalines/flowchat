import {Injectable}    from '@angular/core';
import {$WebSocket}    from './ng2-websocket';


@Injectable()
export class ThreadedChatService {

  private messagesUrl = 'ws://localhost:4567/threaded_chat';

  public ws: $WebSocket;

  constructor() {
    this.ws = new $WebSocket(this.messagesUrl);
    this.ws.connect();
  };

}
