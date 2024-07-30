package org.wishfoundation.userservice.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.wishfoundation.userservice.config.UserContext;
import org.wishfoundation.userservice.entity.ActorEntity;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<ActorEntity> {
    @Override
    public Optional<ActorEntity> getCurrentAuditor() {

        ActorEntity ent = new ActorEntity();
        ent.setAuditPhoneNumber(UserContext.getCurrentPhoneNumber());
        ent.setAuditUserName(UserContext.getCurrentUserName());
        ent.setAuditOrganization(UserContext.getCurrentOrganization());
        return Optional.of(ent);
    }

}
