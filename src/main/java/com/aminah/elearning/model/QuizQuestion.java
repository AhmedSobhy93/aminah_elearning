package com.aminah.elearning.model;


//import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

//@Getter
//@Setter
//@Entity
@Document(collection = "quizQuestions")
@Data
public class QuizQuestion {

    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String question;

    // Comma-separated options (or store JSON for structured options)
    private List<String> options;

    private String answer;

//    @ManyToOne
//    @JoinColumn(name = "tutorial_id")
    private String tutorialId;
}
