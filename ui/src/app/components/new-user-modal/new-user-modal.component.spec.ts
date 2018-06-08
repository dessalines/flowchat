import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NewUserModalComponent } from './new-user-modal.component';

describe('NewUserModalComponent', () => {
  let component: NewUserModalComponent;
  let fixture: ComponentFixture<NewUserModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NewUserModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NewUserModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
