package com.aminah.elearning.repository;

import com.aminah.elearning.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByCourseIdOrderByOrderIndexAsc(Long courseId);
    List<Section> findByCourseIdOrderByIdAsc(Long courseId);

    Optional<Section> findByIdAndCourseAuthorUsername(Long id, String username);

}
