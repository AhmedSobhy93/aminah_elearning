package com.aminah.elearning.repository;

import com.aminah.elearning.model.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByTutorialId(Long tutorialId);

    int countByTutorialId(Long tutorialId);

}
