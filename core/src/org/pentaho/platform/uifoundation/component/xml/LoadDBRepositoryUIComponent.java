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
 * Created Mar 9, 2006 
 * @author wseyler
 */

package org.pentaho.platform.uifoundation.component.xml;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository;
import org.pentaho.platform.uifoundation.messages.Messages;

public class LoadDBRepositoryUIComponent extends XmlComponent {
  private static final String PATH_STR = "path"; //$NON-NLS-1$

  private static final String ROOT = "root"; //$NON-NLS-1$

  private static final String RESULT = "result"; //$NON-NLS-1$

  private static final String TYPE_ATTRIBUTE = "result-type"; //$NON-NLS-1$

  private static final String SUCCESS = "success"; //$NON-NLS-1$

  private static final String FAILURE = "failed"; //$NON-NLS-1$

  private static final String SHOW_INPUT = "show-input"; //$NON-NLS-1$

  private static final String ORPHANED = "orphaned-files"; //$NON-NLS-1$

  private static final String FILENAME = "file-name"; //$NON-NLS-1$

  private static final String ORPHANHANDLING = "orphan-handling"; //$NON-NLS-1$

  private static final String PATHTITLE = "path-title"; //$NON-NLS-1$

  private static final String DELETETITLE = "delete-title"; //$NON-NLS-1$

  IPentahoSession session = null;

  private static final Log logger = LogFactory.getLog(LoadDBRepositoryUIComponent.class);

  private static final long serialVersionUID = 1L;

  public LoadDBRepositoryUIComponent(final IPentahoUrlFactory urlFactory, final List messages,
      final IPentahoSession session) {
    super(urlFactory, messages, null);
    this.session = session;
    setXsl("text/html", "LoadDBRepository.xsl"); //$NON-NLS-1$ //$NON-NLS-2$
    setXslProperty("baseUrl", urlFactory.getDisplayUrlBuilder().getUrl()); //$NON-NLS-1$ 
  }

  private Document doLoad(final String solutionRoot, final boolean deleteOrphans) {

    Document document = DocumentHelper.createDocument();
    document.setName(LoadDBRepositoryUIComponent.PATH_STR);
    Element root = document.addElement(LoadDBRepositoryUIComponent.ROOT);
    Element result = root.addElement(LoadDBRepositoryUIComponent.RESULT);
    boolean usingDbRepository = true;
    try {
      ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, session);
      if (!(repository instanceof DbBasedSolutionRepository)) {
        usingDbRepository = false;
        repository = new DbBasedSolutionRepository();
      }
      List orphanedFiles = ((DbBasedSolutionRepository) repository).loadSolutionFromFileSystem(this.session,
          solutionRoot, deleteOrphans);
      result.addAttribute(LoadDBRepositoryUIComponent.TYPE_ATTRIBUTE, LoadDBRepositoryUIComponent.SUCCESS);
      if (usingDbRepository) {
        result.addText(Messages.getInstance().getString("LoadDBRepositoryUIComponent.INFO_0001_SUCCESS")); //$NON-NLS-1$
      } else {
        result.addText(Messages.getInstance().getString("LoadDBRepositoryUIComponent.INFO_0002_SUCCESS_NEED_CONFIG")); //$NON-NLS-1$
      }
      if ((orphanedFiles != null) && (orphanedFiles.size() > 0)) {
        Iterator iter = orphanedFiles.iterator();
        Element orphans = result.addElement(LoadDBRepositoryUIComponent.ORPHANED);
        orphans
            .addElement(LoadDBRepositoryUIComponent.ORPHANHANDLING)
            .addText(
                deleteOrphans ? Messages.getInstance().getString("LoadDBRepositoryUIComponent.INFO_0004_ORPHANED_DELETED") : Messages.getInstance().getString("LoadDBRepositoryUIComponent.INFO_0005_ORPHANED_IGNORED")); //$NON-NLS-1$ //$NON-NLS-2$
        while (iter.hasNext()) {
          orphans.addElement(LoadDBRepositoryUIComponent.FILENAME).addText(((String) iter.next()));
        }
      }
    } catch (Exception e) {
      result.addAttribute(LoadDBRepositoryUIComponent.TYPE_ATTRIBUTE, LoadDBRepositoryUIComponent.FAILURE);
      result.addText(Messages.getInstance().getString("LoadDBRepositoryUIComponent.ERROR_0001_LOAD_ERROR") + solutionRoot); //$NON-NLS-1$
      e.printStackTrace();
    }
    return document;
  }

  protected Document showInputPage() {
    Document document = DocumentHelper.createDocument();
    document.setName(LoadDBRepositoryUIComponent.PATH_STR);
    Element root = document.addElement(LoadDBRepositoryUIComponent.RESULT);
    root.addAttribute(LoadDBRepositoryUIComponent.TYPE_ATTRIBUTE, LoadDBRepositoryUIComponent.SHOW_INPUT);
    root.addElement(LoadDBRepositoryUIComponent.PATHTITLE).addText(
        Messages.getInstance().getString("LoadDBRepositoryUIComponent.INFO_0003_ENTER_PATH")); //$NON-NLS-1$
    root.addElement(LoadDBRepositoryUIComponent.DELETETITLE).addText(
        Messages.getInstance().getString("LoadDBRepositoryUIComponent.INFO_0006_DELETE_ORPHANS_TITLE")); //$NON-NLS-1$
    return document;
  }

  @Override
  public Document getXmlContent() {

    String solutionRoot = getParameter(LoadDBRepositoryUIComponent.PATH_STR, null);
    if ((solutionRoot == null) || (solutionRoot.length() < 1)) {
      return showInputPage();
    } else {
      boolean deleteOrphans = "on".equalsIgnoreCase(getParameter("delete", "off")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      return doLoad(solutionRoot, deleteOrphans);
    }
  }

  @Override
  public Log getLogger() {
    return LoadDBRepositoryUIComponent.logger;
  }

  @Override
  public boolean validate() {
    return true;
  }
}
