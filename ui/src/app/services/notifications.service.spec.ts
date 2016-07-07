import {
  beforeEachProviders,
  it,
  describe,
  expect,
  inject
} from '@angular/core/testing';
import { NotificationsService } from './notifications.service';

describe('Notifications Service', () => {
  beforeEachProviders(() => [NotificationsService]);

  it('should ...',
      inject([NotificationsService], (service: NotificationsService) => {
    expect(service).toBeTruthy();
  }));
});
