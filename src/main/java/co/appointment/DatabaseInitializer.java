package co.appointment;

import co.appointment.config.AppConfigProperties;
import co.appointment.entity.Role;
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
import org.springframework.stereotype.Component;

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


    @Override
    public void run(String... args) {
        seedRoles();
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
    private void seedClients() {
        List<AppConfigProperties.ClientSettings> clients = appConfigProperties.getRegisteredClients();
        for(AppConfigProperties.ClientSettings client : clients) {
            Set<AuthorizationGrantType> authorizationGrantTypes = client
                    .getAuthorizationGrantTypes()
                    .stream()
                    .map(AuthorizationGrantType::new)
                    .collect(Collectors.toSet());
            Set<ClientAuthenticationMethod> clientAuthenticationMethods = client
                    .getClientAuthenticationMethods()
                    .stream()
                    .map(ClientAuthenticationMethod::new)
                    .collect(Collectors.toSet());

            RegisteredClient.Builder registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId(client.getClientId())
                    .clientSecret(passwordEncoder.encode(client.getClientSecret()))
                    .authorizationGrantTypes(authTypes -> authTypes
                            .addAll(authorizationGrantTypes))
                    .clientAuthenticationMethods(clientAuths -> clientAuths
                            .addAll(clientAuthenticationMethods))
                    .redirectUris(redirectUris -> redirectUris.addAll(client.getRedirectUris()))
                    .scopes(scopes -> scopes.addAll(client.getScopes()));

            RegisteredClient existingclient = registeredClientRepository.findByClientId(client.getClientId());
            if(existingclient == null) {
                registeredClientRepository.save(registeredClient.build());
                return;
            }
            log.info("Client:{} already exists", client.getClientId());
        }
    }
}
