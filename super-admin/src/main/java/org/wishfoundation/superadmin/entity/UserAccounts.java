package org.wishfoundation.superadmin.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_accounts")
@Entity
public class UserAccounts extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "user_id", unique = true, length = 10)
    private String userId;

    @Column(name = "full_name", length = 50)
    private String fullName;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @OneToMany(mappedBy = "userAccounts", fetch = FetchType.LAZY)
    @JsonManagedReference("userAccounts-userRole")
    private List<UserRole> userRole;

}
