/*
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
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * Created May 23, 2006 
 * @author wseyler
 */

package org.pentaho.platform.api.ui;

import java.util.List;

import org.apache.commons.logging.Log;
import org.dom4j.Document;
import org.pentaho.platform.api.engine.IActionRequestHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;

public interface INavigationComponent {
  public Log getLogger();

  public boolean validate();

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.ui.IInterfaceComponent#getXmlContent()
   */
  public Document getXmlContent();

  public void setHrefUrl(String hrefUrl);

  public void setOnClick(String onClick);

  public void setAllowNavigation(Boolean allowNavigation);

  public void setSolutionParamName(String solutionParamName);

  public void setPathParamName(String solutionPathName);

  public void setOptions(String options);

  public boolean validate(IPentahoSession session, IActionRequestHandler actionRequestHandler);

  public void setXsl(String string, String xslName);

  public void setParameterProvider(String name, IParameterProvider parameterProvider);

  public String getContent(String string);

  public void setUrlFactory(IPentahoUrlFactory urlFactory);

  @SuppressWarnings("unchecked")
  public void setMessages(List messages);

  public void setLoggingLevel(int logLevel);
}
