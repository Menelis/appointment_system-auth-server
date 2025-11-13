package co.appointment.config;

import co.appointment.security.service.UserDetailsServiceImpl;
import co.appointment.shared.model.CorsSettings;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
import java.util.stream.Collectors;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final AppConfigProperties appConfigProperties;
    private final UserDetailsServiceImpl userDetailsService;

//    @Bean
//    public RegisteredClientRepository registeredClientRepository(final JdbcTemplate jdbcTemplate) {
//        return new JdbcRegisteredClientRepository(jdbcTemplate);
//    }
//    @Bean
//    public OAuth2AuthorizationService authorizationService(final JdbcTemplate jdbcTemplate,
//                                                           final RegisteredClientRepository registeredClientRepository) {
//        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
//    }
    @Bean
    public RegisteredClientRepository registeredClientRepository(final PasswordEncoder passwordEncoder) {
        AppConfigProperties.ClientSettings registeredClient = appConfigProperties.getRegisteredClients()
                .stream()
                .filter(AppConfigProperties.ClientSettings::isEnabled)
                .findFirst().orElse(null);
        if(registeredClient == null) {
            throw new RuntimeException("No registered Client found");
        }
        RegisteredClient.Builder registeredClientBuilder = RegisteredClient
                .withId(registeredClient.getClientId())
                .clientId(registeredClient.getClientId());
        // Public client does not require secret
        if(StringUtils.hasText(registeredClient.getClientSecret())) {
            registeredClientBuilder.clientSecret(passwordEncoder.encode(registeredClient.getClientSecret()));
        }
        registeredClientBuilder.clientAuthenticationMethods(clientAuthenticationMethods -> clientAuthenticationMethods
                .addAll(registeredClient.getClientAuthenticationMethods()
                        .stream()
                        .map(ClientAuthenticationMethod::new)
                        .collect(Collectors.toSet())));
        registeredClientBuilder.authorizationGrantTypes(authorizationGrantTypes -> authorizationGrantTypes
                .addAll(registeredClient.getAuthorizationGrantTypes().stream()
                        .map(AuthorizationGrantType::new)
                        .collect(Collectors.toSet())));
        registeredClientBuilder.redirectUris(redirectUris -> redirectUris
                .addAll(registeredClient.getRedirectUris()))
                .postLogoutRedirectUris(postLogoutUris -> postLogoutUris
                        .addAll(registeredClient.getPostLogoutRedirectUris()));
        registeredClientBuilder.scopes(scopes -> scopes
                .addAll(registeredClient.getScopes()));
        if(!StringUtils.hasText(registeredClient.getClientSecret())) {
            registeredClientBuilder.clientSettings(
                    ClientSettings.builder()
                            .requireProofKey(false)
                            .build()
            );
        }
        RegisteredClient client = registeredClientBuilder.build();
        log.info("Client is:{}", client);
        return new InMemoryRegisteredClientRepository(client);
    }
    /**
     * This first filter chain is for authorization server-specific configurations.
     * @param http HttpSecurity instance
     * @return {@link SecurityFilterChain }
     * @throws Exception exception
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(final HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();
        final String[] customExposedEndpoints = appConfigProperties.getCustomExposedEndpoints();

        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .securityMatcher(customExposedEndpoints)
                .csrf(csrf -> csrf.ignoringRequestMatchers(customExposedEndpoints))
                .with(authorizationServerConfigurer, (authorizationServer) -> authorizationServer
                        .oidc(Customizer.withDefaults()))// enable openid connect
                .authorizeHttpRequests((authorizeRequests) -> authorizeRequests
                        .requestMatchers(customExposedEndpoints).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        ))
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
                .build();
    }
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(final HttpSecurity http) throws Exception {
        return http
                .formLogin(Customizer.withDefaults()) // enable login
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(appConfigProperties.getWhiteList()).permitAll()
                        .anyRequest().authenticated())
                .build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }
    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }
//    @Bean
//    public AuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
//        authenticationProvider.setPasswordEncoder(passwordEncoder());
//        return authenticationProvider;
//    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsSettings cors = appConfigProperties.getCors();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(cors.getAllowedOrigins());
        corsConfiguration.setAllowedHeaders(cors.getAllowedHeaders());
        corsConfiguration.setAllowedMethods(cors.getAllowedMethods());
        corsConfiguration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return source;
    }
}
