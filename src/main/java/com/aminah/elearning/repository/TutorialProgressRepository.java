package com.aminah.elearning.repository;

import com.aminah.elearning.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TutorialProgressRepository extends JpaRepository<TutorialProgress, Long> {

//    Optional<TutorialProgress> findByUserAndTutorial(User user, Tutorial tutorial);

    Boolean existsByUserAndTutorial(User user, Tutorial tutorial);
    @Query("""
        SELECT COUNT(tp) 
        FROM TutorialProgress tp 
        WHERE tp.user.id = :userId 
          AND tp.tutorial.section.course.id = :courseId 
          AND tp.completed = true
    """)
    long countCompletedTutorials(Long userId, Long courseId);
    Optional<TutorialProgress> findByUserIdAndTutorialId(Long userId, Long tutorialId);

    List<TutorialProgress> findByUserIdAndTutorialSectionCourseId(Long userId, Long courseId);

    int countByUserIdAndTutorialSectionCourseIdAndCompletedTrue(Long userId, Long courseId);

    Optional<TutorialProgress> findByUserAndTutorial(User user, Tutorial tutorial);

    long countByUserAndTutorial_Section(User user, Section section);

    long countByUserAndTutorial_SectionAndCompletedTrue(User user, Section section);

    long countByUserAndTutorial_Section_Course(User user, Course course);

    long countByUserAndTutorial_Section_CourseAndCompletedTrue(User user, Course course);

    @Query("""
               select tp.tutorial.id
               from TutorialProgress tp
               where tp.user = :user
                 and tp.completed = true
            """)
    Set<Long> findCompletedTutorialIds(@Param("user") User user);

}
