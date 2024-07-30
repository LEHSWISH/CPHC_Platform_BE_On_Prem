package org.wishfoundation.abhaservice.entity;

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
import org.wishfoundation.chardhamcore.entity.BaseItem;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class AuditableEntity implements BaseItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7677840644262645675L;

	@Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
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
			@AttributeOverride(name = "yatriUserName", column = @Column(name = "created_by_user_name", nullable = false, updatable = false))})
	protected ActorEntity createdBy;

	@LastModifiedBy
	@Embedded
	@Setter(AccessLevel.PRIVATE)
	@AttributeOverrides({
			@AttributeOverride(name = "yatriUserName", column = @Column(name = "last_modified_by_user_id", nullable = false)) })
	protected ActorEntity lastModifiedBy;

}
