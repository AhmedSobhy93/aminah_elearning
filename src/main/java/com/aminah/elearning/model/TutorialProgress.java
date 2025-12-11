package com.aminah.elearning.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutorialProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutorial_id")
    private Tutorial tutorial;

    private boolean completed = false;

    private LocalDateTime completedAt;

    public TutorialProgress(User user, Tutorial tutorial, boolean completed) {
        this.user=user;
        this.tutorial=tutorial;
        this.completed=completed;
    }

//    public TutorialProgress(User user, Tutorial tutorial, boolean seen) {
//        this.user = user;
//        this.tutorial = tutorial;
//        this.seen = seen;
//    }
}
