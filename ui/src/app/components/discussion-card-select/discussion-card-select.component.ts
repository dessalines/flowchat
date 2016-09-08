import { Component, OnInit, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-discussion-card-select',
  templateUrl: './discussion-card-select.component.html',
  styleUrls: ['./discussion-card-select.component.scss']
})
export class DiscussionCardSelectComponent implements OnInit {

  private defaultSort: string = "time-86400";

  @Output() selectChange = new EventEmitter();

  constructor() { }

  ngOnInit() {
  }

  changeEvent($event) {
    this.selectChange.next($event);
  }

}
