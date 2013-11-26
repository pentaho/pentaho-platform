/*
 * Copyright 2002 - 2013 Pentaho Corporation.  All rights reserved.
 * 
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
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
