package co.appointment.controller.v1;

import static co.appointment.shared.constant.SharedConstants.X_CORRELATION_ID;

import co.appointment.payload.request.PasswordResetRequest;
import co.appointment.payload.request.SignUpRequest;
import co.appointment.service.AuthService;
import co.appointment.shared.dto.ServiceResponseDto;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthControllerV1 {

    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<ServiceResponseDto> registerUser(
            @RequestBody @Valid final SignUpRequest signUpRequest,
            @RequestHeader(X_CORRELATION_ID) final UUID xCorrelationId) {

        Optional<ServiceResponseDto> response = authService.registerUser(signUpRequest, xCorrelationId);
        return ResponseEntity.ok(response.get());
    }

    @GetMapping("/email-confirmation")
    public ResponseEntity<ServiceResponseDto> emailConfirmation(
            @RequestParam(name = "token") final String token,
            @RequestParam(name = "email") final String email,
            @RequestHeader(X_CORRELATION_ID) final UUID xCorrelationId) {

        Optional<ServiceResponseDto> response = authService.confirmEmail(email, token, xCorrelationId);

        return ResponseEntity.ok(response.get());
    }

    @GetMapping("/forgot-password")
    public ResponseEntity<ServiceResponseDto> forgotPassword(
            @RequestParam(name = "email") final String email,
            @RequestHeader(X_CORRELATION_ID) final UUID xCorrelationId) {

        Optional<ServiceResponseDto> response = authService.forgotPassword(email, xCorrelationId);

        return ResponseEntity.ok(response.get());
    }

    @PostMapping("/password-reset")
    public ResponseEntity<ServiceResponseDto> passwordReset(
            @RequestBody @Valid final PasswordResetRequest passwordResetRequest,
            @RequestHeader(X_CORRELATION_ID) final UUID xCorrelationId) {

        Optional<ServiceResponseDto> response = authService.passwordReset(passwordResetRequest, xCorrelationId);

        return ResponseEntity.ok(response.get());
    }
}
