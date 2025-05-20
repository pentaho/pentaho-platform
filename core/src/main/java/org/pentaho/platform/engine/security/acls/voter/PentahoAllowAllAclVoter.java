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

import org.pentaho.platform.api.engine.IAclEntry;
import org.pentaho.platform.api.engine.IAclHolder;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

@SuppressWarnings( "deprecation" )
public class PentahoAllowAllAclVoter extends AbstractPentahoAclVoter {

  public boolean hasAccess( final IPentahoSession session, final IAclHolder holder, final int mask ) {
    // Return true indicating that there are no access prohibitions.
    return true;
  }

  @Override
  public Authentication getAuthentication( final IPentahoSession session ) {
    return SecurityHelper.getInstance().getAuthentication();
  }

  public IAclEntry[] getEffectiveAcls( final IPentahoSession session, final IAclHolder holder ) {
    // Returns all the ACLs on the object which indicates that the
    // user has all the necessary acls to access the object.
    List allAcls = holder.getEffectiveAccessControls();
    IAclEntry[] acls = new IAclEntry[allAcls.size()];
    acls = (IAclEntry[]) allAcls.toArray( acls );
    return acls;
  }

  public IPentahoAclEntry getEffectiveAcl( final IPentahoSession session, final IAclHolder holder ) {
    IPentahoAclEntry rtn = new PentahoAclEntry();
    rtn.setMask( IPentahoAclEntry.PERM_FULL_CONTROL );
    return rtn;
  }

  @Override
  public boolean isPentahoAdministrator( final IPentahoSession session ) {
    // This system is wide open. All users are managers.
    return true;
  }

  @Override
  public boolean isGranted( final IPentahoSession session, final GrantedAuthority auth ) {
    // This system is wide open. Everyone is granted everything.
    return true;
  }

}
