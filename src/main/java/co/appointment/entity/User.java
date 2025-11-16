package co.appointment.entity;

import co.appointment.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name = "first_name", length = 100)
    private String firstName;

    @Column(nullable = false, name = "last_name", length = 100)
    private String lastName;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "contact_no", length = 20)
    private String contactNo;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(nullable = false, name = "is_active")
    private Boolean active = true;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER)
    private Set<UserRole> userRoles = new HashSet<>();

    public void addUserRole(Role role) {
        userRoles.add(new UserRole(this, role, new UserRoleKey(this.getId(), role.getId())));
    }

    public User() {}
    public User(String firstName, String lastName, String email, String contactNo, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.contactNo = contactNo;
        this.password = password;
    }
}
