import { Routes, RouterModule }   from '@angular/router';
import { ModuleWithProviders } from '@angular/core';
import {HomeComponent} from './components/home/index';
import {TagComponent} from './components/tag/index';
import {CommunityComponent} from './components/community/index';
import {CommunityModlogComponent} from './components/community-modlog/index';
import {SidebarComponent} from './components/sidebar/index';
import {DiscussionComponent} from './components/discussion/index';

const appRoutes: Routes = [
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
    path: 'community/:communityId/modlog',
    component: CommunityModlogComponent
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

export const appRoutingProviders: any[] = [

];

export const routing: ModuleWithProviders = RouterModule.forRoot(appRoutes,{ useHash: true });