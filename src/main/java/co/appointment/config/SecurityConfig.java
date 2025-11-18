package co.appointment.config;

import co.appointment.shared.model.CorsSettings;
import co.appointment.shared.util.SharedObjectUtils;
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
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer.authorizationServer;


@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

   private final AppConfigProperties appConfigProperties;

    //TODO: Configure this to persist clients to db
    @Bean
    public RegisteredClientRepository registeredClientRepository(final PasswordEncoder passwordEncoder) {
        List<RegisteredClient> registeredClients = appConfigProperties.getRegisteredClients()
                .stream()
                .map(client -> mapToRegisteredClient(client, passwordEncoder))
                .toList();
        return new InMemoryRegisteredClientRepository(registeredClients);
    }
    private RegisteredClient mapToRegisteredClient(final AppConfigProperties.RegisteredClientSetting client,
                                                   final PasswordEncoder passwordEncoder) {
        RegisteredClient.Builder registeredClient = RegisteredClient
                .withId(UUID.randomUUID().toString());
        if(!StringUtils.hasText(client.getClientId())) {
            throw new IllegalArgumentException("Client Id is required");
        }
        if(client.getClientAuthenticationMethods().isEmpty()) {
            throw new IllegalArgumentException("At least one client authentication method is required");
        }
        if(StringUtils.hasText(client.getClientSecret())) {
           registeredClient.clientSecret(passwordEncoder.encode(client.getClientSecret()));
        }
        registeredClient.clientId(client.getClientId())
                .authorizationGrantTypes(authorizationGrantTypes -> authorizationGrantTypes
                        .addAll(client.getAuthorizationGrantTypes()
                                .stream()
                                .map(AuthorizationGrantType::new)
                                .collect(Collectors.toSet())))
                .clientAuthenticationMethods(clientAuthenticationMethods -> clientAuthenticationMethods
                        .addAll(client.getClientAuthenticationMethods()
                                .stream()
                                .map(ClientAuthenticationMethod::new)
                                .collect(Collectors.toSet())))
                .redirectUris(redirectUris -> redirectUris
                        .addAll(client.getRedirectUris()))
                .postLogoutRedirectUris(postLogOutRedirectUris -> postLogOutRedirectUris
                        .addAll(client.getPostLogoutRedirectUris()))
                .scopes(scopes -> scopes
                        .addAll(client.getScopes()))
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(client.getClientSettings().isRequireProofOfKey())
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(client.getTokenSetting().getAccessTokenTimeToLive()))
                        .refreshTokenTimeToLive(Duration.ofHours(client.getTokenSetting().getRefreshTokenTimeToLive()))
                        .build());
        return registeredClient.build();
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
