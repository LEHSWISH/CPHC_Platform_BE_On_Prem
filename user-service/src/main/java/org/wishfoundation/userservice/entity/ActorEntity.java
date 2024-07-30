package org.wishfoundation.userservice.entity;


import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class ActorEntity {
	private String auditUserName;
	private String auditPhoneNumber;
	private String auditOrganization;
}
