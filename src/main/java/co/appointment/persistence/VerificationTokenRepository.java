package co.appointment.persistence;

import co.appointment.shared.record.VerificationTokenSummary;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

public interface VerificationTokenRepository
        extends CrudRepository<VerificationTokenEntity, UUID>,
                ListPagingAndSortingRepository<VerificationTokenEntity, UUID> {
    Optional<VerificationTokenEntity> findByToken(String token);

    @Query("""
            SELECT VT.id as id,
                   VT.token as token,
                   VT.expiry_date as expiryDate,
                   VT.token_type as tokenType
            FROM users U JOIN verification_tokens VT ON U.id = VT.user_id
            WHERE LOWER(U.email) = LOWER(:email);
            """)
    Optional<VerificationTokenSummary> findByEmail(String email);
}
