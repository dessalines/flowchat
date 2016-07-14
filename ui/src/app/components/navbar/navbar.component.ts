import { Component, OnInit } from '@angular/core';
import {CORE_DIRECTIVES} from '@angular/common';
import {MODAL_DIRECTVES, BS_VIEW_PROVIDERS, DROPDOWN_DIRECTIVES} from 'ng2-bootstrap/ng2-bootstrap';
import {LoginService} from '../../services/login.service';
import {UserService} from '../../services/user.service';
import {DiscussionService} from '../../services/discussion.service';
import {NotificationsService} from '../../services/notifications.service';
import {User, Discussion, Comment, Tools} from '../../shared';
import { Router, ROUTER_DIRECTIVES } from '@angular/router';
import {ChatComponent} from '../chat';
import { Subject } from 'rxjs/Subject';
import { Observable } from 'rxjs/Observable';
import {TYPEAHEAD_DIRECTIVES, TOOLTIP_DIRECTIVES} from 'ng2-bootstrap/ng2-bootstrap';
import {MarkdownPipe} from '../../pipes/markdown.pipe';
import { MomentPipe } from '../../pipes/moment.pipe';

declare var Favico: any;

@Component({
  moduleId: module.id,
  selector: 'app-navbar',
  templateUrl: 'navbar.component.html',
  styleUrls: ['navbar.component.css'],
  directives: [MODAL_DIRECTVES, DROPDOWN_DIRECTIVES, CORE_DIRECTIVES,
    TYPEAHEAD_DIRECTIVES, TOOLTIP_DIRECTIVES, ROUTER_DIRECTIVES],
  providers: [LoginService],
  viewProviders: [BS_VIEW_PROVIDERS],
  pipes: [MomentPipe, MarkdownPipe]
})
export class NavbarComponent implements OnInit {

  private signup: Signup = {};
  private login: Login = {};

  // The search bar
  private discussionSearchTermStream = new Subject<string>();
  private discussionResultsObservable: Observable<any>;
  private discussionSearchResults: Array<Discussion>;
  private discussionSearchTerm: string = '';
  private discussionTypeaheadLoading: boolean = false;
  private discussionTypeaheadNoResults: boolean = false;

  private unreadMessages: Array<Comment>;

  private favIcon: any;

  constructor(private userService: UserService,
    private loginService: LoginService,
    private router: Router,
    private discussionService: DiscussionService,
    private notificationsService: NotificationsService) {

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
      error => console.log(error));
  }

  signupSubmit() {
    this.loginService.signup(this.signup.username,
      this.signup.password,
      this.signup.email).subscribe(
      user => {
        this.setupUser(user);
      },
      error => console.log(error));

  }

  loginSubmit() {
    this.loginService.login(this.login.usernameOrEmail,
      this.login.password).subscribe(
      user => {
        this.setupUser(user);
      },
      error => console.log(error));
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
      this.router.navigate(['/discussion', d.id, {editMode: true }]);
    },
      error => console.log(error));
  }

  // Discussion searching methods
  // Tag search methods
  setupDiscussionSearch() {
    this.discussionResultsObservable = this.discussionSearchTermStream
      .debounceTime(300)
      .distinctUntilChanged()
      .switchMap((term: string) => this.discussionService.searchDiscussions(term));

    this.discussionResultsObservable.subscribe(t => this.discussionSearchResults = t.discussions);
    // this.tagSearch('a');
  }

  discussionSearch(term: string) {
    if (term !== '') {
      this.discussionSearchTermStream.next(term);
    }
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
    this.notificationsService.markMessageAsRead(message.id).subscribe(t => {
      // Remove it from the array
      let index = this.unreadMessages.indexOf(message);
      this.unreadMessages.splice(index, 1);
      this.changeFaviconBasedOnMessages();

      // Navigate to the parent message (IE, your message)
      this.router.navigate(['/discussion', message.discussionId ,
        'comment', message.parentId ]);

    });
  }

  markAllNotificationsAsRead() {
    this.notificationsService.markAllAsRead().subscribe(
      t => {
        this.unreadMessages = [];
        this.changeFaviconBasedOnMessages();
      });
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



