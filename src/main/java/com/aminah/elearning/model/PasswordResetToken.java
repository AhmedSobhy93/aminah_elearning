package com.aminah.elearning.model;
//import jakarta.persistence.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
//@Getter
//@Setter
@NoArgsConstructor
@AllArgsConstructor
//@Builder
//@Document(collection = "passwordResetTokens")
@Data
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;


    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime expiryDate;
    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }
}
