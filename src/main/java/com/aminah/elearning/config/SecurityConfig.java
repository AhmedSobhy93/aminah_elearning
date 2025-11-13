package com.aminah.elearning.config;

import com.aminah.elearning.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UserService userService, PasswordEncoder passwordEncoder){this.userService=userService;
        this.passwordEncoder = passwordEncoder;
    }
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder);
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.
                authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**","/js/**","/images/**","/uploads/**","/","/profile/login","/profile/register","/profile/confirm","/verify", "/forgot-password", "/reset-password","/error","/contactus","/about").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/doctor/**").hasRole("DR")
                        .requestMatchers("/student/**").hasRole("STUDENT")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.loginPage("/profile/login").loginProcessingUrl("/profile/login").defaultSuccessUrl("/", true).failureUrl("/profile/login?error=true").permitAll())
                .logout(logout -> logout.logoutUrl("/profile/logout").logoutSuccessUrl("/profile/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true).deleteCookies("JSESSIONID").permitAll())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/uploads/**"));
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

