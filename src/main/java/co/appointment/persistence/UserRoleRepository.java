package co.appointment.persistence;

import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

public interface UserRoleRepository
        extends CrudRepository<UserRoleEntity, UUID>, ListPagingAndSortingRepository<UserRoleEntity, UUID> {}
