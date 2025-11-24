package com.aminah.elearning.repository;

import com.aminah.elearning.model.VerificationToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
//import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends MongoRepository<VerificationToken,String>
{
    @Override
    Optional<VerificationToken> findById(String aLong);

    Optional<VerificationToken> findByToken(String token);
}
