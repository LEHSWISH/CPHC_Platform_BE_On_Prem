package org.wishfoundation.chardhamcore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class AuditableEntity implements BaseItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7677840644260614484L;

	@Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
	@CreatedDate
	@Setter(AccessLevel.PRIVATE)
	private java.time.Instant createdOn;

	@Column(name = "updated_on", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
	@LastModifiedDate
	@Setter(AccessLevel.PRIVATE)
	private java.time.Instant updatedOn;

//	@CreatedBy
//	@Embedded
//	@Setter(AccessLevel.PRIVATE)
//	@AttributeOverrides({
//			@AttributeOverride(name = "userName", column = @Column(name = "created_by_user_id", nullable = false, updatable = false)) })
//	protected ActorEntity createdBy;
//
//	@LastModifiedBy
//	@Embedded
//	@Setter(AccessLevel.PRIVATE)
//	@AttributeOverrides({
//			@AttributeOverride(name = "userName", column = @Column(name = "last_modified_by_user_id", nullable = false)) })
//	protected ActorEntity lastModifiedBy;

}
