import { RouterConfig }          from '@angular/router';
import {ChatComponent} from '../chat/index';

export const chatListRoutes: RouterConfig = [
  { path: '', component: ChatComponent},
  { path: 'comment/:commentId', component: ChatComponent }
];