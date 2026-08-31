package com.aminah.elearning.dto;

import com.aminah.elearning.model.QuizQuestion;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StudentQuizQuestionDto {
    private Long id;
    private String question;
    private List<String> options;

    public static StudentQuizQuestionDto from(QuizQuestion question) {
        return new StudentQuizQuestionDto(question.getId(), question.getQuestion(), question.getOptions());
    }
}
