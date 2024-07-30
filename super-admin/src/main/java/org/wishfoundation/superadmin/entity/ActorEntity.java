package org.wishfoundation.superadmin.entity;


import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Embeddable
@Getter
@Setter
public class ActorEntity {
	private String auditEmail;
	private UUID userAccountId;
}
