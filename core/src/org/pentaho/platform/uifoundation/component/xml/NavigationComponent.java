/*
 * 
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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Jul 22, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.uifoundation.component.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.ui.INavigationComponent;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.uifoundation.messages.Messages;

public class NavigationComponent extends XmlComponent implements INavigationComponent {

  private static final long serialVersionUID = 851537694797388747L;

  private static final Log logger = LogFactory.getLog(NavigationComponent.class);

  @Override
  public Log getLogger() {
    return NavigationComponent.logger;
  }

  public NavigationComponent() {
    super(null, null, null);
    SimpleParameterProvider parameters = new SimpleParameterProvider();
    setParameterProvider("options", parameters); //$NON-NLS-1$
    //    setXsl( "text/wap", "nav_wap.xsl" );
    //    setXsl( "text/iphone", "nav_iphone.xsl" );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.ui.component.BaseUIComponent#validate()
   */
  @Override
  public boolean validate() {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.ui.IInterfaceComponent#getXmlContent()
   */
  @Override
  public Document getXmlContent() {

    String hrefUrl = getParameter("hrefurl", ""); //$NON-NLS-1$ //$NON-NLS-2$
    String onClick = getParameter("onclick", ""); //$NON-NLS-1$ //$NON-NLS-2$
    String solutionParamName = getParameter("solutionparam", ""); //$NON-NLS-1$ //$NON-NLS-2$;
    String pathParamName = getParameter("pathparam", ""); //$NON-NLS-1$ //$NON-NLS-2$
    String options = getParameter("options", ""); //$NON-NLS-1$ //$NON-NLS-2$
    String path = getParameter(pathParamName, null);
    String allowNavigation = getParameter("navigate", "true"); //$NON-NLS-1$ //$NON-NLS-2$

    setXslProperty("href", hrefUrl); //$NON-NLS-1$ 
    setXslProperty("onClick", onClick); //$NON-NLS-1$ 
    setXslProperty("solutionParam", solutionParamName); //$NON-NLS-1$ 
    setXslProperty("pathParam", pathParamName); //$NON-NLS-1$ 
    setXslProperty("options", options); //$NON-NLS-1$ 
    setXslProperty("navigate", allowNavigation); //$NON-NLS-1$ 
    setXslProperty("baseUrl", urlFactory.getDisplayUrlBuilder().getUrl()); //$NON-NLS-1$ 
    if ("".equals(path)) { //$NON-NLS-1$
      path = null;
    }
    if (path != null) {
      setXslProperty("path", path); //$NON-NLS-1$
    }

    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, getSession());

    if (repository == null) {
      error(Messages.getInstance().getErrorString("NavigationComponent.ERROR_0001_BAD_SOLUTION_REPOSITORY")); //$NON-NLS-1$
      return null;
    }

    String solution = getParameter(solutionParamName, null);
    if ("".equals(solution)) { //$NON-NLS-1$
      solution = null;
    }
    Document document = repository.getNavigationUIDocument(solution, path, ISolutionRepository.ACTION_EXECUTE);

    // see if the xsl name has already been set
    String startingXSLName = getXsl("text/html"); //$NON-NLS-1$
    String xslName = getXSLName(document, solution, startingXSLName);

    setXslProperty("solution", solution); //$NON-NLS-1$ 
    if (xslName == null) {
      // the template has not been set, so provide a default
      xslName = "files-list.xsl"; //$NON-NLS-1$
    }
    setXsl("text/html", xslName); //$NON-NLS-1$Xsl( 
    return document;
  }

  public void setHrefUrl(final String hrefUrl) {
    SimpleParameterProvider parameters = (SimpleParameterProvider) getParameterProviders().get("options"); //$NON-NLS-1$
    parameters.setParameter("hrefurl", hrefUrl); //$NON-NLS-1$
  }

  public String getXSLName(final Document document, final String solution, final String inputXSLName) {
    String xslName = inputXSLName;
    if (solution == null) {
      if (xslName == null) {
        Node node = document.selectSingleNode("/repository/@displaytype"); //$NON-NLS-1$
        if (node != null) {
          String displayType = node.getText();
          if (displayType.endsWith(".xsl")) { //$NON-NLS-1$
            // this folder has a custom XSL
            xslName = displayType;
          }
        }
      }
    } else {
      if (xslName == null) {
        Node node = document.selectSingleNode("/files/@displaytype"); //$NON-NLS-1$
        if (node != null) {
          String displayType = node.getText();
          if (displayType.endsWith(".xsl")) { //$NON-NLS-1$
            // this folder has a custom XSL
            xslName = displayType;
          }
        }
      }
    }
    return xslName;
  }
  
  public void setOnClick(final String onClick) {
    SimpleParameterProvider parameters = (SimpleParameterProvider) getParameterProviders().get("options"); //$NON-NLS-1$
    parameters.setParameter("onClick", onClick); //$NON-NLS-1$
  }

  public void setAllowNavigation(final Boolean allowNavigation) {
    SimpleParameterProvider parameters = (SimpleParameterProvider) getParameterProviders().get("options"); //$NON-NLS-1$
    parameters.setParameter("navigate", allowNavigation.toString()); //$NON-NLS-1$
  }

  public void setSolutionParamName(final String solutionParamName) {
    SimpleParameterProvider parameters = (SimpleParameterProvider) getParameterProviders().get("options"); //$NON-NLS-1$
    parameters.setParameter("solutionparam", solutionParamName); //$NON-NLS-1$
  }

  public void setPathParamName(final String solutionPathName) {
    SimpleParameterProvider parameters = (SimpleParameterProvider) getParameterProviders().get("options"); //$NON-NLS-1$
    parameters.setParameter("pathparam", solutionPathName); //$NON-NLS-1$
  }

  public void setOptions(final String options) {
    SimpleParameterProvider parameters = (SimpleParameterProvider) getParameterProviders().get("options"); //$NON-NLS-1$
    parameters.setParameter("options", options); //$NON-NLS-1$
  }
}
