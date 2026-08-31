package com.aminah.elearning.repository;

import com.aminah.elearning.model.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByTutorialId(Long tutorialId);
    void deleteByTutorialId(Long id);
    int countByTutorialId(Long tutorialId);

    Optional<QuizQuestion> findByIdAndTutorialSectionCourseAuthorUsername(Long id, String username);

}
