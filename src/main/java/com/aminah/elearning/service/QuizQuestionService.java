package com.aminah.elearning.service;

import com.aminah.elearning.model.QuizQuestion;
import com.aminah.elearning.model.Tutorial;
import com.aminah.elearning.repository.QuizQuestionRepository;
import com.aminah.elearning.repository.TutorialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizQuestionService {

    private final QuizQuestionRepository quizRepo;
    private final TutorialRepository tutorialRepository;

    public List<QuizQuestion> getQuestions(Long tutorialId) {
        Tutorial t = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new RuntimeException("Tutorial not found"));
        return t.getQuizQuestions();
    }

    public QuizQuestion save(QuizQuestion q) {
        return quizRepo.save(q);
    }

    public void delete(Long id) {
        quizRepo.deleteById(id);
    }
}
