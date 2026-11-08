package co.appointment.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

public interface RoleRepository
        extends CrudRepository<RoleEntity, UUID>, ListPagingAndSortingRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByName(String name);

    @Query("""
            SELECT R.*
            FROM roles r INNER JOIN user_roles ur on r.id = ur.role_id
            WHERE ur.user_id = :id
            """)
    List<RoleEntity> findRolesByUserId(UUID id);
}
