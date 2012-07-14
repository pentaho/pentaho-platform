package org.pentaho.platform.plugin.services.importer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.repository.messages.Messages;

import java.io.IOException;
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

  /**
   * this is the main method that uses the mime time (from Spring) to determine which handler to invoke.
   */
  public void importFile(IPlatformImportBundle file) throws PlatformImportException {
    String mime = mimeResolver.resolveMimeForBundle(file);
    if(mime == null){
      throw new PlatformImportException(messages.getString("PentahoPlatformImporter.ERROR_0001_INVALID_MIME_TYPE"),PlatformImportException.PUBLISH_GENERAL_ERROR);
    }
    IPlatformImportHandler handler = (importHandlers.containsKey(mime) == false) ? defaultHandler : importHandlers.get(mime);
    if(handler == null){
      throw new PlatformImportException(messages.getString("PentahoPlatformImporter.ERROR_0002_MISSING_IMPORT_HANDLER"),PlatformImportException.PUBLISH_GENERAL_ERROR); //replace with default handler?
    }
    try {
      handler.importFile(file);
    } catch (DomainIdNullException e1) {
      throw new PlatformImportException(messages.getString("PentahoPlatformImporter.ERROR_0004_PUBLISH_TO_SERVER_FAILED"),PlatformImportException.PUBLISH_TO_SERVER_FAILED);
    } catch (DomainAlreadyExistsException e1) {
      throw new PlatformImportException(messages.getString("PentahoPlatformImporter.ERROR_0007_PUBLISH_SCHEMA_EXISTS_ERROR"),PlatformImportException.PUBLISH_SCHEMA_EXISTS_ERROR);
    } catch (DomainStorageException e1) {
      throw new PlatformImportException(messages.getString("PentahoPlatformImporter.ERROR_0004_PUBLISH_TO_SERVER_FAILED"),PlatformImportException.PUBLISH_DATASOURCE_ERROR);
    } catch (IOException e1) {
      throw new PlatformImportException(messages.getString("PentahoPlatformImporter.ERROR_0005_PUBLISH_GENERAL_ERRORR"),PlatformImportException.PUBLISH_GENERAL_ERROR);
    }catch (Exception e1) {
      throw new PlatformImportException(messages.getString("PentahoPlatformImporter.ERROR_0005_PUBLISH_GENERAL_ERRORR"),PlatformImportException.PUBLISH_GENERAL_ERROR);
    }
  }

  private String extractExtension(String name) {
    if( name == null ) {
      return null;
    }
    int idx = name.lastIndexOf(".");
    if(idx == -1){
      return name;
    }
    return name.substring(idx+1);
  }


}
