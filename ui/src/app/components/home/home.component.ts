import { Component, OnInit, Input } from '@angular/core';
import {ToasterContainerComponent, ToasterService, ToasterConfig} from 'angular2-toaster/angular2-toaster';
import {DiscussionService} from '../../services/discussion.service';
import {TagService} from '../../services/tag.service';
import {Discussion} from '../../shared/discussion.interface';
import {Tag} from '../../shared/tag.interface';
import {Tools} from '../../shared/tools';
import {DiscussionCardComponent} from '../discussion-card/index';
import {FooterComponent} from '../footer/index';
import { Router, ROUTER_DIRECTIVES, RouteData, RouteParams } from '@angular/router-deprecated';

@Component({
  moduleId: module.id,
  selector: 'app-home',
  templateUrl: 'home.component.html',
  styleUrls: ['home.component.css'],
  directives: [DiscussionCardComponent, FooterComponent, ROUTER_DIRECTIVES],
  providers: []
})
export class HomeComponent implements OnInit {

  private discussions: Array<Discussion>;
  private popularTags: Array<Tag>;

  constructor(private toasterService: ToasterService,
    private discussionService: DiscussionService,
    private tagService: TagService,
    private router: Router) { }

  popToast() {
    this.toasterService.pop('info', 'Args Title', 'Args Body');
  }

  ngOnInit() {
    this.getDiscussions();
    this.getPopularTags();
  }

  getDiscussions() {
    this.discussionService.getDiscussions().subscribe(
      d => {
        this.discussions = d.discussions;
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
