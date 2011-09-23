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
 * @created Sep 20, 2007 
 * @author wseyler
 */

package org.pentaho.platform.engine.services.metadata.cwm;

import org.pentaho.pms.core.CWM;
import org.pentaho.pms.factory.CwmSchemaFactory;
import org.pentaho.pms.schema.security.SecurityService;

/**
 * @author wseyler
 *
 * This class is no longer used, it has been replaced with MetadataDomainRepository.
 *
 * @deprecated
 */
public class PlatformCWMSchemaFactory extends CwmSchemaFactory {
  private SecurityService securityService = null;

  @Override
  public SecurityService getSecurityService(final CWM cwm) {
    if (securityService == null) {
      securityService = new PlatformSecurityService();
    }
    return securityService;
  }
}
