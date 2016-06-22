import { Component, OnInit } from '@angular/core';
import {CORE_DIRECTIVES} from '@angular/common';
import {MODAL_DIRECTVES, BS_VIEW_PROVIDERS, DROPDOWN_DIRECTIVES} from 'ng2-bootstrap/ng2-bootstrap';
import {LoginService} from '../../services/login.service';
import {UserService} from '../../services/user.service';
import {DiscussionService} from '../../services/discussion.service'
import {User, Tools} from '../../shared';
import { Router, ROUTER_DIRECTIVES } from '@angular/router-deprecated';
import {ChatComponent} from '../chat';



@Component({
  moduleId: module.id,
  selector: 'app-navbar',
  templateUrl: 'navbar.component.html',
  styleUrls: ['navbar.component.css'],
  directives: [MODAL_DIRECTVES, DROPDOWN_DIRECTIVES, CORE_DIRECTIVES, ROUTER_DIRECTIVES],
  providers: [LoginService],
  viewProviders: [BS_VIEW_PROVIDERS]
})
export class NavbarComponent implements OnInit {

  private signup: Signup = {};
  private login: Login = {};

  constructor(private userService: UserService,
    private loginService: LoginService,
    private router: Router,
    private discussionService: DiscussionService) {

  }

  ngOnInit() {
    if (!this.userService.getUser()) {
      this.getOrCreateUser();
    }
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
    this.router.navigate(['Discussion']);
  }

  logout() {
    this.userService.logout();
    this.router.navigate(['Home']);
  }

  createDiscussion() {
    this.discussionService.createDiscussion().subscribe(d => {
      console.log(d);
      this.router.navigate(['Discussion', { discussionId: d.id }]);
    },
    error => console.log(error));
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



