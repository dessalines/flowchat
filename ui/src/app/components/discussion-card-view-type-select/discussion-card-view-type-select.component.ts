import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { UserService } from '../../services';

@Component({
	selector: 'app-discussion-card-view-type-select',
	templateUrl: './discussion-card-view-type-select.component.html',
	styleUrls: ['./discussion-card-view-type-select.component.scss']
})
export class DiscussionCardViewTypeSelectComponent implements OnInit {

	constructor(public userService: UserService) { }

	ngOnInit() {
	}

	changeEvent($event) {
		this.userService.getUserSettings().defaultViewTypeRadioValue = $event;
		this.userService.saveUserSettings();
	}

}
