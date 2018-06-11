import { Component, OnInit, Input, Output, EventEmitter} from '@angular/core';
import { FormGroup, FormControl} from '@angular/forms';
import {Router, ActivatedRoute} from '@angular/router';
import {Community, Tag, User, Tools} from '../../shared';
import {UserService, CommunityService, TagService} from '../../services';
import {ToasterService} from 'angular2-toaster/angular2-toaster';
import { Subject } from 'rxjs/Subject';
import { Observable } from 'rxjs/Observable';

import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinctUntilChanged';
import 'rxjs/add/operator/switchMap';

@Component({

  selector: 'app-community-card',
  templateUrl: './community-card.component.html',
  styleUrls: ['./community-card.component.scss'],
})
export class CommunityCardComponent implements OnInit {

  @Input() community: Community;

  private showVoteSlider: boolean = false;

  @Input() editing: boolean = false;
  @Output() editingChange = new EventEmitter();

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

  // Moderators
  private moderatorSearchResultsObservable: Observable<any>;
  private moderatorSearchSelected: string = '';
  private moderatorTypeaheadLoading: boolean = false;
  private moderatorTypeaheadNoResults: boolean = false;

  private rgex = Tools.rgex;

  private textCollapsed: boolean = false;
  private refresh: boolean = true;

  private isModerator: boolean = false;
  private isCreator: boolean = false;

  private isSaving: boolean = false;

  constructor(private userService: UserService,
    private communityService: CommunityService,
    private tagService: TagService,
    private toasterService: ToasterService,
    private router: Router) { }

  ngOnInit() {
    this.setupTagSearch();
    this.setupUserSearch();
    this.setupBlockedUserSearch();
    this.setupModeratorSearch();
    this.setPermissions();
  }

  ngOnChanges() {
    this.refresh = false;
    setTimeout(() => this.refresh = true,0);
    this.setPermissions();
  }

  setPermissions() {
    this.isModerator = false;
    this.isCreator = false;
    if (this.userService.getUser()) {
      let userId: number = this.userService.getUser().id;

      // The multi-discussion fetch doesnt grab each communities creators, so check for this
      if (userId == this.community.creator.id) {
        this.isCreator = true;
      }

      // check community mods
      let m = this.community.moderators.filter(m => m.id == userId)[0];
      if (m !== undefined || this.community.creator.id == userId) {
        this.isModerator = true;
      }
    }
  }

  toggleEditing() {
    this.editing = !this.editing;
    this.editingChange.next(this.editing);
  }

  setEditText($event) {
    this.community.text = $event;
  }

  saveCommunity() {
    this.isSaving = true;
    this.communityService.saveCommunity(this.community).subscribe(
      c => {
        this.community = c;
        this.editing = false;
        this.editingChange.next(this.editing);
        this.userService.fetchFavoriteCommunities();
        this.isSaving = false;
      },
      error => {
        this.toasterService.pop("error", "Error", error);
        this.isSaving = false;
      });
  }

  deleteCommunity() {
    this.community.deleted = true;
    this.saveCommunity();
  }

  toggleShowVoteSlider() {
    this.showVoteSlider = !this.showVoteSlider;
  }

  updateCommunityRank($event) {
    this.community.userRank = $event;
  }

  saveCommunityRank($event) {
    this.community.userRank = $event;
    this.showVoteSlider = false;
    this.communityService.saveRank(this.community.id, this.community.userRank).subscribe();
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
    if (this.community.tags == null) {
      this.community.tags = [];
    }

    this.tooManyTagsError = this.alreadyAddedTagError = false;

    if (this.community.tags.filter(t => t.id == tag.id).length) {
      this.alreadyAddedTagError = true;
      return;
    }

    if (this.community.tags.length >= 3) {
      this.tooManyTagsError = true;
    } else {
      this.community.tags.push(tag);
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
    let index = this.community.tags.indexOf(tag);
    this.community.tags.splice(index, 1);
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
    if (this.community.privateUsers == null) {
      this.community.privateUsers = [];
    }

    // add it to the list
    this.community.privateUsers.push(user);
    this.userSearchSelected = '';

  }

  userChangeTypeaheadLoading(e: boolean): void {
    this.userTypeaheadLoading = e;
  }

  userChangeTypeaheadNoResults(e: boolean): void {
    this.userTypeaheadNoResults = e;
  }

  removePrivateUser(user: User) {
    let index = this.community.privateUsers.indexOf(user);
    this.community.privateUsers.splice(index, 1);
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
    if (this.community.blockedUsers == null) {
      this.community.blockedUsers = [];
    }

    // add it to the list
    this.community.blockedUsers.push(user);
    this.blockedUserSearchSelected = '';

  }

  blockedUserChangeTypeaheadLoading(e: boolean): void {
    this.blockedUserTypeaheadLoading = e;
  }

  blockedUserChangeTypeaheadNoResults(e: boolean): void {
    this.blockedUserTypeaheadNoResults = e;
  }

  removeBlockedUser(user: User) {
    let index = this.community.blockedUsers.indexOf(user);
    this.community.blockedUsers.splice(index, 1);
  }

  // moderator methods
  setupModeratorSearch() {
    this.moderatorSearchResultsObservable = Observable.create((observer: any) => {
      this.userService.searchUsers(this.moderatorSearchSelected)
        .subscribe(r => {
          observer.next(r);
        });
    });
  }

  moderatorTypeaheadOnSelect(user: User) {

    // Create the array if necessary
    if (this.community.moderators == null) {
      this.community.moderators = [];
    }

    // add it to the list
    this.community.moderators.push(user);
    this.moderatorSearchSelected = '';

  }

  moderatorChangeTypeaheadLoading(e: boolean): void {
    this.moderatorTypeaheadLoading = e;
  }

  moderatorChangeTypeaheadNoResults(e: boolean): void {
    this.moderatorTypeaheadNoResults = e;
  }

  removeModerator(user: User) {
    let index = this.community.moderators.indexOf(user);
    this.community.moderators.splice(index, 1);
  }

  toggleCollapseText() {
    this.textCollapsed = !this.textCollapsed;
  }

  removeQuotes(text: string) {
    return text.replace(/['"]+/g, '');
  }

}
