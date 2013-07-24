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
 * Copyright 2012 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.platform.web.http.api.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginOperation;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCube;
import org.pentaho.platform.plugin.services.pluginmgr.IAdminContentConditionalLogic;
import org.pentaho.platform.util.messages.LocaleHelper;

@Path("/mantle/")
public class UserConsoleResource extends AbstractJaxRSResource {

  private static final Log logger = LogFactory.getLog(UserConsoleResource.class);


  public UserConsoleResource() {
  }

  private IPentahoSession getPentahoSession() {
    return PentahoSessionHolder.getSession();
  }

  @GET
  @Path("/isAdministrator")
  public Response isAdministrator() {
    return Response.ok("" + (SecurityHelper.getInstance().isPentahoAdministrator(getPentahoSession()))).build();
  }

  @GET
  @Path("/isAuthenticated")
  public Response isAuthenticated() {
    return Response.ok("" + (getPentahoSession() != null && getPentahoSession().isAuthenticated())).build();
  }

  @GET
  @Path("/getAdminContent")
  @Produces({ APPLICATION_JSON, APPLICATION_XML })
  public List<Setting> getAdminContent() {
	  
	  ArrayList<Setting> settings = new ArrayList<Setting>();
	  try {
		  IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, getPentahoSession()); 
		  List<String> pluginIds = pluginManager.getRegisteredPlugins();
		  nextPlugin : for (String pluginId : pluginIds) {
			String adminContentInfo = (String) pluginManager.getPluginSetting(pluginId, "admin-content-info", null);    
			String exceptionMessage = (String) pluginManager.getPluginSetting(pluginId, "exception-message", null);
			if(adminContentInfo != null) {
				StringTokenizer nameValuePairs = new StringTokenizer(adminContentInfo, ";");
		 		while(nameValuePairs.hasMoreTokens()) {
		 			 String currentToken = nameValuePairs.nextToken().trim();
		 			 if(currentToken.startsWith("conditional-logic-validator=")) {
		 				 String validatorName = currentToken.substring("conditional-logic-validator=".length());
	  				 	 Class<?> validatorClass = pluginManager.getClassLoader(pluginId).loadClass(validatorName);
		 				 IAdminContentConditionalLogic validator = (IAdminContentConditionalLogic) validatorClass.newInstance();
		 				 int status = validator.validate();
		 				 if(status == IAdminContentConditionalLogic.DISPLAY_ADMIN_CONTENT) {
		 					settings.add(new Setting("admin-content-info", adminContentInfo));
		 				 }
		 				 if(status == IAdminContentConditionalLogic.DISPLAY_EXCEPTION_MESSAGE && exceptionMessage != null) {
		 					settings.add(new Setting("exception-message", exceptionMessage));
		 				 }
		 				 if(status == IAdminContentConditionalLogic.AVOID_ADMIN_CONTENT) {
		 					 continue nextPlugin;
		 				 }
		 			 }
		 		}
			}
		  }
	  }	catch(Exception e) {
		  logger.error(e.getMessage(), e);
	  }
	  return settings;
  }

  @GET
  @Path("/settings")
  @Produces({ APPLICATION_JSON, APPLICATION_XML })
  public List<Setting> getMantleSettings() {
    ArrayList<Setting> settings = new ArrayList<Setting>();
    settings.add(new Setting("login-show-users-list", PentahoSystem.getSystemSetting("login-show-users-list", ""))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    settings.add(new Setting("documentation-url", PentahoSystem.getSystemSetting("documentation-url", ""))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    // Check for override of New Analysis View via pentaho.xml
    // Poked in via pentaho.xml entries
    // <new-analysis-view>
    // <command-url>http://www.google.com</command-url>
    // <command-title>Marc Analysis View</command-title>
    // </new-analysis-view>
    // <new-report>
    // <command-url>http://www.yahoo.com</command-url>
    // <command-title>Marc New Report</command-title>
    // </new-report>
    //
    String overrideNewAnalysisViewCommmand = PentahoSystem.getSystemSetting("new-analysis-view/command-url", null); //$NON-NLS-1$
    String overrideNewAnalysisViewTitle = PentahoSystem.getSystemSetting("new-analysis-view/command-title", null); //$NON-NLS-1$
    if ((overrideNewAnalysisViewCommmand != null) && (overrideNewAnalysisViewTitle != null)) {
      settings.add(new Setting("new-analysis-view-command-url", overrideNewAnalysisViewCommmand)); //$NON-NLS-1$
      settings.add(new Setting("new-analysis-view-command-title", overrideNewAnalysisViewTitle)); //$NON-NLS-1$
    }
    String overrideNewReportCommmand = PentahoSystem.getSystemSetting("new-report/command-url", null); //$NON-NLS-1$
    String overrideNewReportTitle = PentahoSystem.getSystemSetting("new-report/command-title", null); //$NON-NLS-1$
    if ((overrideNewReportCommmand != null) && (overrideNewReportTitle != null)) {
      settings.add(new Setting("new-report-command-url", overrideNewReportCommmand)); //$NON-NLS-1$
      settings.add(new Setting("new-report-command-title", overrideNewReportTitle)); //$NON-NLS-1$
    }

    IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, getPentahoSession()); //$NON-NLS-1$
    if (pluginManager != null) {
      // load content types from IPluginSettings
      int i = 0;
      for (String contentType : pluginManager.getContentTypes()) {
        IContentInfo info = pluginManager.getContentTypeInfo(contentType);
        if (info != null) {
          settings.add(new Setting("plugin-content-type-" + i, "." + contentType)); //$NON-NLS-1$ //$NON-NLS-2$
          settings.add(new Setting("plugin-content-type-icon-" + i, info.getIconUrl())); //$NON-NLS-1$
          int j = 0;
          for (IPluginOperation operation : info.getOperations()) {
            settings.add(new Setting("plugin-content-type-" + i + "-command-" + j, operation.getId())); //$NON-NLS-1$
            settings.add(new Setting("plugin-content-type-" + i + "-command-perspective-" + j, operation.getPerspective())); //$NON-NLS-1$
            j++;
          }
          i++;
        }
      }
    }

    return settings;
  }

  // For New Analysis View
  @GET
  @Path("/cubes")
  @Produces({ APPLICATION_JSON, APPLICATION_XML })
  public List<Cube> getMondrianCatalogs() {
    ArrayList<Cube> cubes = new ArrayList<Cube>();

    IMondrianCatalogService mondrianCatalogService = PentahoSystem.get(IMondrianCatalogService.class, "IMondrianCatalogService", getPentahoSession()); //$NON-NLS-1$
    List<MondrianCatalog> catalogs = mondrianCatalogService.listCatalogs(getPentahoSession(), true);

    for (MondrianCatalog cat : catalogs) {
      for (MondrianCube cube : cat.getSchema().getCubes()) {
        cubes.add(new Cube(cat.getName(), cube.getName(), cube.getId()));
      }
    }
    return cubes;
  }

  @POST
  @Path("/locale")
  public Response setLocaleOverride(String locale) {
    httpServletRequest.getSession().setAttribute("locale_override", locale);
    if (!StringUtils.isEmpty(locale)) {
      LocaleHelper.setLocaleOverride(new Locale(locale));
    } else {
      LocaleHelper.setLocaleOverride(null);
    }
    return getLocale();
  }

  @GET
  @Path("/locale")
  public Response getLocale() {
    return Response.ok(LocaleHelper.getLocale().toString()).build();
  }

  @POST
  @Path("/session-variable")
  public Response setSessionVariable(@QueryParam("key") String key, @QueryParam("value") String value) {
    IPentahoSession session = getPentahoSession();
    session.setAttribute(key, value);
    return Response.ok(session.getAttribute(key)).build();
  }

  @GET
  @Path("/session-variable")
  public Response getSessionVariable(@QueryParam("key") String key) {
    return Response.ok(getPentahoSession().getAttribute(key)).build();
  }

}
