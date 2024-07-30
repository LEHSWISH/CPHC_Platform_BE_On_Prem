package org.wishfoundation.superadmin.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class AuditableEntity implements BaseItem {

    private static final long serialVersionUID = 7677840644260614484L;

    @Column(name = "created_on", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @CreatedDate
    @Setter(AccessLevel.PRIVATE)
    private java.time.Instant createdOn;

    @Column(name = "updated_on", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @LastModifiedDate
    @Setter(AccessLevel.PRIVATE)
    private java.time.Instant updatedOn;

    @CreatedBy
    @Embedded
    @Setter(AccessLevel.PRIVATE)
    @AttributeOverrides({
            @AttributeOverride(name = "auditEmail", column = @Column(name = "created_by_email", nullable = false, updatable = false)),
            @AttributeOverride(name = "userAccountId", column = @Column(name = "created_by_user_account_id", nullable = false, updatable = false))})
    protected ActorEntity createdBy;

    @LastModifiedBy
    @Embedded
    @Setter(AccessLevel.PRIVATE)
    @AttributeOverrides({
            @AttributeOverride(name = "auditEmail", column = @Column(name = "last_modified_by_email", nullable = false)),
            @AttributeOverride(name = "userAccountId", column = @Column(name = "last_modified_by_user_account_id", nullable = false))})
    protected ActorEntity lastModifiedBy;

}
