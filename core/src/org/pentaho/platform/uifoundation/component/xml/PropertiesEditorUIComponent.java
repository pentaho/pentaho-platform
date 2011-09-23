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
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * Created Mar 14, 2006 
 * @author wseyler
 */

package org.pentaho.platform.uifoundation.component.xml;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;

public class PropertiesEditorUIComponent extends XmlComponent {

  private static final long serialVersionUID = 1L;

  private static final Log logger = LogFactory.getLog(PropertiesEditorUIComponent.class);

  private Document document = null;

  protected IPentahoSession session = null;

  protected String baseUrl = null;

  public PropertiesEditorUIComponent(final IPentahoUrlFactory urlFactory, final List messages,
      final IPentahoSession session) {
    super(urlFactory, messages, null);
    this.session = session;
    setXsl("text/html", "PropertiesEditor.xsl"); //$NON-NLS-1$ //$NON-NLS-2$
    setXslProperty("baseUrl", urlFactory.getDisplayUrlBuilder().getUrl()); //$NON-NLS-1$ 
  }

  @Override
  public Document getXmlContent() {

    if (document == null) {
      document = DocumentHelper.createDocument();
      document.addElement("root"); //$NON-NLS-1$
    }
    return document;
  }

  @Override
  public Log getLogger() {
    return PropertiesEditorUIComponent.logger;
  }

  @Override
  public boolean validate() {
    return true;
  }

}
