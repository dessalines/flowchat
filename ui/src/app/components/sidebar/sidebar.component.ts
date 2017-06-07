import { Component, OnInit } from '@angular/core';
import {Router } from '@angular/router';
import {UserService} from '../../services';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss'],
})
export class SidebarComponent implements OnInit {

  public discussionCollapsed: boolean = false;
  public communityCollapsed: boolean = false;

  constructor(private router: Router,
    public userService: UserService) {}

  ngOnInit() {
  }

  toggleDiscussionCollapse() {
    this.discussionCollapsed = !this.discussionCollapsed;
  }

  toggleCommunityCollapse() {
    this.communityCollapsed = !this.communityCollapsed;
  }

}
