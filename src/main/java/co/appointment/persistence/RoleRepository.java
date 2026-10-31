package co.appointment.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

import java.util.UUID;

public interface RoleRepository extends CrudRepository<RoleEntity, UUID>, ListPagingAndSortingRepository<RoleEntity, UUID> {
}
