import { TestBed, async } from '@angular/core/testing';
import { MomentPipe } from './moment.pipe';

describe('Pipe: Moment', () => {
  it('create an instance', () => {
    let pipe = new MomentPipe();
    expect(pipe).toBeTruthy();
  });
});
