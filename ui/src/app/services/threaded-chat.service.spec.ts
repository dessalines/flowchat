import {
  beforeEachProviders,
  it,
  describe,
  expect,
  inject
} from '@angular/core/testing';
import { ThreadedChatService } from './threaded-chat.service';

describe('ThreadedChatService Service', () => {
  beforeEachProviders(() => [ThreadedChatService]);

  it('should ...',
      inject([ThreadedChatService], (service: ThreadedChatService) => {
    expect(service).toBeTruthy();
  }));
});
