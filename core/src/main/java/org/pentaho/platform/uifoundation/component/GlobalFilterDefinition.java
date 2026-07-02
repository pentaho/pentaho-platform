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
