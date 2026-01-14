import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { CoursesComponent } from './courses.component';
import { CourseService } from '../../../services/course.service';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { Course } from '../../../models/Course';
import { CourseFormComponent } from '../create/course-form.component';
import { ConfirmDialogComponent } from '../../../shared/confirm-dialog/confirm-dialog.component';
import { ChangeDetectorRef, Component } from '@angular/core';
import { By } from '@angular/platform-browser';

// Stub pour le composant app-page-header
@Component({ selector: 'app-page-header', template: '' })
class PageHeaderStubComponent {}

describe('CoursesComponent', () => {
  let component: CoursesComponent;
  let fixture: ComponentFixture<CoursesComponent>;
  let courseService: jest.Mocked<CourseService>;
  let dialog: jest.Mocked<MatDialog>;
  let cdr: jest.Mocked<ChangeDetectorRef>;

  const mockCourses: Course[] = [
    { id: 1, name: 'Mathematics', description: 'Intro to Algebra', createdAt: '2023-01-01', students: [], exams: [] },
    { id: 2, name: 'Physics', description: 'Mechanics', createdAt: '2023-01-02', students: [], exams: [] }
  ];

  const mockPageResponse = {
    content: mockCourses,
    totalElements: 2,
    number: 0,
    size: 10
  };

  beforeEach(async () => {
    const courseSpy = {
      getAllPages: jest.fn(),
      getAllPagesWithText: jest.fn(),
      delete: jest.fn()
    };

    const dialogSpy = {
      open: jest.fn()
    };

    const cdrSpy = {
      detectChanges: jest.fn()
    };

    await TestBed.configureTestingModule({
      declarations: [CoursesComponent, PageHeaderStubComponent],
      imports: [
        MatTableModule,
        MatPaginatorModule,
        MatSortModule,
        MatFormFieldModule,
        MatInputModule,
        MatDialogModule,
        MatIconModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: CourseService, useValue: courseSpy },
        { provide: MatDialog, useValue: dialogSpy },
        { provide: ChangeDetectorRef, useValue: cdrSpy }
      ]
    }).compileComponents();

    courseService = TestBed.inject(CourseService) as jest.Mocked<CourseService>;
    dialog = TestBed.inject(MatDialog) as jest.Mocked<MatDialog>;
    cdr = TestBed.inject(ChangeDetectorRef) as jest.Mocked<ChangeDetectorRef>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CoursesComponent);
    component = fixture.componentInstance;
    courseService.getAllPages.mockReturnValue(of(mockPageResponse));
    courseService.getAllPagesWithText.mockReturnValue(of(mockPageResponse));
    fixture.detectChanges(); // Déclenche ngOnInit
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load courses on init', () => {
    expect(courseService.getAllPages).toHaveBeenCalledWith(0, 10); // Attendre des nombres
    expect(component.dataSource.data).toEqual(mockCourses);
    expect(component.totalElements).toBe(2);
  });

  it('should set paginator and sort after view init', () => {
    component.ngAfterViewInit();
    expect(component.dataSource.paginator).toBe(component.paginator);
    expect(component.dataSource.sort).toBe(component.sort);
  });

  it('should apply filter and fetch filtered data', fakeAsync(() => {
    courseService.getAllPagesWithText.mockReturnValue(of(mockPageResponse));
    const input = fixture.debugElement.query(By.css('input')).nativeElement;
    input.value = 'Math';
    input.dispatchEvent(new Event('keyup'));
    tick();

    expect(courseService.getAllPagesWithText).toHaveBeenCalledWith(0, 10, 'math'); // Attendre des nombres
    expect(component.dataSource.data).toEqual(mockCourses);
  }));

  it('should open dialog for new course', fakeAsync(() => {
    const dialogRefSpy = {
      afterClosed: jest.fn().mockReturnValue(of({ data: { id: 3 } }))
    };
    dialog.open.mockReturnValue(dialogRefSpy as any);

    component.openDialog(null);
    tick();

    expect(dialog.open).toHaveBeenCalledWith(CourseFormComponent, {
      width: '640px',
      disableClose: true,
      data: null
    });
    expect(courseService.getAllPages).toHaveBeenCalledWith(0, 10); // Attendre des nombres
  }));

  it('should open dialog for editing a course', fakeAsync(() => {
    const dialogRefSpy = {
      afterClosed: jest.fn().mockReturnValue(of({ data: { id: 1 } }))
    };
    dialog.open.mockReturnValue(dialogRefSpy as any);

    const course = mockCourses[0];
    component.openDialog(course);
    tick();

    expect(dialog.open).toHaveBeenCalledWith(CourseFormComponent, {
      width: '640px',
      disableClose: true,
      data: course
    });
    expect(courseService.getAllPages).toHaveBeenCalledWith(0, 10); // Attendre des nombres
  }));

  it('should delete a course after confirmation', fakeAsync(() => {
    const dialogRefSpy = {
      afterClosed: jest.fn().mockReturnValue(of(true))
    };
    dialog.open.mockReturnValue(dialogRefSpy as any);
    courseService.delete.mockReturnValue(of(null));

    component.delete(1);
    tick();

    expect(dialog.open).toHaveBeenCalledWith(ConfirmDialogComponent, {
      data: 'Are you sure you want to delete the record?'
    });
    expect(courseService.delete).toHaveBeenCalledWith(1);
    expect(courseService.getAllPages).toHaveBeenCalledWith(0, 10); // Attendre des nombres
  }));

  it('should not delete if confirmation is cancelled', fakeAsync(() => {
    const dialogRefSpy = {
      afterClosed: jest.fn().mockReturnValue(of(false))
    };
    dialog.open.mockReturnValue(dialogRefSpy as any);

    component.delete(1);
    tick();

    expect(courseService.delete).not.toHaveBeenCalled();
  }));

  it('should handle pagination', () => {
    const pageEvent = { pageIndex: 1, pageSize: 5, length: 2 };
    courseService.getAllPages.mockReturnValue(of(mockPageResponse));
    component.nextPage(pageEvent);

    expect(courseService.getAllPages).toHaveBeenCalledWith('1', '5'); // Attendre des chaînes
    expect(component.pageIndex).toBe(1);
    expect(component.pageSize).toBe(5);
  });
});