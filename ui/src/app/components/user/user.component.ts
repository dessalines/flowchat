import { Component, OnInit } from '@angular/core';
import {Router, ActivatedRoute} from '@angular/router';
import {UserService} from '../../services';
import {DiscussionRole} from '../../shared';
import {ToasterService} from 'angular2-toaster/angular2-toaster';

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.scss'],
})
export class UserComponent implements OnInit {

  private sub: any;
  private userLog: Array<any>;

  constructor(private route: ActivatedRoute,
    private router: Router,
    private userService: UserService,
    private toasterService: ToasterService) { }

  ngOnInit() {

    this.sub = this.route.params.subscribe(params => {
      let userId: number = +params['userId'];
      this.getUserLog(userId);
    });

  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }

  getUserLog(userId: number) {
    this.userService.getUserLog(userId).subscribe(c => {
      this.userLog = c;
      console.log(this.userLog);
    },
      error => {
        this.toasterService.pop("error", "Error", error);
        this.router.navigate(['/']);
      });
  }

  getRole(id: number): string {
    return DiscussionRole[id];
  }

}
