import { Component, OnInit } from '@angular/core';
import {Router, ActivatedRoute} from '@angular/router';
import {DiscussionService, CommunityService} from '../../services';
import {Discussion, Tag, Community} from '../../shared';
import {ToasterService} from 'angular2-toaster/angular2-toaster';

@Component({
  selector: 'app-community',
  templateUrl: 'community.component.html',
  styleUrls: ['community.component.scss'],
  providers: []
})
export class CommunityComponent implements OnInit {

  private discussions: Array<Discussion>;
  private currentCount: number = 0;
  private sorting: string = "time-86400";
  private viewType: string = "list";

  private community: Community;

  private currentPageNum: number = 1;
  private scrollDebounce: number = 0;

  private sub: any;

  private editing: Boolean = false;

  private loadingDiscussions: boolean = false;

  constructor(private route: ActivatedRoute,
    private router: Router,
    private discussionService: DiscussionService,
    private communityService: CommunityService,
    private toasterService: ToasterService) {
  }

  ngOnInit() {

    this.sub = this.route.params.subscribe(params => {
      let communityId: number = +params['communityId'];
      this.currentPageNum = 1;
      this.scrollDebounce = 0;
      this.getCommunity(communityId);
      this.getDiscussions(communityId, this.currentPageNum, this.sorting);
      this.editing = Boolean(this.route.snapshot.params["editMode"]);
    });

  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }

  getCommunity(communityId: number) {
    this.communityService.getCommunity(communityId).subscribe(c => {
      this.community = c;
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
    } else {
      console.log("No more discussions.");
    }
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


  // editing($event) {
  //   this.route.snapshot.params["editMode"] = $event;
  // }

  resort($event) {
    console.log('resorting' + $event);
    this.sorting = $event;
    this.discussions = undefined;
    this.currentPageNum = 1;
    this.scrollDebounce = 0;
    this.getDiscussions(this.community.id, this.currentPageNum, this.sorting);
  }

  isCard(): boolean {
    return this.viewType==='card';
  }

}


