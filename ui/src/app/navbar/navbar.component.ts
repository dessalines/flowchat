import { Component, OnInit } from '@angular/core';
import {CORE_DIRECTIVES} from '@angular/common';
import {MODAL_DIRECTVES, BS_VIEW_PROVIDERS} from 'ng2-bootstrap/ng2-bootstrap';
import {LoginService} from '../services/login.service';
import {User, Tools} from '../shared';


@Component({
  moduleId: module.id,
  selector: 'app-navbar',
  templateUrl: 'navbar.component.html',
  styleUrls: ['navbar.component.css'],
  directives: [MODAL_DIRECTVES, CORE_DIRECTIVES],
  providers: [],
  viewProviders: [BS_VIEW_PROVIDERS]
})
export class NavbarComponent implements OnInit {

  private signup: Signup = {};
  private login: Login = {};

  private user: User;

  constructor(private loginService: LoginService) {
  }

  ngOnInit() {
  }

  signupSubmit() {
    this.loginService.signup(this.signup.username, 
      this.signup.password, 
      this.signup.email).subscribe(
      user => {
        this.user = user;
        this.setCookies(this.user);
        console.log(this.user);
      },
      error => console.log(error));

  }

  loginSubmit() {
    this.loginService.login(this.login.usernameOrEmail,
      this.login.password).subscribe(
      user => {
        this.user = user;
        this.setCookies(this.user);
        console.log(this.user);
      },
      error => console.log(error));
  }

  public getUser(): User {
    return this.user;
  }

  setCookies(user: User) {
    Tools.createCookie("uid", user.id, user.expire_time);
    Tools.createCookie("auth", user.auth, user.expire_time);
    Tools.createCookie("name", user.name, user.expire_time);
  }

  clearCookies() {
    Tools.eraseCookie("uid");
    Tools.eraseCookie("auth");
    Tools.eraseCookie("name");
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



