package com.aminah.elearning.model;

//import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

//@Entity
//@Getter
//@Setter
//@NoArgsConstructor
@Document(collection = "tutorials")
@Data
public class Tutorial {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String title;
//    @Enumerated(EnumType.STRING)
    private TutorialType type;
    private String filePath; // relative path or external URL
    private int orderIndex = 0;

    private LocalDateTime uploadedAt = LocalDateTime.now();
//    @ManyToOne(fetch = FetchType.LAZY)
    private String courseId;

    private String userId;
//    @Lob
    private String articleContent; // ARTICLE content

//    @OneToMany(mappedBy = "tutorial", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<String> quizQuestionsIds;
}
