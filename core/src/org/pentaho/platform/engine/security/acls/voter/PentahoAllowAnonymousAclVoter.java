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
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.security.acls.voter;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.springframework.security.Authentication;

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
