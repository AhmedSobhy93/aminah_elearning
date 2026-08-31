package com.aminah.elearning.service;

import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Locale;


@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.userRepository = repo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Not found"));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }

    @Transactional
    public User userUpdate(String authenticatedUsername, User updatedUser) {
        User existingUser = userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!StringUtils.hasText(updatedUser.getEmail())) {
            throw new IllegalArgumentException("Email is required");
        }
        String normalizedEmail = updatedUser.getEmail().trim().toLowerCase(Locale.ROOT);
        userRepository.findByEmail(normalizedEmail)
                .filter(other -> !other.getId().equals(existingUser.getId()))
                .ifPresent(other -> {
                    throw new IllegalArgumentException("Email is already in use");
                });
        existingUser.setFullName(updatedUser.getFullName());
        if (StringUtils.hasText(updatedUser.getPassword())) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        existingUser.setEmail(normalizedEmail);
        return userRepository.save(existingUser);
    }

    // Fetch all users with pagination
    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable); // fetch all users
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public void enableUser(Long id) {
        User user = getUserById(id);
        user.setEnabled(true);
        userRepository.save(user);
    }

    public void updateUser(Long id, User updatedUser) {
        User user = getUserById(id);
//        user.setFullName(updatedUser.getFullName());
        user.setEmail(updatedUser.getEmail());
        user.setRole(updatedUser.getRole());
        user.setEnabled(updatedUser.isEnabled());
        userRepository.save(user);
    }
}
