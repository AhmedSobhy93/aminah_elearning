package com.aminah.elearning.repository;

import com.aminah.elearning.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken,Long>
{
    @Override
    Optional<VerificationToken> findById(Long aLong);

    Optional<VerificationToken> findByToken(String token);
}
