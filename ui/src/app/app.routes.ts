import { provideRouter, RouterConfig } from '@angular/router';
import {HomeComponent} from './components/home';
import {TagComponent} from './components/tag';
import {ChatListComponent} from './components/chat-list';
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