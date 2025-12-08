package com.aminah.elearning.repository;

import com.aminah.elearning.model.Tutorial;
import com.aminah.elearning.model.TutorialType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
    Page<Tutorial> findBySectionId(Long sectionId, Pageable pageable);
    List<Tutorial> findBySectionIdOrderByOrderIndexAsc(Long sectionId);

//    Page<Tutorial> findBySectionId(Long sectionId);


    List<Tutorial> findByType(TutorialType type);

//    @Query("SELECT t FROM Tutorial t WHERE t.section.course.id = :courseId ORDER BY t.section.orderIndex, t.orderIndex")
//    List<Tutorial> getTutorialsByCourse(Long courseId);

}