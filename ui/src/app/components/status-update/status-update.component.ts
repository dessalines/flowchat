import { Component, OnInit, Input } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Rx';
import { EmptyDiscussion, Community, Tag, Tools } from '../../shared';
import { TagService, CommunityService, DiscussionService } from '../../services';
import { ToasterService } from 'angular2-toaster/angular2-toaster';

@Component({
  selector: 'app-status-update',
  templateUrl: './status-update.component.html',
  styleUrls: ['./status-update.component.scss']
})
export class StatusUpdateComponent implements OnInit {

  @Input() community: Community;
  @Input() tag: Tag;

  vanilla: Community = {
    creator: {
      id: 1,
      name: "user_1",
      jwt: null
    },
    name: "vanilla",
    id: 1,
    created: null,
    nsfw: false,
    modifiedByUser: null
  }

  discussion: EmptyDiscussion = {
    community: this.vanilla,
  };

  smallVersion: boolean = true;
  isSaving: boolean = false;
  rgex = Tools.rgex;

  // Tag searching
  private tagSearchResultsObservable: Observable<any>;
  private tagSearchSelected: string = '';
  private tooManyTagsError: boolean = false;
  private alreadyAddedTagError: boolean = false;
  private tagTypeaheadLoading: boolean = false;
  private tagTypeaheadNoResults: boolean = false;

  // Community
  private communitySearchResultsObservable: Observable<any>;
  private communitySearchSelected: string = '';
  private tooManyCommunitiesError: boolean = false;
  private communityTypeaheadLoading: boolean = false;
  private communityTypeaheadNoResults: boolean = false;


  constructor(
    private tagService: TagService,
    private communityService: CommunityService,
    private discussionService: DiscussionService,
    private toasterService: ToasterService,
    private router: Router) { }

  ngOnInit() {

    if (this.tag) {
      this.discussion.tags = [this.tag];
    }

    if (this.community) {
      this.discussion.community = this.community;
    }

    this.setupCommunitySearch();
    this.setupTagSearch();
  }

  saveDiscussion() {
    this.isSaving = true;
    this.discussionService.createDiscussion(this.discussion).subscribe(
      d => {
        this.discussion = d;
        this.isSaving = false;
        this.router.navigate(['/discussion', d.id]);
      },
      error => {
        this.toasterService.pop("error", "Error", error);
        this.isSaving = false;
      });
  }

  toggleSmallVersion() {
    this.smallVersion = !this.smallVersion;
    setTimeout(d=> document.getElementById("discussion_title").focus(), 100);
  }

  // Tag search methods
  setupTagSearch() {
    this.tagSearchResultsObservable = Observable.create((observer: any) => {
      this.tagService.searchTags(this.tagSearchSelected)
        .subscribe(r => {
          observer.next(r);
        });
    });
  }


  tagTypeaheadOnSelect(tag: Tag) {
    this.addTag(tag);
  }

  addTag(tag: Tag) {
    // Create the array if necessary
    if (this.discussion.tags == null) {
      this.discussion.tags = [];
    }

    this.tooManyTagsError = this.alreadyAddedTagError = false;

    if (this.discussion.tags.filter(t => t.id == tag.id).length) {
      this.alreadyAddedTagError = true;
      return;
    }

    if (this.discussion.tags.length >= 3) {
      this.tooManyTagsError = true;
    } else {
      this.discussion.tags.push(tag);
      this.tagSearchSelected = '';
    }

  }

  tagChangeTypeaheadLoading(e: boolean): void {
    this.tagTypeaheadLoading = e;
  }

  tagChangeTypeaheadNoResults(e: boolean): void {
    this.tagTypeaheadNoResults = e;
  }

  removeTag(tag: Tag) {
    let index = this.discussion.tags.indexOf(tag);
    this.discussion.tags.splice(index, 1);
    this.tooManyTagsError = this.alreadyAddedTagError = false;
  }

  createTag() {
    this.tagService.createTag(this.tagSearchSelected).subscribe(d => {
      this.tagSearchSelected = '';
      this.toasterService.pop('success', 'New Tag Created', d.name);
      this.addTag(d);
    });
  }

  // Community search methods
  setupCommunitySearch() {
    this.communitySearchResultsObservable = Observable.create((observer: any) => {
      this.communityService.searchCommunities(this.communitySearchSelected)
        .subscribe(r => {
          observer.next(r.communities);
        });
    });
  }

  communityTypeaheadOnSelect(community: Community) {
    // replace the community
    this.discussion.community = community;
    this.communitySearchSelected = '';
  }

  communityChangeTypeaheadLoading(e: boolean): void {
    this.communityTypeaheadLoading = e;
  }

  communityChangeTypeaheadNoResults(e: boolean): void {
    this.communityTypeaheadNoResults = e;
  }

  removeQuotes(text: string) {
    return text.replace(/['"]+/g, '');
  }

}

