package co.appointment.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends CrudRepository<VerificationTokenEntity, UUID>, ListPagingAndSortingRepository<VerificationTokenEntity, UUID> {
    Optional<VerificationTokenEntity> findByToken(String token);
}
