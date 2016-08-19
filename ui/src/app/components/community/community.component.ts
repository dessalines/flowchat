import { Component, OnInit } from '@angular/core';
import {Router, ActivatedRoute, ROUTER_DIRECTIVES} from '@angular/router';
import {DiscussionService} from '../../services/discussion.service';
import {CommunityService} from '../../services/community.service';
import {Discussion} from '../../shared/discussion.interface';
import {Tag} from '../../shared/tag.interface';
import {Community} from '../../shared/community.interface';
import {CommunityCardComponent} from '../community-card/index';
import {DiscussionCardComponent} from '../discussion-card/index';
import {FooterComponent} from '../footer/index';

@Component({
  moduleId: module.id,
  selector: 'app-community',
  templateUrl: 'community.component.html',
  styleUrls: ['community.component.css'],
  directives: [CommunityCardComponent, DiscussionCardComponent, FooterComponent, ROUTER_DIRECTIVES],
  providers: []
})
export class CommunityComponent implements OnInit {

  private discussions: Array<Discussion> = [];
  private sorting: string = "time-86400";

  private community: Community;

  private currentPageNum: number = 1;
  private scrollDebounce: number = 0;

  private sub: any;

  constructor(private route: ActivatedRoute,
    private router: Router,
    private discussionService: DiscussionService,
    private communityService: CommunityService) {
  }

  ngOnInit() {

    this.sub = this.route.params.subscribe(params => {
      let communityId: number = +params['communityId'];
      this.discussions = [];
      this.currentPageNum = 1;
      this.scrollDebounce = 0;
      this.getCommunity(communityId);
      this.getDiscussions(communityId, this.currentPageNum, this.sorting);
    });

  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }

  getCommunity(communityId: number) {
    this.communityService.getCommunity(communityId).subscribe(c => {
      this.community = c;
    });
  }

  getDiscussions(communityId: number, page: number, orderBy: string) {
    this.discussionService.getDiscussions(page, undefined, undefined, communityId.toString(), orderBy).subscribe(
      d => {
        this.discussions.push(...d.discussions);
        console.log(d.discussions);
      });
  }

  onScroll(event) {
    if (this.community != null && (window.innerHeight + window.scrollY) >= document.body.offsetHeight) {
      if (this.scrollDebounce == 0) {
        this.scrollDebounce = 1;
        // you're at the bottom of the page
        this.currentPageNum += 1;
        this.getDiscussions(this.community.id, this.currentPageNum, this.sorting);
        setTimeout(() => this.scrollDebounce = 0, 1000);
      }
    }
  }

  editMode(): Boolean {
    return Boolean(this.route.snapshot.params["editMode"]);
  }

  resort($event) {
    console.log('resorting' + $event);
    this.sorting = $event;
    this.discussions = [];
    this.currentPageNum = 1;
    this.scrollDebounce = 0;
    this.getDiscussions(this.community.id, this.currentPageNum, this.sorting);
  }


}


