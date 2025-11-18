package co.appointment.token;

import co.appointment.shared.constant.TokenConstants;
import co.appointment.shared.security.UserDetailsImpl;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class Oauth2AccessTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Override
    public void customize(final JwtEncodingContext context) {
        if(OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
            context.getClaims().claims(claims -> {
                Object principal = context.getPrincipal().getPrincipal();
                if(principal == null) {
                    return;
                }
                UserDetailsImpl user = (UserDetailsImpl)principal;

                Set<String> roles = AuthorityUtils.authorityListToSet(
                        user.getAuthorities())
                        .stream()
                        .map(c -> c.replaceFirst("^ROLE_", ""))
                        .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
                claims.put(TokenConstants.ROLES, roles);
                claims.put(TokenConstants.EMAIL, user.getEmail());
                claims.put(TokenConstants.FULL_NAME, String.format("%s %s", user.getFirstName(), user.getLastName()));
                claims.put(TokenConstants.USER_ID, user.getId());
            });
        }
    }
}
