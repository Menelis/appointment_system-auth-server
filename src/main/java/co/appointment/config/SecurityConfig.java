package co.appointment.config;

import co.appointment.cache.CustomRequestCache;
import co.appointment.shared.model.CorsSettings;
import co.appointment.shared.security.UserDetailsImpl;
import co.appointment.token.OAuth2PublicClientRefreshTokenGenerator;
import co.appointment.token.Oauth2AccessTokenCustomizer;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppConfigProperties appConfigProperties;

    @Bean
    public RegisteredClientRepository registeredClientRepository(final JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(
            final RegisteredClientRepository registeredClientRepository, final JdbcTemplate jdbcTemplate) {
        JdbcOAuth2AuthorizationService authorizationService =
                new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);

        ClassLoader classLoader = JdbcOAuth2AuthorizationService.class.getClassLoader();

        // 1. Configure polymorphic type validator to safely allow your custom mix-ins/types
        BasicPolymorphicTypeValidator.Builder ptvBuilder = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(UserDetailsImpl.class)
                .allowIfSubType(java.util.UUID.class);
        // 2. Build the Jackson 3 JsonMapper using Spring Security 7's modules and validators
        JsonMapper jsonMapper = JsonMapper.builder()
                .addModules(SecurityJacksonModules.getModules(classLoader, ptvBuilder))
                .addModule(
                        new org.springframework.security.oauth2.server.authorization.jackson
                                .OAuth2AuthorizationServerJacksonModule())
                .build();

        JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationRowMapper rowMapper =
                new JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationRowMapper(
                        registeredClientRepository, jsonMapper);

        authorizationService.setAuthorizationRowMapper(rowMapper);

        return authorizationService;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(final HttpSecurity http) throws Exception {

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();

        return http.cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                                appConfigProperties.getCustomExposedEndpoints()) // Custom endpoints
                        .ignoringRequestMatchers(
                                authorizationServerConfigurer
                                        .getEndpointsMatcher())) // OAuth 2.0 and OpenID Connect protocol
                // endpoints exposed by the Authorization
                // Server.
                .securityMatchers(securityMatcher -> securityMatcher
                        .requestMatchers(
                                authorizationServerConfigurer
                                        .getEndpointsMatcher()) // OAuth 2.0 and OpenID Connect protocol endpoints
                        // exposed by the Authorization Server.
                        .requestMatchers(appConfigProperties.getCustomExposedEndpoints())) // Custom endpoints
                .with(
                        authorizationServerConfigurer,
                        authorizationServer -> authorizationServer.oidc(Customizer.withDefaults())) // Enable Open ID
                .authorizeHttpRequests(authRequests -> authRequests
                        .requestMatchers(appConfigProperties.getCustomExposedEndpoints())
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .exceptionHandling((exceptions) -> exceptions.defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"),
                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(final HttpSecurity http, final CustomRequestCache cache)
            throws Exception {
        return http.formLogin(Customizer.withDefaults())
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(appConfigProperties.getWhiteList())
                        .permitAll()
                        .anyRequest()
                        .permitAll())
                .requestCache(requestCacheConfigurer -> requestCacheConfigurer.requestCache(cache))
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
    OAuth2TokenGenerator<OAuth2Token> tokenGenerator(
            final JWKSource<SecurityContext> jwkSource, final Oauth2AccessTokenCustomizer oauth2AccessTokenCustomizer) {
        JwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource);
        JwtGenerator jwtAccessTokenGenerator = new JwtGenerator(jwtEncoder);
        jwtAccessTokenGenerator.setJwtCustomizer(oauth2AccessTokenCustomizer);

        return new DelegatingOAuth2TokenGenerator(
                jwtAccessTokenGenerator, new OAuth2PublicClientRefreshTokenGenerator());
    }
}
