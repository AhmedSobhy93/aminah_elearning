//package com.aminah.elearning.model;
//
/// /import jakarta.persistence.*;
//import jakarta.persistence.*;
//import lombok.*;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.Set;
//
//
//@Entity
//@Table(name = "users")
////@Getter
////@Setter
//@NoArgsConstructor
//@AllArgsConstructor
////@Builder
////@ToString(exclude = "roles")
////@EqualsAndHashCode(exclude = "roles")
////@Document(collection = "users")
//@Data
//public class User  {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(unique=true, nullable=false)
//    private String username;
//
//    @Column(nullable=false)
//    private String password;
//
//    @Column(unique=true,nullable=false)
//    private String email;
//
//    @Column(nullable=false)
//    private String fullName;
//
//    private boolean enabled = false; // becomes true after verification
//
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<VerificationToken> verificationTokens;
//
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<PasswordResetToken> resetTokens;
//
////    @OneToMany(mappedBy = "user")
////    private Set<VerificationToken> tokens;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable=false)
//    private Role role;
//
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<CourseEnrollment> enrollments = new ArrayList<>();
//
//}

package com.aminah.elearning.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String email;

    private String fullName;

    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Courses authored by this user
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Course> coursesAuthored = new ArrayList<>();

    // Enrollments of this user
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseEnrollment> enrollments = new ArrayList<>();

    private boolean enabled = false;

    public User(Long userId) {
        this.id = userId;
    }
}
