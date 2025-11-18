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
import co.appointment.shared.constant.EventTypeConstants;
import co.appointment.shared.constant.RoleConstants;
import co.appointment.shared.kafka.event.EmailEvent;
import co.appointment.shared.payload.response.ApiResponse;
import co.appointment.shared.service.EncryptionService;
import co.appointment.util.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        VerificationToken verificationToken = verificationTokenService.createVerificationToken(new VerificationToken(user, ETokenType.EMAIL_VERIFICATION_TOKEN.name()));
        sendEmailEvent(user, verificationToken);
        return new ApiResponse<>(true, "User registered successfully.Please check your email for account verification.");
    }
    private void  sendEmailEvent(final User user, final VerificationToken verificationToken) {
        String mailBody = encryptionService.encryptText(ObjectUtils.getUserRegistrationEmailBody(ObjectUtils.getParameterizedClientUrl(appConfigProperties.getClientUrl(), user.getEmail(), verificationToken.getToken())));
        notificationEventService.sendEmailEvent(new EmailEvent(user.getEmail(),"Email Verification", mailBody, true), EMAIL_VERIFICATION_EVENT_HEADERS);
    }
    public ApiResponse<?> confirmEmail(final String email,
                                       final String token) {
        VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);
        if(verificationToken == null) {
            return new ApiResponse<>(false, "Invalid verification token");
        }
        User user = userRepository.findByEmail(email).orElse(null);
        if(user == null) {
            return new ApiResponse<>(false, "No user was found with the supplied email");
        }
        if(verificationTokenService.tokenExpired(verificationToken)) {
            sendEmailEvent(user, verificationTokenService.createVerificationToken(new VerificationToken(user, ETokenType.EMAIL_VERIFICATION_TOKEN.name())));
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
        sendEmailEvent(user, verificationTokenService.createVerificationToken(new VerificationToken(user, ETokenType.PASSWORD_RESET_TOKEN.name())));
        return new ApiResponse<>(true, "Password reset email has been sent to your email");
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
            sendEmailEvent(user, verificationTokenService.createVerificationToken(new VerificationToken(user, ETokenType.PASSWORD_RESET_TOKEN.name())));
            return new ApiResponse<>(false, "The password reset token has expired. Please check your email for new password reset.");
        }
        user.setPassword(passwordEncoder.encode(passwordResetRequest.getPassword()));
        userRepository.save(user);
        return new ApiResponse<>(true, "Password has been changed successfully");
    }
}
