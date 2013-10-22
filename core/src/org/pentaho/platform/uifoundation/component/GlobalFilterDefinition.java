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

package org.pentaho.platform.uifoundation.component;

import org.dom4j.Element;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.uifoundation.messages.Messages;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.util.Map;

/**
 * 
 * @author Steven Barkdull
 * 
 */
public class GlobalFilterDefinition extends FilterDefinition {

  public GlobalFilterDefinition( final Element node, final IPentahoSession session, final ILogger logger ) {
    super( node, session, logger );
  }

  @Override
  protected IPentahoResultSet getResultSet( final Map parameterProviders ) {
    String globalAttribute = XmlDom4JHelper.getNodeText( "global-attribute", node ); //$NON-NLS-1$
    try {
      IPentahoResultSet data = (IPentahoResultSet) PentahoSystem.getGlobalParameters().getParameter( globalAttribute );
      return data;
    } catch ( Exception e ) {
      logger.error(
          Messages.getInstance().getString( "FilterDefinition.ERROR_0003_NOT_IN_SESSION", globalAttribute ), e ); //$NON-NLS-1$
    }
    return null;
  }

  public static void main( final String[] args ) {

  }

}
