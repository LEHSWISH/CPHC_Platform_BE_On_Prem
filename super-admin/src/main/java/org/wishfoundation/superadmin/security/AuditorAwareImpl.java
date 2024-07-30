package org.wishfoundation.superadmin.security;


import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.wishfoundation.superadmin.config.UserAccountContext;
import org.wishfoundation.superadmin.entity.ActorEntity;


import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<ActorEntity> {
    @Override
    public Optional<ActorEntity> getCurrentAuditor() {

        ActorEntity ent = new ActorEntity();
        ent.setAuditEmail(UserAccountContext.getCurrentEmailId());
        ent.setUserAccountId(UserAccountContext.getUserId());
        return Optional.of(ent);
    }

}
