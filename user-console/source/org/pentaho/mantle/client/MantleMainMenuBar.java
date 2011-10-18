package org.pentaho.mantle.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.gwt.widgets.client.menuitem.CheckBoxMenuItem;
import org.pentaho.gwt.widgets.client.menuitem.PentahoMenuItem;
import org.pentaho.mantle.client.commands.AboutCommand;
import org.pentaho.mantle.client.commands.CommandExec;
import org.pentaho.mantle.client.commands.ExecuteGlobalActionsCommand;
import org.pentaho.mantle.client.commands.JavascriptObjectCommand;
import org.pentaho.mantle.client.commands.LogoutCommand;
import org.pentaho.mantle.client.commands.OpenDocCommand;
import org.pentaho.mantle.client.commands.OpenFileCommand;
import org.pentaho.mantle.client.commands.OpenURLCommand;
import org.pentaho.mantle.client.commands.PentahoHomeCommand;
import org.pentaho.mantle.client.commands.PurgeMondrianSchemaCacheCommand;
import org.pentaho.mantle.client.commands.PurgeReportingDataCacheCommand;
import org.pentaho.mantle.client.commands.RefreshMetaDataCommand;
import org.pentaho.mantle.client.commands.RefreshRepositoryCommand;
import org.pentaho.mantle.client.commands.RefreshSystemSettingsCommand;
import org.pentaho.mantle.client.commands.RefreshWorkspaceCommand;
import org.pentaho.mantle.client.commands.SaveCommand;
import org.pentaho.mantle.client.commands.ShowPreferencesCommand;
import org.pentaho.mantle.client.commands.SwitchLocaleCommand;
import org.pentaho.mantle.client.commands.SwitchThemeCommand;
import org.pentaho.mantle.client.commands.UrlCommand;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.client.solutionbrowser.PluginOptionsHelper;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserListener;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand.COMMAND;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

@Deprecated
public class MantleMainMenuBar extends MenuBar implements SolutionBrowserListener {

  private MantleMenuBar viewMenu = new MantleMenuBar(true);
  // menu items (to be enabled/disabled)
  private PentahoMenuItem saveMenuItem;
  private PentahoMenuItem saveAsMenuItem;
  private PentahoMenuItem propertiesMenuItem;
  private MenuBar fileMenu;

  private CommandExec commandExec = GWT.create(CommandExec.class);
  private FileCommand propertiesCommand;
  private Map<String, MenuBar> menuBars = new HashMap<String, MenuBar>();
  private List<UIObject> standardViewMenuItems = new ArrayList<UIObject>();

  public MantleMainMenuBar() {
    super(false);
    getElement().setId("main_toolbar");
    setAutoOpen(false);
    setHeight("26px"); //$NON-NLS-1$
    setWidth("100%"); //$NON-NLS-1$

    setupNativeFunctions(this);
  }

  private native void setupNativeFunctions(MantleMainMenuBar menubar)
  /*-{
    $wnd.addMenuItem = function(text, parentId, command){
      if(typeof(command) == "function"){
        menubar.@org.pentaho.mantle.client.MantleMainMenuBar::addMenuItemWithFunction(Ljava/lang/String;Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(text, parentId, command);
      } else {
        menubar.@org.pentaho.mantle.client.MantleMainMenuBar::addMenuItemWithCommand(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(text, parentId, command);
      }
    }
  }-*/;

  private void addMenuItemWithCommand(String key, String parent, String command) {
    Command namedCommand = commandExec.lookupCommand(command);
    if (namedCommand == null) {
      Window.alert("Error finding command by with the name: " + command);
      return;
    }

    final MenuItem newMenuItem = new MenuItem(Messages.getString(key), namedCommand);//$NON-NLS-1$

    MenuBar targetMenuParent = findMenuBar(parent);
    targetMenuParent.addItem(newMenuItem);
  }

  private void addMenuItemWithFunction(String key, String parent, JavaScriptObject func) {
    final MenuItem newMenuItem = new MenuItem(Messages.getString(key), new JavascriptObjectCommand(func));//$NON-NLS-1$

    MenuBar targetMenuParent = findMenuBar(parent);
    targetMenuParent.addItem(newMenuItem);
  }

  // Amazingly getItems is a protected call preventing us from recursing through the menu-tree. WTG.
  // We're storing created items in a map for now.
  private MenuBar findMenuBar(String parentId) {
    return menuBars.get(parentId);
  }

  public void buildMenuBar(final HashMap<String, String> settings, final boolean isAdministrator) {
    clearItems();
    SolutionBrowserPanel.getInstance().addSolutionBrowserListener(this);

    saveMenuItem = new PentahoMenuItem(Messages.getString("save"), new SaveCommand(false)); //$NON-NLS-1$
    saveMenuItem.getElement().setId("save");
    saveAsMenuItem = new PentahoMenuItem(Messages.getString("saveAsEllipsis"), new SaveCommand(true)); //$NON-NLS-1$
    saveAsMenuItem.getElement().setId("saveAs");
    propertiesMenuItem = new PentahoMenuItem(Messages.getString("propertiesEllipsis"), null); //$NON-NLS-1$
    propertiesMenuItem.getElement().setId("properties");

    fileMenu = new MantleMenuBar(true);
    fileMenu.getElement().setId("file_menu");
    menuBars.put("file_menu", fileMenu);
    MenuBar newMenu = new MantleMenuBar(true);
    newMenu.getElement().setId("new_menu");
    menuBars.put("new_menu", newMenu);
    MenuItem analysisMenuItem = new MenuItem(Messages.getString("newAnalysisViewEllipsis"), PluginOptionsHelper.getNewAnalysisViewCommand());//$NON-NLS-1$
    analysisMenuItem.getElement().setId("new_analysis_view_menu_item");
    newMenu.addItem(analysisMenuItem); //$NON-NLS-1$
    // add additions to the file menu
    customizeMenu(newMenu, "file-new", settings); //$NON-NLS-1$

    MenuItem newMenuBar = new MenuItem(Messages.getString("_new"), newMenu); //$NON-NLS-1$
    newMenuBar.getElement().setId("new_menu_bar");
    fileMenu.addItem(newMenuBar);

    MenuItem openFileMenuItem = new MenuItem(Messages.getString("openEllipsis"), new OpenFileCommand());//$NON-NLS-1$
    openFileMenuItem.getElement().setId("open_file_menu_item");
    fileMenu.addItem(openFileMenuItem); //$NON-NLS-1$
    if (MantleApplication.showAdvancedFeatures) {
      fileMenu.addItem(Messages.getString("openURLEllipsis"), new OpenURLCommand()); //$NON-NLS-1$
    }
    fileMenu.addSeparator();

    fileMenu.addItem(saveMenuItem);
    fileMenu.addItem(saveAsMenuItem);
    fileMenu.addSeparator();

    if (MantleApplication.showAdvancedFeatures) {
      fileMenu.addItem(Messages.getString("userPreferencesEllipsis"), new ShowPreferencesCommand()); //$NON-NLS-1$
      fileMenu.addSeparator();
    }
    MenuBar manageContentMenu = new MantleMenuBar(true);
    manageContentMenu.getElement().setId("manage_content_menu");
    menuBars.put("manage_content_menu", manageContentMenu);
    MenuItem editContent = new MenuItem(Messages.getString("editEllipsis"), new OpenFileCommand(COMMAND.EDIT));//$NON-NLS-1$
    MenuItem shareContent = new MenuItem(Messages.getString("shareEllipsis"), new OpenFileCommand(COMMAND.SHARE)); //$NON-NLS-1$
    MenuItem scheduleContent = new MenuItem(Messages.getString("scheduleEllipsis"), new OpenFileCommand(COMMAND.SCHEDULE_NEW)); //$NON-NLS-1$

    editContent.getElement().setId("edit_content_menu_item");
    shareContent.getElement().setId("share_content_menu_item");
    scheduleContent.getElement().setId("schedule_content_menu_item");

    manageContentMenu.addItem(editContent);
    manageContentMenu.addItem(shareContent);
    manageContentMenu.addItem(scheduleContent);

    customizeMenu(manageContentMenu, "file-manage", settings); //$NON-NLS-1$
    MenuItem manageContentMenuBar = new MenuItem(Messages.getString("manage"), manageContentMenu); //$NON-NLS-1$
    manageContentMenuBar.getElement().setId("manage_content_menu_bar");
    fileMenu.addItem(manageContentMenuBar);
    fileMenu.addSeparator();
    fileMenu.addItem(propertiesMenuItem);
    fileMenu.addSeparator();
    MenuItem logoutMenuItem = new MenuItem(Messages.getString("logout"), true, new LogoutCommand()); //$NON-NLS-1$
    logoutMenuItem.getElement().setId("logout_menu_item");
    fileMenu.addItem(logoutMenuItem);

    // add additions to the file menu
    customizeMenu(fileMenu, "file", settings); //$NON-NLS-1$

    MenuItem fileMenuBar = new MenuItem(Messages.getString("file"), fileMenu);//$NON-NLS-1$
    fileMenuBar.getElement().setId("file_menu_bar");
    addItem(fileMenuBar);

    // add additions to the view menu
    viewMenu.getElement().setId("view_menu");

    standardViewMenuItems.add(new MenuItemSeparator());
    if ("true".equals(settings.get("show-theme-switcher"))) {
      final MenuBar themeMenu = new MantleMenuBar(true);
      themeMenu.getElement().setId("theme_menu");
      MantleServiceCache.getService().getActiveTheme(new AsyncCallback<String>() {
        public void onFailure(Throwable throwable) {
        }

        public void onSuccess(final String activeTheme) {
          MantleServiceCache.getService().getSystemThemes(new AsyncCallback<Map<String, String>>() {
            public void onFailure(Throwable throwable) {

            }

            public void onSuccess(Map<String, String> strings) {
              for (String themeId : strings.keySet()) {
                CheckBoxMenuItem themeMenuItem = new CheckBoxMenuItem(strings.get(themeId), new SwitchThemeCommand(themeId)); //$NON-NLS-1$
                themeMenuItem.getElement().setId(themeId + "_menu_item");
                themeMenuItem.setChecked(themeId.equals(activeTheme));
                themeMenu.addItem(themeMenuItem);

              }
            }
          });
        }
      });

      MenuItem themeMenuBar = new MenuItem(Messages.getString("themes"), themeMenu);//$NON-NLS-1$
      themeMenuBar.getElement().setId("themes_menu_bar");
      standardViewMenuItems.add(themeMenuBar);
    } // conditional theme addition

    Map<String, String> supportedLanguages = Messages.getResourceBundle().getSupportedLanguages();
    if (supportedLanguages != null && supportedLanguages.keySet() != null && !supportedLanguages.isEmpty()) {
      MenuBar langMenu = new MantleMenuBar(true);
      langMenu.getElement().setId("languages_menu");
      for (String lang : supportedLanguages.keySet()) {
        MenuItem langMenuItem = new MenuItem(supportedLanguages.get(lang), new SwitchLocaleCommand(lang)); //$NON-NLS-1$
        langMenuItem.getElement().setId(supportedLanguages.get(lang) + "_menu_item");
        langMenu.addItem(langMenuItem);
      }
      MenuItem langMenuBar = new MenuItem(Messages.getString("languages"), langMenu);//$NON-NLS-1$
      langMenuBar.getElement().setId("languages_menu_bar");
      standardViewMenuItems.add(langMenuBar);
      // toolsMenu.addSeparator();
    }

    standardViewMenuItems.add(new MenuItemSeparator());

    MenuItem refreshItem = new MenuItem(Messages.getString("refresh"), SolutionBrowserPanel.getInstance().isWorkspaceShowing() ? new RefreshWorkspaceCommand()
        : new RefreshRepositoryCommand());
    refreshItem.getElement().setId("view_refresh_menu_item");
    standardViewMenuItems.add(refreshItem); //$NON-NLS-1$
    installViewMenu(viewMenuAdditions);

    customizeMenu(viewMenu, "view", settings); //$NON-NLS-1$
    MenuItem viewMenuBar = new MenuItem(Messages.getString("view"), viewMenu); //$NON-NLS-1$
    viewMenuBar.getElement().setId("view_menu_bar");
    addItem(viewMenuBar);

    MenuBar adminMenu = new MantleMenuBar(true);

    menuBars.put("tools_menu_bar", adminMenu);
    if (isAdministrator) {
      MenuBar refreshMenu = new MantleMenuBar(true);
      refreshMenu.getElement().setId("admin_menu");

      MenuItem refreshRepositoryMenuItem = new MenuItem(Messages.getString("refreshRepository"), new RefreshRepositoryCommand()); //$NON-NLS-1$
      MenuItem refreshSystemSettingsMenuItem = new MenuItem(Messages.getString("refreshSystemSettings"), new RefreshSystemSettingsCommand()); //$NON-NLS-1$
      MenuItem refreshMetadataMenuItem = new MenuItem(Messages.getString("refreshReportingMetadata"), new RefreshMetaDataCommand()); //$NON-NLS-1$
      MenuItem executeGlobalActionsMenuItem = new MenuItem(Messages.getString("executeGlobalActions"), new ExecuteGlobalActionsCommand()); //$NON-NLS-1$
      MenuItem purgeMondrianSchemaCacheMenuItem = new MenuItem(Messages.getString("purgeMondrianSchemaCache"), new PurgeMondrianSchemaCacheCommand()); //$NON-NLS-1$
      MenuItem purgeReportingDataCacheMenuItem = new MenuItem(Messages.getString("purgeReportingDataCache"), new PurgeReportingDataCacheCommand()); //$NON-NLS-1$

      refreshRepositoryMenuItem.getElement().setId("refresh_repository_menu_item");
      refreshSystemSettingsMenuItem.getElement().setId("refresh_system_settings_menu_item");
      refreshMetadataMenuItem.getElement().setId("refresh_metadata_menu_item");
      executeGlobalActionsMenuItem.getElement().setId("execute_global_actions_menu_item");
      purgeMondrianSchemaCacheMenuItem.getElement().setId("purge_mondrian_schema_cache_menu_item");
      purgeReportingDataCacheMenuItem.getElement().setId("purge_reporting_data_cache_menu_item");

      refreshMenu.addItem(refreshRepositoryMenuItem);
      refreshMenu.addItem(refreshSystemSettingsMenuItem);
      refreshMenu.addItem(refreshMetadataMenuItem);
      refreshMenu.addItem(executeGlobalActionsMenuItem);
      refreshMenu.addItem(purgeMondrianSchemaCacheMenuItem);
      refreshMenu.addItem(purgeReportingDataCacheMenuItem);
      // add additions to the admin menu

      MenuItem refreshMenuBar = new MenuItem(Messages.getString("refresh"), refreshMenu);//$NON-NLS-1$
      refreshMenuBar.getElement().setId("admin_menu_bar");
      adminMenu.addItem(refreshMenuBar);

      //      MenuItem monitorCarteMenuItem = new MenuItem(Messages.getString("monitorCarte"), new UrlCommand(PluginOptionsHelper.fixRelativePath("kettle/status"), Messages.getString("monitorCarte"))); //$NON-NLS-1$
      // monitorCarteMenuItem.getElement().setId("monitor_carte_menu_item");
      // toolsMenu.addItem(monitorCarteMenuItem);

      //MenuItem softwareUpdatesMenuItem = new MenuItem(Messages.getString("softwareUpdates"), new CheckForSoftwareUpdatesCommand()); //$NON-NLS-1$
      // softwareUpdatesMenuItem.getElement().setId("software_updates_menu_item");
      // toolsMenu.addItem(softwareUpdatesMenuItem);

      MenuItem toolsMenuBar = new MenuItem(Messages.getString("tools"), adminMenu);//$NON-NLS-1$
      toolsMenuBar.getElement().setId("tools_menu_bar");
      addItem(toolsMenuBar);
      // add additions to the admin menu
      if (settings.get("toolsMenuTitle0") != null) { //$NON-NLS-1$
        adminMenu.addSeparator();
      }
      customizeMenu(adminMenu, "tools", settings); //$NON-NLS-1$
      customizeMenu(refreshMenu, "tools-refresh", settings); //$NON-NLS-1$      
    }

    MenuBar helpMenu = new MenuBar(true);
    helpMenu.getElement().setId("help_menu");

    menuBars.put("help_menu", helpMenu);
    MenuItem docMenuItem = new MenuItem(Messages.getString("documentation"), new OpenDocCommand(settings.get("documentation-url"))); //$NON-NLS-1$ //$NON-NLS-2$
    docMenuItem.getElement().setId("doc_menu_item"); //$NON-NLS-1$
    helpMenu.addItem(docMenuItem);
    helpMenu.addSeparator();

    MenuItem pentahoHomeMenuItem = new MenuItem(Messages.getString("pentahoHomePageName"), new PentahoHomeCommand());//$NON-NLS-1$
    pentahoHomeMenuItem.getElement().setId("pentaho_home_menu_item"); //$NON-NLS-1$
    helpMenu.addItem(pentahoHomeMenuItem);
    helpMenu.addSeparator();
    MenuItem aboutMenuItem = new MenuItem(Messages.getString("about"), new AboutCommand()); //$NON-NLS-1$
    aboutMenuItem.getElement().setId("about_menu_item"); //$NON-NLS-1$
    helpMenu.addItem(aboutMenuItem);

    // add additions to the help menu
    customizeMenu(helpMenu, "help", settings); //$NON-NLS-1$
    MenuItem helpMenuBar = new MenuItem(Messages.getString("help"), helpMenu); //$NON-NLS-1$
    helpMenuBar.getElement().setId("help_menu_bar");
    addItem(helpMenuBar);
  }

  private void customizeMenu(final MenuBar menu, final String menuId, final HashMap<String, String> settings) {

    // see if we have any plugins to add
    if (settings.get(menuId + "MenuTitle0") != null) { //$NON-NLS-1$
      // we have at least one so we add a separator first
      // menu.addSeparator();
      // we're going to loop until we don't find any more
      int idx = 0;
      String title = settings.get(menuId + "MenuTitle" + idx); //$NON-NLS-1$
      String command = settings.get(menuId + "MenuCommand" + idx); //$NON-NLS-1$
      while (title != null) {
        // create a generic UrlCommand for this
        UrlCommand menuCommand = new UrlCommand(command, title);

        MenuItem item = new MenuItem(title, menuCommand);
        // item.getElement().setId(title);

        // add it to the menu
        menu.addItem(item);
        idx++;
        // try to get the next one
        title = settings.get(menuId + "MenuTitle" + idx); //$NON-NLS-1$
        command = settings.get(menuId + "MenuCommand" + idx); //$NON-NLS-1$
      }
    }
  }

  // Cache menu additions for removal later.
  private ArrayList<UIObject> viewMenuAdditions = new ArrayList<UIObject>();

  public void installViewMenu(ArrayList<UIObject> viewMenuItems) {
    viewMenu.clearItems();

    // add new items
    for (UIObject widget : viewMenuItems) {
      if (widget instanceof MenuItem) {
        MenuItem menuItem = (MenuItem) widget;
        viewMenu.addItem(menuItem);
      } else if (widget instanceof MenuItemSeparator) {
        viewMenu.addSeparator((MenuItemSeparator) widget);
      } else if (widget instanceof MenuBar) {
        MenuBar menuBar = (MenuBar) widget;
        viewMenu.addItem(menuBar.getTitle(), menuBar);
      }
    }
    viewMenuAdditions = viewMenuItems;

    // Add in standard items:
    for (UIObject widget : standardViewMenuItems) {
      if (widget instanceof MenuItem) {
        MenuItem menuItem = (MenuItem) widget;
        viewMenu.addItem(menuItem);
      } else if (widget instanceof MenuItemSeparator) {
        viewMenu.addSeparator((MenuItemSeparator) widget);
      } else if (widget instanceof MenuBar) {
        MenuBar menuBar = (MenuBar) widget;
        viewMenu.addItem(menuBar.getTitle(), menuBar);
      }
    }
  }

  public void solutionBrowserEvent(SolutionBrowserListener.EventType type, Widget panel, FileItem selectedFileItem) {
    String selectedTabURL = null;
    boolean saveEnabled = false;
    SolutionFileInfo fileInfo = null;
    if (panel != null && panel instanceof IFrameTabPanel) {
      selectedTabURL = ((IFrameTabPanel) panel).getUrl();
      fileInfo = ((IFrameTabPanel) panel).getFileInfo();
      saveEnabled = ((IFrameTabPanel) panel).isSaveEnabled();
    }

    final boolean isEnabled = (selectedTabURL != null && !"".equals(selectedTabURL)); //$NON-NLS-1$

    // Properties menu item should have a command associated with it ONLY when it is enabled.
    if (isEnabled) {
      if (fileInfo != null) {

        propertiesMenuItem.setEnabled(false);
        propertiesMenuItem.setCommand(null);

        // JCRFIX BISERVER-5367 Needs to be uncommented and fixed to allow viewing of jcr file properties.
        // propertiesMenuItem.setEnabled(true);
        // propertiesMenuItem.setCommand(new FileCommand(FileCommand.COMMAND.PROPERTIES, null, fileInfo));
      } else {
        propertiesMenuItem.setEnabled(false);
        propertiesMenuItem.setCommand(null);
      }
    } else {
      propertiesMenuItem.setCommand(null);
    }

    saveMenuItem.setEnabled(saveEnabled && isEnabled);
    saveAsMenuItem.setEnabled(saveEnabled && isEnabled);
  }

}
