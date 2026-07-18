/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.api.util;

public interface IPasswordService {
  public static final String IPASSWORD_SERVICE = "IPasswordService"; //$NON-NLS-1$

  public String decrypt( String encryptedPassword ) throws PasswordServiceException;

  public String encrypt( String password ) throws PasswordServiceException;

}
