/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.pluginmgr;

import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.platform.plugin.services.pluginmgr.perspective.pojo.DefaultPluginPerspective;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.impl.DefaultXulOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author wseyler
 * 
 */
public class PerspectiveUtil {
  public static final int DEFAULT_LAYOUT_PRIORITY = 1000;

  static IPluginPerspective createPerspective( Element perspectiveNode ) {
    if ( perspectiveNode != null ) {
      String title = perspectiveNode.attributeValue( "title" ); //$NON-NLS-1$
      String id = perspectiveNode.attributeValue( "id" ); //$NON-NLS-1$
      String contentUrl = perspectiveNode.attributeValue( "content-url" ); //$NON-NLS-1$
      String resourceBundleUri = perspectiveNode.attributeValue( "resourcebundle" ); //$NON-NLS-1$
      String layoutPriorityStr = perspectiveNode.attributeValue( "layout-priority" ); //$NON-NLS-1$
      int layoutPriority = DEFAULT_LAYOUT_PRIORITY;
      if ( layoutPriorityStr != null && layoutPriorityStr.length() > 0 ) {
        try {
          layoutPriority = Integer.parseInt( layoutPriorityStr );
        } catch ( Exception e ) {
          layoutPriority = DEFAULT_LAYOUT_PRIORITY;
        }
      }

      String securityActionStr = perspectiveNode.attributeValue( "required-security-action" );
      ArrayList<String> actions = new ArrayList<String>();
      if ( securityActionStr != null ) {
        StringTokenizer st = new StringTokenizer( securityActionStr, ";, " );
        while ( st.hasMoreTokens() ) {
          String action = st.nextToken();
          actions.add( action );
        }
      }

      ArrayList<XulOverlay> overlays = processOverlays( perspectiveNode ); //$NON-NLS-1$

      IPluginPerspective perspective = new DefaultPluginPerspective();
      perspective.setTitle( title );
      perspective.setId( id );
      perspective.setContentUrl( contentUrl );
      perspective.setLayoutPriority( layoutPriority );
      perspective.setOverlays( overlays );
      perspective.setRequiredSecurityActions( actions );
      perspective.setResourceBundleUri( resourceBundleUri );

      return perspective;
    }
    return null;
  }

  private static ArrayList<XulOverlay> processOverlays( Element node ) {
    if ( node == null ) {
      return null;
    }
    ArrayList<XulOverlay> overlays = new ArrayList<XulOverlay>();

    @SuppressWarnings( "unchecked" )
    List<Node> overlayElements = (List<Node>) node.selectNodes( "overlay" );
    for ( Node overlayNode : overlayElements ) {
      DefaultXulOverlay overlay;

      // reuse static method to honor overlay priorities as well
      overlay = SystemPathXmlPluginProvider.processOverlay( (Element) overlayNode );
      if ( overlay != null ) {
        overlays.add( overlay );
      }
    }

    return overlays;
  }
}
