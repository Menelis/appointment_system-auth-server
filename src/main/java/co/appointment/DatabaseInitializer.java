package co.appointment;

import co.appointment.config.AppConfigProperties;
import co.appointment.entity.Role;
import co.appointment.entity.User;
import co.appointment.entity.UserRole;
import co.appointment.entity.UserRoleKey;
import co.appointment.repository.RoleRepository;
import co.appointment.repository.UserRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final RegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppConfigProperties appConfigProperties;
    private final UserRepository userRepository;


    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedRegisteredClients();
        //TODO: Remove this code when deploying to production and separate manage user roles functionality
        seedAdminUser();
    }
    public void seedAdminUser() {
        Role adminRole = roleRepository.findByName(RoleConstants.ADMIN_ROLE).orElse(null);
        if(adminRole == null) {
            return;
        }
        User user = new User("user", "admin", "user@admin.com", "12344", passwordEncoder.encode("useradmin$$$1234"));
        user.setEmailVerified(true);
        user.addUserRole(adminRole);

        Optional<User> userOptional = userRepository.findByEmail(user.getEmail());
        if(userOptional.isPresent()) {
            return;
        }
        userRepository.saveAndFlush(user);
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
    private Optional<Role> getRoleByName(final String name) {
        return roleRepository.findByName(name);
    }
    private void seedRoles() {
        Set<String> roles = Set.of(
                RoleConstants.ADMIN_ROLE,
                RoleConstants.USER_ROLE,
                RoleConstants.CUSTOMER_ROLE);
        roles.forEach(role -> {
            Optional<Role> dbRole = getRoleByName(role);
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
        String forClientIdMessage = String.format("%s for client %s", client.getClientId(), client.getClientSecret());
        if(client.getClientAuthenticationMethods().isEmpty()) {
            throw new IllegalArgumentException(String.format(forClientIdMessage, "At least one client authentication method is required"));
        }
        if(client.getAuthorizationGrantTypes().isEmpty()) {
            throw new IllegalArgumentException(String.format(forClientIdMessage, "At least one authorization grant type is required"));
        }
        if(client.getRedirectUris().isEmpty()) {
            throw new IllegalArgumentException(String.format(forClientIdMessage, "At least one redirect URI is required"));
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
                        .addAll(client.getScopes()));
        if(client.getClientSettings() != null) {
            registeredClient.clientSettings(ClientSettings.builder()
                    .requireProofKey(client.getClientSettings().isRequireProofOfKey())
                    .build());
        }
        if(client.getTokenSetting() != null) {
            registeredClient.tokenSettings(TokenSettings.builder()
                    .accessTokenTimeToLive(Duration.ofMinutes(client.getTokenSetting().getAccessTokenTimeToLive()))
                    .refreshTokenTimeToLive(Duration.ofDays(client.getTokenSetting().getRefreshTokenTimeToLive()))
                    .build());
        }
        return registeredClient.build();
    }
}
