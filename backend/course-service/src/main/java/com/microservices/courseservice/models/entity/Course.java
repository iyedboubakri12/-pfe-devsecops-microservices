package com.microservices.courseservice.models.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.microservices.commonexam.models.entity.Exam;
import com.microservices.commonstudent.models.entity.Student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Column(name = "created_At")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = true, name = "updated_at",
            updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updatedAt;

    @JsonIgnoreProperties(value = {"course"}, allowSetters = true)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "course", cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<CourseStudent> courseStudents = new ArrayList<>();

    // @OneToMany(fetch = FetchType.LAZY)
    @Transient
    private List<Student> students = new ArrayList<>();;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "courses_exams",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "exam_id")
    )
    private List<Exam> exams = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createdAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    public void addStudent(Student student) {
        this.students.add(student);
    }

    public void removeStudents(Student student) {
        this.students.remove(student);
    }

    public void addExams(Exam exam) {
        this.exams.add(exam);
    }

    public void removeExams(Exam exam) {
        this.exams.remove(exam);
    }

    public void addCourseStudent(CourseStudent courseStudent) {
        this.courseStudents.add(courseStudent);
    }

    public void removeCourseStudent(CourseStudent courseStudent) {
        this.courseStudents.remove(courseStudent);
    }
}
