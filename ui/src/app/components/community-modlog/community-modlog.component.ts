import { Component, OnInit } from '@angular/core';
import {Router, ActivatedRoute, ROUTER_DIRECTIVES} from '@angular/router';
import {CommunityService} from '../../services/community.service';
import {Community} from '../../shared/community.interface';
import {ToasterService} from 'angular2-toaster/angular2-toaster';
import { MomentPipe } from '../../pipes/moment.pipe';
import {FooterComponent} from '../footer/index';
import {CommunityRole} from '../../shared/community-role.enum';

@Component({
  moduleId: module.id,
  selector: 'app-community-modlog',
  templateUrl: 'community-modlog.component.html',
  styleUrls: ['community-modlog.component.css'],
  directives: [ROUTER_DIRECTIVES, FooterComponent],
  pipes: [MomentPipe]
})
export class CommunityModlogComponent implements OnInit {

  private sub: any;
  private modlog: Array<any>;

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
      console.log(this.modlog);
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
