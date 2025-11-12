package co.appointment.payload.request;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String email;
    private String token;
    private String password;
}
