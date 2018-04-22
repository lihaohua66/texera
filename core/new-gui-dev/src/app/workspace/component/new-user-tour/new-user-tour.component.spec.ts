import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NewUserTourComponent } from './new-user-tour.component';

describe('NewUserTourComponent', () => {
  let component: NewUserTourComponent;
  let fixture: ComponentFixture<NewUserTourComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NewUserTourComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NewUserTourComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
