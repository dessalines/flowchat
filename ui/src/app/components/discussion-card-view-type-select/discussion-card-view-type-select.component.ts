import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import {UserService} from '../../services';

@Component({
	selector: 'app-discussion-card-view-type-select',
	templateUrl: './discussion-card-view-type-select.component.html',
	styleUrls: ['./discussion-card-view-type-select.component.scss']
})
export class DiscussionCardViewTypeSelectComponent implements OnInit {

	@Input() viewType: string;

	@Output() viewTypeChange = new EventEmitter();

	constructor(private userService: UserService) { }

	ngOnInit() {
	}

	changeEvent($event) {
		this.viewTypeChange.next($event);
		this.userService.getUser().settings.defaultViewTypeRadioValue = $event;
		this.userService.saveUser().subscribe(u => {
      		this.userService.setUserSettings(u.settings);
    	});
	}

}
