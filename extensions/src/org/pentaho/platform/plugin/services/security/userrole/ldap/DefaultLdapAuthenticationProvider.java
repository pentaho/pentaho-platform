package org.pentaho.platform.plugin.services.security.userrole.ldap;


import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.ldap.LdapAuthoritiesPopulator;
import org.springframework.security.providers.ldap.LdapAuthenticationProvider;
import org.springframework.security.providers.ldap.LdapAuthenticator;

public class DefaultLdapAuthenticationProvider extends LdapAuthenticationProvider{

	private IAuthenticationRoleMapper roleMapper;

	public DefaultLdapAuthenticationProvider(LdapAuthenticator authenticator, IAuthenticationRoleMapper roleMapper) {
		super(authenticator);
		this.roleMapper = roleMapper; 
	}
	
	public DefaultLdapAuthenticationProvider(LdapAuthenticator authenticator, LdapAuthoritiesPopulator authoritiesPopulator, IAuthenticationRoleMapper roleMapper) {
		super(authenticator, authoritiesPopulator);
		this.roleMapper = roleMapper; 
	}

	/**
	 * We need to iterate through the authorities and map them to pentaho security equivalent
	 */
	@Override
	protected GrantedAuthority[] loadUserAuthorities(
			DirContextOperations userData, String username, String password) {
		GrantedAuthority[] authorities =  super.loadUserAuthorities(userData, username, password);
		if(roleMapper != null) {
			for(int i=0;i<authorities.length;i++) {
				if(authorities[i] != null) {
					authorities[i] =  new GrantedAuthorityImpl(roleMapper.toPentahoRole(authorities[i].getAuthority()));				
				}
			}
		}
		return authorities;
	}
}
