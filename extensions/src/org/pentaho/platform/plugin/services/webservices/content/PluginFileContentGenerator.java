package org.pentaho.platform.plugin.services.webservices.content;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.SimpleContentGenerator;

public class PluginFileContentGenerator extends SimpleContentGenerator {

  private static final long serialVersionUID = 1L;

  String mimeType;
  String relativeFilePath;
  String pluginId;

  @Override
  public Log getLogger() {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void createContent(OutputStream outputStream) throws Exception {
      IPluginResourceLoader pluginResourceLoader = PentahoSystem.get(IPluginResourceLoader.class);
      IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class);
      ClassLoader classLoader = pluginManager.getClassLoader(pluginId);
      String filePath = !relativeFilePath.startsWith("/") ? "/" + relativeFilePath : relativeFilePath;
      InputStream inputStream = pluginResourceLoader.getResourceAsStream(classLoader, filePath);
      int val;
      while ((val = inputStream.read()) != -1) {
          outputStream.write(val);
      }
      outputStream.flush();
  }

  @Override
  public String getMimeType() {
      return mimeType;
  }

  public String getRelativeFilePath() {
    return relativeFilePath;
  }

  public void setRelativeFilePath(String relativeFilePath) {
    this.relativeFilePath = relativeFilePath;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public String getPluginId() {
    return pluginId;
  }

  public void setPluginId(String pluginId) {
    this.pluginId = pluginId;
  }
}
