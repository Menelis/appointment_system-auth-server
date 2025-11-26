package co.appointment.service;

import co.appointment.config.AppConfigProperties;
import co.appointment.entity.ETokenType;
import co.appointment.entity.Role;
import co.appointment.entity.User;
import co.appointment.entity.VerificationToken;
import co.appointment.payload.request.PasswordResetRequest;
import co.appointment.payload.request.SignUpRequest;
import co.appointment.repository.RoleRepository;
import co.appointment.repository.UserRepository;
import co.appointment.shared.constant.EmailConstants;
import co.appointment.shared.constant.EventTypeConstants;
import co.appointment.shared.constant.RoleConstants;
import co.appointment.shared.kafka.event.EmailEvent;
import co.appointment.shared.payload.response.ApiResponse;
import co.appointment.shared.service.EncryptionService;
import co.appointment.util.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final NotificationEventService notificationEventService;
    private final AppConfigProperties appConfigProperties;
    private final VerificationTokenService verificationTokenService;
    private final EncryptionService encryptionService;

    private static final Map<String, Object> EMAIL_VERIFICATION_EVENT_HEADERS = Map.of(
            EventTypeConstants.EVENT_TYPE, EventTypeConstants.VERIFY_EMAIL_EVENT);
    private static final Map<String, Object> EMAIL_PASSWORD_RESET_EVENT_HEADERS = Map.of(
            EventTypeConstants.EVENT_TYPE, EventTypeConstants.PASSWORD_RESET_EVENT);


    public ApiResponse<?> registerUser(final SignUpRequest signUpRequest) {
        if(userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new ApiResponse<>(false, String.format("Email %s is already in use", signUpRequest.getEmail()));
        }
        User user = new User(
                signUpRequest.getFirstName(),
                signUpRequest.getLastName(),
                signUpRequest.getEmail(),
                signUpRequest.getContactNo(),
                passwordEncoder.encode(signUpRequest.getPassword()));
        //Set default role
        Role customerRole = roleRepository.findByName(RoleConstants.CUSTOMER_ROLE).orElseThrow(() -> new RuntimeException(String.format("No role with name: %s was found in the db", RoleConstants.CUSTOMER_ROLE)));
        user.addUserRole(customerRole);
        userRepository.save(user);
        // Generate verification token
        sendEmailEvent(user, createVerificationToken(user, ETokenType.EMAIL_VERIFICATION_TOKEN), EmailConstants.VERIFY_EMAIL_MAIL);
        return new ApiResponse<>(true, "User registered successfully.Please check your email for account verification.");
    }
    private void  sendEmailEvent(
            final User user, final VerificationToken verificationToken, final String emailType) {
        final String clientUrl = appConfigProperties.getClientUrl();

        final AbstractMap.SimpleImmutableEntry<String, String> emailBodyAndSubject = switch (emailType) {
            case EmailConstants.VERIFY_EMAIL_MAIL -> new AbstractMap.SimpleImmutableEntry<>(
                    EmailConstants.VERIFY_EMAIL_SUBJECT, ObjectUtils.getUserRegistrationEmailBody(clientUrl, user, verificationToken, appConfigProperties.getEmailTemplate().getVerifyEmailTemplate()));
            case EmailConstants.PASSWORD_RESET_MAIL -> new AbstractMap.SimpleImmutableEntry<>(
                    EmailConstants.PASSWORD_RESET_SUBJECT, ObjectUtils.getPasswordResetEmailBody(clientUrl, verificationToken, user, appConfigProperties.getEmailTemplate().getResetPasswordEmailTemplate()));
            default -> throw new IllegalStateException("Unexpected value: " + emailType);
        };

        final String mailBody = encryptionService.encryptText(emailBodyAndSubject.getValue());

        switch (emailType) {
            case EmailConstants.VERIFY_EMAIL_MAIL -> notificationEventService.sendEmailEvent(new EmailEvent(
                    user.getEmail(), EmailConstants.VERIFY_EMAIL_SUBJECT, mailBody, true),
                    EMAIL_VERIFICATION_EVENT_HEADERS);
            case EmailConstants.PASSWORD_RESET_MAIL -> notificationEventService.sendEmailEvent(
                    new EmailEvent(user.getEmail(), EmailConstants.PASSWORD_RESET_SUBJECT, mailBody, true),
                    EMAIL_PASSWORD_RESET_EVENT_HEADERS);
            default -> throw new IllegalStateException("Unexpected value: " + emailType);
        }
    }
    public ApiResponse<?> confirmEmail(final String email, final String token) {
        VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);
        if(verificationToken == null) {
            return new ApiResponse<>(false, "Invalid verification token");
        }
        User user = userRepository.findByEmail(email).orElse(null);
        if(user == null) {
            return new ApiResponse<>(false, "No user was found with the supplied email");
        }
        if(verificationTokenService.tokenExpired(verificationToken)) {
            sendEmailEvent(user, createVerificationToken(user, ETokenType.EMAIL_VERIFICATION_TOKEN), EmailConstants.VERIFY_EMAIL_MAIL);
            return new ApiResponse<>(false, "The verification token has expired. Please check your email for new registration confirmation.");
        }
        // Confirm email verified
        user.setEmailVerified(true);
        userRepository.save(user);
        return new ApiResponse<>(true, "The email confirmation has been completed.");
    }
    public ApiResponse<?> forgotPassword(final String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if(user == null) {
            return new ApiResponse<>(false, "No user was found with the supplied email");
        }
        sendEmailEvent(user, createVerificationToken(user, ETokenType.PASSWORD_RESET_TOKEN), EmailConstants.PASSWORD_RESET_MAIL);
        return new ApiResponse<>(true, "Password reset email has been sent to your email");
    }
    private VerificationToken createVerificationToken(final User user,
                                                      final ETokenType tokenType) {
        VerificationToken verificationToken = verificationTokenService.createVerificationToken(new VerificationToken(user, tokenType.name()));
        if(verificationToken == null) {
            throw new RuntimeException("Verification Token could not be created for user id: "+ user.getId());
        }
        return verificationToken;
    }

    public ApiResponse<?> passwordReset(final PasswordResetRequest passwordResetRequest) {
        VerificationToken verificationToken = verificationTokenService.getVerificationToken(passwordResetRequest.getToken());
        if(verificationToken == null) {
            return new ApiResponse<>(false, "Invalid verification token");
        }
        User user = userRepository.findByEmail(passwordResetRequest.getEmail()).orElse(null);
        if(user == null) {
            return new ApiResponse<>(false, "No user was found with the supplied email");
        }
        if(verificationTokenService.tokenExpired(verificationToken)) {
            sendEmailEvent(user, createVerificationToken(user, ETokenType.PASSWORD_RESET_TOKEN), EmailConstants.PASSWORD_RESET_MAIL);
            return new ApiResponse<>(false, "The password reset token has expired. Please check your email for new password reset.");
        }
        user.setPassword(passwordEncoder.encode(passwordResetRequest.getPassword()));
        userRepository.save(user);
        return new ApiResponse<>(true, "Password has been changed successfully");
    }
}
