package org.pentaho.platform.plugin.services.importer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.plugin.services.messages.Messages;

import java.util.Map;

/**
 * Default implementation of IPlatformImporter. This class serves to route import requests to the appropriate
 * IPlatformImportHandler based on the mime-type of the given content. If not supplied the mime-type will be computed
 * by the IPlatformImportMimeResolver.
 *
 * User: nbaker
 * Date: 5/29/12
 */
public class PentahoPlatformImporter implements IPlatformImporter {

  private static final Log log = LogFactory.getLog(PentahoPlatformImporter.class);
  private static final Messages messages = Messages.getInstance();
  private Map<String, IPlatformImportHandler> importHandlers;
  private IPlatformImportHandler defaultHandler;
  private IPlatformImportMimeResolver mimeResolver;

  public PentahoPlatformImporter(Map<String, IPlatformImportHandler> handlerMap, IPlatformImportMimeResolver mimeResolver){
    this.importHandlers = handlerMap;
    this.mimeResolver = mimeResolver;
  }

  public IPlatformImportHandler getDefaultHandler() {
    return defaultHandler;
  }

  public void setDefaultHandler(IPlatformImportHandler defaultHandler) {
    this.defaultHandler = defaultHandler;
  }

  public void importFile(IPlatformImportBundle file) throws PlatformImportException {
    String mime = mimeResolver.resolveMimeForBundle(file);
    if(mime == null){
      throw new PlatformImportException(messages.getString("PentahoPlatformImporter.ERROR_0001_INVALID_MIME_TYPE"));
    }
    IPlatformImportHandler handler = (importHandlers.containsKey(mime) == false) ? defaultHandler : importHandlers.get(mime);
    if(handler == null){
      throw new PlatformImportException(messages.getString("PentahoPlatformImporter.ERROR_0002_MISSING_IMPORT_HANDLER")); //replace with default handler?
    }
    try{
      handler.importFile(file);
    } catch(Exception e){
      throw new PlatformImportException(messages.getString("PentahoPlatformImporter.ERROR_0003_UNEXPECTED_ERROR", e.getMessage()), e);
    }
  }

}
