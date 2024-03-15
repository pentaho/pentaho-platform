/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.enunciate.Facet;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginOperation;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.RepositoryDownloadWhitelist;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.api.repository2.unified.webservices.ExecutableFileTypeDto;
import org.pentaho.platform.util.RepositoryPathEncoder;
import org.pentaho.platform.web.http.messages.Messages;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static javax.ws.rs.core.MediaType.WILDCARD;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * The RepositoryResource service retrieves the repository files through various methods.  Allows you to execute repository content.
 */
@Path ( "/repos" )
public class RepositoryResource extends AbstractJaxRSResource {

  protected IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );

  private static final Log logger = LogFactory.getLog( RepositoryResource.class );
  public static final String GENERATED_CONTENT_PERSPECTIVE = "generatedContent"; //$NON-NLS-1$

  protected IUnifiedRepository repository = PentahoSystem.get( IUnifiedRepository.class );

  protected RepositoryDownloadWhitelist whitelist;

  @GET
  @Path ( "{pathId : .+}/content" )
  @Produces ( { WILDCARD } )
  @Facet ( name = "Unsupported" )
  public Response doGetFileOrDir( @PathParam ( "pathId" ) String pathId ) throws FileNotFoundException {
    FileResource fileResource = new FileResource( httpServletResponse );
    fileResource.setWhitelist( whitelist );
    return fileResource.doGetFileOrDir( pathId );
  }

  /**
   * Takes a pathId to a file and generates a URI that represents the URL to call to generate content from that file.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho/api/repos/public:steel%20wheels:Invoice%20(report).prpt/default
   * </p>
   *
   * @param pathId @param pathId
   *
   * @return URI that represents a forwarding URL to execute to generate content from the file {pathId}.
   *
   * <p><b>Example Response:</b></p>
   *  <pre function="syntax.xml">
   *    This response does not contain data.
   *  </pre>
   */
  @GET
  @Path ( "{pathId : .+}/default" )
  @Produces ( { WILDCARD } )
  @StatusCodes ( {
    @ResponseCode ( code = 303, condition = "Successfully get the resource." ),
    @ResponseCode ( code = 404, condition = "Failed to find the resource." )
  } )
  public Response doExecuteDefault( @PathParam ( "pathId" ) String pathId ) throws FileNotFoundException,
      MalformedURLException, URISyntaxException {
    String perspective = null;
    StringBuffer buffer = null;
    String url = null;
    String path = FileResource.idToPath( pathId );

    if ( FileResource.getRepository().getFile( path ) == null ) {
      return Response.status( Status.NOT_FOUND ).build();
    }

    String extension = path.substring( path.lastIndexOf( '.' ) + 1 );
    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, PentahoSessionHolder.getSession() );
    IContentInfo info = pluginManager.getContentTypeInfo( extension );
    for ( IPluginOperation operation : info.getOperations() ) {
      if ( operation.getId().equalsIgnoreCase( "RUN" ) ) { //$NON-NLS-1$
        perspective = operation.getPerspective();
        break;
      }
    }
    if ( perspective == null ) {
      perspective = GENERATED_CONTENT_PERSPECTIVE;
    }

    buffer = httpServletRequest.getRequestURL();
    String queryString = httpServletRequest.getQueryString();
    url = buffer.substring( 0, buffer.lastIndexOf( "/" ) + 1 ) + perspective + //$NON-NLS-1$
        ( ( queryString != null && queryString.length() > 0 ) ? "?" + httpServletRequest.getQueryString() : "" );
    return Response.seeOther( ( new URL( url ) ).toURI() ).build();
  }

  /**
   * Gets a resource identified by the compound key contextId and resourceId. This request may include additional parameters used to render the resource.
   *
   * <p><b>Example Request:</b><br />
   *    POST pentaho/api/repos/xanalyzer/service/ajax/lookupXmiId
   * <br /><b>POST data:</b>
   *  <pre function="syntax.xml">
   *      catalog=t&cube=t&time=1389817320072
   *  </pre>
   * </p>
   *
   * @param contextId  Identifies the context in which the resource should be retrieved. This value may be a repository file ID, repository file extension or plugin ID
   * @param resourceId Identifies a resource to be retrieved. This value may be a static file residing in a publicly visible plugin folder, repository file ID or content generator ID
   * @param formParams Any arguments needed to render the resource
   *
   * @return A jax-rs Response object with the appropriate status code, header, and body. In many cases this will trigger a streaming operation after it it is returned to the caller..
   *
   * <p><b>Example Response:</b></p>
   *  <pre function="syntax.xml">
   *    This response does not contain data.
   *  </pre>
   */
  @Path ( "/{contextId}/{resourceId : .+}" )
  @POST
  @Consumes ( APPLICATION_FORM_URLENCODED )
  @Produces ( { WILDCARD } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully get the resource." ),
      @ResponseCode ( code = 404, condition = "Failed to find the resource." )
  } )
  public Response doFormPost( @PathParam ( "contextId" ) String contextId, @PathParam ( "resourceId" ) String resourceId,
                              final MultivaluedMap<String, String> formParams )
    throws ObjectFactoryException, PluginBeanException,
      IOException, URISyntaxException {

    httpServletRequest = JerseyUtil.correctPostRequest( formParams, httpServletRequest );

    if ( logger.isDebugEnabled() ) {
      for ( Object key : httpServletRequest.getParameterMap().keySet() ) {
        logger.debug( "param [" + key + "]" ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }

    return doService( contextId, resourceId );
  }

  /**
   * Gets a resource identified by the compound key contextId and resourceId. This request may include additional parameters used to render the resource.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho/api/repos/admin-plugin/resources/authenticationProviderModule/authenticationProviderAdmin.html
   * </p>
   *
   * @param contextId  Identifies the context in which the resource should be retrieved. This value may be a repository file ID, repository file extension or plugin ID.
   * @param resourceId Identifies a resource to be retrieved. This value may be a static file residing in a publicly visible plugin folder, repository file ID or content generator ID.
   *
   * @return A jax-rs Response object with the appropriate status code, header, and body.
   *
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   *&lt;!DOCTYPE html&gt;
   *&lt;html xmlns:pho=&quot;http:/www.pentaho.com&quot;&gt;
   *&lt;head&gt;
   *&lt;title&gt;Report Parameter UI&lt;/title&gt;
   *&lt;link rel=&quot;stylesheet&quot; type=&quot;text/css&quot; href=&quot;authenticationProviderAdmin.css&quot; /&gt;
   *&lt;link rel=&quot;stylesheet&quot; type=&quot;text/css&quot; href=&quot;../../../common-ui/resources/web/dojo/dijit/themes/pentaho/pentaho.css&quot; /&gt;
   *&lt;script type=&quot;text/javascript&quot; src=&quot;../../../../webcontext.js&quot;&gt;&lt;/script&gt;
   *&lt;script type=&quot;text/javascript&quot;&gt;
   *require([&quot;authenticationProviderAdmin&quot;]);
   *&lt;/script&gt;
   *&lt;/head&gt;
   *&lt;body class=&quot;soria&quot; style=&quot;border: none&quot;&gt;
   *
   *&lt;!--  tree dialog --&gt;
   *&lt;div id=&quot;ldapTreeDialog&quot; data-dojo-type=&quot;dijit.Dialog&quot;  data-dojo-props=&#39;title:&quot;LDAP Browser&quot;&#39; class=&quot;dialog&quot;&gt;
   *&lt;div id=&quot;ldapTreeDialogContent&quot; class=&quot;dialog-content ldap-tree-padding&quot;&gt;
   *&lt;div id=&quot;ldapTree&quot; data-dojo-props=&quot;autoExpand:true&quot;&gt;&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;div class=&quot;dialog-buttons&quot;&gt;
   *&lt;div class=&quot;container&quot;&gt;
   *&lt;button id=&quot;btn_ldapTreeDialogOk&quot; class=&quot;pentaho-button ok-button first&quot;&gt; &lt;/button&gt;
   *&lt;button id=&quot;btn_ldapTreeDialogCancel&quot; class=&quot;pentaho-button cancel-button last&quot;&gt; &lt;/button&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *
   *&lt;!-- override dialog --&gt;
   *&lt;div id=&quot;ldapDirtyDialog&quot; data-dojo-type=&quot;dijit.Dialog&quot; class=&quot;dialog&quot;&gt;
   *&lt;div class=&quot;dialog-content pentaho-padding-sm&quot;&gt;
   *&lt;p class=&quot;message&quot;&gt;You have unsaved changes. Do you want to continue?&lt;/p&gt;
   *&lt;/div&gt;
   *&lt;div class=&quot;dialog-buttons&quot;&gt;
   *&lt;div class=&quot;container&quot;&gt;
   *&lt;button id=&quot;btn_ldapDirtyDialogNo&quot; class=&quot;pentaho-button no-button first&quot;&gt; &lt;/button&gt;
   *&lt;button id=&quot;btn_ldapDirtyDialogYes&quot; class=&quot;pentaho-button yes-button last&quot;&gt; &lt;/button&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *
   *&lt;!-- test dialog --&gt;
   *&lt;div id=&quot;ldapTestMsgDialog&quot; data-dojo-type=&quot;dijit.Dialog&quot; class=&quot;dialog&quot;&gt;
   *&lt;div class=&quot;dialog-content pentaho-padding-sm&quot;&gt;
   *&lt;p class=&quot;message&quot;&gt; &lt;/p&gt;
   *&lt;/div&gt;
   *&lt;div class=&quot;dialog-buttons&quot;&gt;
   *&lt;div class=&quot;container&quot;&gt;
   *&lt;button id=&quot;btn_hideTest&quot; class=&quot;pentaho-button close-button last&quot;&gt; &lt;/button&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *
   *&lt;!-- edit server connection --&gt;
   *&lt;div id=&quot;editServerDialog&quot; data-dojo-type=&quot;dijit.Dialog&quot; data-dojo-props=&#39;title:&quot;Edit External Authentication Server Connection&quot;&#39; class=&quot;dialog&quot;&gt;
   *&lt;div class=&quot;dialog-content pentaho-padding-sm&quot;&gt;
   *&lt;p class=&quot;message&quot;&gt;Changing server conneciton will remove all current authentication and premissions settings. Do you want to continue?&lt;/p&gt;
   *&lt;/div&gt;
   *&lt;div class=&quot;dialog-buttons&quot;&gt;
   *&lt;div class=&quot;container&quot;&gt;
   *&lt;button id=&quot;btn_editServerDialogYesClick&quot; class=&quot;pentaho-button ok-button first&quot;&gt; &lt;/button&gt;
   *&lt;button id=&quot;btn_editServerDialogNoClick&quot; class=&quot;pentaho-button cancel-button last&quot;&gt; &lt;/button&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *
   *&lt;!-- edit authentication method --&gt;
   *&lt;div id=&quot;authenticationChangeDialog&quot; data-dojo-type=&quot;dijit.Dialog&quot; class=&quot;dialog&quot; &gt;
   *&lt;div class=&quot;dialog-content pentaho-padding-sm&quot;&gt;
   *&lt;p class=&quot;message&quot;&gt;Changing the authentication method will remove all current authentication and premissions settings. Do you want to continue?&lt;/p&gt;
   *&lt;/div&gt;
   *&lt;div class=&quot;dialog-buttons&quot;&gt;
   *&lt;div class=&quot;container&quot;&gt;
   *&lt;button id=&quot;btn_processAuthenticationMethodChange&quot; class=&quot;pentaho-button yes-change-button first&quot;&gt; &lt;/button&gt;
   *&lt;button id=&quot;btn_authenticationChangeNoClick&quot; class=&quot;pentaho-button no-button last&quot;&gt; &lt;/button&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *
   *
   *
   *&lt;!-- populator dialog --&gt;
   *&lt;div id=&quot;ldapPopTestDialog&quot; data-dojo-type=&quot;dijit.Dialog&quot; class=&quot;dialog&quot;&gt;
   *&lt;div class=&quot;dialog-content pentaho-padding-sm&quot;&gt;
   *&lt;div class=&quot;groupOption&quot;&gt;
   *&lt;div class=&quot;ldapPopulatorGroupRoleAttributeLabel&quot;&gt;Group Role Attribute:&lt;/div&gt;
   *&lt;div class=&quot;ldapPopulatorGroupRoleAttributeValue value&quot;&gt;&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;div class=&quot;groupOption&quot;&gt;
   *&lt;div class=&quot;ldapPopulatorGroupRoleSearchBaseLabel&quot;&gt;Group Search Base:&lt;/div&gt;
   *&lt;div class=&quot;ldapPopulatorGroupRoleSearchBaseValue value&quot;&gt;&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;div class=&quot;groupOption&quot;&gt;
   *&lt;div class=&quot;ldapPopulatorGroupSearchFilterLabel&quot;&gt;Group Search Filter:&lt;/div&gt;
   *&lt;div class=&quot;ldapPopulatorGroupSearchFilterValue value&quot;&gt;&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;div class=&quot;groupOption&quot;&gt;
   *&lt;div class=&quot;ldapPopulatorRolePrefixLabel&quot;&gt;Role Prefix:&lt;/div&gt;
   *&lt;div class=&quot;ldapPopulatorRolePrefixValue value&quot;&gt;&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;div class=&quot;groupOption&quot;&gt;
   *&lt;div class=&quot;ldapUserLabel&quot;&gt;User Name:&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;input id=&quot;ldapPopTestUserName&quot; type=&quot;text&quot; /&gt;
   *&lt;br /&gt;
   *&lt;div class=&quot;groupOption&quot;&gt;
   *&lt;label class=&quot;ldapUserDomainLabel&quot;&gt;User DN:&lt;/label&gt;
   *&lt;/div&gt;
   *&lt;input id=&quot;ldapPopTestUserDn&quot; type=&quot;text&quot;/&gt;
   *&lt;br /&gt;
   *&lt;/div&gt;
   *&lt;div class=&quot;dialog-buttons&quot;&gt;
   *&lt;div class=&quot;container&quot;&gt;
   *&lt;button id=&quot;btn_testPopulator&quot; class=&quot;pentaho-button ok-button first&quot;&gt; &lt;/button&gt;
   *&lt;button id=&quot;btn_hideLdapPropsTest&quot; class=&quot;pentaho-button cancel-button last&quot;&gt; &lt;/button&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *
   *
   *
   *&lt;!-- user test dialog --&gt;
   *&lt;div id=&quot;ldapUserTestDialog&quot; data-dojo-type=&quot;dijit.Dialog&quot; class=&quot;dialog&quot;&gt;
   *&lt;div class=&quot;dialog-content pentaho-padding-sm&quot;&gt;
   *&lt;p class=&quot;message&quot;&gt;With the search base and search filter configuration search for a user name that exists in your LDAP server.&lt;/p&gt;
   *&lt;br/&gt;
   *&lt;div class=&quot;groupOption&quot;&gt;
   *&lt;div class=&quot;ldapUserTestLabel&quot;&gt;Search For User:&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;input class=&quot;ldapUserTestUserName&quot; type=&quot;text&quot; /&gt;
   *&lt;br /&gt;
   *&lt;/div&gt;
   *&lt;div class=&quot;dialog-buttons&quot;&gt;
   *&lt;div class=&quot;container&quot;&gt;
   *&lt;button id=&quot;btn_testLdapUserSearch&quot; class=&quot;pentaho-button ok-button first&quot;&gt; &lt;/button&gt;
   *&lt;button id=&quot;btn_hideLdapUserTestDialog&quot; class=&quot;pentaho-button cancel-button last&quot;&gt; &lt;/button&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *
   *
   *&lt;div style=&quot;padding: 0px;&quot;&gt;
   *&lt;div class=&quot;pentaho-fieldgroup-major titleLabel&quot;&gt;Authentication&lt;/div&gt;
   *&lt;br/&gt;
   *&lt;!-- CONNECTION PARAMS --&gt;
   *&lt;div id=&quot;authenticationSelector&quot;&gt;
   *
   *&lt;div class=&quot;authenticationMethodLabel authMethod&quot;&gt;Authentication Method&lt;/div&gt;
   *&lt;div class=&quot;authText authenticationMethodDescriptionLabel&quot;&gt;
   *Select where user and their log in credentials will be managed:
   *&lt;/div&gt;
   *
   *&lt;div class=&quot;groupOption&quot;&gt;
   *&lt;input checked=&quot;checked&quot; name=&quot;securityProvider&quot; type=&quot;radio&quot; value=&quot;jackrabbit&quot; /&gt;
   *&lt;div class=&quot;pentahoSecurityLabel authValue&quot;&gt;Local - Use basic Hitachi Vantara authentication&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;div class=&quot;groupOption&quot;&gt;
   *&lt;input name=&quot;securityProvider&quot; type=&quot;radio&quot; value=&quot;ldap&quot; /&gt;
   *&lt;div class=&quot;ldapSecurityLabel authValue&quot;&gt;External - Use LDAP / Active Directory server&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;br /&gt;
   *
   *
   *&lt;br /&gt;
   *&lt;div id=&quot;ldapConnection&quot; style=&quot;display: none&quot;&gt;
   *&lt;div class=&quot;ldapConnectionTitleLabel authMethod&quot;&gt;LDAP Server Connection&lt;/div&gt;
   *
   *&lt;!-- to edit config --&gt;
   *&lt;div id=&quot;ldapConnectionEdit&quot; style=&quot;display:block&quot;&gt;
   *&lt;div class=&quot;authText ldapServerUrlLabel&quot;&gt;Server URL:&lt;/div&gt;
   *&lt;input class=&quot;ldapServerUrlInput authValue adminField&quot; type=&quot;text&quot; /&gt;
   *
   *&lt;div class=&quot;authText ldapUserLabel&quot;&gt;User Name:&lt;/div&gt;
   *&lt;input class=&quot;ldapUserInput authValue adminField&quot; type=&quot;text&quot; /&gt;
   *
   *&lt;div class=&quot;authText ldapPasswordLabel&quot;&gt;Password:&lt;/div&gt;
   *&lt;input class=&quot;ldapPasswordInput authValue adminField&quot; type=&quot;password&quot; /&gt;
   *
   *&lt;br/&gt;&lt;br/&gt;
   *&lt;div class=&quot;authText ldapTestConnectionLabel&quot;&gt;Test connection to complete LDAP setup&lt;/div&gt;
   *&lt;br/&gt;
   *&lt;div class=&quot;securityConfigButton&quot;&gt;
   *&lt;button id=&quot;testServerConnectionButton&quot; class=&quot;pentaho-button testServerConnectionButton&quot; &gt;
   *&lt;/button&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *
   *&lt;!-- edited config --&gt;
   *&lt;div id=&quot;ldapConnectionEditor&quot; style=&quot;display:none&quot;&gt;
   *&lt;div class=&quot;authText ldapServerUrlLabel&quot; &gt;Server URL:&lt;/div&gt;
   *&lt;div class=&quot;groupOption&quot;&gt;
   *&lt;div class=&quot;ldapServerUrlValue authValue&quot;&gt;&lt;/div&gt;
   *&lt;div class=&quot;pentaho-editbutton&quot; id=&quot;btn_editConnection&quot; title=&quot;Edit connection&quot;&gt;&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *
   *&lt;br /&gt;
   *
   *&lt;div&gt;
   *&lt;div id=&quot;ldapSettingsGroup&quot; style=&quot;display: none&quot;&gt;
   *
   *&lt;!-- Ldap administration configuration --&gt;
   *&lt;div id=&quot;ldapAdministration&quot;&gt;
   *&lt;div class=&quot;ldapAdministrationTitleLabel authMethod&quot;&gt;Pentaho System Administrator&lt;/div&gt;
   *&lt;div class=&quot;ldapAdministratiorUserLabel authText&quot;&gt;Select user from LDAP server:&lt;/div&gt;
   *&lt;div class=&quot;groupOption&quot;&gt;
   *&lt;input class=&quot;ldapAdministratorUserInput adminField&quot; type=&quot;text&quot;  /&gt;
   *&lt;button class=&quot;adminButton&quot; id=&quot;btn_ldapAdministratorUserInput&quot;&gt; &lt;/button&gt;
   *&lt;/div&gt;
   *&lt;div class=&quot;ldapAdministrationRoleLabel authText&quot;&gt;Select role from LDAP server:&lt;/div&gt;
   *&lt;div class=&quot;groupOption&quot;&gt;
   *&lt;input class=&quot;ldapAdministratorRoleInput adminField&quot; type=&quot;text&quot;  /&gt;
   *&lt;button class=&quot;adminButton&quot; id=&quot;btn_ldapAdministratorRoleInput&quot; &gt; &lt;/button&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;br/&gt;&lt;br/&gt;
   *
   *&lt;!-- ldap configuration --&gt;
   *&lt;div class=&quot;ldapConfigurationTitle authMethod&quot;&gt;LDAP Configuration&lt;/div&gt;
   *&lt;div class=&quot;authText&quot; id=&quot;customLdapProviderLabel&quot; &gt;Other&lt;/div&gt;
   *&lt;div class=&quot;groupOption&quot;&gt;
   *&lt;select id=&quot;ldapTypeSelector&quot;&gt;
   *&lt;option class=&quot;ldapTypeSelectorApacheOption&quot; selected=&quot;selected&quot; value=&quot;ldapApacheConfiguration&quot;&gt;Apache DS&lt;/option&gt;
   *&lt;option class=&quot;ldapTypeSelectorCustomOption&quot; value=&quot;ldapCustomConfiguration&quot;&gt;Custom&lt;/option&gt;
   *&lt;/select&gt;
   *&lt;/div&gt;
   *
   *&lt;!-- ldap apache configuration --&gt;
   *&lt;div id=&quot;ldapApacheConfiguration&quot; class=&quot;ldapApacheConfiguration configuration&quot; style=&quot;display: none;&quot;&gt;
   *&lt;!-- User Base --&gt;
   *&lt;div class=&quot;ldapUserBaseLabel authText&quot;&gt;User Base:&lt;/div&gt;
   *&lt;div class=&quot;groupOption&quot;&gt;
   *&lt;input class=&quot;ldapUserSearchBaseInput adminField&quot; type=&quot;text&quot; /&gt;
   *&lt;button class=&quot;adminButton&quot; id=&quot;btn_ldapUserSearchBaseInput&quot;&gt; &lt;/button&gt;
   *&lt;/div&gt;
   *&lt;!-- Group Base --&gt;
   *&lt;div class=&quot;ldapGroupBaseLabel authText&quot;&gt;Group Base:&lt;/div&gt;
   *&lt;div class=&quot;groupOption&quot;&gt;
   *&lt;input class=&quot;ldapGroupBaseInput adminField&quot; type=&quot;text&quot; /&gt;
   *&lt;button class=&quot;adminButton&quot; id=&quot;btn_ldapGroupBaseInput&quot; &gt; &lt;/button&gt;
   *&lt;/div&gt;
   *
   *&lt;div style=&quot;display: none&quot;&gt;
   *&lt;!-- This stuff is hidden but populated for save functions --&gt;
   *&lt;input class=&quot;ldapUserSearchFilterInput&quot; /&gt;
   *
   *&lt;input class=&quot;ldapRoleBaseInput&quot; /&gt;
   *&lt;input class=&quot;ldapRoleSearchBaseInput&quot; /&gt;
   *&lt;input class=&quot;ldapRoleSearchFilterInput&quot; /&gt;
   *
   *&lt;input class=&quot;ldapPopulatorGroupRoleAttributeInput&quot; /&gt;
   *&lt;input class=&quot;ldapPopulatorGroupSearchFilterInput&quot; /&gt;
   *&lt;input class=&quot;ldapPopulatorGroupRoleSearchBaseInput&quot; /&gt;
   *&lt;input class=&quot;ldapPopulatorRolePrefixInput&quot; /&gt;
   *&lt;input class=&quot;ldapPopulatorSubtreeInput&quot; name=&quot;ldapPopulatorSubtreeInput&quot; type=&quot;radio&quot; value=&quot;false&quot; /&gt;
   *&lt;input class=&quot;ldapPopulatorUpperCaseInput&quot; name=&quot;ldapPopulatorUpperCaseInput&quot; type=&quot;radio&quot; value=&quot;false&quot; /&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *
   *&lt;div id=&quot;ldapMicrosoftConfiguration&quot; class=&quot;microsoftConfigPanel configuration&quot; style=&quot;display: none;&quot;&gt;
   *&lt;div class=&quot;ldapUserBaseLabel authText&quot;&gt;User Base:&lt;/div&gt;
   *&lt;div class=&quot;groupOption&quot;&gt;
   *&lt;input class=&quot;ldapUserSearchBaseInput adminField&quot; type=&quot;text&quot;  /&gt;
   *&lt;button class=&quot;adminButton&quot; id=&quot;btn_ldapUserSearchBaseInput2&quot; &gt; &lt;/button&gt;
   *&lt;/div&gt;
   *
   *&lt;div class=&quot;ldapGroupBaseLabel authText&quot;&gt;Group Base:&lt;/div&gt;
   *&lt;div class=&quot;groupOption&quot;&gt;
   *&lt;input class=&quot;ldapGroupBaseInput adminField&quot; type=&quot;text&quot;  /&gt;
   *&lt;button class=&quot;adminButton&quot; id=&quot;btn_ldapGroupBaseInput2&quot;&gt; &lt;/button&gt;
   *&lt;/div&gt;
   *
   *&lt;div style=&quot;display: none&quot;&gt;
   *&lt;!-- This stuff is hidden but populated for test and save functions --&gt;
   *&lt;input class=&quot;ldapUserSearchFilterInput&quot; /&gt;
   *
   *&lt;input class=&quot;ldapRoleBaseInput&quot; /&gt;
   *&lt;input class=&quot;ldapRoleSearchBaseInput&quot; /&gt;
   *&lt;input class=&quot;ldapRoleSearchFilterInput&quot; /&gt;
   *
   *&lt;input class=&quot;ldapPopulatorGroupRoleAttributeInput&quot; /&gt;
   *&lt;input class=&quot;ldapPopulatorGroupSearchFilterInput&quot; /&gt;
   *&lt;input class=&quot;ldapPopulatorGroupRoleSearchBaseInput&quot; /&gt;
   *&lt;input class=&quot;ldapPopulatorRolePrefixInput&quot; /&gt;
   *&lt;input class=&quot;ldapPopulatorSubtreeInput&quot; name=&quot;ldapPopulatorSubtreeInput&quot; type=&quot;radio&quot; value=&quot;false&quot; /&gt;
   *&lt;input class=&quot;ldapPopulatorUpperCaseInput&quot; name=&quot;ldapPopulatorUpperCaseInput&quot; type=&quot;radio&quot; value=&quot;false&quot; /&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *
   *&lt;!-- ldap custom configuration --&gt;
   *&lt;div id=&quot;ldapCustomConfiguration&quot; class=&quot;ldapCustomConfiguration configuration&quot; style=&quot;display: none;&quot;&gt;
   *&lt;!-- user search configuration --&gt;
   *&lt;br/&gt;
   *&lt;span class=&quot;ldapCustomUserSearchTitle authMethod&quot;&gt;User Search&lt;/span&gt;
   *&lt;br/&gt;
   *&lt;div&gt;
   *&lt;div class=&quot;ldapUserSearchBaseLabel authText&quot;&gt;Search Base:&lt;/div&gt;
   *&lt;input class=&quot;ldapUserSearchBaseInput adminField&quot; type=&quot;text&quot;  /&gt;
   *
   *&lt;div class=&quot;ldapUserSearchFilderLabel authText&quot;&gt;Search Filter:&lt;/div&gt;
   *&lt;input class=&quot;ldapUserSearchFilterInput adminField&quot; type=&quot;text&quot;  /&gt;
   *
   *&lt;br/&gt; &lt;br/&gt;
   *
   *&lt;div class=&quot;securityConfigButton&quot;&gt;
   *&lt;button class=&quot;pentaho-button test-button&quot; id=&quot;btn_showLdapUserTestDialog&quot;&gt; &lt;/button&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;br/&gt; &lt;br/&gt;
   *&lt;!-- roles configuration --&gt;
   *&lt;span class=&quot;ldapRolesTitle authMethod&quot;&gt;Roles&lt;/span&gt;
   *&lt;br/&gt;
   *&lt;div&gt;
   *&lt;div class=&quot;ldapRoleBaseLabel authText&quot;&gt;Role Attribute:&lt;/div&gt;
   *&lt;input class=&quot;ldapRoleBaseInput adminField&quot; type=&quot;text&quot;  /&gt;
   *
   *&lt;div class=&quot;ldapRoleSearchFilterLabel authText&quot;&gt;Role Search Filter:&lt;/div&gt;
   *&lt;input class=&quot;ldapRoleSearchFilterInput adminField&quot; type=&quot;text&quot;  /&gt;
   *
   *&lt;div class=&quot;ldapRoleSearchBaseLabel authText&quot;&gt;Role Search Base:&lt;/div&gt;
   *&lt;input class=&quot;ldapRoleSearchBaseInput adminField&quot; type=&quot;text&quot;  /&gt;
   *
   *&lt;br/&gt; &lt;br/&gt;
   *
   *&lt;div class=&quot;securityConfigButton&quot;&gt;
   *&lt;button class=&quot;pentaho-button test-button&quot; id=&quot;btn_testAuthoritiesSearch&quot;&gt; &lt;/button&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;br/&gt; &lt;br/&gt;
   *&lt;span class=&quot;ldapPopulatorTitle authMethod&quot;&gt;Populator&lt;/span&gt;
   *&lt;br/&gt;
   *&lt;div&gt;
   *&lt;div class=&quot;ldapPopulatorGroupRoleAttributeLabel authText&quot;&gt;Group Role Attribute:&lt;/div&gt;
   *&lt;input class=&quot;ldapPopulatorGroupRoleAttributeInput adminField&quot; type=&quot;text&quot;  /&gt;
   *
   *&lt;div class=&quot;ldapPopulatorGroupRoleSearchBaseLabel authText&quot;&gt;Group Search Base:&lt;/div&gt;
   *&lt;input class=&quot;ldapPopulatorGroupRoleSearchBaseInput adminField&quot; type=&quot;text&quot;  /&gt;
   *
   *&lt;div class=&quot;ldapPopulatorGroupSearchFilterLabel authText&quot;&gt;Group Search Filter:&lt;/div&gt;
   *&lt;input class=&quot;ldapPopulatorGroupSearchFilterInput adminField&quot; type=&quot;text&quot;  /&gt;
   *
   *&lt;div class=&quot;ldapPopulatorRolePrefixLabel authText&quot;&gt;Role Prefix:&lt;/div&gt;
   *&lt;input  class=&quot;ldapPopulatorRolePrefixInput adminField&quot; type=&quot;text&quot;  /&gt;
   *
   *&lt;div class=&quot;ldapPopulatorUpperCaseLabel authText&quot;&gt;Convert To Upper Case:&lt;/div&gt;
   *&lt;div class=&quot;ldapPopulatorUpperCaseDescription groupOption&quot;&gt;
   *&lt;input name=&quot;ldapPopulatorUpperCaseInput&quot; class=&quot;ldapPopulatorUpperCaseInput&quot; type=&quot;radio&quot; value=&quot;true&quot; /&gt;
   *&lt;label class=&quot;yes-button&quot;&gt;Yes&lt;/label&gt;
   *&lt;input name=&quot;ldapPopulatorUpperCaseInput&quot; class=&quot;ldapPopulatorUpperCaseInput&quot; type=&quot;radio&quot; checked=&quot;checked&quot; value=&quot;false&quot; /&gt;
   *&lt;label class=&quot;no-button&quot;&gt;No&lt;/label&gt;
   *&lt;/div&gt;
   *&lt;div class=&quot;ldapPopulatorSubtreeLabel authText&quot;&gt;Subtree:&lt;/div&gt;
   *&lt;div class=&quot;ldapPopulatorSubtreeDescription groupOption&quot;&gt;
   *&lt;input name=&quot;ldapPopulatorSubtreeInput&quot; class=&quot;ldapPopulatorSubtreeInput&quot; type=&quot;radio&quot; value=&quot;true&quot; /&gt;
   *&lt;label class=&quot;yes-button&quot;&gt;Yes&lt;/label&gt;
   *&lt;input name=&quot;ldapPopulatorSubtreeInput&quot; class=&quot;ldapPopulatorSubtreeInput&quot; type=&quot;radio&quot; checked=&quot;checked&quot; value=&quot;false&quot; /&gt;
   *&lt;label class=&quot;no-button&quot;&gt;No&lt;/label&gt;
   *&lt;/div&gt;
   *&lt;br/&gt;
   *&lt;div class=&quot;securityConfigButton&quot;&gt;
   *&lt;button class=&quot;pentaho-button test-button&quot; id=&quot;btn_showPopulatorTestDialog&quot;&gt; &lt;/button&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *&lt;/div&gt;
   *
   *
   *&lt;footer&gt;
   *&lt;br/&gt;&lt;br/&gt;
   *&lt;div id=&quot;buttonDivSave&quot; class=&quot;securityConfigButton&quot; style=&quot;display: none;&quot;&gt;
   *&lt;button id=&quot;saveConfigButton&quot; class=&quot;pentaho-button&quot; &gt;Save&lt;/button&gt;
   *&lt;/div&gt;
   *&lt;/footer&gt;
   *&lt;/body&gt;
   *&lt;/html&gt;
   * </pre>
   */
  @Path ( "/{contextId}/{resourceId : .+}" )
  @GET
  @Produces ( { WILDCARD } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully get the resource." ),
      @ResponseCode ( code = 404, condition = "Failed to find the resource." )
  } )
  public Response doGet( @PathParam ( "contextId" ) String contextId, @PathParam ( "resourceId" ) String resourceId )
    throws ObjectFactoryException, PluginBeanException, IOException, URISyntaxException {

    if ( logger.isDebugEnabled() ) {
      for ( Object key : httpServletRequest.getParameterMap().keySet() ) {
        logger.debug( "param [" + key + "]" ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }

    return doService( contextId, resourceId );
  }

  /**
   * Retrieves the list of supported content type in the platform
   *
   * @return list of <code> ExecutableFileTypeDto </code>
   */
  @Path ( "/executableTypes" )
  @GET
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  @Facet ( name = "Unsupported" )
  public Response getExecutableTypes() {
    ArrayList<ExecutableFileTypeDto> executableTypes = new ArrayList<ExecutableFileTypeDto>();
    for ( String contentType : pluginManager.getContentTypes() ) {
      IContentInfo contentInfo = pluginManager.getContentTypeInfo( contentType );
      ExecutableFileTypeDto executableFileType = new ExecutableFileTypeDto();
      executableFileType.setDescription( contentInfo.getDescription() );
      executableFileType.setExtension( contentInfo.getExtension() );
      executableFileType.setTitle( contentInfo.getTitle() );
      executableFileType.setCanSchedule( hasOperationId( contentInfo.getOperations(), "SCHEDULE_NEW" ) );
      executableFileType.setCanEdit( hasOperationId( contentInfo.getOperations(), "EDIT" ) );
      executableTypes.add( executableFileType );
    }

    final GenericEntity<List<ExecutableFileTypeDto>> entity = new GenericEntity<List<ExecutableFileTypeDto>>( executableTypes ) { };
    return Response.ok( entity ).build();
  }

  private boolean hasOperationId( final List<IPluginOperation> operations, final String operationId ) {
    if ( operations != null && StringUtils.isNotBlank( operationId ) ) {
      for ( IPluginOperation operation : operations ) {
        if ( operation != null && StringUtils.isNotBlank( operation.getId() ) ) {
          if ( operation.getId().equals( operationId ) && StringUtils.isNotBlank( operation.getPerspective() ) ) {
            return true;
          }
        }
      }
    }
    return false;
  }

  protected Response doService( String contextId, String resourceId ) throws ObjectFactoryException,
      PluginBeanException, IOException, URISyntaxException {

    ctxt( "Is [{0}] a repository file id?", contextId ); //$NON-NLS-1$
    if ( contextId.startsWith( ":" ) || contextId.matches( "^[A-z]\t:.*" ) ) { //$NON-NLS-1$
      //
      // The context is a repository file (A)
      //

      final RepositoryFile file = repository.getFile( FileResource.idToPath( contextId ) );
      if ( file == null ) {
        logger.error( MessageFormat.format( "Repository file [{0}] not found", contextId ) );
        return Response.serverError().build();
      }
      if ( FileResource.idToPath( contextId ).endsWith( ".prpti" ) && !validatePrptiOutputFormat() ) {
        logger.error( MessageFormat.format( "Output Format [{0}] for PIR report not allowed for file [{1}]",
          this.httpServletRequest.getParameterMap().get( "output-target" )[ 0 ], FileResource.idToPath( contextId ) ) );
        return Response.serverError().status( Status.BAD_REQUEST ).build();
      }

      Response response = null;

      ctxt( "Yep, [{0}] is a repository file id", contextId ); //$NON-NLS-1$
      final String ext = RepositoryFilenameUtils.getExtension( file.getName() );
      String pluginId = pluginManager.getPluginIdForType( ext );
      if ( pluginId == null ) {

        // A.3.a (faux content generator for .url files)
        response = getUrlResponse( file, resourceId );
        if ( response != null ) {
          return response;
        } else {
          logger.error( MessageFormat.format( "No plugin was found to service content of type [{0}]", ext ) );
          return Response.serverError().build();
        }
      }

      // A.1.
      response = getPluginFileResponse( pluginId, resourceId );
      if ( response != null ) {
        return response;
      }

      // A.2.
      response = getRepositoryFileResponse( file.getPath(), resourceId );
      if ( response != null ) {
        return response;
      }

      // A.3.b (real content generator)
      CGFactory fac = new RepositoryFileCGFactory( resourceId, file );
      response = getContentGeneratorResponse( fac );
      if ( response != null ) {
        return response;
      }

    } else {
      ctxt( "Nope, [{0}] is not a repository file id", contextId ); //$NON-NLS-1$
      ctxt( "Is [{0}] is a repository file extension?", contextId ); //$NON-NLS-1$
      String pluginId = pluginManager.getPluginIdForType( contextId );
      if ( pluginId != null ) {
        //
        // The context is a file extension (B)
        //
        ctxt( "Yep, [{0}] is a repository file extension", contextId ); //$NON-NLS-1$

        // B.1.
        Response response = getPluginFileResponse( pluginId, resourceId );
        if ( response != null ) {
          return response;
        }

        // B.3.
        CGFactory fac = new ContentTypeCGFactory( resourceId, contextId );
        response = getContentGeneratorResponse( fac );
        if ( response != null ) {
          return response;
        }
      } else {
        ctxt( "Nope, [{0}] is not a repository file extension", contextId ); //$NON-NLS-1$
        ctxt( "Is [{0}] is a plugin id?", contextId ); //$NON-NLS-1$
        if ( pluginManager.getRegisteredPlugins().contains( contextId ) ) {
          //
          // The context is a plugin id (C)
          //
          ctxt( "Yep, [{0}] is a plugin id", contextId ); //$NON-NLS-1$
          pluginId = contextId;

          // C.1.
          Response response = getPluginFileResponse( pluginId, resourceId );
          if ( response != null ) {
            return response;
          }

          // C.3.
          CGFactory fac = new DirectCGFactory( resourceId, contextId );
          response = getContentGeneratorResponse( fac );
          if ( response != null ) {
            return response;
          }
        } else {
          ctxt( "Nope, [{0}] is not a plugin id", contextId ); //$NON-NLS-1$
          logger.warn( MessageFormat.format( "Failed to resolve context [{0}]", contextId ) ); //$NON-NLS-1$
        }
      }
    }

    logger.warn( MessageFormat.format( "End of the resolution chain. No resource [{0}] found in context [{1}].",
        resourceId, contextId ) );
    return Response.status( NOT_FOUND ).build();
  }

  private boolean validatePrptiOutputFormat() {
    boolean valid = true;
    if ( this.httpServletRequest.getParameterMap() != null && this.httpServletRequest.getParameterMap().containsKey( "output-target" ) ) {
      String outputFormat = this.httpServletRequest.getParameterMap().get( "output-target" )[0];
      valid = AllowedPrptiTypes.getByType( outputFormat ) != null;
    }
    return valid;
  }

  abstract class CGFactory implements ContentGeneratorDescriptor {
    String contentGeneratorId;

    String command;

    public CGFactory( String contentGeneratorPath ) {
      if ( contentGeneratorPath.contains( "/" ) ) { //$NON-NLS-1$
        contentGeneratorId = contentGeneratorPath.substring( 0, contentGeneratorPath.indexOf( '/' ) );
        command = contentGeneratorPath.substring( contentGeneratorPath.indexOf( '/' ) + 1 );
        debug( "decomposing path [{0}] into content generator id [{1}] and command [{2}]", contentGeneratorPath,
            //$NON-NLS-1$
            contentGeneratorId, command );
      } else {
        contentGeneratorId = contentGeneratorPath;
      }
    }

    public String getContentGeneratorId() {
      return contentGeneratorId;
    }

    public String getCommand() {
      return command;
    }

    abstract IContentGenerator create();

    abstract GeneratorStreamingOutput getStreamingOutput( IContentGenerator cg );
  }

  class RepositoryFileCGFactory extends ContentTypeCGFactory {
    RepositoryFile file;

    public RepositoryFileCGFactory( String contentGeneratorPath, RepositoryFile file ) {
      super( contentGeneratorPath, file.getName().substring( file.getName().lastIndexOf( '.' ) + 1 ) );
      this.file = file;
    }

    @Override
    GeneratorStreamingOutput getStreamingOutput( IContentGenerator cg ) {
      return new GeneratorStreamingOutput( cg, this, httpServletRequest, httpServletResponse, acceptableMediaTypes,
          file, command );
    }
  }

  class ContentTypeCGFactory extends CGFactory {
    String repoFileExt;

    public ContentTypeCGFactory( String contentGeneratorPath, String repoFileExt ) {
      super( contentGeneratorPath );
      this.repoFileExt = repoFileExt;
    }

    @Override
    public IContentGenerator create() {
      return pluginManager.getContentGenerator( repoFileExt, contentGeneratorId );
    }

    @Override
    GeneratorStreamingOutput getStreamingOutput( IContentGenerator cg ) {
      return new GeneratorStreamingOutput( cg, this, httpServletRequest, httpServletResponse, acceptableMediaTypes,
          null, command );
    }

    @Override
    public String getServicingFileType() {
      return repoFileExt;
    }

    @Override
    public String getPluginId() {
      return PentahoSystem.get( IPluginManager.class ).getPluginIdForType( repoFileExt );
    }
  }

  class DirectCGFactory extends CGFactory {
    String pluginId;

    public DirectCGFactory( String contentGeneratorPath, String pluginId ) {
      super( contentGeneratorPath );
      this.pluginId = pluginId;
    }

    @Override
    IContentGenerator create() {
      return pluginManager.getContentGenerator( null, contentGeneratorId );
    }

    @Override
    GeneratorStreamingOutput getStreamingOutput( IContentGenerator cg ) {
      return new GeneratorStreamingOutput( cg, this, httpServletRequest, httpServletResponse, acceptableMediaTypes,
          null, command );
    }

    @Override
    public String getServicingFileType() {
      return null;
    }

    @Override
    public String getPluginId() {
      return pluginId;
    }

  }

  protected Response getUrlResponse( RepositoryFile file, String resourceId ) throws MalformedURLException,
      URISyntaxException {
    String ext = file.getName().substring( file.getName().indexOf( '.' ) + 1 );
    if ( !( ext.equals( "url" ) && resourceId.equals( "generatedContent" ) ) ) {
      return null; //$NON-NLS-1$ //$NON-NLS-2$
    }

    String url = extractUrl( file );
    if ( !url.trim().startsWith( "http" ) ) { //$NON-NLS-1$
      // if path is relative, prepend FQSURL
      url = PentahoSystem.getApplicationContext().getFullyQualifiedServerURL() + url;
    }
    return Response.seeOther( ( new URL( url ) ).toURI() ).build();
  }

  protected Response getContentGeneratorResponse( CGFactory fac ) {
    rsc( "Is [{0}] a content generator ID?", fac.getContentGeneratorId() ); //$NON-NLS-1$
    final IContentGenerator contentGenerator;
    try {
      contentGenerator = fac.create();
    } catch ( NoSuchBeanDefinitionException e ) {
      rsc( "Nope, [{0}] is not a content generator ID.", fac.getContentGeneratorId() ); //$NON-NLS-1$
      return null;
    }
    if ( contentGenerator == null ) {
      rsc( "Nope, [{0}] is not a content generator ID.", fac.getContentGeneratorId() ); //$NON-NLS-1$
      return null;
    }
    Response response = checkPermissionIfUserIsEditingContent( fac.getContentGeneratorId() );
    if ( response == null ) {
      rsc(
              "Yep, [{0}] is a content generator ID. Executing (where command path is {1})..", fac.getContentGeneratorId(),
              fac.getCommand() ); //$NON-NLS-1$
      GeneratorStreamingOutput gso = fac.getStreamingOutput( contentGenerator );
      response = Response.ok( gso ).build();
    }
    return response;
  }

  protected Response getPluginFileResponse( String pluginId, String filePath ) throws IOException {
    rsc( "Is [{0}] a path to a plugin file?", filePath ); //$NON-NLS-1$
    if ( pluginManager.isPublic( pluginId, filePath ) ) {
      PluginResource pluginResource = new PluginResource( httpServletResponse );
      Response readFileResponse = pluginResource.readFile( pluginId, filePath );
      // TODO: should we assume forbidden means move on in the resolution chain, or abort??
      if ( readFileResponse.getStatus() != Status.NOT_FOUND.getStatusCode() ) {
        rsc( "Yep, [{0}] is a path to a static plugin file", filePath ); //$NON-NLS-1$
        return readFileResponse;
      }
    }
    rsc( "Nope, [{0}] is not a path to a static plugin file", filePath ); //$NON-NLS-1$
    return null;
  }

  protected Response getRepositoryFileResponse( String filePath, String relPath ) throws IOException {
    rsc( "Is [{0}] a relative path to a repository file, relative to [{1}]?", relPath, filePath ); //$NON-NLS-1$

    FileResource fileResource = new FileResource( httpServletResponse );
    fileResource.setWhitelist( whitelist );
    String path =
        RepositoryFilenameUtils
            .separatorsToRepository( RepositoryFilenameUtils.concat( filePath, "../" + relPath ) ); //$NON-NLS-1$
    Response response = fileResource.doGetFileOrDir( RepositoryPathEncoder.encodeRepositoryPath( path ).substring( 1 ) );
    if ( response.getStatus() != Status.NOT_FOUND.getStatusCode() ) {
      rsc( "Yep, [{0}] is a repository file", path ); //$NON-NLS-1$
      return response;
    }
    rsc( "Nope, [{0}] is not a repository file", path ); //$NON-NLS-1$
    return null;
  }

  private void ctxt( String msg, Object... args ) {
    debug( "[RESOLVING CONTEXT ID] ==> " + msg, args ); //$NON-NLS-1$
  }

  private void rsc( String msg, Object... args ) {
    debug( "[RESOLVING RESOURCE ID] ==> " + msg, args ); //$NON-NLS-1$
  }

  private void debug( String msg, Object... args ) {
    logger.debug( MessageFormat.format( msg, args ) );
  }

  protected String extractUrl( RepositoryFile file ) {

    SimpleRepositoryFileData data = null;

    data = repository.getDataForRead( file.getId(), SimpleRepositoryFileData.class );
    StringWriter writer = new StringWriter();
    try {
      IOUtils.copy( data.getInputStream(), writer );
    } catch ( IOException e ) {
      return ""; //$NON-NLS-1$
    }

    String props = writer.toString();
    StringTokenizer tokenizer = new StringTokenizer( props, "\n" ); //$NON-NLS-1$
    while ( tokenizer.hasMoreTokens() ) {
      String line = tokenizer.nextToken();
      int pos = line.indexOf( '=' );
      if ( pos > 0 ) {
        String propname = line.substring( 0, pos );
        String value = line.substring( pos + 1 );
        if ( ( value != null ) && ( value.length() > 0 ) && ( value.charAt( value.length() - 1 ) == '\r' ) ) {
          value = value.substring( 0, value.length() - 1 );
        }
        if ( "URL".equalsIgnoreCase( propname ) ) { //$NON-NLS-1$
          return value;
        }

      }
    }
    // No URL found
    return ""; //$NON-NLS-1$

  }

  public RepositoryDownloadWhitelist getWhitelist() {
    return whitelist;
  }

  public void setWhitelist( RepositoryDownloadWhitelist whitelist ) {
    this.whitelist = whitelist;
  }

  private Response checkPermissionIfUserIsEditingContent( String resourceId ) {
    // Check if we are editing a content
    String perspectiveId = resourceId;
    if ( perspectiveId != null && perspectiveId.indexOf( "." ) >= 0 ) {
      String[] parts = perspectiveId.split( "\\." );
      if ( parts != null && parts.length > 0 ) {
        perspectiveId = parts[1];
      }
    }

    if ( perspectiveId != null && ( perspectiveId.equals( "editor" ) || perspectiveId.equals( "edit" ) ) ) {
      // Check if user has permission to edit the content. If they do not have access, throw and error
      if ( !canEdit() ) {
        logger.error( Messages.getInstance().getString( "RepositoryResource.USER_NOT_AUTHORIZED_TO_EDIT" ) );
        return buildSafeHtmlServerErrorResponse( Messages.getInstance().getString( "RepositoryResource.USER_NOT_AUTHORIZED_TO_EDIT" ) );
      }
    }
    return null;
  }

  protected Response buildSafeHtmlServerErrorResponse( String msg ) {
    return Response.status( Status.FORBIDDEN ).entity( new SafeHtmlBuilder()
            .appendEscapedLines( msg ).toSafeHtml().asString() ).build();
  }

  boolean canEdit() {
    String editPermission = PentahoSystem.getSystemSetting( "edit-permission", "" );
    if ( editPermission != null && editPermission.length() > 0 ) {
      IAuthorizationPolicy authorizationPolicy = PentahoSystem.get( IAuthorizationPolicy.class );
      return authorizationPolicy.isAllowed( editPermission );
    }
    return true;
  }

  public enum AllowedPrptiTypes {
    MIME_TYPE_HTML_1( "table/html;page-mode=page" ),
    MIME_TYPE_HTML_2( "table/html;page-mode=stream" ),
    MIME_TYPE_EMAIL( "mime-message/text/html" ),
    MIME_TYPE_PDF( "pageable/pdf" ),
    MIME_TYPE_CSV( "table/csv;page-mode=stream" ),
    MIME_TYPE_XLSX( "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;page-mode=flow" ),
    MIME_TYPE_TXT( "pageable/text" ),
    MIME_TYPE_RTF( "table/rtf;page-mode=flow" );
    private final String type;

    AllowedPrptiTypes( String type ) {
      this.type = type;
    }

    public String getAllowedPrptiType() {
      return type;
    }

    public static AllowedPrptiTypes getByType( String type ) {
      if ( type == null || type.isEmpty() ) {
        return null;
      }
      AllowedPrptiTypes result = null;
      for ( AllowedPrptiTypes en : AllowedPrptiTypes.values() ) {
        if ( en.getAllowedPrptiType().equals( type ) ) {
          result = en;
        }
      }
      return result;
    }
  }
}
