package com.aminah.elearning.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Data
public class Tutorial {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Enumerated(EnumType.STRING)
    private TutorialType type;
    private String filePath; // relative path or external URL
    private int orderIndex = 0;

    private LocalDateTime uploadedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "course_id")
//    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String articleContent; // ARTICLE content

    @OneToMany(mappedBy = "tutorial", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizQuestion> quizQuestions = new ArrayList<>();

    public Tutorial(Long tutorialId) {
        this.id = tutorialId;
    }

    public void addQuizQuestion(QuizQuestion question) {
        quizQuestions.add(question);
        question.setTutorial(this);
    }
    @Column(nullable = false)
    private boolean preview = false;

    @Transient
    private boolean read;

}
