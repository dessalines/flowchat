import { Component, OnInit, Input} from '@angular/core';
import {FORM_DIRECTIVES, REACTIVE_FORM_DIRECTIVES, FormGroup, FormControl} from '@angular/forms';
import {DomSanitizationService, SafeHtml} from '@angular/platform-browser';
import {Community} from '../../shared/community.interface';
import {Tag} from '../../shared/tag.interface';
import {User} from '../../shared/user.interface';
import {Tools} from '../../shared/tools';
import { MomentPipe } from '../../pipes/moment.pipe';
import {MarkdownPipe} from '../../pipes/markdown.pipe';
import {UserService} from '../../services/user.service';
import {CommunityService} from '../../services/community.service';
import {TagService} from '../../services/tag.service';
import { Router, ROUTER_DIRECTIVES } from '@angular/router';
import {MarkdownEditComponent} from '../markdown-edit/index';
import {TYPEAHEAD_DIRECTIVES, TOOLTIP_DIRECTIVES} from 'ng2-bootstrap/ng2-bootstrap';
import {ToasterService} from 'angular2-toaster/angular2-toaster';
import { Subject } from 'rxjs/Subject';
import { Observable } from 'rxjs/Observable';

import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinctUntilChanged';
import 'rxjs/add/operator/switchMap';

@Component({
  moduleId: module.id,
  selector: 'app-community-card',
  templateUrl: 'community-card.component.html',
  styleUrls: ['community-card.component.css'],
  directives: [MarkdownEditComponent, TYPEAHEAD_DIRECTIVES, TOOLTIP_DIRECTIVES,
    ROUTER_DIRECTIVES, FORM_DIRECTIVES, REACTIVE_FORM_DIRECTIVES],
  pipes: [MomentPipe, MarkdownPipe]
})
export class CommunityCardComponent implements OnInit {

  @Input() community: Community;

  private showVoteSlider: boolean = false;

  @Input() editMode: boolean = false;

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

  private rgex = Tools.rgex;

  constructor(private userService: UserService,
    private communityService: CommunityService,
    private tagService: TagService,
    private toasterService: ToasterService,
    private router: Router) { }

  ngOnInit() {
    this.setupTagSearch();
    this.setupUserSearch();
    this.setupBlockedUserSearch();
  }

  ngAfterViewInit() {
  }


  isCreator(): boolean {
    console.log(this.community);
    if (this.userService.getUser() != null) {
      return this.userService.getUser().id == this.community.creator.id;
    } else {
      return false;
    }
  }

  toggleEditMode() {
    this.editMode = !this.editMode;
  }

  setEditText($event) {
    this.community.text = $event;
  }

  saveCommunity() {
    this.communityService.saveCommunity(this.community).subscribe(
      c => {
        this.community = c;
        this.editMode = false;
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
        .subscribe((result: any) => {
          observer.next(result.tags);
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
      console.log(d);
      this.tagSearchSelected = '';
      this.toasterService.pop('success', 'New Tag Created', d.name);
      this.addTag(d);
    });

  }

  // User search methods
  setupUserSearch() {
    this.userSearchResultsObservable = Observable.create((observer: any) => {
      this.userService.searchUsers(this.userSearchSelected)
        .subscribe((result: any) => {
          observer.next(result.users);
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

  privateUsersWithoutYou() {
    return this.community.privateUsers.slice(1);
  }

  // Blocked user methods
  setupBlockedUserSearch() {
    this.blockedUserSearchResultsObservable = Observable.create((observer: any) => {
      this.userService.searchUsers(this.blockedUserSearchSelected)
        .subscribe((result: any) => {
          observer.next(result.users);
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

  removeQuotes(text: string) {
    return text.replace(/['"]+/g, '');
  }

}
