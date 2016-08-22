import { provideRouter, RouterConfig } from '@angular/router';
import {HomeComponent} from './components/home/index';
import {TagComponent} from './components/tag/index';
import {CommunityComponent} from './components/community/index';
import {SidebarComponent} from './components/sidebar/index';
import {DiscussionComponent} from './components/discussion/index';

const routes: RouterConfig = [
  {
    path: '',
    component: HomeComponent
  },
  {
    path: 'all',
    component: HomeComponent
  },
  {
    path: 'tag/:tagId',
    component: TagComponent
  },
  {
    path: 'community/:communityId',
    component: CommunityComponent
  },
  {
    path: 'discussion/:discussionId',
    component: DiscussionComponent,
  },
  {
    path: 'discussion/:discussionId/comment/:commentId',
    component: DiscussionComponent
  }
];

export const appRouterProviders = [
  provideRouter(routes)
];