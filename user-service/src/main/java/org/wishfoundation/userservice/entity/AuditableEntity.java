package org.wishfoundation.userservice.entity;

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

/**
 * This class serves as a base entity for all entities that require auditing.
 * It includes fields for tracking the creation and modification dates, as well as the actors responsible for these actions.
 *
 * @author Sandeep kumar
 * @version 1.0
 * @since 2023-01-01
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class AuditableEntity implements BaseItem {

    /**
     * A unique identifier for serialization purposes.
     */
    private static final long serialVersionUID = 7677840644260614484L;

    /**
     * The date and time when the entity was created.
     * This field is automatically populated by Spring Data JPA.
     */
    @Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @CreatedDate
    @Setter(AccessLevel.PRIVATE)
    private java.time.Instant createdOn;

    /**
     * The date and time when the entity was last updated.
     * This field is automatically populated by Spring Data JPA.
     */
    @Column(name = "updated_on", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @LastModifiedDate
    @Setter(AccessLevel.PRIVATE)
    private java.time.Instant updatedOn;

    /**
     * The actor who created the entity.
     * This field is automatically populated by Spring Data JPA.
     */
    @CreatedBy
    @Embedded
    @Setter(AccessLevel.PRIVATE)
    @AttributeOverrides({
            @AttributeOverride(name = "auditUserName", column = @Column(name = "created_by_user", nullable = false, updatable = false)),
            @AttributeOverride(name = "auditPhoneNumber", column = @Column(name = "created_by_phone_number", nullable = false, updatable = false)),
            @AttributeOverride(name = "auditOrganization", column = @Column(name = "created_by_organization", nullable = false, updatable = false))})
    protected ActorEntity createdBy;

    /**
     * The actor who last modified the entity.
     * This field is automatically populated by Spring Data JPA.
     */
    @LastModifiedBy
    @Embedded
    @Setter(AccessLevel.PRIVATE)
    @AttributeOverrides({
            @AttributeOverride(name = "auditUserName", column = @Column(name = "last_modified_by_user", nullable = false)),
            @AttributeOverride(name = "auditPhoneNumber", column = @Column(name = "last_modified_by_phone_number", nullable = false)),
            @AttributeOverride(name = "auditOrganization", column = @Column(name = "last_modified_by_organization", nullable = false, updatable = false))})
    protected ActorEntity lastModifiedBy;

}
