/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.security.userrole.memory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.beans.propertyeditors.PropertiesEditor;
import org.springframework.security.core.userdetails.memory.UserAttribute;

import java.beans.PropertyEditorSupport;

import java.util.Properties;

/**
 * Brought over from spring-security 3.2.5, as this class no longer exists in spring-security 4.1
 * @see https://github.com/spring-projects/spring-security/blob/3.2.5.RELEASE/core/src/main/java/org/springframework/security/core/userdetails/memory/UserMapEditor.java
 */
public class PentahoUserMapEditor extends PropertyEditorSupport {

  private static final Log logger = LogFactory.getLog( PentahoUserMapEditor.class );

  public static PentahoUserMap addUsersFromProperties( PentahoUserMap userMap, Properties props ) {
    // Now we have properties, process each one individually
    PentahoUserAttributeEditor configAttribEd = new PentahoUserAttributeEditor();

    for ( Object o : props.keySet() ) {
      String username = (String) o;
      String value = props.getProperty( username );

      // Convert value to a password, enabled setting, and list of granted authorities
      configAttribEd.setAsText( value );

      UserAttribute attr = (UserAttribute) configAttribEd.getValue();

      // Make a user object, assuming the properties were properly provided
      if ( attr != null ) {
        UserDetails user = new User( username, attr.getPassword(), attr.isEnabled(), true, true, true, attr.getAuthorities() );
        userMap.addUser( user );
      }
    }
    return userMap;
  }

  public void setAsText( String s ) throws IllegalArgumentException {
    PentahoUserMap userMap = new PentahoUserMap();

    if ( ( s == null ) || "".equals( s ) ) {
      // Leave value in property editor null
      logger.debug( "Leaving value in property editor null" );
    } else {
      // Use properties editor to tokenize the string
      PropertiesEditor propertiesEditor = new PropertiesEditor();
      propertiesEditor.setAsText( s );

      Properties props = (Properties) propertiesEditor.getValue();
      addUsersFromProperties( userMap, props );
    }

    setValue( userMap );
  }
}
