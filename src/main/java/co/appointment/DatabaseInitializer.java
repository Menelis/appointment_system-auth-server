package co.appointment;

import co.appointment.config.AppConfigProperties;
import co.appointment.entity.Role;
import co.appointment.repository.RoleRepository;
import co.appointment.shared.constant.RoleConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final RegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppConfigProperties appConfigProperties;


    @Override
    public void run(String... args) {
        seedRoles();
        seedRegisteredClients();
    }
    private void seedRegisteredClients() {
        List<RegisteredClient> registeredClients = appConfigProperties.getRegisteredClients()
                .stream()
                .map(this::mapToRegisteredClient)
                .toList();
        registeredClients.forEach(registeredClient -> {
            RegisteredClient optionalRegisteredClient = registeredClientRepository.findByClientId(registeredClient.getClientId());
            if(optionalRegisteredClient == null) {
                registeredClientRepository.save(registeredClient);
            }
        });
    }
    private void seedRoles() {
        Set<String> roles = Set.of(
                RoleConstants.ADMIN_ROLE,
                RoleConstants.USER_ROLE,
                RoleConstants.CUSTOMER_ROLE);
        roles.forEach(role -> {
            Optional<Role> dbRole = roleRepository.findByName(role);
            if(dbRole.isEmpty()) {
                roleRepository.save(new Role(role));
            }
        });
    }
    private RegisteredClient mapToRegisteredClient(final AppConfigProperties.RegisteredClientSetting client) {
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
}
