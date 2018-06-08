import { Component, OnInit, Input } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { Router, ActivatedRoute } from '@angular/router';

import { ToasterContainerComponent, ToasterService, ToasterConfig } from 'angular2-toaster/angular2-toaster';
import { DiscussionService, TagService, CommunityService, UserService } from '../../services';
import { Discussion, Tag, Community, Tools, User } from '../../shared';


@Component({

  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  providers: []
})
export class HomeComponent implements OnInit {

  public discussions: Array<Discussion>;
  public currentCount: number = 0;
  public popularTags: Array<Tag>;
  public popularCommunities: Array<Community>;

  public currentPageNum: number = 1;
  public scrollDebounce: number = 0;

  public communityId: string;

  public loadingDiscussions: boolean = true;
  public updateMasonryLayout: boolean = false;

  constructor(private router: Router,
    private route: ActivatedRoute,
    private titleService: Title,
    private toasterService: ToasterService,
    public userService: UserService,
    private discussionService: DiscussionService,
    private tagService: TagService,
    private communityService: CommunityService) { }

  popToast() {
    this.toasterService.pop('info', 'Args Title', 'Args Body');
  }

  ngOnInit() {

    this.communityId = this.route.snapshot.url.toString();

    if (this.communityId === "") {
      this.communityId = "favorites";
    } else {
      this.communityId = "all";
    }

    this.userService.userObservable.subscribe(user => {
      this.discussions = undefined;
      this.currentPageNum = 1;
      this.scrollDebounce = 0;
      if (user) {

        this.getDiscussions(this.communityId, this.currentPageNum, this.userService.getUserSettings().defaultSortTypeRadioValue);
        this.getPopularTags(this.userService.getUserSettings().defaultSortTypeRadioValue);
        this.getPopularCommunities(this.userService.getUserSettings().defaultSortTypeRadioValue);
      }
    });

    this.titleService.setTitle('FlowChat');
  }

  onScroll(event) {

    if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight - 100) {
      if (this.scrollDebounce == 0) {
        this.scrollDebounce = 1;
        // you're at the bottom of the page
        this.currentPageNum += 1;
        this.getDiscussions(this.communityId, this.currentPageNum, this.userService.getUserSettings().defaultSortTypeRadioValue);
        setTimeout(() => this.scrollDebounce = 0, 200);
      }
    }
  }

  getDiscussions(communityId: string, page: number, orderBy: string) {


    if (this.discussions === undefined || this.discussions.length < this.currentCount) {


      this.discussionService.getDiscussions(page, undefined, undefined, communityId, orderBy).subscribe(
        d => {
          // Append them
          if (this.discussions === undefined) {
            this.discussions = [];
          }

          this.currentCount = d.count;
          this.discussions.push(...d.discussions);
          this.loadingDiscussions = false;
          // setTimeout(() => this.updateMasonryLayout = !this.updateMasonryLayout, 2000);

        });

    }
  }

  getPopularTags(orderBy: string) {
    this.tagService.getPopularTags(5, undefined, orderBy).subscribe(
      t => {
        this.popularTags = t;
      });
  }

  getPopularCommunities(orderBy: string) {
    this.communityService.getCommunities(undefined, 5, undefined, orderBy).subscribe(
      t => {
        this.popularCommunities = t.communities
      });
  }

  removeQuotes(text: string) {
    return Tools.removeQuotes(text);
  }

  isCard(): boolean {
    return this.userService.getUserSettings().defaultViewTypeRadioValue === 'card';
  }

}
