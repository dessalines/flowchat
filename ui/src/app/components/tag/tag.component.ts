import { Component, OnInit } from '@angular/core';
import { RouteConfig, ROUTER_DIRECTIVES, Router, RouteParams} from '@angular/router-deprecated';
import {DiscussionService} from '../../services/discussion.service';
import {TagService} from '../../services/tag.service';
import {Discussion} from '../../shared/discussion.interface';
import {Tag} from '../../shared/tag.interface';
import {DiscussionCardComponent} from '../discussion-card/index';

@Component({
  moduleId: module.id,
  selector: 'app-tag',
  templateUrl: 'tag.component.html',
  styleUrls: ['tag.component.css'],
  directives: [DiscussionCardComponent],
  providers: [TagService]
})
export class TagComponent implements OnInit {

  private discussions: Array<Discussion>;

  private tag: Tag;

  constructor(private routeParams: RouteParams,
    private discussionService: DiscussionService,
    private tagService: TagService) { 
    

  }

  ngOnInit() {

    let tagId = this.routeParams.params['tagId'];

    this.tagService.getTag(tagId).subscribe(t => {
      this.tag = t;
    });

    this.discussionService.getDiscussions(undefined, undefined, tagId).subscribe(
      d => this.discussions = d.discussions);

  }

  fillDiscussion() {

  }

}
