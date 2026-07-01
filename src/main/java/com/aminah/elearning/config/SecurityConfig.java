package com.aminah.elearning.config;

import com.aminah.elearning.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final UserService userService;

    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.
                authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**", "/css/**","/js/**","/images/**","/webfonts/**","/","/profile/login","/profile/register","/profile/confirm","/verify", "/forgot-password", "/reset-password","/error","/contactus","/about").permitAll()
                        .requestMatchers("/payments/callback", "/payments/webhook").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/dr/**").hasRole("DR")
                        .requestMatchers("/student/**").hasRole("STUDENT")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.loginPage("/profile/login").loginProcessingUrl("/profile/login").defaultSuccessUrl("/", true).failureUrl("/profile/login?error=true").permitAll())
                .logout(logout -> logout.logoutUrl("/profile/logout").logoutSuccessUrl("/profile/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true).deleteCookies("JSESSIONID").permitAll())

                .csrf(csrf -> csrf.ignoringRequestMatchers("/payments/webhook"))
                .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())); // allow same-origin iframe embedding);
        return http.build();
    }
    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder encoder){
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setPasswordEncoder(encoder);
        p.setUserDetailsService(userService);
        return p;
    }
    // 👇 This is the key part
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

   }
