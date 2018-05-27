import { TestBed, async, inject } from '@angular/core/testing';
import { UserService } from './user.service';

describe('User Service', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [UserService]
    });
  });
  it('should ...',
      inject([UserService], (service: UserService) => {
    expect(service).toBeTruthy();
  }));
});
