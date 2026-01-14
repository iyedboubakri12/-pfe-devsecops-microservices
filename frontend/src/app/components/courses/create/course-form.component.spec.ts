import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { CourseFormComponent } from './course-form.component';
import { CourseService } from '@services/course.service';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormBuilder, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { Course } from '@models/Course';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('CourseFormComponent', () => {
  let component: CourseFormComponent;
  let fixture: ComponentFixture<CourseFormComponent>;
  let courseService: jest.Mocked<CourseService>;
  let dialogRef: jest.Mocked<MatDialogRef<CourseFormComponent>>;
  let snackBar: jest.Mocked<MatSnackBar>;

  const mockCourse: Course = {
    id: 1,
    name: 'Mathematics',
    description: 'Introduction to Algebra',
    createdAt: '2023-01-01',
    students: [],
    exams: []
  };

  beforeEach(async () => {
    const courseSpy = {
      create: jest.fn(),
      update: jest.fn()
    };
    const dialogSpy = {
      close: jest.fn()
    };
    const snackBarSpy = {
      open: jest.fn()
    };

    await TestBed.configureTestingModule({
      declarations: [CourseFormComponent],
      imports: [
        ReactiveFormsModule,
        FormsModule,
        MatGridListModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        NoopAnimationsModule
      ],
      providers: [
        FormBuilder,
        { provide: CourseService, useValue: courseSpy },
        { provide: MatDialogRef, useValue: dialogSpy },
        { provide: MAT_DIALOG_DATA, useValue: null },
        { provide: MatSnackBar, useValue: snackBarSpy }
      ]
    }).compileComponents();

    courseService = TestBed.inject(CourseService) as jest.Mocked<CourseService>;
    dialogRef = TestBed.inject(MatDialogRef) as jest.Mocked<MatDialogRef<CourseFormComponent>>;
    snackBar = TestBed.inject(MatSnackBar) as jest.Mocked<MatSnackBar>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CourseFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form in add mode when no data is provided', () => {
    expect(component.title).toBe('Add Course');
    expect(component.titleButton).toBe('Save');
    expect(component.form.get('id')?.value).toBeNull();
    expect(component.form.get('name')?.value).toBeNull();
    expect(component.form.get('description')?.value).toBeNull();
    expect(component.breakpoint).toBe(window.innerWidth <= 600 ? 1 : 2);
  });

  it('should initialize form in edit mode when data is provided', async () => {
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      declarations: [CourseFormComponent],
      imports: [
        ReactiveFormsModule,
        FormsModule,
        MatGridListModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        NoopAnimationsModule
      ],
      providers: [
        FormBuilder,
        { provide: CourseService, useValue: courseService },
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: MAT_DIALOG_DATA, useValue: mockCourse },
        { provide: MatSnackBar, useValue: snackBar }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CourseFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.title).toBe('Edit Course');
    expect(component.titleButton).toBe('Edit');
    expect(component.form.get('id')?.value).toBe(1);
    expect(component.form.get('name')?.value).toBe('Mathematics');
    expect(component.form.get('description')?.value).toBe('Introduction to Algebra');
  });

  it('should create a new course on submit', fakeAsync(() => {
    courseService.create.mockReturnValue(of(mockCourse));
    component.form.setValue({ id: null, name: 'Mathematics', description: 'Introduction to Algebra' });
    component.onSubmit();
    tick();

    expect(courseService.create).toHaveBeenCalledWith(expect.objectContaining({
      name: 'Mathematics',
      description: 'Introduction to Algebra'
    }));
    expect(snackBar.open).toHaveBeenCalledWith('Information saved successfully', 'OK', { duration: 3000 });
    expect(dialogRef.close).toHaveBeenCalledWith({ event: 'close', data: mockCourse, method: 'create' });
    expect(component.form.pristine).toBe(true);
  }));

  it('should update an existing course on submit', fakeAsync(() => {
    const updatedCourse = { ...mockCourse, name: 'Mathematics Updated' };
    courseService.update.mockReturnValue(of(updatedCourse));
    component.form.setValue({ id: 1, name: 'Mathematics Updated', description: 'Introduction to Algebra' });
    component.onSubmit();
    tick();

    expect(courseService.update).toHaveBeenCalledWith(expect.objectContaining({
      id: 1,
      name: 'Mathematics Updated',
      description: 'Introduction to Algebra'
    }));
    expect(snackBar.open).toHaveBeenCalledWith('Information updated successfully', 'OK', { duration: 3000 });
    expect(dialogRef.close).toHaveBeenCalledWith({ event: 'close', data: updatedCourse, method: 'edit' });
    expect(component.form.pristine).toBe(true);
  }));

  it('should close dialog on cancel', () => {
    component.openDialog();
    expect(dialogRef.close).toHaveBeenCalledWith({ event: 'close', data: null, method: 'create' });
  });

  it('should not submit if form is invalid', () => {
    component.form.setValue({ id: null, name: 'ab', description: 'def' });
    component.onSubmit();
    expect(courseService.create).not.toHaveBeenCalled();
    expect(courseService.update).not.toHaveBeenCalled();
  });

  it('should adjust breakpoint on window resize', () => {
    component.breakpoint = 2;
    component.onResize({ target: { innerWidth: 500 } });
    expect(component.breakpoint).toBe(1);

    component.onResize({ target: { innerWidth: 700 } });
    expect(component.breakpoint).toBe(2);
  });

  it('should detect form changes', () => {
    expect(component.wasFormChanged).toBe(false);
    component.form.get('name')?.setValue('Test');
    component.formChanged();
    expect(component.wasFormChanged).toBe(true);
  });
});