import {Injectable}    from '@angular/core';
import {$WebSocket, WebSocketConfig}    from './ng2-websocket';
import {environment} from '../environment';

@Injectable()
export class ThreadedChatService {

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

    let url = environment.websocket + "?discussionId=" + discussionId + "&topParentId=" + topParentId;
    // this.ws = new $WebSocket(url, null, this.config);
    this.ws = new $WebSocket(url);
    this.ws.connect();

  }

  send(data) {
    try {
      this.ws.send(data);
    } catch (e) {
      console.log('aaaah I died');
      alert('DERP');
      console.log(e);
    }
  }

  reconnect() {
    return this.connect(this.discussionId, this.topParentId);
  }


}
