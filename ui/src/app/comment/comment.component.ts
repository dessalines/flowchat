import { Component, OnInit, Input } from '@angular/core';
import {Comment} from '../shared/comment.interface';

@Component({
  moduleId: module.id,
  selector: 'app-comment',
  templateUrl: 'comment.component.html',
  styleUrls: ['comment.component.css'],
  // directives: [CommentComponent]
})

export class CommentComponent implements OnInit {

	@Input() comment: any;

  constructor() {
  }

  ngOnInit() {
		console.log(this.comment);
  }

}
