package com.aminah.elearning.dto;

import com.aminah.elearning.model.QuizQuestion;
import com.aminah.elearning.model.Tutorial;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionDto {
    private Long id;
    private String question;
    private List<String> options;
    private int correctOptionIndex;

    public static QuizQuestionDto from(com.aminah.elearning.model.QuizQuestion q) {
        QuizQuestionDto dto = new QuizQuestionDto();
        dto.setId(q.getId());
        dto.setQuestion(q.getQuestion());
        dto.setOptions(q.getOptions()); // Assuming getOptions() returns List<String>
        dto.setCorrectOptionIndex(q.getCorrectOptionIndex());
        return dto;
    }

    // DTO → Entity
    public QuizQuestion toEntity(Tutorial tutorial) {
        QuizQuestion q = new QuizQuestion();
        q.setQuestion(this.question);
        q.setOptions(this.options);
        q.setCorrectOptionIndex(this.correctOptionIndex);
        q.setTutorial(tutorial);
        return q;
    }

}
