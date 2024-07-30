package org.wishfoundation.chardhamcore.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.wishfoundation.chardhamcore.config.UserContext;
import org.wishfoundation.chardhamcore.entity.YatriPulseUsers;
import org.wishfoundation.chardhamcore.entity.repository.YatriPulseUsersRepository;
import org.wishfoundation.chardhamcore.enums.ErrorCode;


import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {


	@Autowired
	private YatriPulseUsersRepository yatriPulseUsersRepo;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		Optional<YatriPulseUsers> userOpt = yatriPulseUsersRepo.findUserByUserName(username);

		// TODO HANDLING FOR ROLES (DB changes need to be final)
		if (userOpt.isPresent()) {
			YatriPulseUsers user = userOpt.get();
			UserContext.setUserId(user.getId());
			return new User(user.getUserName(), user.getPassword(),
					Collections.singleton(new SimpleGrantedAuthority(user.getUserName())));
		}
		throw new UsernameNotFoundException(ErrorCode.USER_IS_NOT_PRESENT.getMessage());

	}
}
