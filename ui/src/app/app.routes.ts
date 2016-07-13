import { provideRouter, RouterConfig } from '@angular/router';
import {HomeComponent} from './components/home/index';
import {TagComponent} from './components/tag/index';
import {ChatListComponent} from './components/chat-list/index';
import {chatListRoutes} from './components/chat-list/chat-list.routes';

const routes: RouterConfig = [
  ...chatListRoutes,
  { path: '', component: HomeComponent },
  { path: 'tag/:tagId', component: TagComponent },
  { path: 'discussion/:discussionId/...', component: ChatListComponent }
];

export const appRouterProviders = [
  provideRouter(routes)
];