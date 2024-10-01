package org.wishfoundation.chardhamcore.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.wishfoundation.chardhamcore.utils.EnvironmentConfigCommon;

import java.util.Arrays;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig {


    @Autowired
    JwtRequestFilter jwtRequestFilter;
    @Autowired
    CustomUserDetailsService customUserDetailsService;

    @Autowired
    CorsFilter corsFilter;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails admin = User.builder().username("admin").password(passwordEncoder().encode("admin")).roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public PasswordEncoder userPasswordEncoder() {
        return new BCryptPasswordEncoder(10);
    }


    public void configure(WebSecurity web) throws Exception {

    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        try {
            http.csrf((c) -> c.disable())
                    .authorizeHttpRequests((authz -> authz
                            .requestMatchers(EnvironmentConfigCommon.IGNORED_API_FOR_AUTH
                                    .toArray(new String[EnvironmentConfigCommon.IGNORED_API_FOR_AUTH.size()])).permitAll()
                            .anyRequest().permitAll()
                    )).sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS).disable())
                    .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                    .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                    .addFilterBefore(corsFilter, AbstractPreAuthenticatedProcessingFilter.class)
                    .httpBasic(Customizer.withDefaults());


            return http.build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authProvider() {
        return new CustomUserDetailsAuthenticationProvider(userPasswordEncoder(), customUserDetailsService);

    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}
