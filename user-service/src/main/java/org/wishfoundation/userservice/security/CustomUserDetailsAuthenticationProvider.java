package org.wishfoundation.userservice.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ObjectUtils;
import org.wishfoundation.userservice.enums.ErrorCode;

public class CustomUserDetailsAuthenticationProvider
  extends AbstractUserDetailsAuthenticationProvider {

  private static final Logger log = LoggerFactory.getLogger(
    CustomUserDetailsAuthenticationProvider.class
  );

  private final PasswordEncoder passwordEncoder;
  private final CustomUserDetailsService userDetailsService;

  public CustomUserDetailsAuthenticationProvider(
    PasswordEncoder passwordEncoder,
    CustomUserDetailsService userDetailsService
  ) {
    this.passwordEncoder = passwordEncoder;
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected void additionalAuthenticationChecks(
    UserDetails userDetails,
    UsernamePasswordAuthenticationToken authentication
  ) throws AuthenticationException {
    if (authentication.getCredentials() == null) {
      logger.debug("Authentication failed: no credentials provided");
      throw new BadCredentialsException(
        messages.getMessage(
          "AbstractUserDetailsAuthenticationProvider.badCredentials",
          "Bad credentials"
        )
      );
    }

    if (authentication.getPrincipal() == null) {
      logger.debug("Authentication failed: no email provided");
      throw new BadCredentialsException(
        messages.getMessage(
          "AbstractUserDetailsAuthenticationProvider.badCredentials",
          "Bad  Email "
        )
      );
    }

    String presentedPassword = authentication.getCredentials().toString();

    // Log for debugging
    logger.debug("Presented Password: " + presentedPassword);
    logger.debug("Stored Password (Hashed): " + userDetails.getPassword());
    // end
    if (
      !passwordEncoder.matches(presentedPassword, userDetails.getPassword())
    ) {
      logger.debug(
        "Authentication failed: password does not match stored value"
      );
      throw new BadCredentialsException(
        messages.getMessage(
          "AbstractUserDetailsAuthenticationProvider.badCredentials",
          "Bad credentials"
        )
      );
    }
  }

  @Override
  protected UserDetails retrieveUser(
    String username,
    UsernamePasswordAuthenticationToken authentication
  ) throws AuthenticationException {
    UserDetails loadedUser = null;
    try {
      loadedUser = this.userDetailsService.loadUserByUsername(username);
      Map<String, Object> map = new HashMap<>();
      // TODO GET ROLES FROM DYNAMODB
      map.put(
        JWTService.USER_ROLE,
        new ArrayList<>(Arrays.asList("END_USER", "PARA", "DOC"))
      );
      authentication.setDetails(map);
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (
      ObjectUtils.isEmpty(loadedUser)
    ) throw new InternalAuthenticationServiceException(
      ErrorCode.USER_IS_NOT_PRESENT.getMessage()
    );
    return loadedUser;
  }
}
