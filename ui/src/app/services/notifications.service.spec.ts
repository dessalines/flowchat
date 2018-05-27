import { TestBed, async, inject } from '@angular/core/testing';
import { NotificationsService } from './notifications.service';

describe('Notifications Service', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [NotificationsService]
    });
  });
  it('should ...',
      inject([NotificationsService], (service: NotificationsService) => {
    expect(service).toBeTruthy();
  }));
});
