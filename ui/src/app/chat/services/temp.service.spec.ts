import {
  beforeEachProviders,
  it,
  describe,
  expect,
  inject
} from '@angular/core/testing';
import { TempService } from './temp.service';

describe('TempService Service', () => {
  beforeEachProviders(() => [TempService]);

  it('should ...',
      inject([TempService], (service: TempService) => {
    expect(service).toBeTruthy();
  }));
});
