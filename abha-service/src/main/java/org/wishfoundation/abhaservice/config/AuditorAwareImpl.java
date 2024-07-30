package org.wishfoundation.abhaservice.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.wishfoundation.abhaservice.entity.ActorEntity;
import org.wishfoundation.chardhamcore.config.UserContext;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<ActorEntity> {
    @Override
    public Optional<ActorEntity> getCurrentAuditor() {
        ActorEntity ent = new ActorEntity();
        ent.setYatriUserName(UserContext.getCurrentUserName());
        return Optional.of(ent);
    }

}
