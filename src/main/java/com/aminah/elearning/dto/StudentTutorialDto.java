package com.aminah.elearning.dto;

import com.aminah.elearning.model.Tutorial;
import com.aminah.elearning.model.TutorialType;
import lombok.Data;

import java.util.List;

@Data
public class StudentTutorialDto {
    private Long id;
    private String title;
    private TutorialType type;
    private String filePath;
    private String articleContent;
    private List<StudentQuizQuestionDto> quizQuestions;

    public static StudentTutorialDto from(Tutorial tutorial) {
        StudentTutorialDto dto = new StudentTutorialDto();
        dto.setId(tutorial.getId());
        dto.setTitle(tutorial.getTitle());
        dto.setType(tutorial.getType());
        dto.setFilePath(tutorial.getFilePath() == null ? "" : tutorial.getFilePath());
        dto.setArticleContent(tutorial.getArticleContent() == null ? "" : tutorial.getArticleContent());
        dto.setQuizQuestions(tutorial.getQuizQuestions() == null
                ? List.of()
                : tutorial.getQuizQuestions().stream().map(StudentQuizQuestionDto::from).toList());
        return dto;
    }
}
