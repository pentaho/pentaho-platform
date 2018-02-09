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

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.userdetails.memory.UserAttribute;
import org.springframework.util.StringUtils;

/**
 * Brought over from spring-security 3.2.5, as this class no longer exists in spring-security 4.1
 * @see https://github.com/spring-projects/spring-security/blob/3.2.5.RELEASE/core/src/main/java/org/springframework/security/core/userdetails/memory/UserAttributeEditor.java
 */
public class PentahoUserAttributeEditor extends PropertyEditorSupport {
  //~ Methods ========================================================================================================

  public void setAsText( String s ) throws IllegalArgumentException {
    if ( StringUtils.hasText( s ) ) {
      String[] tokens = StringUtils.commaDelimitedListToStringArray( s );
      UserAttribute userAttrib = new UserAttribute();

      List<String> authoritiesAsStrings = new ArrayList<String>();

      for ( int i = 0; i < tokens.length; i++ ) {
        String currentToken = tokens[i].trim();

        if ( i == 0 ) {
          userAttrib.setPassword( currentToken );
        } else {
          if ( currentToken.toLowerCase().equals( "enabled" ) ) {
            userAttrib.setEnabled( true );
          } else if ( currentToken.toLowerCase().equals( "disabled" ) ) {
            userAttrib.setEnabled( false );
          } else {
            authoritiesAsStrings.add( currentToken );
          }
        }
      }
      userAttrib.setAuthoritiesAsString( authoritiesAsStrings );

      if ( userAttrib.isValid() ) {
        setValue( userAttrib );
      } else {
        setValue( null );
      }
    } else {
      setValue( null );
    }
  }
}
