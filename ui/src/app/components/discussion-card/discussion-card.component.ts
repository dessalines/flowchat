import { Component, OnInit, Input} from '@angular/core';
import {Discussion} from '../../shared/discussion.interface';
import { MomentPipe } from '../../pipes/moment.pipe';
import {MarkdownPipe} from '../../pipes/markdown.pipe';
import {UserService} from '../../services/user.service';
import {DiscussionService} from '../../services/discussion.service';
import { Router, ROUTER_DIRECTIVES } from '@angular/router-deprecated';
import {MarkdownEditComponent} from '../markdown-edit/index';


@Component({
  moduleId: module.id,
  selector: 'app-discussion-card',
  templateUrl: 'discussion-card.component.html',
  styleUrls: ['discussion-card.component.css'],
  directives: [MarkdownEditComponent, ROUTER_DIRECTIVES],
  pipes: [MomentPipe, MarkdownPipe]
})
export class DiscussionCardComponent implements OnInit {

  @Input() discussion: Discussion;

  private showVoteSlider: boolean = false;

  private editMode: boolean = false;

  constructor(private userService: UserService,
    private discussionService:DiscussionService,
    private router: Router) {}

  ngOnInit() {
  }

  ngAfterViewInit() {
  }


  isCreator(): boolean {
    return this.userService.getUser().id == this.discussion.userId;
  }

  toggleEditMode() {
    this.editMode = !this.editMode;
  }

  setEditText($event) {
    this.discussion.text = $event;
  }

  saveDiscussion() {
    this.discussionService.saveDiscussion(this.discussion).subscribe(
      d => {
        this.discussion = d;
        this.editMode = false;
      });
  }

  toggleShowVoteSlider() {
    this.showVoteSlider = !this.showVoteSlider;
  }

  updateDiscussionRank($event) {
    this.discussion.userRank = $event;
  }

  saveDiscussionRank($event) {
    this.discussion.userRank = $event;
    this.showVoteSlider = false;
    this.discussionService.saveRank(this.discussion.id, this.discussion.userRank).subscribe();
  }

}
