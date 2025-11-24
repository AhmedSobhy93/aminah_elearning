package com.aminah.elearning.model;
//import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


//@Entity
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
@Document(collection = "passwordResetTokens")
@Data
public class PasswordResetToken {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private String id;

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
