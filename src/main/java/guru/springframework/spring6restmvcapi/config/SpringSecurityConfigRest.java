package guru.springframework.spring6restmvcapi.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("!test-disabled-security")
public class SpringSecurityConfigRest {

    private static final List<String> ALLOWED_HEADERS = List.of("*");
    private static final List<String> ALLOWED_METHODS = List.of("POST", "GET", "PUT", "OPTIONS", "DELETE", "PATCH");

    private final AllowedOriginConfig allowedOriginConfig;

    @PostConstruct
    public void init() {
        log.info("### Allowed origins: {}", allowedOriginConfig);
    }
    
    @Bean
    @Order(99)
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
            .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                .anyRequest().permitAll())
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**") // CSRF-Schutz für H2-Console deaktivieren
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable) 
            );
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(allowedOriginConfig.getAllowedOrigins());
        configuration.setAllowedMethods(ALLOWED_METHODS);
        configuration.setAllowedHeaders(ALLOWED_HEADERS);
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Component
    @ConfigurationProperties(prefix = "security.cors")
    @Data
    public static class AllowedOriginConfig {
        private List<String> allowedOrigins;
    }

}
