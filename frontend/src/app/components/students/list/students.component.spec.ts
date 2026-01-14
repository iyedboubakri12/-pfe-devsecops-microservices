import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StudentsComponent } from './students.component';
import { StudentService } from '../../../services/student.service';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';
import { Student } from '../../../models/Student';
import { ConfirmDialogComponent } from '../../../shared/confirm-dialog/confirm-dialog.component';
import { StudentFormComponent } from '../create/student-form.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ChangeDetectorRef } from '@angular/core';
import { PageHeaderComponent } from '../../../layout/page-header.component';

describe('StudentsComponent', () => {
  let component: StudentsComponent;
  let fixture: ComponentFixture<StudentsComponent>;
  let studentService: jest.Mocked<StudentService>;
  let dialog: jest.Mocked<MatDialog>;
  let cdr: jest.Mocked<ChangeDetectorRef>;

  const mockStudent: Student = {
    id: 1,
    name: 'John',
    lastName: 'Doe',
    email: 'john.doe@example.com',
    image: null,
    createdAt: '2023-10-01',
  };

  const mockPageData = {
    content: [mockStudent],
    totalElements: 1,
    totalPages: 1,
    number: 0,
    size: 10,
  };

  beforeEach(async () => {
    const studentServiceMock = {
      getAllPages: jest.fn(),
      getAllPagesWithText: jest.fn(),
      delete: jest.fn(),
    };
    const dialogMock = {
      open: jest.fn(),
    };
    const cdrMock = {
      detectChanges: jest.fn(),
    };

    await TestBed.configureTestingModule({
      declarations: [StudentsComponent, PageHeaderComponent],
      imports: [
        HttpClientTestingModule,
        MatTableModule,
        MatPaginatorModule,
        MatSortModule,
        MatFormFieldModule,
        MatInputModule,
        MatIconModule,
        MatButtonModule,
        MatDialogModule,
        BrowserAnimationsModule,
      ],
      providers: [
        { provide: StudentService, useValue: studentServiceMock },
        { provide: MatDialog, useValue: dialogMock },
        { provide: ChangeDetectorRef, useValue: cdrMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(StudentsComponent);
    component = fixture.componentInstance;
    studentService = TestBed.inject(StudentService) as jest.Mocked<StudentService>;
    dialog = TestBed.inject(MatDialog) as jest.Mocked<MatDialog>;
    cdr = TestBed.inject(ChangeDetectorRef) as jest.Mocked<ChangeDetectorRef>;
  });

  beforeEach(() => {
    studentService.getAllPages.mockReturnValue(of(mockPageData));
    studentService.getAllPagesWithText.mockReturnValue(of(mockPageData)); // Ajout du mock pour getAllPagesWithText
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should call getDataPage on initialization', () => {
      const getDataPageSpy = jest.spyOn(component, 'getDataPage');
      component.ngOnInit();
      expect(getDataPageSpy).toHaveBeenCalledWith(0, 10);
    });
  });

  describe('getDataPage', () => {
    it('should fetch paginated data and update dataSource', () => {
      const detectChangesSpy = jest.spyOn(component, 'detectChanges');

      component.getDataPage(0, 10);

      expect(studentService.getAllPages).toHaveBeenCalledWith('0', '10'); // Attendre des chaînes
      expect(component.dataSource.data).toEqual(mockPageData.content);
      expect(component.totalElements).toBe(mockPageData.totalElements);
      expect(detectChangesSpy).toHaveBeenCalled();
    });

    it('should reset dataSource on error when fetching data', () => {
      const errorObj = { error: { message: 'Error fetching data' } };
      studentService.getAllPages.mockReturnValue(throwError(() => errorObj));

      component.getDataPage(0, 10);

      expect(component.dataSource.data).toEqual([]);
    });
  });

  describe('applyFilter', () => {
    it('should call getDataPageWithText with trimmed lowercase filter value', () => {
      const getDataPageWithTextSpy = jest.spyOn(component, 'getDataPageWithText');
      const event = { target: { value: ' John ' } } as any;

      component.applyFilter(event);

      expect(getDataPageWithTextSpy).toHaveBeenCalledWith(0, 10, 'john');
    });
  });

  describe('openDialog', () => {
    it('should open dialog with StudentFormComponent and handle result', () => {
      const dialogRefSpy = {
        afterClosed: jest.fn().mockReturnValue(of({ data: { id: 1 } })),
      };
      dialog.open.mockReturnValue(dialogRefSpy as any);

      component.openDialog(null);

      expect(dialog.open).toHaveBeenCalledWith(StudentFormComponent, {
        width: '640px',
        disableClose: true,
        data: null,
      });
      expect(studentService.getAllPages).toHaveBeenCalledWith('0', '10'); // Attendre des chaînes
    });

    it('should not call getDataPage if dialog result is empty', () => {
      const dialogRefSpy = {
        afterClosed: jest.fn().mockReturnValue(of(null)),
      };
      dialog.open.mockReturnValue(dialogRefSpy as any);
      const getDataPageSpy = jest.spyOn(component, 'getDataPage');

      component.openDialog(null);

      expect(getDataPageSpy).not.toHaveBeenCalled();
    });
  });

  describe('delete', () => {
    it('should open confirm dialog and delete student if confirmed', () => {
      const dialogRefSpy = {
        afterClosed: jest.fn().mockReturnValue(of(true)),
      };
      dialog.open.mockReturnValue(dialogRefSpy as any);
      studentService.delete.mockReturnValue(of({} as Student));

      component.delete(1);

      expect(dialog.open).toHaveBeenCalledWith(ConfirmDialogComponent, {
        data: expect.any(String),
      });
      expect(studentService.delete).toHaveBeenCalledWith(1);
      expect(studentService.getAllPages).toHaveBeenCalledWith('0', '10'); // Attendre des chaînes
    });

    it('should not delete if dialog is not confirmed', () => {
      const dialogRefSpy = {
        afterClosed: jest.fn().mockReturnValue(of(false)),
      };
      dialog.open.mockReturnValue(dialogRefSpy as any);

      component.delete(1);

      expect(studentService.delete).not.toHaveBeenCalled();
    });

    it('should not reload data on error during deletion', () => {
      const dialogRefSpy = {
        afterClosed: jest.fn().mockReturnValue(of(true)),
      };
      dialog.open.mockReturnValue(dialogRefSpy as any);
      const errorObj = { error: { message: 'Delete error' } };
      studentService.delete.mockReturnValue(throwError(() => errorObj));
      const getDataPageSpy = jest.spyOn(component, 'getDataPage');

      component.delete(1);

      expect(getDataPageSpy).not.toHaveBeenCalled();
    });
  });

  describe('nextPage', () => {
    it('should update pageIndex and pageSize and call getDataPage', () => {
      const getDataPageSpy = jest.spyOn(component, 'getDataPage');
      const pageEvent = { pageIndex: 1, pageSize: 25 } as any;

      component.nextPage(pageEvent);

      expect(component.pageIndex).toBe(1);
      expect(component.pageSize).toBe(25);
      expect(getDataPageSpy).toHaveBeenCalledWith(1, 25);
    });
  });
});