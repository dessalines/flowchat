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
  @Input() show: boolean;
  @Output() hideEvent = new EventEmitter();

  constructor(public userService: UserService,
    private loginService: LoginService,
    private router: Router,
    private toasterService: ToasterService) { }

  ngOnInit() {

    this.loginModal.onShown.subscribe(() => document.getElementById("login-input").focus());

    // Create a new user if there is none
    if (this.userService.getUser() == null) {
      this.createNewUser();
    }

  }

  ngOnChanges() {
    this.showModal();
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

  showModal() {
    if (this.show) {
      this.loginModal.show();
      this.show = false;
    }
  }

  hiddenEvent() {
    this.hideEvent.next(true);
  }

  createNewUser() {
    let obs: Observable<string> = this.userService.createAnonymousUser();
    obs.subscribe(rJWT => {
      Tools.createCookie('jwt', rJWT, 9999);
      this.userService.setUserFromCookie();
    },
      error => {
        console.error(error);
        this.toasterService.pop("error", error._body);
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

