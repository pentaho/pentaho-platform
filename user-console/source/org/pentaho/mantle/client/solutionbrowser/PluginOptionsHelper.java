package org.pentaho.mantle.client.solutionbrowser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.pentaho.mantle.client.commands.AddDatasourceCommand;
import org.pentaho.mantle.client.commands.AnalysisViewCommand;
import org.pentaho.mantle.client.commands.ManageDatasourcesCommand;
import org.pentaho.mantle.client.commands.UrlCommand;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand.COMMAND;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

public class PluginOptionsHelper {

  private static ArrayList<FileTypeEnabledOptions> enabledOptionsList = new ArrayList<FileTypeEnabledOptions>();
  private static ArrayList<ContentTypePlugin> contentTypePluginList = new ArrayList<ContentTypePlugin>();
  private static String newAnalysisViewOverrideCommandUrl;
  private static String newAnalysisViewOverrideCommandTitle;
  private static String manageDatasourcesOverrideCommandUrl;
  private static String manageDatasourcesOverrideCommandTitle;
  private static String addDatasourceOverrideCommandUrl;
  private static String addDatasourceOverrideCommandTitle;

  public static void buildEnabledOptionsList(Map<String, String> settings) {
    enabledOptionsList.clear();
    contentTypePluginList.clear();

    // Check for override of NewAnalysisView
    // Poked in via pentaho.xml entry
    if (settings.containsKey("new-analysis-view-command-url")) { //$NON-NLS-1$
      newAnalysisViewOverrideCommandUrl = settings.get("new-analysis-view-command-url"); //$NON-NLS-1$
      newAnalysisViewOverrideCommandTitle = settings.get("new-analysis-view-command-title"); //$NON-NLS-1$
    }
    // Check for override of New Report
    // Poked in via pentaho.xml entry
    // <new-analysis-view>
    // <command-url>http://www.google.com</command-url>
    // <command-title>Marc Analysis View</command-title>
    // </new-analysis-view>
    // <new-report>
    // <command-url>http://www.yahoo.com</command-url>
    // <command-title>Marc New Report</command-title>
    // </new-report>
    //

    if (settings.containsKey("manage-datasources-command-url")) { //$NON-NLS-1$
      manageDatasourcesOverrideCommandUrl = settings.get("manage-datasources-command-url"); //$NON-NLS-1$
      manageDatasourcesOverrideCommandTitle = settings.get("manage-datasources-command-title"); //$NON-NLS-1$
    }

    if (settings.containsKey("add-datasource-command-url")) { //$NON-NLS-1$
      addDatasourceOverrideCommandUrl = settings.get("add-datasource-command-url"); //$NON-NLS-1$
      addDatasourceOverrideCommandTitle = settings.get("add-datasource-command-title"); //$NON-NLS-1$
    }

    // load plugins
    int index = 0;
    String pluginSetting = "plugin-content-type-" + index; //$NON-NLS-1$
    while (settings.containsKey(pluginSetting)) {
      String fileExtension = settings.get(pluginSetting);
      String fileIcon = settings.get("plugin-content-type-icon-" + index);
      FileTypeEnabledOptions pluginMenu = new FileTypeEnabledOptions(fileExtension);
      ContentTypePlugin plugin = new ContentTypePlugin(fileExtension, fileIcon);

      int cmdIndex = 0;
      String cmdSetting = pluginSetting + "-command-" + cmdIndex;
      while (settings.containsKey(cmdSetting)) {
        try {
          COMMAND cmd = COMMAND.valueOf(settings.get(cmdSetting));
          String perspective = settings.get(pluginSetting + "-command-perspective-" + cmdIndex);
          pluginMenu.addCommand(cmd);
          plugin.addCommandPerspective(cmd, perspective);
          cmdSetting = pluginSetting + "-command-" + (++cmdIndex);
        } catch (Throwable t) {
          cmdSetting = pluginSetting + "-command-" + (++cmdIndex);
          // command is not found, invalid, we cannot let this break
          // the entire application, and it doesn't help to annoy every
          // single user everytime they start their application if
          // a plugin has a poorly configured plugin
        }
      }

      // all files can share, delete, and have properties
      pluginMenu.addCommand(COMMAND.SHARE);
      pluginMenu.addCommand(COMMAND.DELETE);
      pluginMenu.addCommand(COMMAND.PROPERTIES);
      pluginMenu.addCommand(COMMAND.EXPORT);

      contentTypePluginList.add(plugin);
      enabledOptionsList.add(pluginMenu);

      // check for another one
      pluginSetting = "plugin-content-type-" + (++index); //$NON-NLS-1$
    }

    FileTypeEnabledOptions analysisMenu = new FileTypeEnabledOptions(FileItem.ANALYSIS_VIEW_SUFFIX);
    analysisMenu.addCommand(COMMAND.RUN);
    analysisMenu.addCommand(COMMAND.NEWWINDOW);
    analysisMenu.addCommand(COMMAND.EDIT);
    analysisMenu.addCommand(COMMAND.EDIT_ACTION);
    analysisMenu.addCommand(COMMAND.DELETE);
    analysisMenu.addCommand(COMMAND.SHARE);
    analysisMenu.addCommand(COMMAND.PROPERTIES);
    analysisMenu.addCommand(COMMAND.EXPORT);
    enabledOptionsList.add(analysisMenu);

    FileTypeEnabledOptions xactionMenu = new FileTypeEnabledOptions(FileItem.XACTION_SUFFIX);
    xactionMenu.addCommand(COMMAND.RUN);
    xactionMenu.addCommand(COMMAND.NEWWINDOW);
    xactionMenu.addCommand(COMMAND.BACKGROUND);
    xactionMenu.addCommand(COMMAND.EDIT_ACTION);
    xactionMenu.addCommand(COMMAND.DELETE);
    xactionMenu.addCommand(COMMAND.SCHEDULE_NEW);
    xactionMenu.addCommand(COMMAND.SHARE);
    xactionMenu.addCommand(COMMAND.PROPERTIES);
    xactionMenu.addCommand(COMMAND.EXPORT);
    enabledOptionsList.add(xactionMenu);

    FileTypeEnabledOptions defaultMenu = new FileTypeEnabledOptions(null);
    defaultMenu.addCommand(COMMAND.RUN);
    defaultMenu.addCommand(COMMAND.NEWWINDOW);
    defaultMenu.addCommand(COMMAND.DELETE);
    defaultMenu.addCommand(COMMAND.SHARE);
    defaultMenu.addCommand(COMMAND.PROPERTIES);
    defaultMenu.addCommand(COMMAND.EXPORT);
    enabledOptionsList.add(defaultMenu);
  }

  public static FileTypeEnabledOptions getEnabledOptions(String filename) {
    for (FileTypeEnabledOptions option : enabledOptionsList) {
      if (option.isSupportedFile(filename)) {
        return option;
      }
    }
    return null;
  }

  public static ContentTypePlugin getContentTypePlugin(String filename) {
    for (ContentTypePlugin plugin : contentTypePluginList) {
      if (plugin.isSupportedFile(filename)) {
        return plugin;
      }
    }
    return null;
  }

  public static Command getNewAnalysisViewCommand() {
    if (newAnalysisViewOverrideCommandUrl == null) {
      return new AnalysisViewCommand();
    } else {
      return new UrlCommand(newAnalysisViewOverrideCommandUrl, newAnalysisViewOverrideCommandTitle);
    }
  }

  public static Command getManageDatasourcesCommand() {
    if (manageDatasourcesOverrideCommandUrl == null) {
      return new ManageDatasourcesCommand();
    } else {
      return new UrlCommand(manageDatasourcesOverrideCommandUrl, manageDatasourcesOverrideCommandTitle);
    }
  }

  public static Command getAddDatasourceCommand() {
    if (addDatasourceOverrideCommandUrl == null) {
      return new AddDatasourceCommand();
    } else {
      return new UrlCommand(addDatasourceOverrideCommandUrl, addDatasourceOverrideCommandTitle);
    }
  }

  public static String fixRelativePath(String url) {
    if (!url.startsWith("http") /* && GWT.isScript() */) {
    	String href = Window.Location.getHref().substring(0, Window.Location.getHref().indexOf("Home"));
      if (href.endsWith("/") || url.startsWith("/")) {
        url = href += url;
      } else {
        url = href + "/" + url;
      }
    }
    return url;
  }

  public static class ContentTypePlugin {

    String fileExtension;
    String fileIcon;
    Map<COMMAND, String> commands;

    ContentTypePlugin(String fileExtension, String fileIcon) {
      this.fileExtension = fileExtension;
      this.fileIcon = fileIcon;
      this.commands = new HashMap<COMMAND, String>();
    }

    public void addCommandPerspective(COMMAND cmd, String perspective) {
        commands.put(cmd, perspective);
    }

    public boolean hasCommand(COMMAND cmd) {
        return commands.containsKey(cmd);
    }

    public String getCommandPerspective(COMMAND cmd) {
      return commands.get(cmd);
      }
    public boolean isSupportedFile(String filename) {
      return filename != null && filename.endsWith(fileExtension);
    }

    private static native String getLanguagePreference()
    /*-{
      var m = $doc.getElementsByTagName('meta');
      for(var i in m) {
        if(m[i].name == 'gwt:property' && m[i].content.indexOf('locale=') != -1) {
          return m[i].content.substring(m[i].content.indexOf('=')+1);
        }
      }
      return "default";
    }-*/;


    public String getFileIcon() {
      return fileIcon;
    }
  }

}
