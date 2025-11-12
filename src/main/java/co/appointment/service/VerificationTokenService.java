package co.appointment.service;

import co.appointment.config.AppConfigProperties;
import co.appointment.entity.VerificationToken;
import co.appointment.repository.VerificationTokenRepository;
import co.appointment.util.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class VerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final AppConfigProperties.VerificationTokenSettings verificationTokenSettings;

    public VerificationTokenService(final VerificationTokenRepository verificationTokenRepository, final AppConfigProperties appConfigProperties) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.verificationTokenSettings = appConfigProperties.getVerificationToken();
    }
    public VerificationToken createVerificationToken(final VerificationToken verificationToken) {
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setExpiryDate(ObjectUtils.addMillisecondsToCurrentDate(verificationTokenSettings.getExpirationMs()).getTime());
        if(verificationToken.getId() != null) {
            verificationToken.setCreatedAt(LocalDateTime.now());
        }
        return verificationTokenRepository.save(verificationToken);
    }
    public VerificationToken getVerificationToken(final String token) {
        return verificationTokenRepository.findByToken(token).orElse(null);
    }
    public boolean tokenExpired(final VerificationToken verificationToken) {
        LocalDateTime expiryLocalDateTime = ObjectUtils.timstampMillisToLocalDateTime(verificationToken.getExpiryDate());
        LocalDateTime currentLocalDateTime = LocalDateTime.now();
        log.debug("Current Date is:{}, Expiry Date is: {}", currentLocalDateTime, currentLocalDateTime);
        log.debug("Is Current Date after Expiry Date? {}", currentLocalDateTime.isAfter(expiryLocalDateTime));
        return currentLocalDateTime.isAfter(expiryLocalDateTime);
    }
}
