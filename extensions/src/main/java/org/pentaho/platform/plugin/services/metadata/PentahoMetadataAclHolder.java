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


package org.pentaho.platform.plugin.services.metadata;

import org.pentaho.metadata.model.concept.Concept;
import org.pentaho.metadata.model.concept.IConcept;
import org.pentaho.metadata.model.concept.security.Security;
import org.pentaho.metadata.model.concept.security.SecurityOwner;
import org.pentaho.platform.api.engine.IAclHolder;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PentahoMetadataAclHolder implements IAclHolder {

  private List<IPentahoAclEntry> accessControls = new ArrayList<IPentahoAclEntry>();

  public PentahoMetadataAclHolder( final IConcept aclHolder ) {
    try {
      Security sec = (Security) aclHolder.getProperty( Concept.SECURITY_PROPERTY );
      if ( sec != null ) {
        Map<SecurityOwner, Integer> securityMap = sec.getOwnerAclMap();
        SecurityOwner secOwn = null;
        for ( Map.Entry<SecurityOwner, Integer> entry : securityMap.entrySet() ) {
          // We now have the SecurityOwner and the Rights in there.
          secOwn = entry.getKey();
          int rights = entry.getValue().intValue();

          if ( secOwn.getOwnerType() == SecurityOwner.OwnerType.USER ) {
            accessControls.add( new PentahoAclEntry( secOwn.getOwnerName(), rights ) );
          } else {
            accessControls.add( new PentahoAclEntry( new SimpleGrantedAuthority( secOwn.getOwnerName() ), rights ) );
          }

        }
      }
    } catch ( Throwable th ) {
      // Just being paranoid here in case something doesn't support it.
    }

  }

  public List<IPentahoAclEntry> getAccessControls() {
    return accessControls;
  }

  public List<IPentahoAclEntry> getEffectiveAccessControls() {
    return accessControls;
  }

  public void resetAccessControls( final List<IPentahoAclEntry> acls ) {
    throw new UnsupportedOperationException( "Cannot set Metadata Acls yet" ); //$NON-NLS-1$
  }

  public void setAccessControls( final List<IPentahoAclEntry> acls ) {
    throw new UnsupportedOperationException( "Cannot set Metadata Acls yet" ); //$NON-NLS-1$
  }

}
