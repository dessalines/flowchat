/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { CommunityService } from './community.service';

describe('Community Service', () => {
	beforeEach(() => {
		TestBed.configureTestingModule({
			providers: [CommunityService]
		});
	});

	it('should ...',
		inject([CommunityService], (service: CommunityService) => {
			expect(service).toBeTruthy();
		}));
});
