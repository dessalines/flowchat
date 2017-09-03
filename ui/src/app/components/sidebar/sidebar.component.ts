import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { UserService } from '../../services';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss'],
})
export class SidebarComponent implements OnInit {

  public discussionCollapsed: boolean = false;
  public communityCollapsed: boolean = false;
  public splitUrl: Array<string>;

  constructor(private router: Router,
    public userService: UserService) { }

  ngOnInit() {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.splitUrl = event.url.split('/');
      }
    });
  }

  toggleDiscussionCollapse() {
    this.discussionCollapsed = !this.discussionCollapsed;
  }

  toggleCommunityCollapse() {
    this.communityCollapsed = !this.communityCollapsed;
  }

  public isActive(type: string, id: string) {
    if (this.splitUrl != null) {

      // Home and All types
      if (this.splitUrl.length == 2) {
        return (this.splitUrl[1] === type);
      } else {
        return (this.splitUrl[1] === type && this.splitUrl[2] === id.toString());
      }
    }
  }


}
