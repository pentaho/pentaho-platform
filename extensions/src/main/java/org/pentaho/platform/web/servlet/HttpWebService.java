/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.web.servlet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.ActionSequenceJCRHelper;
import org.pentaho.platform.engine.services.SoapHelper;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.uifoundation.chart.ChartDefinition;
import org.pentaho.platform.uifoundation.chart.ChartHelper;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.http.request.HttpWebServiceRequestHandler;
import org.pentaho.platform.web.http.session.HttpSessionParameterProvider;
import org.pentaho.platform.web.servlet.messages.Messages;
import org.pentaho.reporting.libraries.base.util.StringUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Servlet Class
 * 
 * web.servlet name="ViewAction" display-name="Name for ViewAction" description="Description for ViewAction"
 * web.servlet-mapping url-pattern="/ViewAction" web.servlet-init-param name="A parameter" value="A value"
 */
public class HttpWebService extends ServletBase {

  /**
   * 
   */
  private static final long serialVersionUID = -2011812808062152707L;

  private static final Log logger = LogFactory.getLog( HttpWebService.class );

  @Override
  public Log getLogger() {
    return HttpWebService.logger;
  }

  /**
   * 
   */
  public HttpWebService() {
    super();
  }

  public String getPayloadAsString( final HttpServletRequest request ) throws IOException {
    BufferedReader reader = request.getReader();
    if ( reader != null ) {
      return IOUtils.toString( reader );
    } else {
      return null;
    }
  }

  public void doGetFixMe( final HttpServletRequest request, final HttpServletResponse response )
    throws ServletException, IOException {

    //
    // System Entry/Exit point handled by the doGet method.
    //

    try {
      String actionPath = request.getParameter( "path" ); //$NON-NLS-1$
      String solutionName = actionPath.substring( 0, actionPath.indexOf( '/', 1 ) );
      String actionName = actionPath.substring( actionPath.lastIndexOf( '/' ) );

      String actionSeqPath = ActionInfo.buildSolutionPath( solutionName, actionPath, actionName );

      String component = request.getParameter( "component" ); //$NON-NLS-1$
      String content = getPayloadAsString( request );

      IParameterProvider parameterProvider = null;
      HashMap parameters = new HashMap();
      if ( ( content != null ) && ( content.length() > 0 ) ) {
        Document doc = XmlDom4JHelper.getDocFromString( content, new PentahoEntityResolver() );
        List parameterNodes = doc.selectNodes( "//SOAP-ENV:Body/*/*" ); //$NON-NLS-1$
        for ( int i = 0; i < parameterNodes.size(); i++ ) {
          Node parameterNode = (Node) parameterNodes.get( i );
          String parameterName = parameterNode.getName();
          String parameterValue = parameterNode.getText();
          // String type = parameterNode.selectSingleNode( "@type" );
          // if( "xml-data".equalsIgnoreCase( ) )
          if ( "action".equals( parameterName ) ) { //$NON-NLS-1$
            ActionInfo info = ActionInfo.parseActionString( parameterValue );
            solutionName = info.getSolutionName();
            actionPath = info.getPath();
            actionName = info.getActionName();
          } else if ( "component".equals( parameterName ) ) { //$NON-NLS-1$
            component = parameterValue;
          } else {
            parameters.put( parameterName, parameterValue );
          }

        }
        parameterProvider = new SimpleParameterProvider( parameters );
      } else {
        parameterProvider = new HttpRequestParameterProvider( request );
      }

      response.setContentType( "text/xml" ); //$NON-NLS-1$
      response.setCharacterEncoding( LocaleHelper.getSystemEncoding() );
      // PentahoHttpSession userSession = new PentahoHttpSession(
      // request.getRemoteUser(), request.getSession(),
      // request.getLocale() );
      IPentahoSession userSession = getPentahoSession( request );

      String instanceId = request.getParameter( "instance-id" ); //$NON-NLS-1$
      String processId = this.getClass().getName();

      OutputStream contentStream = new ByteArrayOutputStream();

      SimpleOutputHandler outputHandler = new SimpleOutputHandler( contentStream, false );

      // send the header of the message to prevent time-outs while we are
      // working
      OutputStream outputStream = response.getOutputStream();
      if ( ( component == null ) || "action".equals( component ) ) { //$NON-NLS-1$
        // assume this is an action sequence execute
        HttpWebServiceRequestHandler requestHandler =
            new HttpWebServiceRequestHandler( userSession, null, outputHandler, parameterProvider, null );

        requestHandler.setParameterProvider( IParameterProvider.SCOPE_SESSION, new HttpSessionParameterProvider(
          userSession ) );
        requestHandler.setInstanceId( instanceId );
        requestHandler.setProcessId( processId );
        requestHandler.setActionPath( actionSeqPath );

        if ( ServletBase.debug ) {
          debug( Messages.getInstance().getString( "HttpWebService.DEBUG_WEB_SERVICE_START" ) ); //$NON-NLS-1$
        }
        IRuntimeContext runtime = null;
        try {
          runtime = requestHandler.handleActionRequest( 0, 0 );
          Document responseDoc =
              SoapHelper.createSoapResponseDocument( runtime, outputHandler, contentStream, requestHandler
                .getMessages() );
          XmlDom4JHelper.saveDom( responseDoc, outputStream, PentahoSystem.getSystemSetting( "web-service-encoding",
            "utf-8" ), true );
        } finally {
          if ( runtime != null ) {
            runtime.dispose();
          }
        }
      } else if ( "dial".equals( component ) ) { //$NON-NLS-1$
        doDial( solutionName, actionPath, actionName, parameterProvider, outputStream, userSession );
      } else if ( "chart".equals( component ) ) { //$NON-NLS-1$
        doChart( actionPath, parameterProvider, outputStream, userSession );
      } else if ( "xaction-parameter".equals( component ) ) { //$NON-NLS-1$
        doParameter( solutionName, actionPath, actionName, parameterProvider, outputStream, userSession, response );
      }

    } catch ( Throwable t ) {
      error( Messages.getInstance().getErrorString( "HttpWebService.ERROR_0001_ERROR_DURING_WEB_SERVICE" ), t ); //$NON-NLS-1$
    }
    if ( ServletBase.debug ) {
      debug( Messages.getInstance().getString( "HttpWebService.DEBUG_WEB_SERVICE_END" ) ); //$NON-NLS-1$
    }

  }

  @Override
  protected void doPost( final HttpServletRequest request, final HttpServletResponse response )
    throws ServletException, IOException {

    doGet( request, response );

  }

  private void doParameter( final String solutionName, final String actionPath, final String actionName,
      final IParameterProvider parameterProvider, final OutputStream outputStream, final IPentahoSession userSession,
      final HttpServletResponse response ) throws IOException {

    final IActionSequence actionSequence =
        new ActionSequenceJCRHelper().getActionSequence( ActionInfo.buildSolutionPath( solutionName, actionPath,
            actionName ), PentahoSystem.loggingLevel, RepositoryFilePermission.READ );
    if ( actionSequence == null ) {
      logger.debug( Messages.getInstance().getString( "HttpWebService.ERROR_0002_NOTFOUND", solutionName, actionPath,
        actionName ) );
      response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
      return;
    }

    final Document document = DocumentHelper.createDocument();
    try {
      final Element parametersElement = document.addElement( "parameters" );

      // noinspection unchecked
      final Map<String, IActionParameter> params =
          actionSequence.getInputDefinitionsForParameterProvider( IParameterProvider.SCOPE_REQUEST );
      for ( final Map.Entry<String, IActionParameter> entry : params.entrySet() ) {
        final String paramName = entry.getKey();
        final IActionParameter paramDef = entry.getValue();
        final String value = paramDef.getStringValue();
        final Class type;
        // yes, the actual type-code uses equals-ignore-case and thus allows the user
        // to specify type information in a random case. sTrInG is equal to STRING is equal to the value
        // defined as constant (string)
        if ( IActionParameter.TYPE_LIST.equalsIgnoreCase( paramDef.getType() ) ) {
          type = String[].class;
        } else {
          type = String.class;
        }
        final String label = paramDef.getSelectionDisplayName();

        final String[] values;
        if ( StringUtils.isEmpty( value ) ) {
          values = new String[0];
        } else {
          values = new String[] { value };
        }

        createParameterElement( parametersElement, paramName, type, label, "user", "parameters", values );
      }

      // built in parameters: solution, path, action, prompt, instance-id
      createParameterElement( parametersElement, "solution", String.class, null, "system", "system",
          new String[] { solutionName } );
      createParameterElement( parametersElement, "path", String.class, null, "system", "system",
          new String[] { actionPath } );
      createParameterElement( parametersElement, "action", String.class, null, "system", "system",
          new String[] { actionName } );
      createParameterElement( parametersElement, "prompt", String.class, null, "system", "system", new String[] {
        "yes", "no" } );
      createParameterElement( parametersElement, "instance-id", String.class, null, "system", "system",
          new String[] { parameterProvider.getStringParameter( "instance-id", null ) } );

    } catch ( Exception e ) {
      logger.warn( Messages.getInstance().getString( "HttpWebService.ERROR_0003_UNEXPECTED" ), e );
      response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
      return;
    }
    // no close, as far as I know tomcat does not like it that much ..
    final XMLWriter writer = new XMLWriter( outputStream, OutputFormat.createPrettyPrint() );
    writer.write( document );
    writer.flush();
  }

  private Element createParameterElement( final Element parametersElement, final String paramName, final Class type,
      final String label, final String role, final String group, final String[] values ) {
    final Element parameterElement = parametersElement.addElement( "parameter" );
    parameterElement.addAttribute( "name", paramName );
    parameterElement.addAttribute( "type", type.getName() );

    if ( StringUtils.isEmpty( label ) == false ) {
      final Element labelAttr = parameterElement.addElement( "attribute" );
      labelAttr.addAttribute( "namespace", "http://reporting.pentaho.org/namespaces/engine/parameter-attributes/core" );
      labelAttr.addAttribute( "name", "label" );
      labelAttr.addAttribute( "value", label );
    }

    final Element roleAttr = parameterElement.addElement( "attribute" );
    roleAttr.addAttribute( "namespace", "http://reporting.pentaho.org/namespaces/engine/parameter-attributes/core" );
    roleAttr.addAttribute( "name", "role" );
    roleAttr.addAttribute( "value", role );

    final Element paramGroupAttr = parameterElement.addElement( "attribute" );
    paramGroupAttr.addAttribute( "namespace",
      "http://reporting.pentaho.org/namespaces/engine/parameter-attributes/core" );
    paramGroupAttr.addAttribute( "name", "parameter-group" );
    paramGroupAttr.addAttribute( "value", group );

    final Element paramGroupLabelAttr = parameterElement.addElement( "attribute" );
    paramGroupLabelAttr.addAttribute( "namespace",
      "http://reporting.pentaho.org/namespaces/engine/parameter-attributes/core" );
    paramGroupLabelAttr.addAttribute( "name", "parameter-group-label" );
    paramGroupLabelAttr.addAttribute( "value", lookupParameterGroupLabel( group ) );

    if ( values.length > 0 ) {
      final Element valuesElement = parameterElement.addElement( "values" );
      for ( final String value : values ) {
        final Element valueAttr = valuesElement.addElement( "value" );
        valueAttr.addAttribute( "type", String.class.getName() );
        valueAttr.addAttribute( "value", value );
        valueAttr.addAttribute( "selected", String.valueOf( values.length == 1 ) );
      }
    }
    return parameterElement;
  }

  private String lookupParameterGroupLabel( final String group ) {
    if ( "system".equals( group ) ) {
      return Messages.getInstance().getString( "HttpWebService.PARAMETER_GROUP_SYSTEM" );
    }
    return Messages.getInstance().getString( "HttpWebService.PARAMETER_GROUP_USER" );
  }

  @SuppressWarnings( "deprecation" )
  protected void doDial( final String solutionName, final String actionPath, final String actionName,
      final IParameterProvider parameterProvider, final OutputStream outputStream, final IPentahoSession userSession ) {

    ArrayList messages = new ArrayList();
    StringBuffer buffer = new StringBuffer();
    boolean ok =
        ChartHelper.doDial( solutionName, actionPath, actionName, parameterProvider, buffer, userSession, messages,
            this );
    if ( !ok ) {
      PentahoSystem
          .get( IMessageFormatter.class, userSession )
          .formatErrorMessage(
            "text/html", Messages.getInstance().getString( "Widget.ERROR_0001_COULD_NOT_CREATE_WIDGET" ), messages,
            buffer ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    try {
      XmlDom4JHelper.saveDom( SoapHelper.createSoapResponseDocument( buffer.toString() ), outputStream, PentahoSystem
          .getSystemSetting( "web-service-encoding", "utf-8" ), true );
    } catch ( IOException e ) {
      // not much we can do here...
    }

  }

  protected void doChart( final String actionPath, final IParameterProvider parameterProvider,
      final OutputStream outputStream, final IPentahoSession userSession ) {

    String chartTypeStr = parameterProvider.getStringParameter( "chart-type", "" ); //$NON-NLS-1$ //$NON-NLS-2$
    if ( ChartDefinition.PIE_CHART_STR.equals( chartTypeStr ) ) {
      doPieChart( actionPath, parameterProvider, outputStream, userSession );
    } else {
      doOtherChart( actionPath, parameterProvider, outputStream, userSession );
    }
  }

  @SuppressWarnings( "deprecation" )
  protected void doPieChart( final String actionPath, final IParameterProvider parameterProvider,
      final OutputStream outputStream, final IPentahoSession userSession ) {

    ArrayList messages = new ArrayList();
    StringBuffer buffer = new StringBuffer();
    boolean ok = ChartHelper.doPieChart( actionPath, parameterProvider, buffer, userSession, messages, this );
    if ( !ok ) {
      PentahoSystem
          .get( IMessageFormatter.class, userSession )
          .formatErrorMessage(
            "text/html", Messages.getInstance().getString( "Widget.ERROR_0001_COULD_NOT_CREATE_WIDGET" ), messages,
            buffer ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    try {
      XmlDom4JHelper.saveDom( SoapHelper.createSoapResponseDocument( buffer.toString() ), outputStream, PentahoSystem
          .getSystemSetting( "web-service-encoding", "utf-8" ), true );
    } catch ( IOException e ) {
      // not much we can do here...
    }

  }

  protected void doOtherChart( final String actionPath, final IParameterProvider parameterProvider,
      final OutputStream outputStream, final IPentahoSession userSession ) {

    ArrayList messages = new ArrayList();
    StringBuffer buffer = new StringBuffer();
    boolean ok = ChartHelper.doChart( actionPath, parameterProvider, buffer, userSession, messages, this );
    if ( !ok ) {
      PentahoSystem
          .get( IMessageFormatter.class, userSession )
          .formatErrorMessage(
            "text/html", Messages.getInstance().getString( "Widget.ERROR_0001_COULD_NOT_CREATE_WIDGET" ), messages,
            buffer ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    try {
      XmlDom4JHelper.saveDom( SoapHelper.createSoapResponseDocument( buffer.toString() ), outputStream, PentahoSystem
          .getSystemSetting( "web-service-encoding", "utf-8" ), true );
    } catch ( IOException e ) {
      // not much we can do here...
    }

  }

  //
  // FIXME: Copied straight from something else - make this nice
  //
  /**
   * <ol>
   * <li>Make sure this request has required parameters either on query string or in body of post.</li>
   * <li>Make calls into <code>UserDetailsRoleListService</code> instance for users and roles.</li>
   * <li>Make static call into <code>PentahoAclEntry</code> for ACLs.</li>
   * <li>Construct SOAP response.</li>
   * </ol>
   */
  @Override
  public void doGet( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException,
    IOException {

    PentahoSystem.systemEntryPoint();
    try {

      if ( !isSecurityDetailsRequest( request ) ) {
        if ( HttpWebService.logger.isDebugEnabled() ) {
          HttpWebService.logger.debug( Messages.getInstance().getString(
            "HttpWebService.DEBUG_MISSING_ACTION_PARAMETER" ) ); //$NON-NLS-1$
        }
        doGetFixMe( request, response );
        return;
      }

      String responseEncoding = PentahoSystem.getSystemSetting( "web-service-encoding", "utf-8" );
      response.setContentType( "text/xml" ); //$NON-NLS-1$
      response.setCharacterEncoding( responseEncoding );

      String details = getDetailsParameter( request );
      if ( "users".equalsIgnoreCase( details ) ) { //$NON-NLS-1$
        Document usersDoc = getUsers();
        Document aclsDoc = getACLs();
        XmlDom4JHelper.saveDom( SoapHelper.createSoapResponseDocument( new Document[] { usersDoc, aclsDoc } ), response
            .getOutputStream(), responseEncoding, true );
      } else if ( "roles".equalsIgnoreCase( details ) ) { //$NON-NLS-1$

        Document rolesDoc = getRoles();
        Document aclsDoc = getACLs();
        XmlDom4JHelper.saveDom( SoapHelper.createSoapResponseDocument( new Document[] { rolesDoc, aclsDoc } ), response
            .getOutputStream(), responseEncoding, true );
      } else if ( "acls".equalsIgnoreCase( details ) ) { //$NON-NLS-1$
        Document aclsDoc = getACLs();
        XmlDom4JHelper.saveDom( SoapHelper.createSoapResponseDocument( aclsDoc ), response.getOutputStream(),
          responseEncoding, true );
      } else {

        if ( !"all".equalsIgnoreCase( details ) ) { //$NON-NLS-1$
          if ( HttpWebService.logger.isWarnEnabled() ) {
            HttpWebService.logger.warn( Messages.getInstance().getString(
                "HttpWebService.WARN_MISSING_DETAILS_PARAMETER" ) ); //$NON-NLS-1$
          }
        }
        Document usersDoc = getUsers();
        Document rolesDoc = getRoles();
        Document aclsDoc = getACLs();
        XmlDom4JHelper.saveDom(
            SoapHelper.createSoapResponseDocument( new Document[] { usersDoc, rolesDoc, aclsDoc } ), response
                .getOutputStream(), responseEncoding, true );
      }
    } finally {
      PentahoSystem.systemExitPoint();
    }
  }

  /**
   * Returns true if the required parameter is either in the query string or body.
   */
  protected boolean isSecurityDetailsRequest( final HttpServletRequest request ) {
    return hasActionInQueryString( request ) || hasActionInBody( request );
  }

  /**
   * Returns parameter named <code>details</code> either from query string or body. This parameter specifies how much
   * information the caller wants returned.
   */
  protected String getDetailsParameter( final HttpServletRequest request ) {
    // check query string first
    String details = request.getParameter( "details" ); //$NON-NLS-1$
    if ( null != details ) {
      return details;
    }

    // now check body
    String payload;
    try {
      payload = getPayloadAsString( request );
    } catch ( IOException e ) {
      if ( HttpWebService.logger.isErrorEnabled() ) {
        HttpWebService.logger.error( e );
      }
      return null;
    }
    if ( ( null != payload ) && ( payload.length() > 0 ) ) {
      Map parameters = getParameterMapFromPayload( payload );
      Object obj = parameters.get( "details" ); //$NON-NLS-1$
      if ( null != obj ) {
        return obj.toString();
      }
    }
    return null;
  }

  /**
   * Returns true if required parameter is in body of request.
   */
  protected boolean hasActionInBody( final HttpServletRequest request ) {
    String payload;
    try {
      payload = getPayloadAsString( request );
    } catch ( IOException e ) {
      if ( HttpWebService.logger.isErrorEnabled() ) {
        HttpWebService.logger.error( e );
      }
      return false;
    }
    if ( ( null != payload ) && ( payload.length() > 0 ) ) {
      Map parameters = getParameterMapFromPayload( payload );
      Object obj = parameters.get( "action" ); //$NON-NLS-1$
      if ( ( null != obj ) && obj.toString().equalsIgnoreCase( "securitydetails" ) ) { //$NON-NLS-1$
        return true;
      }

    }
    return false;
  }

  /**
   * Returns true if required parameter named <code>action</code> is in query string.
   */
  protected boolean hasActionInQueryString( final HttpServletRequest request ) {
    String action = request.getParameter( "action" ); //$NON-NLS-1$
    if ( ( "securitydetails" ).equalsIgnoreCase( action ) ) { //$NON-NLS-1$
      return true;
    } else {
      return false;
    }
  }

  protected Map getParameterMapFromPayload( final String xml ) {
    Map parameters = new HashMap();
    Document doc = null;
    try {
      doc = XmlDom4JHelper.getDocFromString( xml, new PentahoEntityResolver() );
    } catch ( XmlParseException e ) {
      error( Messages.getInstance().getErrorString( "HttpWebService.ERROR_0001_ERROR_DURING_WEB_SERVICE" ), e ); //$NON-NLS-1$
      return parameters;
    }
    List parameterNodes = doc.selectNodes( "//SOAP-ENV:Body/*/*" ); //$NON-NLS-1$
    for ( int i = 0; i < parameterNodes.size(); i++ ) {
      Node parameterNode = (Node) parameterNodes.get( i );
      String parameterName = parameterNode.getName();
      String parameterValue = parameterNode.getText();
      parameters.put( parameterName, parameterValue );
    }
    return parameters;
  }

  /**
   * Returns XML for list of users.
   */
  protected Document getUsers() throws ServletException, IOException {
    IUserRoleListService service = PentahoSystem.get( IUserRoleListService.class );
    Element rootElement = new DefaultElement( "users" );
    Document doc = DocumentHelper.createDocument( rootElement );
    if ( service != null ) {
      List users = service.getAllUsers();
      for ( Iterator usersIterator = users.iterator(); usersIterator.hasNext(); ) {
        String username = usersIterator.next().toString();
        if ( ( null != username ) && ( username.length() > 0 ) ) {
          rootElement.addElement( "user" ).addCDATA( username );
        }
      }
    }
    return doc;
  }

  /**
   * Returns XML for list of roles.
   */
  protected Document getRoles() throws ServletException, IOException {
    IUserRoleListService service = PentahoSystem.get( IUserRoleListService.class );
    Element rootElement = new DefaultElement( "roles" );
    Document doc = DocumentHelper.createDocument( rootElement );
    if ( service != null ) {
      List roles = service.getAllRoles();
      for ( Iterator rolesIterator = roles.iterator(); rolesIterator.hasNext(); ) {
        String roleName = rolesIterator.next().toString();
        if ( ( null != roleName ) && ( roleName.length() > 0 ) ) {
          rootElement.addElement( "role" ).addCDATA( roleName );
        }
      }
    }
    return doc;
  }

  /**
   * Returns XML for list of ACLs.
   */
  protected Document getACLs() throws ServletException, IOException {
    Map validPermissionsNameMap = /*PentahoAclEntry.getValidPermissionsNameMap( IPentahoAclEntry.PERMISSIONS_LIST_ALL );*/ new HashMap<String, String>();
    Element rootElement = new DefaultElement( "acls" );
    Document doc = DocumentHelper.createDocument( rootElement );
    if ( validPermissionsNameMap != null ) {
      Set aclsKeySet = validPermissionsNameMap.keySet();
      for ( Iterator aclsIterator = aclsKeySet.iterator(); aclsIterator.hasNext(); ) {
        String aclName = aclsIterator.next().toString();
        String aclMask =
            null != validPermissionsNameMap.get( aclName ) ? validPermissionsNameMap.get( aclName ).toString() : null;

        if ( ( null != aclName ) && ( aclName.length() > 0 ) && ( null != aclMask ) && ( aclMask.length() > 0 ) ) {
          Element aclElement = rootElement.addElement( "acl" );
          aclElement.addElement( "name" ).addCDATA( aclName );
          aclElement.addElement( "mask" ).setText( aclMask );
        }

      }
    }
    return doc;
  }

}
