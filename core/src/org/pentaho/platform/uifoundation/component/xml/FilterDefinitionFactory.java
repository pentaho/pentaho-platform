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
