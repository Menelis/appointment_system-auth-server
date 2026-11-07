package co.appointment.persistence;

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
            SELECT VT.*
            FROM users U JOIN verification_tokens VT ON U.id = VT.user_id
            WHERE LOWER(U.email) = LOWER(:email) AND LOWER(VT.token_type) = LOWER(:tokenType);
            """)
    Optional<VerificationTokenEntity> findByVerificationToken(String email, String tokenType);
}
