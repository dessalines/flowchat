import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
	selector: 'app-discussion-card-view-type-select',
	templateUrl: './discussion-card-view-type-select.component.html',
	styleUrls: ['./discussion-card-view-type-select.component.scss']
})
export class DiscussionCardViewTypeSelectComponent implements OnInit {

	@Input() viewType: string = "card";

	@Output() viewTypeChange = new EventEmitter();

	constructor() { }

	ngOnInit() {
	}

	changeEvent($event) {
		this.viewTypeChange.next($event);
	}

}
