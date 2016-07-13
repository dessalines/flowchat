import { RouterConfig }          from '@angular/router';
import {ChatComponent} from '../chat';

export const chatListRoutes: RouterConfig = [
  { path: '/', component: ChatComponent},
  { path: '/comment/:commentId', component: ChatComponent }
];