//package com.aminah.elearning.model;
//
//
////import jakarta.persistence.*;
//import jakarta.persistence.*;
//import lombok.Data;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.util.List;
//
//@Entity
//@Getter
//@Setter
//@Data
//public class QuizQuestion {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String question;
//
//    // Comma-separated options (or store JSON for structured options)
//    @ElementCollection
//    private List<String> options;
//
//    private String answer;
//
//    @ManyToOne
//    @JoinColumn(name = "tutorial_id")
//    private Tutorial tutorial; // link back to Tutorial
//}

package com.aminah.elearning.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;

    @ElementCollection
    @CollectionTable(name = "quiz_options", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option_text")
    private List<String> options;

    private int correctOptionIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutorial_id")
    private Tutorial tutorial;
}
