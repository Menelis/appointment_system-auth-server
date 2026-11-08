package co.appointment.service;

import co.appointment.config.AppConfigProperties;
import co.appointment.persistence.VerificationTokenEntity;
import co.appointment.persistence.VerificationTokenRepository;
import co.appointment.util.ObjectUtils;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final AppConfigProperties.VerificationTokenSettings verificationTokenSettings;

    public VerificationTokenService(
            final VerificationTokenRepository tokenRepository, final AppConfigProperties appConfigProperties) {
        this.verificationTokenRepository = tokenRepository;
        this.verificationTokenSettings = appConfigProperties.getVerificationToken();
    }

    public VerificationTokenEntity createVerificationToken(
            final VerificationTokenEntity verificationToken, final String email) {
        Optional<VerificationTokenEntity> verificationTokenEntityOptional =
                verificationTokenRepository.findByVerificationToken(email, verificationToken.getTokenType());

        if (verificationTokenEntityOptional.isEmpty()) {
            if (log.isTraceEnabled()) {
                log.trace("Creating new token for user email=[{}]", email);
            }
            return updateOrCreateToken(verificationToken);
        }
        if (tokenExpired(verificationTokenEntityOptional.get())) {
            if (log.isTraceEnabled()) {
                log.trace("Updating token for user email=[{}] due to expiry", email);
            }
            return updateOrCreateToken(verificationTokenEntityOptional.get());
        }
        return verificationTokenEntityOptional.get();
    }

    VerificationTokenEntity updateOrCreateToken(final VerificationTokenEntity verificationToken) {
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setExpiryDate(
                ObjectUtils.addMillisecondsToCurrentDate(verificationTokenSettings.getExpirationMs())
                        .getTime());
        return verificationTokenRepository.save(verificationToken);
    }

    public VerificationTokenEntity getVerificationToken(final String token) {
        return verificationTokenRepository.findByToken(token).orElse(null);
    }

    public boolean tokenExpired(final VerificationTokenEntity verificationToken) {
        LocalDateTime expiryLocalDateTime =
                ObjectUtils.timstampMillisToLocalDateTime(verificationToken.getExpiryDate());
        LocalDateTime currentLocalDateTime = LocalDateTime.now();
        log.debug("Current Date is:{}, Expiry Date is: {}", currentLocalDateTime, expiryLocalDateTime);
        log.debug("Is Current Date after Expiry Date? {}", currentLocalDateTime.isAfter(expiryLocalDateTime));
        return currentLocalDateTime.isAfter(expiryLocalDateTime);
    }
}
