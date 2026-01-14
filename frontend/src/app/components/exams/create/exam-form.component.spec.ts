import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ExamFormComponent } from './exam-form.component';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ExamService } from '../../../services/exam.service'; // Ajusté selon la structure réelle
import { MatGridListModule } from '@angular/material/grid-list';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { Exam } from '../../../models/Exam'; // Ajusté selon la structure réelle

describe('ExamFormComponent', () => {
  let component: ExamFormComponent;
  let fixture: ComponentFixture<ExamFormComponent>;
  let examService: jest.Mocked<ExamService>;
  let dialogRef: jest.Mocked<MatDialogRef<ExamFormComponent>>;
  let snackBar: jest.Mocked<MatSnackBar>;

  const mockExam = { id: 1, name: 'Math Exam', createdAt: '2023-01-01', questions: [], subjectFather: null, subjectChildren: null, replied: false };
  const mockSubjects = [{ id: 1, name: 'Math' }, { id: 2, name: 'Physics' }];
  const mockData = { data: mockExam, subjects: mockSubjects };

  beforeEach(async () => {
    const examServiceSpy = {
      create: jest.fn(),
      update: jest.fn()
    };
    const dialogRefSpy = {
      close: jest.fn()
    };
    const snackBarSpy = {
      open: jest.fn()
    };

    await TestBed.configureTestingModule({
      declarations: [ExamFormComponent],
      imports: [
        ReactiveFormsModule,
        MatGridListModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatButtonModule,
        NoopAnimationsModule
      ],
      providers: [
        FormBuilder,
        { provide: ExamService, useValue: examServiceSpy },
        { provide: MatDialogRef, useValue: dialogRefSpy },
        { provide: MAT_DIALOG_DATA, useValue: mockData },
        { provide: MatSnackBar, useValue: snackBarSpy }
      ]
    }).compileComponents();

    examService = TestBed.inject(ExamService) as jest.Mocked<ExamService>;
    dialogRef = TestBed.inject(MatDialogRef) as jest.Mocked<MatDialogRef<ExamFormComponent>>;
    snackBar = TestBed.inject(MatSnackBar) as jest.Mocked<MatSnackBar>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ExamFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges(); // Déclenche ngOnInit
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with exam data for edit', () => {
    expect(component.title).toBe('Edit Exam');
    expect(component.titleButton).toBe('Edit');
    expect(component.form.value).toEqual({ id: 1, name: 'Math Exam' });
  });

  it('should initialize form with empty data for create', async () => {
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      declarations: [ExamFormComponent],
      imports: [
        ReactiveFormsModule,
        MatGridListModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatButtonModule,
        NoopAnimationsModule
      ],
      providers: [
        FormBuilder,
        { provide: ExamService, useValue: examService },
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: MAT_DIALOG_DATA, useValue: { data: new Exam(), subjects: mockSubjects } },
        { provide: MatSnackBar, useValue: snackBar }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ExamFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.title).toBe('Add Exam');
    expect(component.titleButton).toBe('Save');
    expect(component.form.value).toEqual({ id: null, name: null });
  });

  it('should set breakpoint based on window width', () => {
    expect(component.breakpoint).toBe(2); // Par défaut, > 600
    component.onResize({ target: { innerWidth: 500 } });
    expect(component.breakpoint).toBe(1);
  });

  it('should close dialog on cancel', () => {
    component.openDialog();
    expect(dialogRef.close).toHaveBeenCalledWith({ event: 'close', data: null, method: 'create' });
  });

  it('should mark form as dirty on addCus', () => {
    component.onAddCus();
    expect(component.form.dirty).toBe(true); // Remplacé toBeTrue()
  });

  it('should set wasFormChanged on form change', () => {
    component.form.controls['name'].setValue('New Exam');
    component.formChanged();
    expect(component.wasFormChanged).toBe(true); // Remplacé toBeTrue()
  });

  it('should not submit if form is invalid', () => {
    component.form.controls['name'].setValue('ab'); // Trop court
    component.onSubmit();
    expect(examService.create).not.toHaveBeenCalled();
    expect(examService.update).not.toHaveBeenCalled();
  });

  it('should create exam on submit if form is valid and id is not set', fakeAsync(() => {
    const newExam = { id: 3, name: 'New Exam', createdAt: '2023-01-03', questions: [], subjectFather: null, subjectChildren: null, replied: false };
    examService.create.mockReturnValue(of(newExam));
    component.form.controls['id'].setValue(null); // Pas d'ID pour création
    component.form.controls['name'].setValue('New Exam');
    component.onSubmit();
    tick();

    expect(examService.create).toHaveBeenCalledWith(expect.objectContaining({ id: null, name: 'New Exam' }));
    expect(snackBar.open).toHaveBeenCalledWith('Information saved successfully', 'OK', { duration: 3000 });
    expect(dialogRef.close).toHaveBeenCalledWith({ event: 'close', data: newExam, method: 'create' });
  }));

  it('should update exam on submit if form is valid and id is set', fakeAsync(() => {
    const updatedExam = { id: 1, name: 'Updated Exam', createdAt: '2023-01-01', questions: [], subjectFather: null, subjectChildren: null, replied: false };
    examService.update.mockReturnValue(of(updatedExam));
    component.form.controls['name'].setValue('Updated Exam');
    component.onSubmit();
    tick();

    expect(examService.update).toHaveBeenCalledWith(expect.objectContaining({ id: 1, name: 'Updated Exam' }));
    expect(snackBar.open).toHaveBeenCalledWith('Information updated successfully', 'OK', { duration: 3000 });
    expect(dialogRef.close).toHaveBeenCalledWith({ event: 'close', data: updatedExam, method: 'edit' });
  }));
});