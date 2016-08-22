import { Component, OnInit } from '@angular/core';
import {Router, ROUTER_DIRECTIVES } from '@angular/router';
import {DiscussionComponent} from '../discussion/index';
import {UserService} from '../../services/user.service';

@Component({
  moduleId: module.id,
  selector: 'app-sidebar',
  templateUrl: 'sidebar.component.html',
  styleUrls: ['sidebar.component.css'],
  directives: [ROUTER_DIRECTIVES]
})
export class SidebarComponent implements OnInit {

  discussionCollapsed: boolean = false;
  communityCollapsed: boolean = false;

  constructor(private router: Router,
    private userService: UserService) {}

  ngOnInit() {
  }

  toggleDiscussionCollapse() {
    this.discussionCollapsed = !this.discussionCollapsed;
  }

  toggleCommunityCollapse() {
    this.communityCollapsed = !this.communityCollapsed;
  }

}
