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
