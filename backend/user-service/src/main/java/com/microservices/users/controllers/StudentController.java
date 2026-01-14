package com.microservices.users.controllers;

import java.io.IOException;
import java.util.List;

import javax.validation.Valid;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.microservices.commonstudent.models.entity.Student;
import com.microservices.users.services.StudentService;

@RestController
@CrossOrigin(origins = "*", allowCredentials = "false")
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/students-by-course")
    public ResponseEntity<?> getStudentsByCourse(@RequestParam("ids") List<Long> ids) {
        return ResponseEntity.ok(studentService.findAllById(ids));
    }

    @GetMapping("/page/{page}/{size}")
    public ResponseEntity<Page<Student>> index(@PathVariable Integer page, @PathVariable Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(studentService.findAllPage(pageable));
    }

    @GetMapping("/page/{page}/{size}/{text}")
    public ResponseEntity<Page<Student>> indexPageWithText(@PathVariable Integer page, @PathVariable Integer size,
                                                           @PathVariable String text) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(studentService.findByNameAndLastNameWithPageable(text, pageable));
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Student student,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return validate(bindingResult);
        }
        Student studentBD = studentService.findById(id);
        studentBD.setName(student.getName());
        studentBD.setLastName(student.getLastName());
        studentBD.setEmail(student.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.update(studentBD));
    }

    @GetMapping("/filter/{text}")
    public ResponseEntity<?> filter(@PathVariable String text) {
        return ResponseEntity.ok(studentService.findByNameAndLastName(text));
    }

    @PostMapping("/create-with-image")
    public ResponseEntity<?> createWithImage(@Valid Student student, BindingResult bindingResult,
                                             @RequestParam("file") MultipartFile file) throws IOException {
        if (bindingResult.hasErrors()) {
            return validate(bindingResult);
        }
        if (!file.isEmpty()) {
            student.setImage(file.getBytes());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.save(student));
    }

    @PutMapping("/{id}/update-with-image")
    public ResponseEntity<?> updateWithImage(@PathVariable Long id, @Valid Student student,
                                             BindingResult bindingResult,
                                             @RequestParam("file") MultipartFile file) throws IOException {
        if (bindingResult.hasErrors()) {
            return validate(bindingResult);
        }
        Student studentBD = studentService.findById(id);
        studentBD.setName(student.getName());
        studentBD.setLastName(student.getLastName());
        studentBD.setEmail(student.getEmail());
        if (!file.isEmpty()) {
            studentBD.setImage(file.getBytes());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.update(studentBD));
    }

    @GetMapping("/{id}/uploads/image")
    public ResponseEntity<?> viewImage(@PathVariable Long id) {
        Student studentBD = studentService.findById(id);
        if (studentBD.getImage() == null) {
            return ResponseEntity.notFound().build();
        }
        Resource image = new ByteArrayResource(studentBD.getImage());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(image);
    }

    private ResponseEntity<?> validate(BindingResult bindingResult) {
        return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
    }
}
