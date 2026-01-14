import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { StudentFormComponent } from './student-form.component';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { StudentService } from '../../../services/student.service'; // Ajusté selon la structure réelle
import { MatGridListModule } from '@angular/material/grid-list';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { Student } from '../../../models/Student'; // Ajusté selon la structure réelle

describe('StudentFormComponent', () => {
  let component: StudentFormComponent;
  let fixture: ComponentFixture<StudentFormComponent>;
  let studentService: jest.Mocked<StudentService>;
  let dialogRef: jest.Mocked<MatDialogRef<StudentFormComponent>>;
  let snackBar: jest.Mocked<MatSnackBar>;

  const mockStudent: Student = {
    id: 1,
    name: 'John',
    lastName: 'Doe',
    email: 'john.doe@example.com',
    image: null,
    createdAt: '2023-01-01'
  };

  const mockFile = new File([''], 'test.png', { type: 'image/png' });

  // Helper function to create mock FileList
  const createMockFileList = (file: File): FileList => {
    return {
      0: file,
      length: 1,
      item: (index: number) => (index === 0 ? file : null)
    } as unknown as FileList;
  };

  beforeEach(async () => {
    const studentServiceSpy = {
      createWithImage: jest.fn(),
      editWithImage: jest.fn()
    };
    const dialogRefSpy = {
      close: jest.fn()
    };
    const snackBarSpy = {
      open: jest.fn()
    };

    await TestBed.configureTestingModule({
      declarations: [StudentFormComponent],
      imports: [
        ReactiveFormsModule,
        MatGridListModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        NoopAnimationsModule,
        HttpClientTestingModule
      ],
      providers: [
        FormBuilder,
        { provide: StudentService, useValue: studentServiceSpy },
        { provide: MatDialogRef, useValue: dialogRefSpy },
        { provide: MAT_DIALOG_DATA, useValue: mockStudent },
        { provide: MatSnackBar, useValue: snackBarSpy }
      ]
    }).compileComponents();

    studentService = TestBed.inject(StudentService) as jest.Mocked<StudentService>;
    dialogRef = TestBed.inject(MatDialogRef) as jest.Mocked<MatDialogRef<StudentFormComponent>>;
    snackBar = TestBed.inject(MatSnackBar) as jest.Mocked<MatSnackBar>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(StudentFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with student data for edit', () => {
    expect(component.title).toBe('Edit Student');
    expect(component.titleButton).toBe('Edit');
    expect(component.addStudentForm.value).toEqual({
      id: 1,
      name: 'John',
      lastName: 'Doe',
      email: 'john.doe@example.com'
    });
  });

  it('should initialize form with empty data for create', async () => {
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      declarations: [StudentFormComponent],
      imports: [
        ReactiveFormsModule,
        MatGridListModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        NoopAnimationsModule,
        HttpClientTestingModule
      ],
      providers: [
        FormBuilder,
        { provide: StudentService, useValue: studentService },
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: MAT_DIALOG_DATA, useValue: null },
        { provide: MatSnackBar, useValue: snackBar }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(StudentFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.title).toBe('Add Student');
    expect(component.titleButton).toBe('Save');
    expect(component.addStudentForm.value).toEqual({
      id: null,
      name: null,
      lastName: null,
      email: null
    });
  });

  it('should handle file input change', () => {
    const mockFileList = createMockFileList(mockFile);
    component.handleFileInputChange(mockFileList);
    fixture.detectChanges();

    expect(component.file_store).toEqual(mockFileList);
    expect(component.display.value).toBe('test.png');
  });

  it('should create student on submit if form is valid and id is not set', fakeAsync(() => {
    const newStudent = {
      id: null,
      name: 'Jane',
      lastName: 'Smith',
      email: 'jane.smith@example.com',
      image: null,
      createdAt: '2023-01-01'
    };

    studentService.createWithImage.mockReturnValue(of(newStudent));

    // Set form values without image
    component.addStudentForm.setValue({
      id: null,
      name: 'Jane',
      lastName: 'Smith',
      email: 'jane.smith@example.com'
    });

    // Simulate file input change
    const mockFileList = createMockFileList(mockFile);
    component.handleFileInputChange(mockFileList);

    expect(component.addStudentForm.valid).toBe(true); // Remplacé toBeTrue()

    component.onSubmit();
    tick();

    expect(studentService.createWithImage).toHaveBeenCalledWith(
      expect.objectContaining({
        name: 'Jane',
        lastName: 'Smith',
        email: 'jane.smith@example.com'
      }),
      mockFile
    );
    expect(snackBar.open).toHaveBeenCalledWith('Information saved successfully', 'OK', { duration: 3000 });
    expect(dialogRef.close).toHaveBeenCalledWith({
      event: 'close',
      data: newStudent,
      method: 'create'
    });
  }));

  it('should update student on submit if form is valid and id is set', fakeAsync(() => {
    const updatedStudent = { ...mockStudent, name: 'Updated John' };
    studentService.editWithImage.mockReturnValue(of(updatedStudent));

    // Set form values without image
    component.addStudentForm.setValue({
      id: 1,
      name: 'Updated John',
      lastName: 'Doe',
      email: 'john.doe@example.com'
    });

    // Simulate file input change
    const mockFileList = createMockFileList(mockFile);
    component.handleFileInputChange(mockFileList);

    expect(component.addStudentForm.valid).toBe(true); // Remplacé toBeTrue()

    component.onSubmit();
    tick();

    expect(studentService.editWithImage).toHaveBeenCalledWith(
      expect.objectContaining({
        id: 1,
        name: 'Updated John',
        lastName: 'Doe',
        email: 'john.doe@example.com'
      }),
      mockFile
    );
    expect(snackBar.open).toHaveBeenCalledWith('Information updated successfully', 'OK', { duration: 3000 });
    expect(dialogRef.close).toHaveBeenCalledWith({
      event: 'close',
      data: updatedStudent,
      method: 'edit'
    });
  }));
});