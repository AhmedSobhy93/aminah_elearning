package com.aminah.elearning.repository;

import com.aminah.elearning.model.Tutorial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
    Page<Tutorial> findByCourseId(Long courseId, Pageable pageable);

    List<Tutorial> findByCourseId(Long courseId);
}