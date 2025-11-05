package com.aminah.elearning.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Tutorial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Enumerated(EnumType.STRING)
    private TutorialType type;
    private String filePath; // relative path or external URL
    private int orderIndex = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    private Course course;
}
