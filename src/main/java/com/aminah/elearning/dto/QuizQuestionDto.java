package com.aminah.elearning.dto;

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
}
