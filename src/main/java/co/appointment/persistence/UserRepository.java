package co.appointment.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

public interface UserRepository
        extends CrudRepository<UserEntity, UUID>, ListPagingAndSortingRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);

    Boolean existsByEmail(String email);

    Boolean existsByEmailAndEmailVerified(String email, boolean verified);
}
