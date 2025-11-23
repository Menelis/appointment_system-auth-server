package co.appointment.mixin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

public abstract class UserDetailsImplMixin {

    @JsonCreator
    public UserDetailsImplMixin(
            @JsonProperty("id") final UUID id,
            @JsonProperty("firstName") final String firstName,
            @JsonProperty("lastName") final String lastName,
            @JsonProperty("email") final String email,
            @JsonProperty("contactNo") final String contactNo,
            @JsonProperty("password") final String password,
            @JsonProperty("authorities") final Collection<? extends GrantedAuthority> authorities) {}

    @JsonIgnore
    public abstract String getUsername();

    @JsonIgnore
    public abstract boolean isEnabled();

    @JsonIgnore
    public abstract boolean isAccountNonExpired();

    @JsonIgnore
    public abstract boolean isAccountNonLocked();

    @JsonIgnore
    public abstract boolean isCredentialsNonExpired();
}
