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

  public community: Community;

  public currentPageNum: number = 1;
  public scrollDebounce: number = 0;

  public sub: any;

  public editing: Boolean = false;

  public loadingDiscussions: boolean = true;
  public updateMasonryLayout: boolean = false;

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
      this.userService.userObservable.subscribe(user => {
        if (user) {
          this.discussions = undefined;
          this.currentPageNum = 1;
          this.scrollDebounce = 0;
          this.getDiscussions(communityId, this.currentPageNum, this.userService.getUserSettings().defaultSortTypeRadioValue);
        }
      });
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

      this.discussionService.getDiscussions(page, undefined, undefined, communityId.toString(), orderBy).subscribe(
        d => {
          if (this.discussions === undefined) {
            this.discussions = [];
          }

          this.currentCount = d.count;
          this.discussions.push(...d.discussions);
          this.loadingDiscussions = false;
          setTimeout(() => this.updateMasonryLayout = !this.updateMasonryLayout, 2000);

        });
    }
  }

  onScroll(event) {
    if (this.community != null && (window.innerHeight + window.scrollY) >= document.body.offsetHeight - 100) {
      if (this.scrollDebounce == 0) {
        this.scrollDebounce = 1;
        // you're at the bottom of the page
        this.currentPageNum += 1;
        this.getDiscussions(this.community.id, this.currentPageNum, this.userService.getUserSettings().defaultSortTypeRadioValue);
        setTimeout(() => this.scrollDebounce = 0, 1000);
      }
    }
  }


  isCard(): boolean {
    return this.userService.getUserSettings().defaultViewTypeRadioValue==='card';
  }

}


