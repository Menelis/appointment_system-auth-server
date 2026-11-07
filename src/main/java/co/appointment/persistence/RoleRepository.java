package co.appointment.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

public interface RoleRepository
        extends CrudRepository<RoleEntity, UUID>, ListPagingAndSortingRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByName(String name);
}
