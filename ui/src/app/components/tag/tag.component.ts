import { Component, OnInit } from '@angular/core';
import {Router, ActivatedRoute, ROUTER_DIRECTIVES} from '@angular/router';
import {DiscussionService} from '../../services/discussion.service';
import {CommunityService} from '../../services/community.service';
import {TagService} from '../../services/tag.service';
import {Discussion} from '../../shared/discussion.interface';
import {Community} from '../../shared/community.interface';
import {Tag} from '../../shared/tag.interface';
import {Tools} from '../../shared/tools';
import {DiscussionCardComponent} from '../discussion-card/index';
import {FooterComponent} from '../footer/index';

@Component({

  selector: 'app-tag',
  templateUrl: 'tag.component.html',
  styleUrls: ['tag.component.scss'],
  directives: [DiscussionCardComponent, FooterComponent, ROUTER_DIRECTIVES],
  providers: []
})
export class TagComponent implements OnInit {

  private discussions: Array<Discussion>;
  private communities: Array<Community>;

  private sorting: string = "time-86400";

  private tag: Tag;

  private currentPageNum: number = 1;
  private scrollDebounce: number = 0;

  private sub: any;

  constructor(private route: ActivatedRoute,
    private router: Router,
    private discussionService: DiscussionService,
    private communityService: CommunityService,
    private tagService: TagService) {
  }

  ngOnInit() {

    this.sub = this.route.params.subscribe(params => {
      let tagId: number = +params['tagId'];
      this.discussions = [];
      this.currentPageNum = 1;
      this.scrollDebounce = 0;
      this.getTag(tagId);
      this.getDiscussions(tagId, this.currentPageNum, this.sorting);
      this.getCommunities(tagId, this.sorting);
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

  getDiscussions(tagId: number, page: number, orderBy: string) {
    this.discussionService.getDiscussions(page, undefined, tagId.toString(), undefined, orderBy).subscribe(
      d => {
        if (this.discussions === undefined) {
          this.discussions = [];
        }
        this.discussions.push(...d.discussions);
        console.log(d.discussions);
      });
  }

  getCommunities(tagId: number, orderBy: string) {
    this.communityService.getCommunities(undefined, undefined, tagId.toString()).subscribe(
      c => {
        this.communities = c.communities;
      });
  }

  onScroll(event) {
    if (this.tag != null && (window.innerHeight + window.scrollY) >= document.body.offsetHeight) {
      if (this.scrollDebounce == 0) {
        this.scrollDebounce = 1;
        // you're at the bottom of the page
        this.currentPageNum += 1;
        this.getDiscussions(this.tag.id, this.currentPageNum, this.sorting);
        setTimeout(() => this.scrollDebounce = 0, 1000);
      }
    }
  }

  resort($event) {
    console.log('resorting' + $event);
    this.sorting = $event;
    this.discussions = undefined;
    this.currentPageNum = 1;
    this.scrollDebounce = 0;
    this.getDiscussions(this.tag.id, this.currentPageNum, this.sorting);
  }

  removeQuotes(text: string) {
    return Tools.removeQuotes(text);
  }

}
