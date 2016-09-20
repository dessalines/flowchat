import { Component, OnInit, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-discussion-card-sort-select',
  templateUrl: './discussion-card-sort-select.component.html',
  styleUrls: ['./discussion-card-sort-select.component.scss']
})
export class DiscussionCardSortSelectComponent implements OnInit {

  private defaultSort: string = "time-86400";

  @Output() selectChange = new EventEmitter();

  constructor() { }

  ngOnInit() {
  }

  changeEvent($event) {
    this.selectChange.next($event);
  }

}
