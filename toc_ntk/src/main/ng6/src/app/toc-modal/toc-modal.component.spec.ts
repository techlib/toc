import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TocModalComponent } from './toc-modal.component';

describe('TocModalComponent', () => {
  let component: TocModalComponent;
  let fixture: ComponentFixture<TocModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TocModalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TocModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
