package co.appointment.service;

import static co.appointment.shared.constant.EventTypeConstants.PASSWORD_RESET_EVENT;
import static co.appointment.shared.constant.EventTypeConstants.VERIFY_EMAIL_EVENT;

import co.appointment.config.AppConfigProperties;
import co.appointment.enums.TokenType;
import co.appointment.kafka.KafkaProducerService;
import co.appointment.payload.request.PasswordResetRequest;
import co.appointment.payload.request.SignUpRequest;
import co.appointment.persistence.RoleEntity;
import co.appointment.persistence.RoleRepository;
import co.appointment.persistence.UserEntity;
import co.appointment.persistence.UserRepository;
import co.appointment.persistence.UserRoleEntity;
import co.appointment.persistence.UserRoleRepository;
import co.appointment.persistence.VerificationTokenEntity;
import co.appointment.shared.constant.RoleConstants;
import co.appointment.shared.dto.ServiceResponseDto;
import co.appointment.shared.event.AuthEvent;
import co.appointment.shared.event.DomainEventMessage;
import co.appointment.shared.exception.ServiceException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    // Repositories
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    // Services
    private final AppConfigProperties configProperties;
    private final VerificationTokenService verificationTokenService;
    private final KafkaProducerService kafkaProducerService;

    public Optional<ServiceResponseDto> registerUser(final SignUpRequest signUpRequest, final UUID xCorrelationId) {

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new ServiceException(
                    HttpStatus.CONFLICT,
                    ServiceResponseDto.builder()
                            .errorDescription("Email %s is already in use".formatted(signUpRequest.getEmail()))
                            .build());
        }

        UserEntity user = UserEntity.builder()
                .firstName(signUpRequest.getFirstName())
                .lastName(signUpRequest.getLastName())
                .email(signUpRequest.getEmail())
                .contactNo(signUpRequest.getContactNo())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .build();

        // Set default role
        RoleEntity customerRole = roleRepository
                .findByName(RoleConstants.CUSTOMER_ROLE)
                .orElseThrow(() -> new RuntimeException(
                        String.format("No role with name: %s was found in the db", RoleConstants.CUSTOMER_ROLE)));

        userRepository.save(user);

        userRoleRepository.save(UserRoleEntity.builder()
                .userId(user.getId())
                .roleId(customerRole.getId())
                .build());

        createVerificationToken(user, TokenType.EMAIL_VERIFICATION_TOKEN);

        publishEvent(
                getDomainEventMessage(
                        AuthEvent.builder()
                                .email(user.getEmail())
                                .tokenType(TokenType.EMAIL_VERIFICATION_TOKEN.name())
                                .build(),
                        xCorrelationId,
                        VERIFY_EMAIL_EVENT),
                Map.of());

        return Optional.of(ServiceResponseDto.builder()
                .success(true)
                .correlationId(xCorrelationId)
                .build());
    }

    DomainEventMessage<AuthEvent> getDomainEventMessage(
            final AuthEvent authEvent, final UUID xCorrelationId, final String eventType) {

        return DomainEventMessage.<AuthEvent>builder()
                .event(authEvent)
                .correlationId(xCorrelationId.toString())
                .eventType(eventType)
                .build();
    }

    void publishEvent(final DomainEventMessage<AuthEvent> domainEventMessage, final Map<String, Object> headers) {
        String topic = configProperties.getKafka().getTopic().getOutgoing().getNotification();
        if (log.isTraceEnabled()) {
            log.trace("Event=[{}] to be sent to topic=[{}]", domainEventMessage, topic);
        }
        kafkaProducerService.publish(topic, domainEventMessage, headers);
    }

    public Optional<ServiceResponseDto> confirmEmail(
            final String email, final String token, final UUID xCorrelationId) {

        VerificationTokenEntity verificationToken = verificationTokenService.getVerificationToken(token);

        if (verificationToken == null) {
            throw new ServiceException(
                    HttpStatus.BAD_REQUEST,
                    ServiceResponseDto.builder()
                            .success(false)
                            .errorDescription("Invalid verification token")
                            .correlationId(xCorrelationId)
                            .build());
        }

        Optional<UserEntity> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            throw new ServiceException(HttpStatus.NO_CONTENT);
        }

        if (verificationTokenService.tokenExpired(verificationToken)) {

            createVerificationToken(user.get(), TokenType.EMAIL_VERIFICATION_TOKEN);

            publishEvent(
                    getDomainEventMessage(
                            AuthEvent.builder()
                                    .email(user.get().getEmail())
                                    .tokenType(TokenType.EMAIL_VERIFICATION_TOKEN.name())
                                    .build(),
                            xCorrelationId,
                            VERIFY_EMAIL_EVENT),
                    Map.of());

            throw new ServiceException(
                    HttpStatus.BAD_REQUEST,
                    ServiceResponseDto.builder()
                            .success(false)
                            .errorDescription(
                                    "The verification token has expired. Please check your email for new registration confirmation.")
                            .correlationId(xCorrelationId)
                            .build());
        }
        // Confirm email verified
        UserEntity userEntity = user.get();
        userEntity.setEmailVerified(true);
        userRepository.save(userEntity);

        return Optional.of(ServiceResponseDto.builder()
                .success(true)
                .correlationId(xCorrelationId)
                .build());
    }

    public Optional<ServiceResponseDto> forgotPassword(final String email, final UUID xCorrelationId) {
        Optional<UserEntity> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            throw new ServiceException(
                    HttpStatus.BAD_REQUEST,
                    ServiceResponseDto.builder()
                            .success(false)
                            .correlationId(xCorrelationId)
                            .errorDescription("No user was found with the supplied email")
                            .build());
        }

        createVerificationToken(user.get(), TokenType.PASSWORD_RESET_TOKEN);

        publishEvent(
                getDomainEventMessage(
                        AuthEvent.builder()
                                .email(user.get().getEmail())
                                .tokenType(TokenType.PASSWORD_RESET_TOKEN.name())
                                .build(),
                        xCorrelationId,
                        PASSWORD_RESET_EVENT),
                Map.of());

        return Optional.of(ServiceResponseDto.builder()
                .success(true)
                .correlationId(xCorrelationId)
                .successMessage("Password reset email has been sent to your email")
                .build());
    }

    void createVerificationToken(final UserEntity user, final TokenType tokenType) {
        VerificationTokenEntity verificationToken = verificationTokenService.createVerificationToken(
                VerificationTokenEntity.builder()
                        .userId(user.getId())
                        .tokenType(tokenType.name())
                        .build(),
                user.getEmail());
        if (verificationToken == null) {
            throw new RuntimeException("Verification Token could not be created for user id: " + user.getId());
        }
    }

    public Optional<ServiceResponseDto> passwordReset(
            final PasswordResetRequest passwordResetRequest, final UUID xCorrelationId) {

        VerificationTokenEntity verificationToken =
                verificationTokenService.getVerificationToken(passwordResetRequest.getToken());

        if (verificationToken == null) {
            throw new ServiceException(
                    HttpStatus.BAD_REQUEST,
                    ServiceResponseDto.builder()
                            .correlationId(xCorrelationId)
                            .success(false)
                            .errorDescription("Invalid verification token")
                            .build());
        }
        Optional<UserEntity> user = userRepository.findByEmail(passwordResetRequest.getEmail());
        if (user.isEmpty()) {
            throw new ServiceException(HttpStatus.NO_CONTENT);
            // return new ApiResponse<>(false, "No user was found with the supplied email");
        }
        if (verificationTokenService.tokenExpired(verificationToken)) {
            createVerificationToken(user.get(), TokenType.PASSWORD_RESET_TOKEN);
            publishEvent(
                    getDomainEventMessage(
                            AuthEvent.builder()
                                    .email(user.get().getEmail())
                                    .tokenType(TokenType.PASSWORD_RESET_TOKEN.name())
                                    .build(),
                            xCorrelationId,
                            PASSWORD_RESET_EVENT),
                    Map.of());
            throw new ServiceException(
                    HttpStatus.BAD_REQUEST,
                    ServiceResponseDto.builder()
                            .success(false)
                            .correlationId(xCorrelationId)
                            .errorDescription(
                                    "The password reset token has expired. Please check your email for new password reset.")
                            .build());
        }
        UserEntity userEntity = user.get();
        userEntity.setPassword(passwordEncoder.encode(passwordResetRequest.getPassword()));
        userRepository.save(userEntity);
        return Optional.of(ServiceResponseDto.builder()
                .success(true)
                .successMessage("Password has been changed successfully")
                .correlationId(xCorrelationId)
                .build());
    }
}
