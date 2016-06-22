import {
  beforeEachProviders,
  it,
  describe,
  expect,
  inject
} from '@angular/core/testing';
import { TagService } from './tag.service';

describe('Tag Service', () => {
  beforeEachProviders(() => [TagService]);

  it('should ...',
      inject([TagService], (service: TagService) => {
    expect(service).toBeTruthy();
  }));
});
