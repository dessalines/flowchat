import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';
import { UserService } from '../../services';

@Component({
  selector: 'app-discussion-card-sort-select',
  templateUrl: './discussion-card-sort-select.component.html',
  styleUrls: ['./discussion-card-sort-select.component.scss']
})
export class DiscussionCardSortSelectComponent implements OnInit {

  constructor(private userService: UserService) { }

  ngOnInit() {
  }

  changeEvent($event) {
    this.userService.getUserSettings().defaultSortTypeRadioValue = $event;
    this.userService.saveUserSettings();
  }

}
