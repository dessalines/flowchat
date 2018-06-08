import { Component, OnInit, ViewChild, Output, EventEmitter, Input } from '@angular/core';
import { ModalDirective } from 'ngx-bootstrap/modal';
import { Observable } from 'rxjs/Observable';

import { UserService } from '../../services';

import { Tools } from '../../shared';
import { ToasterService } from 'angular2-toaster';


@Component({
	selector: 'app-new-user-modal',
	templateUrl: './new-user-modal.component.html',
	styleUrls: ['./new-user-modal.component.scss']
})
export class NewUserModalComponent implements OnInit {

	@ViewChild('smModal') private smModal: ModalDirective;
	@Output() userCreated = new EventEmitter();

	public showLoginModal: boolean = false;

	public name: string;

	constructor(private userService: UserService,
		private toasterService: ToasterService) { }

	ngOnInit() {
		// focus when the modal is shown
		this.smModal.onShown.subscribe(() => document.getElementById("new-user-input").focus());

		if (this.userService.getUser() == null) {
			setTimeout(() => this.smModal.show(), 100);
		} else {
			this.userCreated.emit();
		}
	}

	onSubmit() {

		let obs: Observable<string> = this.userService.createNewUser(this.name);
		obs.subscribe(rJWT => {
			Tools.createCookie('jwt', rJWT, 9999);
			this.userService.setUserFromCookie();
			this.smModal.hide();
			this.userCreated.emit();

			// TODO 
			// if (!this.createNewUser) {
			// 	location.reload();
			// }
		},
			error => {
				console.error(error);
				this.toasterService.pop("error", error._body);
			});
	}

	hiddenEvent() {
		this.smModal.hide();
	}
}

