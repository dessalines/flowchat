import { Component, OnInit } from '@angular/core';
import {CORE_DIRECTIVES} from '@angular/common';
import {MODAL_DIRECTVES, BS_VIEW_PROVIDERS} from 'ng2-bootstrap/ng2-bootstrap';

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
  private login: Login;

  constructor() {
  }

  ngOnInit() {
  }

}

interface Signup {
  username?: string;
  password?: string;
  verifyPassword?: string;
  email?: string;
}

interface Login {
  username: string;
  password: string;
}
