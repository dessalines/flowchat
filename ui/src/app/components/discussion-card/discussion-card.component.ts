import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormGroup, FormControl } from '@angular/forms';
import { Discussion, Tag, User, Community, Tools } from '../../shared';
import { UserService, DiscussionService, CommunityService, TagService } from '../../services';
import { Router } from '@angular/router';
import { ToasterService } from 'angular2-toaster/angular2-toaster';
import { Subject } from 'rxjs/Subject';
import { Observable } from 'rxjs/Observable';

import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinctUntilChanged';
import 'rxjs/add/operator/switchMap';


@Component({
  selector: 'app-discussion-card',
  templateUrl: './discussion-card.component.html',
  styleUrls: ['./discussion-card.component.scss'],
})
export class DiscussionCardComponent implements OnInit {

  @Input() discussion: Discussion;

  private showVoteSlider: boolean = false;

  @Input() editing: boolean = false;
  @Output() editingChange = new EventEmitter();

  private isCreator: boolean = false;
  private isModerator: boolean = false;

  // tag searching
  private tagSearchResultsObservable: Observable<any>;
  private tagSearchSelected: string = '';
  private tooManyTagsError: boolean = false;
  private alreadyAddedTagError: boolean = false;
  private tagTypeaheadLoading: boolean = false;
  private tagTypeaheadNoResults: boolean = false;

  // For the private users
  private userSearchResultsObservable: Observable<any>;
  private userSearchSelected: string = '';
  private userTypeaheadLoading: boolean = false;
  private userTypeaheadNoResults: boolean = false;

  // Blocked users
  private blockedUserSearchResultsObservable: Observable<any>;
  private blockedUserSearchSelected: string = '';
  private blockedUserTypeaheadLoading: boolean = false;
  private blockedUserTypeaheadNoResults: boolean = false;

  // Community
  private communitySearchResultsObservable: Observable<any>;
  private communitySearchSelected: string = '';
  private tooManyCommunitiesError: boolean = false;
  private communityTypeaheadLoading: boolean = false;
  private communityTypeaheadNoResults: boolean = false;

  private rgex = Tools.rgex;

  private isSaving: boolean = false;

  constructor(private userService: UserService,
    private discussionService: DiscussionService,
    private communityService: CommunityService,
    private tagService: TagService,
    private toasterService: ToasterService,
    private router: Router) { }

  ngOnInit() {
    this.setupTagSearch();
    this.setupUserSearch();
    this.setupBlockedUserSearch();
    this.setupCommunitySearch();
    this.setPermissions();
  }

  ngAfterViewInit() {
    Tools.zooming.listen('.img-zoomable');
  }

  ngOnChanges() {
    this.setPermissions();
  }

  setPermissions() {
    this.isModerator = false;
    this.isCreator = false;
    if (this.userService.getUser()) {
      let userId: number = this.userService.getUser().id;

      // The multi-discussion fetch doesnt grab each communities creators, so check for this
      if (userId == this.discussion.creator.id ||
        (this.discussion.community.creator != null && userId == this.discussion.community.creator.id)) {
        // Creators also have mod abilities
        this.isCreator = true;
        this.isModerator = true;

      } else {
        let m = this.discussion.community.moderators.filter(m => m.id == userId)[0];
        if (m !== undefined) {
          this.isModerator = true;
        }

      }
    }
  }


  toggleEditing() {
    this.editing = !this.editing;
    this.editingChange.next(this.editing);
  }

  setEditText($event) {
    this.discussion.text = $event;
  }

  saveDiscussion() {
    this.isSaving = true;
    this.discussionService.saveDiscussion(this.discussion).subscribe(
      d => {
        this.discussion = d;
        this.editing = false;
        this.editingChange.next(this.editing);
        this.userService.fetchFavoriteDiscussions();
        this.isSaving = false;
      },
      error => {
        this.toasterService.pop("error", "Error", error);
        this.isSaving = false;
      });
  }

  deleteDiscussion() {
    this.discussion.deleted = true;
    this.saveDiscussion();
  }

  toggleShowVoteSlider() {
    if (!this.isCreator) {
      this.showVoteSlider = !this.showVoteSlider;
    }
  }

  updateDiscussionRank($event) {
    this.discussion.userRank = $event;
  }

  saveDiscussionRank($event) {
    this.discussion.userRank = $event;
    this.showVoteSlider = false;
    this.discussionService.saveRank(this.discussion.id, this.discussion.userRank).subscribe();
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

  // User search methods
  setupUserSearch() {
    this.userSearchResultsObservable = Observable.create((observer: any) => {
      this.userService.searchUsers(this.userSearchSelected)
        .subscribe(r => {
          observer.next(r);
        });
    });
  }

  userTypeaheadOnSelect(user: User) {

    // Create the array if necessary
    if (this.discussion.privateUsers == null) {
      this.discussion.privateUsers = [];
    }

    // add it to the list
    this.discussion.privateUsers.push(user);
    this.userSearchSelected = '';

  }

  userChangeTypeaheadLoading(e: boolean): void {
    this.userTypeaheadLoading = e;
  }

  userChangeTypeaheadNoResults(e: boolean): void {
    this.userTypeaheadNoResults = e;
  }

  removePrivateUser(user: User) {
    let index = this.discussion.privateUsers.indexOf(user);
    this.discussion.privateUsers.splice(index, 1);
  }

  // Blocked user methods
  setupBlockedUserSearch() {
    this.blockedUserSearchResultsObservable = Observable.create((observer: any) => {
      this.userService.searchUsers(this.blockedUserSearchSelected)
        .subscribe(r => {
          observer.next(r);
        });
    });
  }

  blockedUserTypeaheadOnSelect(user: User) {

    // Create the array if necessary
    if (this.discussion.blockedUsers == null) {
      this.discussion.blockedUsers = [];
    }

    // add it to the list
    this.discussion.blockedUsers.push(user);
    this.blockedUserSearchSelected = '';

  }

  blockedUserChangeTypeaheadLoading(e: boolean): void {
    this.blockedUserTypeaheadLoading = e;
  }

  blockedUserChangeTypeaheadNoResults(e: boolean): void {
    this.blockedUserTypeaheadNoResults = e;
  }

  removeBlockedUser(user: User) {
    let index = this.discussion.blockedUsers.indexOf(user);
    this.discussion.blockedUsers.splice(index, 1);
  }

  // User search methods
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

  isImageType(text: string): boolean {
    return Tools.isImageType(text);
  }

  isCard(): boolean {
    return this.userService.getUserSettings().defaultViewTypeRadioValue === 'card';
  }

  hasImage(): boolean {
    return this.discussion.link && this.isImageType(this.discussion.link);
  }

  shortenedDiscussionLink(): string {
    return new URL(this.discussion.link).hostname;
  }

  voteHtml() {
    let yourVote: string = (this.discussion.userRank == null) ?
      'None' :
      this.discussion.userRank.toString();
    let voteLine: string = (this.isCreator)
      ? 'You can\'t vote on your own post'
      : 'Click to vote';
    return `<span>` +
      `<b>` + voteLine + `</b><br><br>` +
      `Average Score: ` + this.avgVote() + `<br>` +
      `Your Vote: ` + yourVote + `<br>` +
      `# of Votes: ` + this.discussion.numberOfVotes + ` votes <br>` +
      `</span>`;
  }

  avgVote(): Number {
    return (this.discussion.avgRank == null) ? 0 : this.discussion.avgRank;
  }

  voteExists(): boolean {
    return this.discussion.userRank !== undefined && this.discussion.userRank !== null;
  }

  upvote() {
    let newVote: number = (this.discussion.userRank !== 100) ? 100 : null;
    this.saveDiscussionRank(newVote);
  }

  downvote() {
    let newVote: number = (this.discussion.userRank !== 0) ? 0 : null;
    this.saveDiscussionRank(newVote);
  }

}
