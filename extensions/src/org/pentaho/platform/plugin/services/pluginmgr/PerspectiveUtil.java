/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Oct 6, 2011 
 * @author wseyler
 */


package org.pentaho.platform.plugin.services.pluginmgr;

import org.dom4j.Element;
import org.pentaho.platform.api.engine.perspective.IPluginPerspectiveManager;
import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.pluginmgr.perspective.pojo.DefaultPluginPerspective;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.impl.DefaultXulOverlay;

/**
 * @author wseyler
 *
 */
public class PerspectiveUtil {
  static IPluginPerspective createPerspective(Element perspectiveNode) {
    if (perspectiveNode != null) {
      String title = perspectiveNode.attributeValue("title"); //$NON-NLS-1$
      String id = perspectiveNode.attributeValue("id"); //$NON-NLS-1$
      String contentUrl = perspectiveNode.attributeValue("content-url"); //$NON-NLS-1$
      XulOverlay menuOverlay = processOverlay((Element)perspectiveNode.selectSingleNode("//menu-overlay")); //$NON-NLS-1$
      XulOverlay toolbarOverlay = processOverlay((Element)perspectiveNode.selectSingleNode("//toolbar-overlay")); //$NON-NLS-1$
      
      IPluginPerspective perspective = new DefaultPluginPerspective();
      perspective.setTitle(title);
      perspective.setId(id);
      perspective.setContentUrl(contentUrl);
      perspective.setMenuBarOverlay(menuOverlay);
      perspective.setToolBarOverlay(toolbarOverlay);
      PentahoSystem.get(IPluginPerspectiveManager.class).addPluginPerspective(perspective);
      return perspective;
    }
    return null;
  }

  /**
   * @param selectSingleNode
   * @return
   */
  static private XulOverlay processOverlay(Element node) {
    if (node == null) {
      return null;
    }
    String xml = null;
    String id = node.attributeValue("id"); //$NON-NLS-1$
    String resourceBundleUri = node.attributeValue("resourcebundle"); //$NON-NLS-1$
    if (node.elements() != null && node.elements().size() > 0) {
      xml = ((Element) node.elements().get(0)).asXML();
    }
    return new DefaultXulOverlay(id, null, xml, resourceBundleUri);
  }
}
