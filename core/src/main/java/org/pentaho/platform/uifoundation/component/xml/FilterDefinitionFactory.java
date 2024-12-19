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


package org.pentaho.platform.uifoundation.component.xml;

import org.dom4j.Element;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.uifoundation.component.ActionFilterDefinition;
import org.pentaho.platform.uifoundation.component.FilterDefinition;
import org.pentaho.platform.uifoundation.component.GlobalFilterDefinition;
import org.pentaho.platform.uifoundation.component.SessionFilterDefinition;
import org.pentaho.platform.uifoundation.component.StaticFilterDefinition;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

/**
 * Factory up the correct FilterDefinition derived class based on the contents of the <code>node</code>.
 * 
 * @author Steven Barkdull
 * 
 */
public class FilterDefinitionFactory {

  public static FilterDefinition create( final Element node, final IPentahoSession session, final ILogger logger )
    throws FilterPanelException {
    FilterDefinition fd = null;
    if ( null != XmlDom4JHelper.getNodeText( "session-attribute", node ) ) { //$NON-NLS-1$
      fd = new SessionFilterDefinition( node, session, logger );
    } else if ( null != XmlDom4JHelper.getNodeText( "global-attribute", node ) ) { //$NON-NLS-1$
      fd = new GlobalFilterDefinition( node, session, logger );
    } else if ( null != XmlDom4JHelper.getNodeText( "data-solution", node ) ) { //$NON-NLS-1$
      fd = new ActionFilterDefinition( node, session, logger );
    } else if ( null != XmlDom4JHelper.getNodeText( "static-lov", node ) ) { //$NON-NLS-1$
      fd = new StaticFilterDefinition( node, session, logger );
    } else {
      //
    }
    if ( fd != null ) {
      fd.fromXml( node );
    }
    return fd;
  }
}
