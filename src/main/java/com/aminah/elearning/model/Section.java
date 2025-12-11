package com.aminah.elearning.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private int durationMinutes;   // auto-summed later

    private boolean locked = false;

    private boolean unlockOnFinishPrevious = false;

    private int orderIndex;

    @ManyToOne
    private Course course;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<Tutorial> tutorials = new ArrayList<>();

    public void addTutorial(Tutorial tutorial){
        tutorial.setSection(this);
        tutorial.setOrderIndex(tutorials.size() + 1);
        tutorials.add(tutorial);
    }
}