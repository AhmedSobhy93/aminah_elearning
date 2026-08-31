package com.aminah.elearning.dto;

import com.aminah.elearning.model.QuizQuestion;
import com.aminah.elearning.model.Tutorial;
import com.aminah.elearning.model.TutorialType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StudentTutorialDtoTest {

    @Test
    void studentQuizJsonOmitsCorrectAnswer() throws Exception {
        Tutorial tutorial = new Tutorial();
        tutorial.setId(3L);
        tutorial.setType(TutorialType.QUIZ);
        QuizQuestion question = new QuizQuestion();
        question.setId(8L);
        question.setQuestion("Question?");
        question.setOptions(List.of("A", "B"));
        question.setCorrectOptionIndex(1);
        tutorial.setQuizQuestions(new ArrayList<>(List.of(question)));

        String json = new ObjectMapper().writeValueAsString(StudentTutorialDto.from(tutorial));

        assertThat(json).contains("Question?").contains("\"id\":8");
        assertThat(json).doesNotContain("correctOptionIndex").doesNotContain("correctAnswer");
    }
}
