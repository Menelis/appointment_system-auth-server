package co.appointment.config;

import co.appointment.shared.model.CorsSettings;
import co.appointment.token.OAuth2PublicClientRefreshTokenGenerator;
import co.appointment.token.Oauth2AccessTokenCustomizer;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import static org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer.authorizationServer;


@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

   private final AppConfigProperties appConfigProperties;

    @Bean
    public RegisteredClientRepository registeredClientRepository(final JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }
    @Bean
    public OAuth2AuthorizationService authorizationService(final RegisteredClientRepository registeredClientRepository,
                                                           final JdbcTemplate jdbcTemplate) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(final HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = authorizationServer();
        return http
                .cors(Customizer.withDefaults())
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, authorizationServer -> authorizationServer
                        .oidc(Customizer.withDefaults())) // Enable Open ID
                .authorizeHttpRequests(authRequests -> authRequests.anyRequest().authenticated())
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        ))
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
                .build();
    }
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(final HttpSecurity http) throws Exception {
        return http
                .formLogin(Customizer.withDefaults())
                .authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())
                .build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsSettings cors = appConfigProperties.getCors();
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(cors.getAllowedOrigins());
        config.setAllowedHeaders(cors.getAllowedHeaders());
        config.setAllowedMethods(cors.getAllowedMethods());
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    OAuth2TokenGenerator<OAuth2Token> tokenGenerator(final JWKSource<SecurityContext> jwkSource,
                                                     final Oauth2AccessTokenCustomizer oauth2AccessTokenCustomizer) {
        JwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource);
        JwtGenerator jwtAccessTokenGenerator = new JwtGenerator(jwtEncoder);
        jwtAccessTokenGenerator.setJwtCustomizer(oauth2AccessTokenCustomizer);

        return new DelegatingOAuth2TokenGenerator(jwtAccessTokenGenerator,
                new OAuth2PublicClientRefreshTokenGenerator());
    }
}
