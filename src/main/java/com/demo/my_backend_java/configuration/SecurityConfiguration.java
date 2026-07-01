package com.demo.my_backend_java.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.core.userdetails.User;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import com.demo.my_backend_java.service.UserService;


@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final UserService userService;

    public SecurityConfiguration(UserService userService) {
        this.userService = userService;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        httpSecurity.csrf(csrf -> csrf.disable());

        httpSecurity.sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        httpSecurity.authorizeHttpRequests(request -> request
            //public
            .requestMatchers("/api/user/register", "/api/user/login").permitAll()

            //admin
            .requestMatchers("/api/user/delete/**", "/api/mark/delete/**", "/api/friendships/delete-friend/**").hasRole("ADMIN")

            //user 
            .anyRequest().authenticated()
        );

        return httpSecurity.build();
    }

    //testing in memory (only on local machine)
    // @Bean
    // public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    //     UserDetails admin = User.builder()
    //         .username("admin")
    //         .password(passwordEncoder.encode("admin-pass"))
    //         .roles("ADMIN", "USER")
    //         .build();

    //     UserDetails user = User.builder()
    //         .username("user")
    //         .password(passwordEncoder.encode("user-pass"))
    //         .roles("USER")
    //         .build();

    //     return new InMemoryUserDetailsManager(admin, user);
    // }

    @Bean 
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(); 
        provider.
        return provider;

    }

    @Bean 
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
