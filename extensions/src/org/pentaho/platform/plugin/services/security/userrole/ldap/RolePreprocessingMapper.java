/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.security.userrole.ldap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.ldap.LdapUserDetailsMapper;

/**
 * Extension of <code>LdapUserDetailsMapper</code> which extracts the value of the component named
 * <code>tokenName</code> within any attribute in <code>roleAttributes</code>.
 * 
 * <p>
 * Example LDIF:
 * </p>
 * 
 * <pre>
 *        dn: uid=admin,ou=users,ou=system
 *        ...
 *        uniqueMember: cn=ceo,ou=roles
 * </pre>
 * 
 * Assume that you want the value of the <code>cn</code> component within the value of the <code>uniqueMember</code>
 * attribute to be used as the role name.
 * 
 * You would use <code>mapper.setTokenName("cn")</code> or the equivalent to this setter call in your Spring beans XML.
 * 
 * @author mlowery
 * 
 */
public class RolePreprocessingMapper extends LdapUserDetailsMapper implements InitializingBean {

  private static final Log logger = LogFactory.getLog( RolePreprocessingMapper.class );

  private String tokenName = "cn"; //$NON-NLS-1$

  public RolePreprocessingMapper() {
    super();
  }

  public RolePreprocessingMapper( final String tokenName ) {
    super();
    this.tokenName = tokenName;
  }

  @Override
  protected GrantedAuthority createAuthority( final Object role ) {
    Object newRole = preprocessRole( role );
    return super.createAuthority( newRole );
  }

  protected Object preprocessRole( final Object role ) {
    final String COMPONENT_SEPARATOR = ","; //$NON-NLS-1$
    final String NAME_VALUE_PAIR_SEPARATOR = "="; //$NON-NLS-1$
    if ( role instanceof String ) {
      String roleString = (String) role;
      String[] tokens = roleString.split( COMPONENT_SEPARATOR );
      for ( String rdnString : tokens ) {
        String[] rdnTokens = rdnString.split( NAME_VALUE_PAIR_SEPARATOR );
        if ( rdnTokens[0].trim().equals( tokenName ) ) {
          return rdnTokens[1].trim();
        }
      }
      if ( RolePreprocessingMapper.logger.isWarnEnabled() ) {
        RolePreprocessingMapper.logger.warn( Messages.getInstance().getString(
            "RolePreprocessingMapper.WARN_TOKEN_NOT_FOUND", tokenName ) ); //$NON-NLS-1$
      }
      // return null so that superclass does not use the value of this
      // attribute
      return null;
    } else {
      // do not know what to do with this; return it unmodified
      return role;
    }
  }

  public void setTokenName( final String tokenName ) {
    this.tokenName = tokenName;
  }

  public String getTokenName() {
    return tokenName;
  }

  public void afterPropertiesSet() throws Exception {
  }

}
