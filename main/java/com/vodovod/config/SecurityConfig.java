package com.vodovod.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import java.net.URI;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            RequestCache requestCache = new HttpSessionRequestCache();
            SavedRequest savedRequest = requestCache.getRequest(request, response);

            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isSuperAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

            // Super admin always lands on organizations, regardless of any saved request
            if (isSuperAdmin) {
                response.sendRedirect("/organizations");
                return;
            }

            if (savedRequest != null) {
                String redirectUrl = savedRequest.getRedirectUrl();
                String path = URI.create(redirectUrl).getPath();
                boolean adminOnlyTarget = "/dashboard".equals(path)
                        || path.startsWith("/admin/")
                        || path.startsWith("/users/")
                        || path.startsWith("/readings/")
                        || path.startsWith("/bills/")
                        || path.startsWith("/payments/")
                        || path.startsWith("/settings/");

                if (adminOnlyTarget && !isAdmin) {
                    response.sendRedirect("/my-bills");
                    return;
                }

                response.sendRedirect(redirectUrl);
                return;
            }

            response.sendRedirect(isAdmin ? "/dashboard" : "/my-bills");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/css/**", "/js/**", "/images/**", "/h2-console/**").permitAll()
                .requestMatchers("/organizations/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/dashboard", "/users/**", "/readings/**", "/bills/**", "/payments/**", "/settings/**").hasRole("ADMIN")
                .requestMatchers("/my-bills/**", "/my-account").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(authenticationSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()) // Za H2 console
            );

        return http.build();
    }
}