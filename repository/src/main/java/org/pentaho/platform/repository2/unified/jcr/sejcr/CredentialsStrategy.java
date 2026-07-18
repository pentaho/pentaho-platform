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



package org.pentaho.platform.repository2.unified.jcr.sejcr;

import javax.jcr.Credentials;

/**
 * Determines the credentials passed to session.login().
 * 
 * @author mlowery
 */
public interface CredentialsStrategy {
  Credentials getCredentials();
}
