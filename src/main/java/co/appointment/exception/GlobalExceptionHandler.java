package co.appointment.exception;

import co.appointment.shared.dto.ServiceResponseDto;
import co.appointment.shared.exception.ServiceException;
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
        return ResponseEntity.internalServerError()
                .body(ServiceResponseDto.builder()
                        .success(false)
                        .responseMessage(exception.getClass().getSimpleName())
                        .build());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(final BadCredentialsException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.badRequest()
                .body(ServiceResponseDto.builder()
                        .success(false)
                        .responseMessage("Invalid username or password")
                        .build());
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<?> handleServiceException(final ServiceException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(exception.getStatus()).body(exception.getResponse());
    }
}
