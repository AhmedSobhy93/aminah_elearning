package com.aminah.elearning.model;
//import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    private @ManyToOne private User student;
    @ManyToOne private Course course; id;

//    @Column(nullable = false, unique = true)
    private String token;

//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
    private String userId;

    private LocalDateTime expiryDate;
    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }
}
