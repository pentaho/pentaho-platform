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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 *
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.mantle.client.objects.MantleXulOverlay;
import org.pentaho.mantle.client.objects.SimpleMessageException;
import org.pentaho.mantle.client.service.MantleService;
import org.pentaho.mantle.client.usersettings.IMantleUserSettingsConstants;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginOperation;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.perspective.IPluginPerspectiveManager;
import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.ui.IThemeManager;
import org.pentaho.platform.api.ui.Theme;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCube;
import org.pentaho.platform.util.VersionHelper;
import org.pentaho.platform.util.VersionInfo;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.versionchecker.PentahoVersionCheckReflectHelper;
import org.pentaho.ui.xul.XulOverlay;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class MantleServlet extends RemoteServiceServlet implements MantleService {

  private static final long serialVersionUID = 119274827408056040L;

  protected static final Log logger = LogFactory.getLog(MantleServlet.class);
  private static final String DESC_SEPERATOR = " : "; //$NON-NLS-1$

  protected void onBeforeRequestDeserialized(String serializedRequest) {
    PentahoSystem.systemEntryPoint();
  }

  protected void onAfterResponseSerialized(String serializedResponse) {
    PentahoSystem.systemExitPoint();
  }

  @Override
  protected void doUnexpectedFailure(Throwable e) {
    e.printStackTrace();
    try {
      getThreadLocalResponse().sendRedirect("../Home"); //$NON-NLS-1$
      PentahoSystem.systemExitPoint();
    } catch (IOException e1) {
      logger.error("doUnexpectedFailure", e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    logger.warn("GET request not supported");
    try {
      resp.sendRedirect("../Home"); //$NON-NLS-1$
    } catch (IOException e1) {
    }
  }

  private IPentahoSession getPentahoSession() {
    return PentahoSessionHolder.getSession();
  }

  public boolean isAdministrator() {
    return SecurityHelper.isPentahoAdministrator(getPentahoSession());
  }

  // @SuppressWarnings("rawtypes")
  // private UserFilesComponent getUserFilesComponent() {
  //    UserFilesComponent userFiles = PentahoSystem.get(UserFilesComponent.class, "IUserFilesComponent", getPentahoSession()); //$NON-NLS-1$
  // IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
  //    String thisUrl = requestContext.getContextPath() + "UserContent?"; //$NON-NLS-1$
  // SimpleUrlFactory urlFactory = new SimpleUrlFactory(thisUrl);
  // userFiles.setUrlFactory(urlFactory);
  // userFiles.setRequest(getThreadLocalRequest());
  // userFiles.setResponse(getThreadLocalResponse());
  // userFiles.setMessages(new ArrayList());
  // userFiles.validate(getPentahoSession(), null);
  // return userFiles;
  // }

  @SuppressWarnings("rawtypes")
  public String getSoftwareUpdatesDocument() {
    if (PentahoVersionCheckReflectHelper.isVersionCheckerAvailable()) {
      List results = PentahoVersionCheckReflectHelper.performVersionCheck(false, -1);
      return PentahoVersionCheckReflectHelper.logVersionCheck(results, logger);
    }
    return "<vercheck><error><[!CDATA[Version Checker is disabled]]></error></vercheck>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  public void executeGlobalActions() {
    if (isAdministrator()) {
      PentahoSystem.publish(getPentahoSession(), org.pentaho.platform.engine.core.system.GlobalListsPublisher.class.getName());
    }
  }

  public String refreshMetadata() {
    String result = null;
    if (isAdministrator()) {
      result = PentahoSystem.publish(getPentahoSession(), org.pentaho.platform.engine.services.metadata.MetadataPublisher.class.getName());
    }
    return result;
  }

  public void refreshSystemSettings() {
    if (isAdministrator()) {
      PentahoSystem.publish(getPentahoSession(), org.pentaho.platform.engine.core.system.SettingsPublisher.class.getName());
    }
  }

  /**
   * Note that this implementation is different from MantleLoginServlet.isAuthenticated. This method may return true even if the user is anonymous. That is not
   * the case for MantleLoginServlet.isAuthenticated.
   */
  public boolean isAuthenticated() {
    return getPentahoSession() != null && getPentahoSession().isAuthenticated();
  }

  public boolean deleteContentItem(String contentId) {
    // UserFilesComponent userFiles = getUserFilesComponent();
    // boolean status = userFiles.deleteContent(contentId);
    // return status;
    return false;
  }

  public void refreshRepository() {
    if (isAdministrator()) {
      PentahoSystem.get(ISolutionRepository.class, getPentahoSession()).reloadSolutionRepository(getPentahoSession(), getPentahoSession().getLoggingLevel());
    }
  }

  public void flushMondrianSchemaCache() {
    if (isAdministrator()) {
      IMondrianCatalogService mondrianCatalogService = PentahoSystem.get(IMondrianCatalogService.class, "IMondrianCatalogService", getPentahoSession()); //$NON-NLS-1$
      mondrianCatalogService.reInit(getPentahoSession());
    }
  }

  public ArrayList<String> getAllRoles() {
    IUserRoleListService userRoleListService = PentahoSystem.get(IUserRoleListService.class);
    return new ArrayList<String>(userRoleListService.getAllRoles());
  }

  public ArrayList<String> getAllUsers() {
    IUserRoleListService userRoleListService = PentahoSystem.get(IUserRoleListService.class);
    return new ArrayList<String>(userRoleListService.getAllUsers());
  }

  public boolean hasAccess(String path, String fileName, int actionOperation) {
    return true;
  }

  public boolean doesSolutionRepositorySupportPermissions() {
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, getPentahoSession());
    return repository.supportsAccessControls();
  }

  @SuppressWarnings("rawtypes")
  public HashMap<String, String> getMantleSettings() {
    HashMap<String, String> settings = new HashMap<String, String>();
    // read properties file
    Properties props = new Properties();
    try {
      props.load(getClass().getResourceAsStream("/org/pentaho/mantle/server/MantleSettings.properties")); //$NON-NLS-1$
      Enumeration keys = props.keys();
      while (keys.hasMoreElements()) {
        String key = (String) keys.nextElement();
        String value = (String) props.getProperty(key);
        settings.put(key, value);
      }

      settings.put("login-show-users-list", PentahoSystem.getSystemSetting("login-show-users-list", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      settings.put("documentation-url", PentahoSystem.getSystemSetting("documentation-url", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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
        settings.put("new-analysis-view-command-url", overrideNewAnalysisViewCommmand); //$NON-NLS-1$
        settings.put("new-analysis-view-command-title", overrideNewAnalysisViewTitle); //$NON-NLS-1$
      }
      String overrideNewReportCommmand = PentahoSystem.getSystemSetting("new-report/command-url", null); //$NON-NLS-1$
      String overrideNewReportTitle = PentahoSystem.getSystemSetting("new-report/command-title", null); //$NON-NLS-1$
      if ((overrideNewReportCommmand != null) && (overrideNewReportTitle != null)) {
        settings.put("new-report-command-url", overrideNewReportCommmand); //$NON-NLS-1$
        settings.put("new-report-command-title", overrideNewReportTitle); //$NON-NLS-1$
      }

      IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, getPentahoSession()); //$NON-NLS-1$
      if (pluginManager != null) {
        // load content types from IPluginSettings
        int i = 0;
        for (String contentType : pluginManager.getContentTypes()) {
          IContentInfo info = pluginManager.getContentTypeInfo(contentType);
          if (info != null) {
            settings.put("plugin-content-type-" + i, "." + contentType); //$NON-NLS-1$ //$NON-NLS-2$
            settings.put("plugin-content-type-icon-" + i, info.getIconUrl()); //$NON-NLS-1$
            int j = 0;
            for (IPluginOperation operation : info.getOperations()) {
              settings.put("plugin-content-type-" + i + "-command-" + j, operation.getId()); //$NON-NLS-1$
              settings.put("plugin-content-type-" + i + "-command-perspective-" + j, operation.getPerspective()); //$NON-NLS-1$
              j++;
            }
            i++;
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return settings;
  }

  /**
   * Gets the mondrian catalogs and populates a hash map with schema name as the key and list of cube names as strings.
   * 
   * @return HashMap The hashmap has schema name as keys and a list of cube names and captions as values
   */
  public HashMap<String, ArrayList<String[]>> getMondrianCatalogs() {
    HashMap<String, ArrayList<String[]>> catalogCubeHashMap = new LinkedHashMap<String, ArrayList<String[]>>();

    IMondrianCatalogService mondrianCatalogService = PentahoSystem.get(IMondrianCatalogService.class, "IMondrianCatalogService", getPentahoSession()); //$NON-NLS-1$
    List<MondrianCatalog> catalogs = mondrianCatalogService.listCatalogs(getPentahoSession(), true);

    for (MondrianCatalog cat : catalogs) {
      ArrayList<String[]> cubes = new ArrayList<String[]>();
      catalogCubeHashMap.put(cat.getName(), cubes);
      for (MondrianCube cube : cat.getSchema().getCubes()) {
        cubes.add(new String[] { cube.getName(), cube.getId() });
      }
      // Sort the cubes names.
      Collections.sort(cubes, new Comparator<String[]>() {
        public int compare(String[] o1, String[] o2) {
          return o1[0].compareTo(o2[0]);
        }
      });
    }
    return catalogCubeHashMap;
  }

  public ArrayList<IUserSetting> getUserSettings() {
    try {
      IUserSettingService settingsService = PentahoSystem.get(IUserSettingService.class, getPentahoSession());
      ArrayList<IUserSetting> settings = (ArrayList<IUserSetting>) settingsService.getUserSettings();
      return settings;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public IUserSetting getUserSetting(String settingName) throws SimpleMessageException {
    try {
      IUserSettingService settingsService = PentahoSystem.get(IUserSettingService.class, getPentahoSession());
      IUserSetting setting = settingsService.getUserSetting(settingName, null);
      return setting;
    } catch (Exception e) {
      throw new SimpleMessageException(e.getMessage());
    }
  }

  public void setLocaleOverride(String locale) {
    getThreadLocalRequest().getSession().setAttribute("locale_override", locale);
    if (!StringUtils.isEmpty(locale)) {
      LocaleHelper.setLocaleOverride(new Locale(locale));
    } else {
      LocaleHelper.setLocaleOverride(null);
    }
  }

  public Map<String, String> getSystemThemes() {
    IThemeManager themeManager = PentahoSystem.get(IThemeManager.class);
    List<String> ids = themeManager.getSystemThemeIds();
    Map<String, String> themes = new HashMap<String, String>();
    for (String id : ids) {
      Theme theme = themeManager.getSystemTheme(id);
      if (theme.isHidden() == false) {
        themes.put(id, theme.getName());
      }
    }
    return themes;
  }

  public void setTheme(String theme) throws SimpleMessageException {
    getPentahoSession().setAttribute("pentaho-user-theme", theme);
    try {
      IUserSettingService settingsService = PentahoSystem.get(IUserSettingService.class, getPentahoSession());
      settingsService.setUserSetting("pentaho-user-theme", theme);
    } catch (Exception e) {
      throw new SimpleMessageException(e.getMessage());
    }
  }

  public String getActiveTheme() {
    IUserSettingService settingsService = PentahoSystem.get(IUserSettingService.class, getPentahoSession());
    return StringUtils.defaultIfEmpty((String) getPentahoSession().getAttribute("pentaho-user-theme"),
        settingsService.getUserSetting("pentaho-user-theme", PentahoSystem.getSystemSetting("default-theme", "onyx")).getSettingValue());
  }

  public void setUserSetting(String settingName, String settingValue) throws SimpleMessageException {
    try {
      IUserSettingService settingsService = PentahoSystem.get(IUserSettingService.class, getPentahoSession());
      settingsService.setUserSetting(settingName, settingValue);
    } catch (Exception e) {
      throw new SimpleMessageException(e.getMessage());
    }
  }

  public void setShowNavigator(boolean showNavigator) {
    IUserSettingService settingsService = PentahoSystem.get(IUserSettingService.class, getPentahoSession());
    settingsService.setUserSetting(IMantleUserSettingsConstants.MANTLE_SHOW_NAVIGATOR, "" + showNavigator); //$NON-NLS-1$
  }

  public void setShowLocalizedFileNames(boolean showLocalizedFileNames) {
    IUserSettingService settingsService = PentahoSystem.get(IUserSettingService.class, getPentahoSession());
    settingsService.setUserSetting(IMantleUserSettingsConstants.MANTLE_SHOW_LOCALIZED_FILENAMES, "" + showLocalizedFileNames); //$NON-NLS-1$
  }

  public void setShowHiddenFiles(boolean showHiddenFiles) {
    IUserSettingService settingsService = PentahoSystem.get(IUserSettingService.class, getPentahoSession());
    settingsService.setUserSetting(IMantleUserSettingsConstants.MANTLE_SHOW_HIDDEN_FILES, "" + showHiddenFiles); //$NON-NLS-1$
  }

  public boolean repositorySupportsACLS() {
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, getPentahoSession());
    return repository.supportsAccessControls();
  }

  public String getVersion() {
    VersionInfo versionInfo = VersionHelper.getVersionInfo(PentahoSystem.class);
    return versionInfo.getVersionNumber();
  }

  public ArrayList<XulOverlay> getOverlays() {
    IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, getPentahoSession()); //$NON-NLS-1$

    List<XulOverlay> overlays = pluginManager.getOverlays();
    ArrayList<XulOverlay> result = new ArrayList<XulOverlay>();
    for (XulOverlay overlay : overlays) {
      MantleXulOverlay tempOverlay = new MantleXulOverlay(overlay.getId(), overlay.getOverlayUri(), overlay.getSource(), overlay.getResourceBundleUri());
      result.add(tempOverlay);
    }
    return result;
  }

  public ArrayList<IPluginPerspective> getPluginPerpectives() {
    IPluginPerspectiveManager manager = PentahoSystem.get(IPluginPerspectiveManager.class, getPentahoSession()); //$NON-NLS-1$

    for (IPluginPerspective perspective : manager.getPluginPerspectives()) {
      if (perspective.getOverlays() != null) {
        ArrayList<XulOverlay> safeOverlays = new ArrayList<XulOverlay>();
        for (XulOverlay orig : perspective.getOverlays()) {
          MantleXulOverlay tmpOverlay = new MantleXulOverlay(orig.getId(), orig.getOverlayUri(), orig.getSource(), orig.getResourceBundleUri());
          safeOverlays.add(tmpOverlay);
        }
        perspective.setOverlays(safeOverlays);
      }
    }

    return new ArrayList<IPluginPerspective>(manager.getPluginPerspectives());
  }

  public void purgeReportingDataCache() {
    ICacheManager cacheManager = PentahoSystem.get(ICacheManager.class);
    cacheManager.clearRegionCache("report-dataset-cache");
    cacheManager.clearRegionCache("report-output-handlers");

  }

}