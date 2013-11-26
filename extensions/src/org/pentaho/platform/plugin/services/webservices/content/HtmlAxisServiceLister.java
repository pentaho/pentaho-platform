/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.webservices.content;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IServiceConfig;
import org.pentaho.platform.plugin.services.webservices.AxisUtil;
import org.pentaho.platform.plugin.services.webservices.SystemSolutionAxisConfigurator;
import org.pentaho.platform.plugin.services.webservices.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A content generator for listing metadata on Axis web services.
 * 
 * @author jamesdixon
 * 
 */
public class HtmlAxisServiceLister extends AbstractAxisServiceContentGenerator {

  private static final long serialVersionUID = -1772210710764038165L;

  @SuppressWarnings( "unchecked" )
  @Override
  public void createContent( AxisConfiguration axisConfiguration, ConfigurationContext context, OutputStream out )
    throws Exception {

    HashMap serviceMap = axisConfiguration.getServices();

    StringBuilder sb = new StringBuilder();

    getPageTitle( serviceMap, sb );

    Collection servicecol = serviceMap.values();
    // list each web service
    for ( Iterator iterator = servicecol.iterator(); iterator.hasNext(); ) {
      AxisService axisService = (AxisService) iterator.next();

      getTitleSection( axisService, axisConfiguration, sb );

      getWsdlSection( axisService, sb );

      getRunSection( axisService, sb );

      getOperationsSection( axisService, sb );

    }

    getPageFooter( serviceMap, sb );

    out.write( sb.toString().getBytes( LocaleHelper.getSystemEncoding() ) );
  }

  /**
   * Writes the HTML page title area
   * 
   * @param serviceMap
   *          Map of current web services
   * @param sb
   *          StringBuilder to write content to
   */
  @SuppressWarnings( "unchecked" )
  protected void getPageTitle( HashMap serviceMap, StringBuilder sb ) {
    // write out the page title
    sb.append( "<div id=\"webservicediv\">" ); //$NON-NLS-1$
    sb.append( "<h1>" ).append( Messages.getInstance().getString( "ListServices.USER_WEB_SERVICES" ) ).append(
      "</h1>\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    if ( serviceMap.isEmpty() ) {
      // there are no services defined
      sb.append( Messages.getInstance().getString( "ListServices.USER_NO_SERVICES" ) ); //$NON-NLS-1$
    }
  }

  /**
   * Writes the title section for a service to the HTML page
   * 
   * @param axisService
   *          the Axis service
   * @param axisConfiguration
   *          the Axis configuration
   * @param sb
   *          StringBuilder to write content to
   */
  protected void getTitleSection( AxisService axisService, AxisConfiguration axisConfiguration, StringBuilder sb ) {

    // get the wrapper for the web service so we can get the localized title and description
    IServiceConfig wsDef =
        AxisUtil
            .getSourceDefinition( axisService, (SystemSolutionAxisConfigurator) axisConfiguration.getConfigurator() );

    sb.append( "<table>\n<tr>\n<td colspan=\"2\"><h2>" ).append( wsDef.getTitle() ).append(
      "</h2></td></tr>\n<tr><td>" ); //$NON-NLS-1$ //$NON-NLS-2$

    String serviceDescription = axisService.getDocumentation();
    if ( serviceDescription == null || "".equals( serviceDescription ) ) { //$NON-NLS-1$
      serviceDescription = Messages.getInstance().getString( "WebServicePlugin.USER_NO_DESCRIPTION" ); //$NON-NLS-1$
    }

    // write out the description
    sb.append( Messages.getInstance().getString( "WebServicePlugin.USER_SERVICE_DESCRIPTION" ) ) //$NON-NLS-1$
        .append( "</td><td>" ) //$NON-NLS-1$
        .append( serviceDescription ).append( "</td></tr>\n" ); //$NON-NLS-1$

    // write out the enable/disable controls
    sb.append( "<tr><td>" ).append( Messages.getInstance().getString( "WebServicePlugin.USER_SERVICE_STATUS" ) ) //$NON-NLS-1$ //$NON-NLS-2$

        .append( "</td><td>" ); //$NON-NLS-1$
    if ( axisService.isActive() ) {
      sb.append( Messages.getInstance().getString( "WebServicePlugin.USER_ENABLED" ) ); //$NON-NLS-1$
    } else {
      sb.append( Messages.getInstance().getString( "WebServicePlugin.USER_DISABLED" ) ); //$NON-NLS-1$
    }
  }

  /**
   * Writes the WSDL section for a service to the HTML page
   * 
   * @param axisService
   *          the Axis service
   * @param sb
   *          StringBuilder to write content to
   */
  protected void getWsdlSection( AxisService axisService, StringBuilder sb ) {
    // write out the WSDL URL
    String wsdlUrl = AxisUtil.getWebServiceWsdlUrl();
    sb.append( "<tr><td>" ).append( Messages.getInstance().getString( "WebServicePlugin.USER_SERVICE_WSDL" ) ) //$NON-NLS-1$ //$NON-NLS-2$
        .append( "</td><td><a href=\"" ).append( wsdlUrl + axisService.getName() ) //$NON-NLS-1$
        .append( "\">" ).append( wsdlUrl + axisService.getName() ) //$NON-NLS-1$
        .append( "</a></td></tr>\n" ); //$NON-NLS-1$
  }

  /**
   * Writes the execute URL section for a service to the HTML page
   * 
   * @param axisService
   *          the Axis service
   * @param sb
   *          StringBuilder to write content to
   */
  protected void getRunSection( AxisService axisService, StringBuilder sb ) {
    // write out the execution URL
    String serviceUrl = AxisUtil.getWebServiceExecuteUrl();
    sb.append( "<tr><td>" ).append( Messages.getInstance().getString( "WebServicePlugin.USER_SERVICE_URL" ) ) //$NON-NLS-1$ //$NON-NLS-2$
        .append( "</td><td><a href=\"" ).append( serviceUrl + axisService.getName() ) //$NON-NLS-1$
        .append( "\">" ).append( serviceUrl + axisService.getName() ) //$NON-NLS-1$
        .append( "</a></td></tr>\n" ); //$NON-NLS-1$

  }

  /**
   * Writes the list of operations for a service to the HTML page
   * 
   * @param axisService
   *          the Axis service
   * @param sb
   *          StringBuilder to write content to
   */
  @SuppressWarnings( "unchecked" )
  protected void getOperationsSection( AxisService axisService, StringBuilder sb ) {
    String serviceUrl = AxisUtil.getWebServiceExecuteUrl();

    // write out the operations
    Iterator it = axisService.getOperations();

    sb.append( "<tr><td valign=\"top\">" ) //$NON-NLS-1$
        .append( Messages.getInstance().getString( "WebServicePlugin.USER_OPERATIONS" ) ) //$NON-NLS-1$
        .append( "</td><td>" ); //$NON-NLS-1$
    // now do the operations
    if ( !it.hasNext() ) {
      sb.append( Messages.getInstance().getString( "WebServicePlugin.USER_NO_OPERATIONS" ) ); //$NON-NLS-1$
    } else {

      // write out the names of the operations
      // TODO localize these?
      while ( it.hasNext() ) {
        AxisOperation axisOperation = (AxisOperation) it.next();
        String opName = axisOperation.getName().getLocalPart();

        String opUrl = serviceUrl + axisService.getName() + "/" + opName; //$NON-NLS-1$

        sb.append( "<a href=\"" ) //$NON-NLS-1$
            .append( opUrl ).append( "\">" ).append( opName ) //$NON-NLS-1$
            .append( "</a>" ); //$NON-NLS-1$

        if ( it.hasNext() ) {
          sb.append( "<br/>" ); //$NON-NLS-1$
        }
      }
      sb.append( "</td></tr>\n</table>\n" ); //$NON-NLS-1$
    }
  }

  /**
   * Writes the HTML page footer
   * 
   * @param serviceMap
   *          Map of current web services
   * @param sb
   *          StringBuilder to write content to
   */
  @SuppressWarnings( "unchecked" )
  protected void getPageFooter( HashMap serviceMap, StringBuilder sb ) {
    // write out the page footer
    sb.append( "</div" ); //$NON-NLS-1$
  }

  @Override
  public String getMimeType() {
    return "text/html"; //$NON-NLS-1$
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog( HtmlAxisServiceLister.class );
  }

}
