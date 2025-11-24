package com.aminah.elearning.model;

//import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


//@Entity
//@Table(name = "users")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@ToString(exclude = "roles")
//@EqualsAndHashCode(exclude = "roles")
@Document(collection = "users")
@Data
public class User {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

//    @Column(unique=true, nullable=false)
    private String username;

//    @Column(nullable=false)
    private String password;

//    @Column(unique=true,nullable=false)
    private String email;

//    @Column(nullable=false)
    private String fullName;

    private boolean enabled = false; // becomes true after verification

//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VerificationToken> verificationTokens;

//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PasswordResetToken> resetTokens;

//    @OneToMany(mappedBy = "user")
//    private Set<VerificationToken> tokens;

    private Role role;


}
