package co.appointment.controller;

import co.appointment.payload.request.PasswordResetRequest;
import co.appointment.payload.request.SignUpRequest;
import co.appointment.service.AuthService;
import co.appointment.shared.payload.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthControllerV1 {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> registerUser(@RequestBody @Valid final SignUpRequest signUpRequest) {
        ApiResponse<?> apiResponse = authService.registerUser(signUpRequest);
        if(!apiResponse.success()) {
            return ResponseEntity.badRequest().body(apiResponse);
        }
        return ResponseEntity.ok(apiResponse);
    }
    @GetMapping("/email-confirmation")
    public ResponseEntity<?> emailConfirmation(@RequestParam(name = "token") final String token,
                                               @RequestParam(name = "email") final String email) {
        ApiResponse<?> apiResponse = authService.confirmEmail(email, token);
        if(!apiResponse.success()) {
            return ResponseEntity.badRequest().body(apiResponse);
        }
        return ResponseEntity.ok().body(apiResponse);
    }
    @GetMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam(name = "email") final String email) {
        ApiResponse<?> response = authService.forgotPassword(email);
        if(!response.success()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(response);
    }
    @PostMapping("/password-reset")
    public ResponseEntity<?> passwordReset(@RequestBody @Valid final PasswordResetRequest passwordResetRequest) {
        ApiResponse<?> response = authService.passwordReset(passwordResetRequest);
        if(!response.success()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok().body(response);
    }
}
