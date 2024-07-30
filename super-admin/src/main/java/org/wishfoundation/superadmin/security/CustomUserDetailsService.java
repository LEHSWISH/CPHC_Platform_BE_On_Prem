package org.wishfoundation.superadmin.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.wishfoundation.superadmin.config.UserAccountContext;
import org.wishfoundation.superadmin.entity.UserAccounts;
import org.wishfoundation.superadmin.entity.repository.UserAccountsRepository;
import org.wishfoundation.superadmin.enums.ErrorCode;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserAccountsRepository userAccountsRepo;
    @Override
    public UserDetails loadUserByUsername(String emailId) throws UsernameNotFoundException {


        Optional<UserAccounts> userOpt = userAccountsRepo.findUserByEmail(emailId);

        if (userOpt.isPresent()) {
            UserAccounts user = userOpt.get();
            UserAccountContext.setUserId(user.getId());
            return new User(user.getEmail(), user.getPassword(),
                    Collections.singleton(new SimpleGrantedAuthority(user.getEmail())));
        }
        throw new UsernameNotFoundException(ErrorCode.USER_IS_NOT_PRESENT.getMessage());

    }
}
