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


package org.pentaho.platform.repository2.userroledao.jackrabbit.security;

import org.apache.commons.lang.Validate;
import org.apache.jackrabbit.core.security.authentication.CryptedSimpleCredentials;
import org.pentaho.platform.engine.security.messages.Messages;
import org.pentaho.platform.util.StringUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.jcr.SimpleCredentials;

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
      CryptedSimpleCredentials credentials = new CryptedSimpleCredentials( "dummyUser", encPass );
      return credentials.matches( new SimpleCredentials( "dummyUser", rawPass.toCharArray() ) );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  @Override public String encode( CharSequence charSequence ) {
    return this.encodePassword( charSequence.toString(), null );
  }

  @Override public boolean matches( CharSequence charSequence, String s ) {
    return isPasswordValid( s, charSequence.toString(), null );
  }
}
