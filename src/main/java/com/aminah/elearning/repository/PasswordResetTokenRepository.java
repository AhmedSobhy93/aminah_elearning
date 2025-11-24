package com.aminah.elearning.repository;

import com.aminah.elearning.model.PasswordResetToken;
//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken,String> {
    Optional<PasswordResetToken> findByToken(String token);
}
