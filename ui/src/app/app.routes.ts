import { provideRouter, RouterConfig } from '@angular/router';
import {HomeComponent} from './components/home/index';
import {TagComponent} from './components/tag/index';
import {ChatListComponent} from './components/chat-list/index';
import {ChatComponent} from './components/chat/index';

const routes: RouterConfig = [
  {
    path: '',
    component: HomeComponent
  },
  {
    path: 'tag/:tagId',
    component: TagComponent
  },
  {
    path: 'discussion',
    component: ChatListComponent,
    children: [
      {
        path: ':discussionId', 
        component: ChatComponent
      },
      {
        path: ':discussionId/comment/:commentId',
        component: ChatComponent
      }
    ]
  }
];

export const appRouterProviders = [
  provideRouter(routes)
];