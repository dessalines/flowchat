import { Component, OnInit } from '@angular/core';
import { RouteConfig, ROUTER_DIRECTIVES, Router, RouteParams } from '@angular/router-deprecated';
import {ChatComponent} from '../chat/index';
import {RouteParamService} from '../../services/route-param.service';
import {UserService} from '../../services/user.service';

@Component({
  moduleId: module.id,
  selector: 'app-chat-list',
  templateUrl: 'chat-list.component.html',
  styleUrls: ['chat-list.component.css'],
  directives: [ROUTER_DIRECTIVES],
  providers: [RouteParamService]
})
@RouteConfig([
  { path: '/', name: 'Chat', component: ChatComponent, useAsDefault: true},
  { path: '/comment/:commentId', name: 'ChatSub', component: ChatComponent }
])
export class ChatListComponent implements OnInit {

  constructor(private router: Router,
    private routeParams: RouteParams,
    private userService: UserService) {}

  ngOnInit() {
  	console.log(this.routeParams);
  }

}
