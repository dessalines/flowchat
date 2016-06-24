import {
  beforeEachProviders,
  it,
  describe,
  expect,
  inject
} from '@angular/core/testing';
import { UserSearchService } from './user-search.service';

describe('UserSearch Service', () => {
  beforeEachProviders(() => [UserSearchService]);

  it('should ...',
      inject([UserSearchService], (service: UserSearchService) => {
    expect(service).toBeTruthy();
  }));
});
