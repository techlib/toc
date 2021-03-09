import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TocDialogComponent } from './toc-dialog.component';

describe('TocDialogComponent', () => {
  let component: TocDialogComponent;
  let fixture: ComponentFixture<TocDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TocDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TocDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
