import { Injectable } from '@angular/core';
import {User, Tools} from '../shared';

@Injectable()
export class UserService {

  private user: User;

  constructor() {
		this.setUserFromCookie();
  }

	public getUser(): User {
    return this.user;
  }

  setUser(user: User) {
		this.user = user;
		this.setCookies(this.user);
  }

  setUserFromCookie() {
		if (Tools.readCookie("uid") != null) {
			this.user = {
				id: Number(Tools.readCookie("uid")),
				name: Tools.readCookie("name"),
				auth: Tools.readCookie("auth")
			}
		}
		console.log(this.user);
  }

  logout() {
		this.user = null;
		this.clearCookies();
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
