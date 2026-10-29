package com.aminah.elearning.repository;

import com.aminah.elearning.model.User;
import com.aminah.elearning.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @org.springframework.data.jpa.repository.Modifying(flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query("update User u set u.password = :password where u.id = :id")
    int updatePasswordHash(@org.springframework.data.repository.query.Param("id") Long id,
                           @org.springframework.data.repository.query.Param("password") String password);
    long countByRole(Role role);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from User u where u.role = :role order by u.id")
    java.util.List<User> findByRoleForUpdate(@org.springframework.data.repository.query.Param("role") Role role);

}
