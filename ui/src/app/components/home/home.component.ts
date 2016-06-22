import { Component, OnInit } from '@angular/core';
import {ToasterContainerComponent, ToasterService, ToasterConfig} from 'angular2-toaster/angular2-toaster';
import {DiscussionService} from '../../services/discussion.service';

import {DiscussionCardComponent} from '../discussion-card/index';

@Component({
  moduleId: module.id,
  selector: 'app-home',
  templateUrl: 'home.component.html',
  styleUrls: ['home.component.css'],
  directives: [DiscussionCardComponent]
})
export class HomeComponent implements OnInit {

  constructor(private toasterService: ToasterService,
    private discussionService: DiscussionService) {}

  popToast() {
    this.toasterService.pop('info', 'Args Title', 'Args Body');
  }

}
