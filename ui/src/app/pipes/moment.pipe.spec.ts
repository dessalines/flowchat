import {
  it,
  describe,
  expect,
  inject,
  beforeEachProviders
} from '@angular/core/testing';
import { MomentPipe } from './moment.pipe';

describe('Moment Pipe', () => {
    beforeEachProviders(() => [MomentPipe]);

  it('should transform the input', inject([MomentPipe], (pipe: MomentPipe) => {
      // expect(pipe.transform(true)).toBe(null);
  }));
});
