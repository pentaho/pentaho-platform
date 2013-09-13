package org.pentaho.platform.engine.security;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.springframework.dao.DataAccessException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.security.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.util.Assert;

/**
 * A subclass of {@link JdbcDaoImpl} that allows the addition of a default role to all authenticated users.
 * 
 * @author mlowery
 */
public class DefaultRoleJdbcDaoImpl extends JdbcDaoImpl {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  /**
   * A default role which will be assigned to all authenticated users if set
   */
  private GrantedAuthority defaultRole;

  private IAuthenticationRoleMapper roleMapper;

  // ~ Constructors ====================================================================================================

  
  ITenantedPrincipleNameResolver userNameUtils;
  public DefaultRoleJdbcDaoImpl(ITenantedPrincipleNameResolver userNameUtils) {
    super();
    this.userNameUtils = userNameUtils;
  }

  // ~ Methods =========================================================================================================

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
    return super.loadUserByUsername(userNameUtils.getPrincipleName(username));
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void addCustomAuthorities(final String username, final List authorities) {
    if (defaultRole != null && !authorities.contains(defaultRole)) {
      authorities.add(defaultRole);
    }

    // also add roles mapped to pentaho security roles if available
    if(roleMapper != null) {
      List<GrantedAuthority> currentAuthorities = new ArrayList<GrantedAuthority>();
      currentAuthorities.addAll(authorities);

      for (GrantedAuthority role : currentAuthorities) {
        GrantedAuthority mappedRole = new GrantedAuthorityImpl(roleMapper.toPentahoRole(role.getAuthority()));
        if(!authorities.contains(mappedRole)){
          authorities.add(mappedRole);
        }
      }
    }
  }

  /**
   * The default role which will be assigned to all users.
   *
   * @param defaultRole the role name, including any desired prefix.
   */
  public void setDefaultRole(String defaultRole) {
    Assert.notNull(defaultRole);
    this.defaultRole = new GrantedAuthorityImpl(defaultRole);
  }

  public void setRoleMapper(IAuthenticationRoleMapper roleMapper) {
    this.roleMapper = roleMapper;
  }
}
