import { Component, OnInit, Input } from '@angular/core';
import {ToasterContainerComponent, ToasterService, ToasterConfig} from 'angular2-toaster/angular2-toaster';
import {DiscussionService} from '../../services/discussion.service';
import {TagService} from '../../services/tag.service';
import {Discussion} from '../../shared/discussion.interface';
import {Tag} from '../../shared/tag.interface';
import {Tools} from '../../shared/tools';
import {DiscussionCardComponent} from '../discussion-card/index';
import {FooterComponent} from '../footer/index';
import { Router, ROUTER_DIRECTIVES } from '@angular/router';

@Component({
  moduleId: module.id,
  selector: 'app-home',
  templateUrl: 'home.component.html',
  styleUrls: ['home.component.css'],
  directives: [DiscussionCardComponent, FooterComponent, ROUTER_DIRECTIVES],
  providers: []
})
export class HomeComponent implements OnInit {

  private discussions: Array<Discussion> = [];
  private popularTags: Array<Tag>;
  private discussionSorting: string = "time-3600";

  private currentPageNum: number = 1;
  private scrollDebounce: number = 0;

  constructor(private toasterService: ToasterService,
    private discussionService: DiscussionService,
    private tagService: TagService,
    private router: Router) { }

  popToast() {
    this.toasterService.pop('info', 'Args Title', 'Args Body');
  }

  ngOnInit() {
    this.getDiscussions(this.currentPageNum, this.discussionSorting);
    this.getPopularTags();
  }

  resort() {
    console.log('resorting');
    this.getDiscussions(this.currentPageNum, this.discussionSorting);
  }

  onScroll(event) {

    if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight) {
      if (this.scrollDebounce == 0) {
        this.scrollDebounce = 1;
        // you're at the bottom of the page
        this.currentPageNum += 1;
        this.getDiscussions(this.currentPageNum, this.discussionSorting);
        setTimeout(() => this.scrollDebounce = 0, 1000);
      }
    }
  }

  getDiscussions(page: number, orderBy: string) {
    this.discussionService.getDiscussions(page, undefined, undefined, orderBy).subscribe(
      d => {
        // Append them
        this.discussions.push(...d.discussions);
      });
  }

  getPopularTags() {
    this.tagService.getPopularTags().subscribe(
      t => {
        this.popularTags = t
        console.log(this.popularTags);
      });
  }

  removeQuotes(text: string) {
    return Tools.removeQuotes(text);
  }

}
