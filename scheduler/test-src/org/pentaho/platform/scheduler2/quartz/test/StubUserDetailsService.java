package org.pentaho.platform.scheduler2.quartz.test;

import org.springframework.dao.DataAccessException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

public class StubUserDetailsService implements UserDetailsService {

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
    GrantedAuthority[]  auths = new GrantedAuthority[2];
    auths[0] = new GrantedAuthorityImpl("Authenticated");
    auths[1] = new GrantedAuthorityImpl("Administrator");
    
    UserDetails user = new User("admin", "password", true, true, true, true, auths);

    return user;
  }

}
