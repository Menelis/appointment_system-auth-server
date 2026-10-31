package co.appointment.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

import java.util.UUID;

public interface UserRoleRepository extends CrudRepository<UserRoleEntity, UUID>, ListPagingAndSortingRepository<UserRoleEntity, UUID> {
}
