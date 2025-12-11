package com.aminah.elearning.repository;

import com.aminah.elearning.model.Tutorial;
import com.aminah.elearning.model.TutorialProgress;
import com.aminah.elearning.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TutorialProgressRepository extends JpaRepository<TutorialProgress, Long> {

    Optional<TutorialProgress> findByUserAndTutorial(User user, Tutorial tutorial);

    @Query("""
        SELECT COUNT(tp) 
        FROM TutorialProgress tp 
        WHERE tp.user.id = :userId 
          AND tp.tutorial.course.id = :courseId 
          AND tp.completed = true
    """)
    long countCompletedTutorials(Long userId, Long courseId);
    Optional<TutorialProgress> findByUserIdAndTutorialId(Long userId, Long tutorialId);

    List<TutorialProgress> findByUserIdAndTutorialSectionCourseId(Long userId, Long courseId);

    int countByUserIdAndTutorialSectionCourseIdAndCompletedTrue(Long userId, Long courseId);
}
