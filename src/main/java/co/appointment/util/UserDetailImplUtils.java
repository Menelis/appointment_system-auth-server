package co.appointment.util;

import co.appointment.persistence.RoleEntity;
import co.appointment.persistence.UserEntity;
import co.appointment.shared.security.UserDetailsImpl;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@SuppressWarnings({"HideUtilityClassConstructor"})
public class UserDetailImplUtils {

    public static UserDetailsImpl build(final UserEntity user, final List<RoleEntity> roles) {
        Set<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(
                        "ROLE_%s".formatted(role.getName().toUpperCase())))
                .collect(Collectors.toSet());

        return new UserDetailsImpl(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getContactNo(),
                user.getPassword(),
                authorities);
    }
}
