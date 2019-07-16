/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 */
package org.pentaho.platform.web.http.security;

import org.dom4j.Element;
import org.pentaho.platform.api.engine.CsrfProtectionDefinition;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.references.SingletonPentahoObjectReference;
import org.pentaho.platform.web.WebUtil;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.plugin.services.messages.Messages;
import java.util.List;

public class CsrfProtectionSystemListener implements IPentahoSystemListener {

  @Override
  public boolean startup( IPentahoSession session ) {
    // Register pentaho.xml's CsrfProtectionDefinition.
    if ( PentahoSystem.isCsrfProtectionEnabled() ) {

      List csrProtectionNodes = PentahoSystem.getSystemSettings().getSystemSettings( "csrf-protection" );
      if ( csrProtectionNodes.size() > 0 ) {
        Element csrfProtectionElem = (Element) csrProtectionNodes.get( 0 );

        try {
          CsrfProtectionDefinition csrfProtectionDefinition = WebUtil.parseXmlCsrfProtectionDefinition( csrfProtectionElem );

          PentahoSystem.registerReference(
              new SingletonPentahoObjectReference.Builder<CsrfProtectionDefinition>( CsrfProtectionDefinition.class )
                  .object( csrfProtectionDefinition )
                  .build(),
              CsrfProtectionDefinition.class );

        } catch ( IllegalArgumentException parseError ) {
          Logger.warn(
              CsrfProtectionSystemListener.class.getName(),
              Messages.getInstance().getString(
                "CsrfProtectionSystemListener.WARN_CSRF_SYSTEM_NOT_REGISTERED",
                parseError.getMessage() ) );
        }
      }
    }

    return true;
  }

  @Override
  public void shutdown() {
    // Nothing required
  }
}
