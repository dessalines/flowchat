import { Component, OnInit } from '@angular/core';
import {FormGroup, FormControl} from '@angular/forms';
import {LoginService, UserService, DiscussionService, CommunityService, NotificationsService} from '../../services';
import {User, Discussion, Comment, Tools} from '../../shared';
import { Router } from '@angular/router';
import { Subject } from 'rxjs/Subject';
import { Observable } from 'rxjs/Observable';
import {ToasterService} from 'angular2-toaster/angular2-toaster';

declare var Favico: any;

@Component({

  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
  providers: [],
})
export class NavbarComponent implements OnInit {

  private signup: Signup = {};
  private login: Login = {};

  // The search bar
  public discussionSearchControl: FormControl = new FormControl();
  public discussionSearchForm: FormGroup = new FormGroup({
    discussionSearchControl: this.discussionSearchControl
  });
  private discussionSearchSelected: string = '';
  private discussionSearchResultsObservable: Observable<any>;
  private discussionTypeaheadLoading: boolean = false;
  private discussionTypeaheadNoResults: boolean = false;

  private unreadMessages: Array<Comment>;

  private favIcon: any;

  private collapseNavbar: boolean = true;

  constructor(private userService: UserService,
    private loginService: LoginService,
    private router: Router,
    private discussionService: DiscussionService,
    private communityService: CommunityService,
    private notificationsService: NotificationsService,
    private toasterService: ToasterService) {

  }

  ngOnInit() {

    this.setupFavIcon();

    if (!this.userService.getUser()) {
      this.getOrCreateUser();
    }

    this.setupDiscussionSearch();
    this.fetchNotifications();
  }

  setupFavIcon() {
    this.favIcon = new Favico({
      animation: 'pop'
    });
  }

  getOrCreateUser() {
    this.loginService.getOrCreateUser().subscribe(
      user => {
        this.setupUser(user);
      },
      error => {
        console.log(error);
        this.toasterService.pop("error", "Error", error);
      });
  }

  signupSubmit() {
    this.loginService.signup(this.signup.username,
      this.signup.password,
      this.signup.verifyPassword,
      this.signup.email).subscribe(
      user => {
        this.setupUser(user);
      },
      error => {
        console.log(error);
        this.toasterService.pop("error", "Error", error);
      });

  }

  loginSubmit() {
    this.loginService.login(this.login.usernameOrEmail,
      this.login.password).subscribe(
      user => {
        this.setupUser(user);
      },
      error => {
        console.log(error);
        this.toasterService.pop("error", "Error", error);
      });
  }

  setupUser(user: any) {
    this.userService.setUser(user);
    this.userService.sendLoginEvent(user);
    document.getElementById('closeModalButton').click();
  }

  gotoSample() {
    this.router.navigate(['/discussion']);
  }

  logout() {
    this.userService.logout();

    // Then relog back in as a random user (Necessary because a lot of fetches fail otherwise)
    this.getOrCreateUser();
    this.router.navigate(['/']);

  }

  createDiscussion() {
    this.discussionService.createDiscussion().subscribe(d => {
      console.log(d);
      this.userService.fetchFavoriteDiscussions();
      this.router.navigate(['/discussion', d.id, { editMode: true }]);
    },
      error => console.log(error));
  }

  createCommunity() {
    this.communityService.createCommunity().subscribe(d => {
      console.log(d);
      this.userService.fetchFavoriteCommunities();
      this.router.navigate(['/community', d.id, { editMode: true }]);
    },
      error => console.log(error));
  }

  // Discussion searching methods
  // Tag search methods
  setupDiscussionSearch() {
    this.discussionSearchResultsObservable = Observable.create((observer: any) => {
      console.log(this.discussionSearchControl.value);
      this.discussionService.searchDiscussions(this.discussionSearchControl.value)
        .subscribe((result: any) => {
          console.log(result);
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
      console.log(error);
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



}

interface Signup {
  username?: string;
  password?: string;
  verifyPassword?: string;
  email?: string;
}

interface Login {
  usernameOrEmail?: string;
  password?: string;
}



