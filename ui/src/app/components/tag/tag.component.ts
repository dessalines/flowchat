import { Component, OnInit } from '@angular/core';
import {Router, ActivatedRoute, ROUTER_DIRECTIVES} from '@angular/router';
import {DiscussionService} from '../../services/discussion.service';
import {TagService} from '../../services/tag.service';
import {Discussion} from '../../shared/discussion.interface';
import {Tag} from '../../shared/tag.interface';
import {DiscussionCardComponent} from '../discussion-card/index';
import {FooterComponent} from '../footer/index';

@Component({
  moduleId: module.id,
  selector: 'app-tag',
  templateUrl: 'tag.component.html',
  styleUrls: ['tag.component.css'],
  directives: [DiscussionCardComponent, FooterComponent, ROUTER_DIRECTIVES],
  providers: []
})
export class TagComponent implements OnInit {

  private discussions: Array<Discussion> = [];

  private tag: Tag;

  private currentPageNum: number = 1;
  private scrollDebounce: number = 0;

  private sub: any;

  constructor(private route: ActivatedRoute,
    private router: Router,
    private discussionService: DiscussionService,
    private tagService: TagService) {
  }

  ngOnInit() {

    this.sub = this.route.params.subscribe(params => {
      let tagId: number = +params['tagId'];
      this.discussions = [];
      this.currentPageNum = 1;
      this.scrollDebounce = 0;
      this.getTag(tagId);
      this.getDiscussions(tagId, this.currentPageNum);
    });

  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }

  getTag(tagId: number) {
    this.tagService.getTag(tagId).subscribe(t => {
      this.tag = t;
    });
  }

  getDiscussions(tagId: number, page: number) {
    this.discussionService.getDiscussions(page, undefined, tagId.toString()).subscribe(
      d => {
        this.discussions.push(...d.discussions);
        console.log(d.discussions);
      });
  }

  onScroll(event) {
    if (this.tag != null && (window.innerHeight + window.scrollY) >= document.body.offsetHeight) {
      if (this.scrollDebounce == 0) {
        this.scrollDebounce = 1;
        // you're at the bottom of the page
        this.currentPageNum += 1;
        this.getDiscussions(this.tag.id, this.currentPageNum);
        setTimeout(() => this.scrollDebounce = 0, 1000);
      }
    }
  }

}
