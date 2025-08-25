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


package org.pentaho.platform.engine.security.acls.voter;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.springframework.security.core.Authentication;

/**
 * Extends the BasicAclVoter, but overrides the getAuthentication() method to allow anonymous sessions. This is the
 * simplest case to add to other voters.
 *
 * @author mbatchel
 *
 */

public class PentahoAllowAnonymousAclVoter extends PentahoBasicAclVoter {

  // Allow anonymous users to have possible acls on an entry.
  @Override
  public Authentication getAuthentication( final IPentahoSession session ) {
    return SecurityHelper.getInstance().getAuthentication( session, true );
  }

}
