package com.aminah.elearning.service;

import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.CourseEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class StudentService {

    private final CourseEnrollmentRepository enrollmentRepository;

    public Page<CourseEnrollment> getEnrolledCourses(User student, int page, int size) {
        return enrollmentRepository.findByUserId(student.getId(), PageRequest.of(page, size));
    }

}