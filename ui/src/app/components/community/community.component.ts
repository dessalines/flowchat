import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { ToasterService } from 'angular2-toaster/angular2-toaster';
import { Title }     from '@angular/platform-browser';
import { DiscussionService, CommunityService, UserService } from '../../services';
import { Discussion, Tag, Community } from '../../shared';

@Component({
  selector: 'app-community',
  templateUrl: './community.component.html',
  styleUrls: ['./community.component.scss'],
  providers: []
})
export class CommunityComponent implements OnInit {

  public discussions: Array<Discussion>;
  public currentCount: number = 0;
  public sortType: string = this.userService.getUserSettings().defaultSortTypeRadioValue;
  public viewType: string = this.userService.getUserSettings().defaultViewTypeRadioValue;

  public community: Community;

  public currentPageNum: number = 1;
  public scrollDebounce: number = 0;

  public sub: any;

  public editing: Boolean = false;

  public loadingDiscussions: boolean = false;

  constructor(private route: ActivatedRoute,
    private router: Router,
    private titleService: Title,
    private discussionService: DiscussionService,
    private communityService: CommunityService,
    private toasterService: ToasterService,
    private userService: UserService) {
  }

  ngOnInit() {

    this.sub = this.route.params.subscribe(params => {
      let communityId: number = +params['communityId'];
      this.currentPageNum = 1;
      this.scrollDebounce = 0;
      this.getCommunity(communityId);
      this.getDiscussions(communityId, this.currentPageNum, this.sortType);
      this.editing = Boolean(this.route.snapshot.params["editMode"]);
    });

  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }

  getCommunity(communityId: number) {
    this.communityService.getCommunity(communityId).subscribe(c => {
      this.community = c;
      this.titleService.setTitle(c.name + ' - FlowChat');
    },
      error => {
        this.toasterService.pop("error", "Error", error);
        this.router.navigate(['/']);
      });
  }

  getDiscussions(communityId: number, page: number, orderBy: string) {

    if (this.discussions === undefined || this.discussions.length < this.currentCount) {

      this.loadingDiscussions = true;

      this.discussionService.getDiscussions(page, undefined, undefined, communityId.toString(), orderBy).subscribe(
        d => {
          if (this.discussions === undefined) {
            this.discussions = [];
          }

          this.currentCount = d.count;
          this.discussions.push(...d.discussions);
          this.loadingDiscussions = false;
        });
    }
  }

  onScroll(event) {
    if (this.community != null && (window.innerHeight + window.scrollY) >= document.body.offsetHeight - 100) {
      if (this.scrollDebounce == 0) {
        this.scrollDebounce = 1;
        // you're at the bottom of the page
        this.currentPageNum += 1;
        this.getDiscussions(this.community.id, this.currentPageNum, this.sortType);
        setTimeout(() => this.scrollDebounce = 0, 1000);
      }
    }
  }


  // editing($event) {
  //   this.route.snapshot.params["editMode"] = $event;
  // }

  resort($event) {
    this.sortType = $event;
    this.discussions = undefined;
    this.currentPageNum = 1;
    this.scrollDebounce = 0;
    this.getDiscussions(this.community.id, this.currentPageNum, this.sortType);
  }

  isCard(): boolean {
    return this.viewType==='card';
  }

}


