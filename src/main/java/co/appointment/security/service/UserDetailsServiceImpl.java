package co.appointment.security.service;

import co.appointment.persistence.UserEntity;
import co.appointment.persistence.UserRepository;
import co.appointment.util.UserDetailImplUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(final  String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(String.format("No user was found which matches the email: %s", username)));
        log.info("User found: {}", user);
        return UserDetailImplUtils.build(user);
    }
}
