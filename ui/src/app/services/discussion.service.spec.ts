import { TestBed, async, inject } from '@angular/core/testing';
import { DiscussionService } from './discussion.service';

describe('Discussion Service', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DiscussionService]
    });
  });

  it('should ...',
      inject([DiscussionService], (service: DiscussionService) => {
    expect(service).toBeTruthy();
  }));
});
