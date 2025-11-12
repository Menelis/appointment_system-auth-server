package co.appointment.util;

import co.appointment.entity.User;
import co.appointment.shared.security.UserDetailsImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

public class UserDetailImplUtils {
    public static UserDetailsImpl build(final User user) {
        List<GrantedAuthority> authorities = user.getUserRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(String.format("ROLE_%s",
                        role.
                                getRole()
                                .getName()
                                .toUpperCase())))
                .collect(Collectors.toList());
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
