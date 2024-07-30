package org.wishfoundation.userservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.wishfoundation.userservice.config.UserContext;
import org.wishfoundation.userservice.entity.repository.YatriPulseUsersRepository;
import org.wishfoundation.userservice.enums.ErrorCode;
import org.wishfoundation.userservice.response.UserAuthDetailsResp;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private YatriPulseUsersRepository yatriPulseUsersRepo;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		Optional<UserAuthDetailsResp> userOpt = yatriPulseUsersRepo.findUserAuthDetailsByUserName(username);

		// TODO HANDLING FOR ROLES (DB changes need to be final)
		if (userOpt.isPresent()) {
			UserAuthDetailsResp user = userOpt.get();
			UserContext.setUserId(user.getId());
			UserContext.setCareType(user.getCareType());
			return new User(user.getUserName(), user.getPassword(),
					Collections.singleton(new SimpleGrantedAuthority(user.getUserName())));
		}
		throw new UsernameNotFoundException(ErrorCode.USER_IS_NOT_PRESENT.getMessage());

	}
}
