import { Component, OnInit } from '@angular/core';
import {UserService} from '../../services';

@Component({
  selector: 'app-onboard-alert',
  templateUrl: './onboard-alert.component.html',
  styleUrls: ['./onboard-alert.component.scss']
})
export class OnboardAlertComponent implements OnInit {

  constructor(private userService: UserService) { }

  ngOnInit() {
  }

  readOnboardAlert() {
    console.log(this.userService.getUser());
    this.userService.getUser().settings.readOnboardAlert = true;
    this.userService.saveUser().subscribe(u => {
      this.userService.setUserSettings(u.settings);
    });
  }

}
