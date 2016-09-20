import { Component, OnInit } from '@angular/core';
import {Router } from '@angular/router';
import {UserService} from '../../services';

@Component({
  selector: 'app-sidebar',
  templateUrl: 'sidebar.component.html',
  styleUrls: ['sidebar.component.scss'],
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
