import { TestBed, async, inject } from '@angular/core/testing';
import { LoginService } from './login.service';

describe('Login Service', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LoginService]
    });
  });
  it('should ...',
      inject([LoginService], (service: LoginService) => {
    expect(service).toBeTruthy();
  }));
});
