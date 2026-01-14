import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ConfirmDialogComponent } from './confirm-dialog.component';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('ConfirmDialogComponent', () => {
  let component: ConfirmDialogComponent;
  let fixture: ComponentFixture<ConfirmDialogComponent>;
  let dialogRef: MatDialogRef<ConfirmDialogComponent>;

  const mockData = {
    message: 'Are you sure you want to delete this record?'
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ConfirmDialogComponent],
      imports: [
        MatDialogModule,
        MatButtonModule,
        NoopAnimationsModule
      ],
      providers: [
        { 
          provide: MatDialogRef, 
          useValue: {
            close: jest.fn()
          } 
        },
        { provide: MAT_DIALOG_DATA, useValue: mockData }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ConfirmDialogComponent);
    component = fixture.componentInstance;
    dialogRef = TestBed.inject(MatDialogRef);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display the provided message', () => {
    const messageElement = fixture.nativeElement.querySelector('p');
    expect(messageElement.textContent).toBe(mockData.message);
  });

  it('should close dialog with true when confirm is clicked', () => {
    const closeSpy = jest.spyOn(dialogRef, 'close');
    component.onConfirm();
    expect(closeSpy).toHaveBeenCalledWith(true);
  });

  it('should close dialog with false when cancel is clicked', () => {
    const closeSpy = jest.spyOn(dialogRef, 'close');
    component.onDismiss();
    expect(closeSpy).toHaveBeenCalledWith(false);
  });
});