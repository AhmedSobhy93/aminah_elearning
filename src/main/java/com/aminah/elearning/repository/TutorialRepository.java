package com.aminah.elearning.repository;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.Tutorial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TutorialRepository extends MongoRepository<Tutorial, String> {
    List<Tutorial> findByCourseIdOrderByOrderIndex(String courseId);
//    List<Tutorial> findByCourseOrderByOrderIndexAsc(Course course);
//    List<Tutorial> findAllByCourseIdOrderByOrderIndexAsc(Course course);
    Page<Tutorial> findByCourseId(String courseId, Pageable pageable);
    List<Tutorial> findByCourseId(String courseId);
}