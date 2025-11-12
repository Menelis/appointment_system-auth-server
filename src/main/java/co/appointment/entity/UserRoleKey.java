package co.appointment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
public class UserRoleKey implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "role_id")
    private Integer roleId;

    public UserRoleKey() {}
    public UserRoleKey(UUID userId, Integer roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }
}
