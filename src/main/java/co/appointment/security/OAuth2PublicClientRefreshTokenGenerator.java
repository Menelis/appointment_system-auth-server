package co.appointment.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import javax.annotation.Nullable;
import java.util.Base64;

@RequiredArgsConstructor
public class OAuth2PublicClientRefreshTokenGenerator implements OAuth2TokenGenerator<OAuth2RefreshToken> {

    private final StringKeyGenerator refreshTokenGenerator = new Base64StringKeyGenerator(Base64.getEncoder(), 96);

    @Nullable
    @Override
    public OAuth2RefreshToken generate(OAuth2TokenContext context) {
        return null;
    }
}
