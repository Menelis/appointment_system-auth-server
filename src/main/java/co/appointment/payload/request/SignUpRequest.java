package co.appointment.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpRequest {
    @NotBlank(message = "First Name cannot be blank")
    @NotEmpty(message = "First Name cannot be empty")
    @Size(max = 100, message = "First Name cannot exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last Name cannot be blank")
    @NotEmpty(message = "Last Name cannot be empty")
    @Size(max = 100, message = "Last Name cannot exceed 100 characters")
    private String lastName;

    @NotBlank(message = "Email cannot be blank")
    @NotEmpty(message = "Email Name cannot be empty")
    @Size(max = 100, message = "Email Name cannot exceed 100 characters")
    @Email
    private String email;

    @Size(max = 20, message = "Contact Number cannot exists 20 characters")
    private String contactNo;

    @NotBlank(message = "Password cannot be blank")
    @NotEmpty(message = "Password cannot be empty")
    @Size(max = 20, message = "Password cannot exceed 20 characters")
    private String password;
}
