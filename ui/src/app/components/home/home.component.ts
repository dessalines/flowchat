import { Component, OnInit } from '@angular/core';
import {ToasterContainerComponent, ToasterService, ToasterConfig} from 'angular2-toaster/angular2-toaster';

@Component({
  moduleId: module.id,
  selector: 'app-home',
  templateUrl: 'home.component.html',
  styleUrls: ['home.component.css'],
})
export class HomeComponent {

  constructor(private toasterService: ToasterService) {}

  popToast() {
    this.toasterService.pop('info', 'Args Title', 'Args Body');
  }

}
