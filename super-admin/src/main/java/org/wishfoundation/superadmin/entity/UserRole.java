package org.wishfoundation.superadmin.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.wishfoundation.superadmin.enums.Roles;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_role", uniqueConstraints = @UniqueConstraint(columnNames = {"user_account_id", "role_name"}))
@Getter
@Setter
public class UserRole extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_account_id", length = 50)
    private UUID userAccountId;

    @Column(name = "role_name", length = 50)
    @Enumerated(EnumType.STRING)
    private Roles roleName;

    @JoinColumn(name = "user_account_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userAccountId")
    @JsonBackReference("userAccounts-userRole")
    private UserAccounts userAccounts;

}
