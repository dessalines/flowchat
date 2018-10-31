import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl } from '@angular/forms';
import { LoginService, UserService, DiscussionService, CommunityService, NotificationsService } from '../../services';
import { User, Discussion, Comment, Tools, Theme } from '../../shared';
import { Router } from '@angular/router';
import { Observable, Subject } from 'rxjs/Rx';
import { ToasterService } from 'angular2-toaster';
import { versions } from 'environments/versions';

declare var Favico: any;

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
  providers: [],
})
export class NavbarComponent implements OnInit {

  public showLoginModal: boolean = false;

  // The search bar
  public discussionSearchControl: FormControl = new FormControl();
  public discussionSearchForm: FormGroup = new FormGroup({
    discussionSearchControl: this.discussionSearchControl
  });
  public discussionSearchSelected: string = '';
  public discussionSearchResultsObservable: Observable<any>;
  public discussionTypeaheadLoading: boolean = false;
  public discussionTypeaheadNoResults: boolean = false;

  public unreadMessages: Array<Comment>;

  public favIcon: any;

  public collapseNavbar: boolean = true;

  public version: string = JSON.stringify(versions, null, 2);

  constructor(public userService: UserService,
    private loginService: LoginService,
    private router: Router,
    private discussionService: DiscussionService,
    private communityService: CommunityService,
    private notificationsService: NotificationsService,
    private toasterService: ToasterService) {

  }

  ngOnInit() {

    this.setupFavIcon();

    this.setupDiscussionSearch();
    if (this.userService.getUser()) {
      this.fetchNotifications();
    }
  }

  setupFavIcon() {
    this.favIcon = new Favico({
      animation: 'pop'
    });
  }

  setupUser(user: any) {
    this.userService.setUserFromCookie();
  }

  gotoSample() {
    this.router.navigate(['/discussion']);
  }

  createDiscussion() {
    this.discussionService.createDiscussionBlank().subscribe(d => {
      this.userService.fetchFavoriteDiscussions();
      this.router.navigate(['/discussion', d.id, { editMode: true }]);
    },
      error => console.error(error));
  }

  createCommunity() {
    this.communityService.createCommunity().subscribe(d => {
      this.userService.fetchFavoriteCommunities();
      this.router.navigate(['/community', d.id, { editMode: true }]);
    },
      error => console.error(error));
  }

  // Discussion searching methods
  // Tag search methods
  setupDiscussionSearch() {
    this.discussionSearchResultsObservable = Observable.create((observer: any) => {
      this.discussionService.searchDiscussions(this.discussionSearchControl.value)
        .subscribe((result: any) => {
          observer.next(result.discussions);
        });
    });
  }


  discussionTypeaheadOnSelect(discussion: Discussion) {
    this.router.navigate(['/discussion', discussion.id]);
  }

  discussionChangeTypeaheadLoading(e: boolean): void {
    this.discussionTypeaheadLoading = e;
  }

  discussionChangeTypeaheadNoResults(e: boolean): void {
    this.discussionTypeaheadNoResults = e;
  }

  fetchNotifications() {
    this.notificationsService.getUnreadMessages().subscribe(
      t => {
        this.unreadMessages = t.comments;
        this.changeFaviconBasedOnMessages();
      });
  }

  changeFaviconBasedOnMessages() {
    if (this.unreadMessages.length > 0) {
      this.favIcon.badge(this.unreadMessages.length);
    } else {
      this.favIcon.reset();
    }
  }

  gotoMessage(message: Comment) {

    // Mark the message as read
    this.notificationsService.markMessageAsRead(message.id).subscribe(() => {

      // Remove it from the array
      let index = this.unreadMessages.indexOf(message);
      this.unreadMessages.splice(index, 1);
      this.changeFaviconBasedOnMessages();


      // Navigate to the parent message (IE, your message)
      this.router.navigate(['/discussion', message.discussionId,
        'comment', message.parentId]);

    },
      error => {
        console.error(error);
        this.toasterService.pop("error", "Error", error);
      });
  }

  markAllNotificationsAsRead() {
    this.notificationsService.markAllAsRead().subscribe(
      t => {
        this.unreadMessages = [];
        this.changeFaviconBasedOnMessages();
      });
  }

  toggleCollapseNavbar() {
    this.collapseNavbar = !this.collapseNavbar;
  }

  toggleLoginModal() {
    this.showLoginModal = true;
  }

  hiddenEvent() {
    this.showLoginModal = false;
  }

  logout() {
    Tools.eraseCookie('jwt');
    location.reload();
  }

  changeTheme() {

    // Toggle the theme, save the new one.
    let newTheme: Theme = (this.userService.getUserSettings().theme == Theme.Light) ? Theme.Dark :  Theme.Light;
    this.userService.getUserSettings().theme = newTheme;
    this.userService.saveUserSettings();

    Tools.applyTheme(newTheme);
    // location.reload();
  }

}



