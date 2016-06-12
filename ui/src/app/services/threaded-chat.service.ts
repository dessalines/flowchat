import {Injectable}    from '@angular/core';
import {$WebSocket, WebSocketConfig}    from './ng2-websocket';


@Injectable()
export class ThreadedChatService {

  private messagesUrl = 'ws://localhost:4567/threaded_chat';

  public ws: $WebSocket;

  public config: WebSocketConfig;

  constructor() {
		this.config = {
			initialTimeout: 30000,
			maxTimeout: 120000,
			reconnectIfNotNormalClose: true
		}

    this.ws = new $WebSocket(this.messagesUrl, null, this.config);
    this.ws.connect();
  }

  reconnect() {
    return this.constructor();
  }


}
