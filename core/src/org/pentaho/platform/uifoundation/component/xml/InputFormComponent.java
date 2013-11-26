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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.ISolutionActionDefinition;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.services.ActionSequenceJCRHelper;
import org.pentaho.platform.engine.services.SolutionURIResolver;
import org.pentaho.platform.uifoundation.messages.Messages;
import org.pentaho.platform.util.xml.XForm;

import java.io.File;
import java.util.List;

public class InputFormComponent extends XmlComponent {

  /**
   * 
   */
  private static final long serialVersionUID = -6106477602576378538L;

  String templateName;

  String stylesheetName;

  String solution;

  String path;

  String actionName;

  String instanceId;

  public InputFormComponent( final IPentahoUrlFactory urlFactory, final String instanceId, final String templateName,
      final String stylesheetName, final String solution, final String path, final String actionName,
      final List messages ) {
    super( urlFactory, messages, solution + File.separator + path );
    this.instanceId = instanceId;
    this.templateName = templateName;
    this.stylesheetName = stylesheetName;
    this.solution = solution;
    this.path = path;
    this.actionName = actionName;
  }

  private static final Log logger = LogFactory.getLog( InputFormComponent.class );

  @Override
  public Log getLogger() {
    return InputFormComponent.logger;
  }

  @Override
  public boolean validate() {
    boolean ok = true;

    if ( solution == null ) {
      error( Messages.getInstance().getString( "InputForm.ERROR_0001_SOLUTION_NOT_SPECIFIED" ) ); //$NON-NLS-1$
      ok = false;
    }

    if ( path == null ) {
      error( Messages.getInstance().getString( "InputForm.ERROR_0002_ACTION_NAME_NOT_SPECIFIED" ) ); //$NON-NLS-1$
      ok = false;
    }

    if ( actionName == null ) {
      // TODO log this
      error( Messages.getInstance().getString( "InputForm.ERROR_0003_ACTION_PATH_NOT_SPECIFIED" ) ); //$NON-NLS-1$
      ok = false;
    }

    return ok;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.ui.component.BaseUIComponent#getXmlContent()
   */
  @Override
  public Document getXmlContent() {

    ActionSequenceJCRHelper actionHelper = new ActionSequenceJCRHelper( getSession() );
    IActionSequence actionSequence =
        actionHelper.getActionSequence( ActionInfo.buildSolutionPath( solution, path, actionName ), getLoggingLevel(),
            RepositoryFilePermission.READ );

    if ( actionSequence == null ) {
      // TODO log this
      error( Messages.getInstance().getString( "InputForm.ERROR_0004_ACTION_NOT_FOUND" ) + solution + path + actionName ); //$NON-NLS-1$
      return null;
    }

    List actions = actionSequence.getActionDefinitionsAndSequences();
    ISolutionActionDefinition action = (ISolutionActionDefinition) actions.get( 0 );

    Node node = action.getComponentSection();
    if ( node == null ) {
      error( Messages.getInstance().getString( "InputForm.ERROR_0005_INBOX_DEFINITION_MISSING" ) + solution + path + actionName ); //$NON-NLS-1$
      return null;
    }

    if ( templateName == null ) {

      // see if the template is specified in the action document
      Node templateNode = node.selectSingleNode( "//template" ); //$NON-NLS-1$
      if ( templateNode != null ) {
        templateName = templateNode.getText();
      }
      if ( templateName == null ) {
        error( Messages.getInstance().getString( "InputForm.ERROR_0006_TEMPLATE_NOT_SPECIFIED" ) ); //$NON-NLS-1$
        return null;
      }
    }
    Node xFormNode = node.selectSingleNode( "//xForm" ); //$NON-NLS-1$

    try {

      String actionTitle = actionSequence.getTitle();
      if ( actionTitle != null ) {
        setXslProperty( "title", actionTitle ); //$NON-NLS-1$
      }

      String description = actionSequence.getDescription();
      if ( description != null ) {
        setXslProperty( "description", description ); //$NON-NLS-1$
      }

      String xFormHtml = XForm.transformSnippet( xFormNode, getSession(), new SolutionURIResolver() );
      if ( xFormHtml == null ) {
        error( Messages.getInstance().getString( "InputForm.ERROR_0007_INBOX_DEFINITION_INVALID" ) + solution + path + actionName ); //$NON-NLS-1$
        return null;
      }
      Document document = DocumentHelper.parseText( xFormHtml );
      Node xFormHtmlNode = document.selectSingleNode( "//xForm" ); //$NON-NLS-1$

      setXslProperty( "xForm", xFormHtmlNode.asXML() ); //$NON-NLS-1$

      if ( ( stylesheetName != null ) && !"".equals( stylesheetName ) ) { //$NON-NLS-1$
        setXslProperty( "css", stylesheetName ); //$NON-NLS-1$
      }
      setXsl( "text/html", templateName ); //$NON-NLS-1$

      return document;

    } catch ( Exception e ) {
      return null;
    }

  }

}
