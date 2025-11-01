/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.engine.security;

import org.pentaho.platform.engine.security.messages.Messages;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

import java.util.Collection;

/**
 * The purpose of this class is to provide a means of normalizing user ids in case-insensitive LDAP environments.
 * This was successfully tested with MS Active Directory, but should also work with any other directory that is
 * case insensitive. The problem being addressed is well stated in BISERVER-5994. This mapper gets used in place of
 * the default LdapUserDetailsMapper in the applicationContext-spring-security-ldap.xml
 * 
 * To install this class, you need to do the following:
 * <ol>
 * <li>Modify applicationContext-spring-security-ldap.xml</li>
 * <li>Locate the bean <code>daoAuthenticationProvider</code></li>
 * <li>After the constructor arg bits, add a new property as follows:</li>
 * 
 * <pre>
 *    &lt;property name="userDetailsContextMapper"&gt;
 *      &lt;ref local="ldapContextMapper" /&gt;
 *    &lt;/property&gt;
 * </pre>
 * 
 * <li>Below the close of the definition of the <code>daoAuthenticationProvider</code> bean, create the
 * <code>ldapContextMapper</code> bean as shown - make sure you update the property name to match your environment.
 * The default is <code>samAccountName</code></li>
 * 
 * <pre>
 *   &lt;bean id="ldapContextMapper" class="org.pentaho.platform.engine.security.UseridAttributeLdapContextMapper"&gt;
 *     &lt;property name="ldapUsernameAttribute" value="samAccountName" /&gt;
 *   &lt;/bean&gt;
 * </pre>
 * 
 * </ol>
 * 
 */

public class UseridAttributeLdapContextMapper extends LdapUserDetailsMapper {

  private String ldapUsernameAttribute = "samAccountName";

  @Override
  public UserDetails mapUserFromContext( DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities ) {

    String usernameAttributeValue = ctx.getStringAttribute( getLdapUsernameAttribute() );
    if ( usernameAttributeValue == null ) {
      throw new IllegalStateException( Messages.getInstance().getErrorString(
          "UseridAttributeLdapContextMapper.ERROR_0001_ATTRIBUTE_NOT_FOUND", getLdapUsernameAttribute() ) );
    }
    // Pass along the attribute value, not the typed in value
    UserDetails rtn = super.mapUserFromContext( ctx, usernameAttributeValue, authorities );
    return rtn;
  }

  /**
   * Sets the name of the LDAP attribute to use for the login name after authentication.
   * <p>
   * Example - <code>cn</code>
   * <p>
   * Default value: <code>samAccountName</code>
   * <p>
   * Set the value as a bean property in the <code>applicationContext-spring-security-ldap.xml</code>
   * 
   * @param value
   */
  public void setLdapUsernameAttribute( String value ) {
    this.ldapUsernameAttribute = value;
  }

  public String getLdapUsernameAttribute() {
    return this.ldapUsernameAttribute;
  }

}
