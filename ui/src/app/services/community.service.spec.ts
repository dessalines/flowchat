/* tslint:disable:no-unused-variable */

import {
  beforeEach, beforeEachProviders,
  describe, xdescribe,
  expect, it, xit,
  async, inject
} from '@angular/core/testing';
import { CommunityService } from './community.service';

describe('Community Service', () => {
  beforeEachProviders(() => [CommunityService]);

  it('should ...',
      inject([CommunityService], (service: CommunityService) => {
    expect(service).toBeTruthy();
  }));
});
