package co.appointment.exception;

import co.appointment.shared.constant.SharedConstants;
import co.appointment.shared.payload.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(final Exception exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.internalServerError().body(new ApiResponse<>(false, SharedConstants.INTERNAL_SERVER_ERROR));
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(final BadCredentialsException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Invalid username or password"));
    }
}
