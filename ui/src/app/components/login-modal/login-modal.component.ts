import { Component, OnInit, ViewChild, Output, EventEmitter, Input } from '@angular/core';
import { ModalDirective } from 'ngx-bootstrap/modal';
import { Observable } from 'rxjs/Observable';
import { LoginService, UserService, DiscussionService, CommunityService, NotificationsService } from '../../services';
import { User, Discussion, Comment, Tools } from '../../shared';
import { Router } from '@angular/router';
import { ToasterService } from 'angular2-toaster';


@Component({
  selector: 'app-login-modal',
  templateUrl: './login-modal.component.html',
  styleUrls: ['./login-modal.component.scss']
})
export class LoginModalComponent implements OnInit {

  public signup: Signup = {
    username: (this.userService.getUser()) ? this.userService.getUser().name : undefined
  };

  public login: Login = {};

  @ViewChild('loginModal') private loginModal: ModalDirective;
  @Output() hideEvent = new EventEmitter();

  constructor(public userService: UserService,
    private loginService: LoginService,
    private router: Router,
    private toasterService: ToasterService) { }

  ngOnInit() {

    this.loginModal.onShown.subscribe(() => document.getElementById("login-input").focus());

    setTimeout(() => {
      this.loginModal.show();
      // document.getElementById("login-input").focus()
    }, 100);

  }

  signupSubmit() {
    this.loginService.signup(this.signup.username,
      this.signup.password,
      this.signup.verifyPassword,
      this.signup.email).subscribe(
        jwt => {
          Tools.createCookie('jwt', jwt, 9999);
          this.userService.setUserFromCookie();
          document.getElementById('closeModalButton').click();

        },
        error => {
          console.error(error);
          this.toasterService.pop("error", "Error", error);
        });

  }

  loginSubmit() {
    this.loginService.login(this.login.usernameOrEmail,
      this.login.password).subscribe(
        jwt => {
          Tools.createCookie('jwt', jwt, 9999);
          this.userService.setUserFromCookie();
          document.getElementById('closeModalButton').click();

        },
        error => {
          console.error(error);
          this.toasterService.pop("error", "Error", error);
        });
  }

  hiddenEvent() {
    this.hideEvent.next(true);
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

