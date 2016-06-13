import {Injectable}    from '@angular/core';
import {$WebSocket, WebSocketConfig}    from './ng2-websocket';


@Injectable()
export class ThreadedChatService {

  private messagesUrl = 'ws://localhost:4567/threaded_chat';

  public ws: $WebSocket;

  public config: WebSocketConfig;

  private discussionId: number;
  private topParentId: number;

  constructor() {
		// this.config = {
		// 	initialTimeout: 30000,
		// 	maxTimeout: 120000,
		// 	reconnectIfNotNormalClose: true
		// }

  }

  connect(discussionId: number, topParentId: number) {
    this.discussionId = discussionId;
    this.topParentId = topParentId;

    let url = this.messagesUrl + "?discussionId=" + discussionId + "&topParentId=" + topParentId;
    // this.ws = new $WebSocket(url, null, this.config);
    this.ws = new $WebSocket(url);
    this.ws.connect();
  }

  reconnect() {
    return this.connect(this.discussionId, this.topParentId);
  }


}
