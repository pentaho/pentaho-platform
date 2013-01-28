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
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.ISolutionFilter;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.uifoundation.messages.Messages;

public class SolutionTreeUIComponent extends XmlComponent implements ISolutionFilter {

  private static final long serialVersionUID = 1L;

  private static final Log logger = LogFactory.getLog(SolutionTreeUIComponent.class);

  protected IPentahoSession session = null;

  public SolutionTreeUIComponent(final IPentahoUrlFactory urlFactory, final List messages, final IPentahoSession session) {
    super(urlFactory, messages, null);
    this.session = session;
    setXsl("text/html", "xmlTree.xsl"); //$NON-NLS-1$ //$NON-NLS-2$
    setXslProperty("baseUrl", urlFactory.getDisplayUrlBuilder().getUrl()); //$NON-NLS-1$ 
  }

  public boolean keepFile(final ISolutionFile solutionFile, final int actionOperation) {
    return SecurityHelper.getInstance().canHaveACLS(solutionFile);
  }

  @Override
  public Document getXmlContent() {
    if (SecurityHelper.getInstance().isPentahoAdministrator(session)) {
      try {
        ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, session);
        return repository.getSolutionTree(ISolutionRepository.ACTION_ADMIN, this);
      } catch (Exception e) {
        Document document = DocumentHelper.createDocument();
        document
            .addElement("error").setText(Messages.getInstance().getString("SolutionTreeUIComponent.ERROR_0001_PERMISSIONS_NOT_SUPPORTED")); //$NON-NLS-1$ //$NON-NLS-2$
        return document;
      }
    } else {
      return null;
    }
  }

  @Override
  public Log getLogger() {
    return SolutionTreeUIComponent.logger;
  }

  @Override
  public boolean validate() {
    return true;
  }

}
