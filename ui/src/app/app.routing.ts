import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {HomeComponent, 
  TagComponent, 
  UserComponent, 
  CommunityComponent, 
  CommunityModlogComponent, 
  SidebarComponent, 
  DiscussionComponent} from './components';

const routes: Routes = [
  {
    path: '',
    component: HomeComponent
  },
  {
    path: 'all',
    component: HomeComponent
  },
  {
    path: 'user/:userId',
    component: UserComponent
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
@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule { }