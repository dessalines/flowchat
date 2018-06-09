import {Injectable}    from '@angular/core';
import {NG2WebSocket}    from './ng2-websocket';
import {WebSocketConfig} from '../shared';
import {environment} from '../../environments/environment';

@Injectable()
export class ThreadedChatService {

  public ws: NG2WebSocket;

  public config: WebSocketConfig;

  private discussionId: number;
  private topParentId: number;
  private sortType: string;

  constructor() {
    // this.config = {
    // 	initialTimeout: 30000,
    // 	maxTimeout: 120000,
    // 	reconnectIfNotNormalClose: true
    // }

  }

  connect(discussionId: number, topParentId: number, sortType: string) {
    this.discussionId = discussionId;
    this.topParentId = topParentId;
    this.sortType = sortType;

    let url = environment.websocket + "?discussionId=" + discussionId + "&topParentId=" + topParentId + "&sortType=" + sortType;
    // this.ws = new $WebSocket(url, null, this.config);
    this.ws = new NG2WebSocket(url);
    this.ws.connect();

  }

  send(data) {
    try {
      this.ws.send(data);
    } catch (e) {
      console.error('aaaah I died');
    }
  }

  reconnect() {
    return this.connect(this.discussionId, this.topParentId, this.sortType);
  }


}
