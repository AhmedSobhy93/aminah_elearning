package com.aminah.elearning.repository;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.Tutorial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
    List<Tutorial> findByCourseIdOrderByOrderIndex(Long courseId);
    List<Tutorial> findByCourseOrderByOrderIndexAsc(Course course);
    List<Tutorial> findAllByCourseOrderByOrderIndexAsc(Course course);
    Page<Tutorial> findByCourseId(Long courseId, Pageable pageable);

}