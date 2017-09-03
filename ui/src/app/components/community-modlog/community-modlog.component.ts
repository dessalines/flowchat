import { Component, OnInit } from '@angular/core';
import {Router, ActivatedRoute} from '@angular/router';
import {CommunityService} from '../../services';
import {ToasterService} from 'angular2-toaster/angular2-toaster';
import {CommunityRole, Community} from '../../shared';

@Component({
  selector: 'app-community-modlog',
  templateUrl: './community-modlog.component.html',
  styleUrls: ['./community-modlog.component.css'],
})
export class CommunityModlogComponent implements OnInit {

  public sub: any;
  public modlog: Array<any>;

  constructor(private route: ActivatedRoute,
    private router: Router,
    private communityService: CommunityService,
    private toasterService: ToasterService) { }

  ngOnInit() {

    this.sub = this.route.params.subscribe(params => {
      let communityId: number = +params['communityId'];
      this.getCommunityModlog(communityId);
    });

  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }

  getCommunityModlog(communityId: number) {
    this.communityService.getCommunityModlog(communityId).subscribe(c => {
      this.modlog = c;
    },
      error => {
        this.toasterService.pop("error", "Error", error);
        this.router.navigate(['/']);
      });
  }

  getRole(id: number): string {
    return CommunityRole[id];
  }

}
