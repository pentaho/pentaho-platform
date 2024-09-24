/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.repository2.userroledao.jackrabbit.security;

import com.google.gwt.user.server.Base64Utils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.jackrabbit.core.security.authentication.CryptedSimpleCredentials;
import org.pentaho.platform.engine.security.messages.Messages;
import org.pentaho.platform.util.StringUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.jcr.SimpleCredentials;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Default password encoder for the BI Server.
 *
 * <p>
 * This encoder uses Jackrabbit to encode the raw password.
 * </p>
 *
 * <p>
 * This class is instantiated by Hitachi Vantara Admin Console so there should not be a dependency on classes to which PAC
 * will not have access.
 * </p>
 *
 * <p>
 * This implementation of password encoding is completely independent of any datasource connection password
 * encoding.
 * </p>
 *
 * @author mlowery
 */
public class DefaultPentahoPasswordEncoder implements PasswordEncoder {

  public String encodePassword( final String rawPass, final Object salt ) throws DataAccessException {
    Validate.notNull( rawPass, Messages.getInstance().getString(
        "DefaultPentahoPasswordEncoder.ERROR_0001_RAWPASS_CANNOT_BE_NULL" ) ); //$NON-NLS-1$

    if ( StringUtil.isEmpty( rawPass ) ) {
      return rawPass;
    }
    CryptedSimpleCredentials cryptedCredentials;
    try {
      cryptedCredentials = new CryptedSimpleCredentials( new SimpleCredentials( "dummyUser", rawPass.toCharArray() ) );
      return cryptedCredentials.getPassword();
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  public boolean isPasswordValid( final String encPass, final String rawPass, final Object salt )
    throws DataAccessException {
    try {
      Validate.notNull( encPass, Messages.getInstance().getString(
          "DefaultPentahoPasswordEncoder.ERROR_0002_ENCPASS_CANNOT_BE_NULL" ) ); //$NON-NLS-1$
      Validate.notNull( rawPass, Messages.getInstance().getString(
          "DefaultPentahoPasswordEncoder.ERROR_0001_RAWPASS_CANNOT_BE_NULL" ) ); //$NON-NLS-1$
    } catch ( IllegalArgumentException e ) {
      return false;
    }
    try {
      char[] decodedPassword = decodePassword( rawPass );
      CryptedSimpleCredentials credentials = new CryptedSimpleCredentials( "dummyUser", encPass );
      return credentials.matches( new SimpleCredentials( "dummyUser", decodedPassword ) );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  private static char[] decodePassword( String rawPass ) {
    try {
      if ( !StringUtils.isEmpty( rawPass ) && rawPass.startsWith( "ENC:" ) ) {
        String password = new String( Base64Utils.fromBase64( rawPass.substring( 4 ) ), StandardCharsets.UTF_8 );
        return URLDecoder.decode( password.replace( "+", "%2B" ), StandardCharsets.UTF_8.name() ).toCharArray();
      } else {
        return rawPass.toCharArray();
      }
    } catch ( UnsupportedEncodingException e ) {
      return rawPass.toCharArray();
    }
  }

  @Override public String encode( CharSequence charSequence ) {
    return this.encodePassword( charSequence.toString(), null );
  }

  @Override public boolean matches( CharSequence charSequence, String s ) {
    return isPasswordValid( s, charSequence.toString(), null );
  }
}
