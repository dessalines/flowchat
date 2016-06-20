import {
  beforeEachProviders,
  it,
  describe,
  expect,
  inject
} from '@angular/core/testing';
import { DiscussionService } from './discussion.service';

describe('Discussion Service', () => {
  beforeEachProviders(() => [DiscussionService]);

  it('should ...',
      inject([DiscussionService], (service: DiscussionService) => {
    expect(service).toBeTruthy();
  }));
});
