import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';
import {UserService} from '../../services';

@Component({
  selector: 'app-discussion-card-sort-select',
  templateUrl: './discussion-card-sort-select.component.html',
  styleUrls: ['./discussion-card-sort-select.component.scss']
})
export class DiscussionCardSortSelectComponent implements OnInit {

  @Input() sortType: string;

  @Output() sortTypeChange = new EventEmitter();

  constructor(private userService: UserService) { }

  ngOnInit() {
  }

  changeEvent($event) {
    this.sortTypeChange.next($event);
    this.userService.getUser().settings.defaultSortTypeRadioValue = $event;
    this.userService.saveUser().subscribe(u => {
      this.userService.setUserSettings(u.settings);
    });
  }

}
