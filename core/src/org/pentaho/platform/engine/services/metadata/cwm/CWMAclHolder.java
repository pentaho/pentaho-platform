/*
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
 * Copyright 2007 - 2008 Pentaho Corporation.  All rights reserved.
 *  
 */

package org.pentaho.platform.engine.services.metadata.cwm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.pentaho.platform.api.engine.IAclHolder;
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;
import org.pentaho.pms.schema.concept.ConceptUtilityInterface;
import org.pentaho.pms.schema.security.Security;
import org.pentaho.pms.schema.security.SecurityOwner;
import org.springframework.security.GrantedAuthorityImpl;

@SuppressWarnings("deprecation")
public class CWMAclHolder implements IAclHolder {

  List accessControls = new ArrayList();

  public CWMAclHolder(final ConceptUtilityInterface aclHolder) {
    try {
      Security sec = aclHolder.getSecurity();
      if (sec != null) {
        Map securityMap = sec.getOwnerAclMap();
        Map.Entry entry = null;
        SecurityOwner secOwn = null;
        Iterator it = securityMap.entrySet().iterator();
        while (it.hasNext()) {
          entry = (Map.Entry) it.next();
          // We now have the SecurityOwner and the Rights in there.
          secOwn = (SecurityOwner) entry.getKey();
          int rights = ((Integer) entry.getValue()).intValue();
          if (secOwn.getOwnerType() == SecurityOwner.OWNER_TYPE_USER) {
            accessControls.add(new PentahoAclEntry(secOwn.getOwnerName(), rights));
          } else {
            accessControls.add(new PentahoAclEntry(new GrantedAuthorityImpl(secOwn.getOwnerName()), rights));
          }
        }
      }
    } catch (Throwable th) {
      // Just being paranoid here in case something doesn't support it.
    }

  }

  public List getAccessControls() {
    return accessControls;
  }

  public List getEffectiveAccessControls() {
    return accessControls;
  }

  public void resetAccessControls(final List acls) {
    throw new UnsupportedOperationException("Cannot set CWM Acls yet"); //$NON-NLS-1$
  }

  public void setAccessControls(final List acls) {
    throw new UnsupportedOperationException("Cannot set CWM Acls yet"); //$NON-NLS-1$
  }

}
