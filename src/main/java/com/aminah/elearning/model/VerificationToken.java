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
@Document(collection = "verificationTokens")
@Data
public class VerificationToken {

    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String token;

//    @ManyToOne
//    @JoinColumn(nullable = false, name = "user_id")
    private String userId;

    private LocalDateTime expiryDate;
    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }
}