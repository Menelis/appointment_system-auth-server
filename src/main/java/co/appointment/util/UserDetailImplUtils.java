package co.appointment.util;

import co.appointment.persistence.UserEntity;
import co.appointment.shared.security.UserDetailsImpl;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;

@SuppressWarnings({"HideUtilityClassConstructor"})
public class UserDetailImplUtils {

    public static UserDetailsImpl build(final UserEntity user) {
        Set<GrantedAuthority> authorities = Set.of() /*user.getUserRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(String.format("ROLE_%s",
                        role.
                                getRole()
                                .getName()
                                .toUpperCase())))
                .collect(Collectors.toSet())*/;
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
