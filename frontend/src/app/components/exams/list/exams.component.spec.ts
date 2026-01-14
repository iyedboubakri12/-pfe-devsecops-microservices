import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ExamsComponent } from './exams.component';
import { ExamService } from '../../../services/exam.service';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator } from '@angular/material/paginator';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { Exam } from '../../../models/Exam';
import { Subject } from '../../../models/Subject';
import { ExamFormComponent } from '../create/exam-form.component';
import { ConfirmDialogComponent } from '../../../shared/confirm-dialog/confirm-dialog.component';
import { ChangeDetectorRef } from '@angular/core';
import { By } from '@angular/platform-browser';
import { Component } from '@angular/core';

// Stub pour app-page-header
@Component({ selector: 'app-page-header', template: '' })
class PageHeaderStubComponent {}

describe('ExamsComponent', () => {
  let component: ExamsComponent;
  let fixture: ComponentFixture<ExamsComponent>;
  let examService: jest.Mocked<ExamService>;
  let dialog: jest.Mocked<MatDialog>;
  let cdr: jest.Mocked<ChangeDetectorRef>;

  const mockExams: Exam[] = [
    { id: 1, name: 'Math Exam', createdAt: '2023-01-01', questions: [], subjectFather: null, subjectChildren: null, replied: false },
    { id: 2, name: 'Physics Exam', createdAt: '2023-01-02', questions: [], subjectFather: null, subjectChildren: null, replied: false }
  ];

  const mockPageResponse = {
    content: mockExams,
    totalElements: 2,
    number: 0,
    size: 10
  };

  const mockSubjects: Subject[] = [
    { id: 1, name: 'Math', father: null, children: null },
    { id: 2, name: 'Physics', father: null, children: null }
  ];

  beforeEach(async () => {
    const examServiceSpy = {
      getAllPages: jest.fn(),
      getAllPagesWithText: jest.fn(),
      delete: jest.fn(),
      getAlllSubjects: jest.fn()
    };
    const dialogSpy = {
      open: jest.fn()
    };
    const cdrSpy = {
      detectChanges: jest.fn()
    };

    await TestBed.configureTestingModule({
      declarations: [ExamsComponent, PageHeaderStubComponent],
      imports: [
        MatTableModule,
        MatPaginatorModule,
        MatSortModule,
        MatFormFieldModule,
        MatInputModule,
        MatIconModule,
        MatButtonModule,
        MatDialogModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: ExamService, useValue: examServiceSpy },
        { provide: MatDialog, useValue: dialogSpy },
        { provide: ChangeDetectorRef, useValue: cdrSpy }
      ]
    }).compileComponents();

    examService = TestBed.inject(ExamService) as jest.Mocked<ExamService>;
    dialog = TestBed.inject(MatDialog) as jest.Mocked<MatDialog>;
    cdr = TestBed.inject(ChangeDetectorRef) as jest.Mocked<ChangeDetectorRef>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ExamsComponent);
    component = fixture.componentInstance;
    examService.getAllPages.mockReturnValue(of(mockPageResponse));
    examService.getAlllSubjects.mockReturnValue(of(mockSubjects));
    examService.getAllPagesWithText.mockReturnValue(of(mockPageResponse)); // Ajout du mock pour éviter erreurs
    fixture.detectChanges(); // Déclenche ngOnInit
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load exams and subjects on init', () => {
    expect(examService.getAllPages).toHaveBeenCalledWith(0, 10);
    expect(examService.getAlllSubjects).toHaveBeenCalled();
    expect(component.dataSource.data).toEqual(mockExams);
    expect(component.totalElements).toBe(2);
    expect(component.dataListSubjects).toEqual(mockSubjects);
  });

  it('should set paginator and sort after view init', () => {
    component.ngAfterViewInit();
    expect(component.dataSource.paginator).toBe(component.paginator);
    expect(component.dataSource.sort).toBe(component.sort);
  });

  it('should apply filter and fetch filtered data', fakeAsync(() => {
    examService.getAllPagesWithText.mockReturnValue(of(mockPageResponse));
    const input = fixture.debugElement.query(By.css('input')).nativeElement;
    input.value = 'Math';
    input.dispatchEvent(new Event('keyup'));
    tick();

    expect(examService.getAllPagesWithText).toHaveBeenCalledWith(0, 10, 'math');
    expect(component.dataSource.data).toEqual(mockExams);
  }));

  it('should open dialog for new exam', fakeAsync(() => {
    const dialogRefSpy = {
      afterClosed: jest.fn().mockReturnValue(of({ data: { id: 3 } }))
    };
    dialog.open.mockReturnValue(dialogRefSpy as any);

    component.openDialog(null);
    tick();

    expect(dialog.open).toHaveBeenCalledWith(ExamFormComponent, {
      width: '640px',
      disableClose: true,
      data: { data: null, subjects: mockSubjects }
    });
    expect(examService.getAllPages).toHaveBeenCalledWith(0, 10);
  }));

  it('should open dialog for editing an exam', fakeAsync(() => {
    const dialogRefSpy = {
      afterClosed: jest.fn().mockReturnValue(of({ data: { id: 1 } }))
    };
    dialog.open.mockReturnValue(dialogRefSpy as any);

    const exam = mockExams[0];
    component.openDialog(exam);
    tick();

    expect(dialog.open).toHaveBeenCalledWith(ExamFormComponent, {
      width: '640px',
      disableClose: true,
      data: { data: exam, subjects: mockSubjects }
    });
    expect(examService.getAllPages).toHaveBeenCalledWith(0, 10);
  }));

  it('should delete an exam after confirmation', fakeAsync(() => {
    const dialogRefSpy = {
      afterClosed: jest.fn().mockReturnValue(of(true))
    };
    dialog.open.mockReturnValue(dialogRefSpy as any);
    examService.delete.mockReturnValue(of(null));

    component.delete(1);
    tick();

    expect(dialog.open).toHaveBeenCalledWith(ConfirmDialogComponent, {
      data: 'Are you sure you want to delete the record?'
    });
    expect(examService.delete).toHaveBeenCalledWith(1);
    expect(examService.getAllPages).toHaveBeenCalledWith(0, 10);
  }));

  it('should not delete if confirmation is cancelled', fakeAsync(() => {
    const dialogRefSpy = {
      afterClosed: jest.fn().mockReturnValue(of(false))
    };
    dialog.open.mockReturnValue(dialogRefSpy as any);

    component.delete(1);
    tick();

    expect(examService.delete).not.toHaveBeenCalled();
  }));

  it('should handle pagination', () => {
    const pageEvent = { pageIndex: 1, pageSize: 5, length: 2 };
    component.nextPage(pageEvent);

    expect(examService.getAllPages).toHaveBeenCalledWith('1', '5'); // Attendre des chaînes
    expect(component.pageIndex).toBe(1);
    expect(component.pageSize).toBe(5);
  });
});