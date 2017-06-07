import { TestBed, async, inject } from '@angular/core/testing';
import { ThreadedChatService } from './threaded-chat.service';

describe('ThreadedChatService Service', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ThreadedChatService]
    });
  });

  it('should ...',
      inject([ThreadedChatService], (service: ThreadedChatService) => {
    expect(service).toBeTruthy();
  }));
});
