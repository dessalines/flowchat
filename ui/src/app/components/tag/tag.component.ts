import { Component, OnInit } from '@angular/core';
import {Router, ActivatedRoute} from '@angular/router';
import {DiscussionService, CommunityService, TagService, UserService} from '../../services';
import {Discussion, Community, Tag, Tools} from '../../shared';


@Component({
  selector: 'app-tag',
  templateUrl: './tag.component.html',
  styleUrls: ['./tag.component.scss'],
  providers: []
})
export class TagComponent implements OnInit {

  public discussions: Array<Discussion>;
  public currentCount: number = 0;
  public communities: Array<Community>;

  public sortType: string = this.userService.getUserSettings().defaultSortTypeRadioValue;
  public viewType: string = this.userService.getUserSettings().defaultViewTypeRadioValue;

  public tag: Tag;

  public currentPageNum: number = 1;
  public scrollDebounce: number = 0;

  public sub: any;

  public loadingDiscussions: boolean = false;

  constructor(private route: ActivatedRoute,
    private router: Router,
    private discussionService: DiscussionService,
    private communityService: CommunityService,
    private tagService: TagService,
    private userService: UserService) {
  }

  ngOnInit() {

    this.sub = this.route.params.subscribe(params => {
      let tagId: number = +params['tagId'];
      this.currentPageNum = 1;
      this.scrollDebounce = 0;
      this.getTag(tagId);
      this.getDiscussions(tagId, this.currentPageNum, this.sortType);
      this.getCommunities(tagId, this.sortType);
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

    if (this.discussions === undefined || this.discussions.length < this.currentCount) {

      this.loadingDiscussions = true;

      this.discussionService.getDiscussions(page, undefined, tagId.toString(), undefined, orderBy).subscribe(
        d => {
          if (this.discussions === undefined) {
            this.discussions = [];
          }
          this.currentCount = d.count;
          this.discussions.push(...d.discussions);
          this.loadingDiscussions = false;
        });
    } else {
      console.log("No more discussions.");
    }
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
        this.getDiscussions(this.tag.id, this.currentPageNum, this.sortType);
        setTimeout(() => this.scrollDebounce = 0, 1000);
      }
    }
  }

  resort($event) {
    console.log('resorting' + $event);
    this.sortType = $event;
    this.discussions = undefined;
    this.currentPageNum = 1;
    this.scrollDebounce = 0;
    this.getDiscussions(this.tag.id, this.currentPageNum, this.sortType);
  }

  removeQuotes(text: string) {
    return Tools.removeQuotes(text);
  }

  isCard(): boolean {
    return this.viewType === 'card';
  }

}
