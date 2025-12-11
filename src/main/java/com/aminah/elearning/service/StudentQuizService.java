package com.aminah.elearning.service;

import com.aminah.elearning.model.QuizQuestion;
import com.aminah.elearning.repository.QuizQuestionRepository;
import com.aminah.elearning.repository.TutorialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentQuizService {

    private final TutorialRepository tutorialRepo;
    private final QuizQuestionRepository quizRepo;

    public List<QuizQuestion> getQuiz(Long tutorialId) {
        return quizRepo.findByTutorialId(tutorialId);
    }

    public boolean submitQuiz(Long tutorialId, List<Integer> answers) {

        List<QuizQuestion> questions = quizRepo.findByTutorialId(tutorialId);

        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).getCorrectOptionIndex() != answers.get(i))
                return false;
        }
        return true;
    }
}
