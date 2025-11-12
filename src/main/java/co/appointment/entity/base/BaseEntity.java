package co.appointment.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Base entity for field created_At.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {
    /**
     * Audit fields of when record when created.
     */
    @Column(nullable = false, name = "created_At")
    private LocalDateTime createdAt = LocalDateTime.now();
}
