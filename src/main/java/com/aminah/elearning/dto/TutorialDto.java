package com.aminah.elearning.dto;

import com.aminah.elearning.model.QuizQuestion;
import com.aminah.elearning.model.Tutorial;
import com.aminah.elearning.model.TutorialType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutorialDto {
    private Long id;
    private String title;
    private TutorialType type;
    private String status;
    private int orderIndex;
    private String filePath;
    private String articleContent;
    private LocalDateTime uploadedAt; // <--- add this
    private List<QuizQuestionDto> quizQuestions; // for QUIZ
    private Long sectionId;

    public static TutorialDto from(Tutorial t) {
        TutorialDto dto = new TutorialDto();
        dto.setId(t.getId());
        dto.setTitle(t.getTitle());
        dto.setType(t.getType() != null ? t.getType() : TutorialType.valueOf(""));
        dto.setStatus(t.getStatus() != null ? t.getStatus() : "");
        dto.setOrderIndex(t.getOrderIndex());
        dto.setFilePath(t.getFilePath() != null ? t.getFilePath() : "");
        dto.setArticleContent(t.getArticleContent() != null ? t.getArticleContent() : "");
        dto.setUploadedAt(t.getUploadedAt() != null ? t.getUploadedAt() : LocalDateTime.now());
        dto.setSectionId(t.getSection().getId());
        // Map quiz questions safely
        List<QuizQuestionDto> quizDtos = new ArrayList<>();
        if (t.getQuizQuestions() != null) {
            t.getQuizQuestions().forEach(q -> quizDtos.add(QuizQuestionDto.from(q)));
        }
        dto.setQuizQuestions(quizDtos);

        return dto;
    }


}

