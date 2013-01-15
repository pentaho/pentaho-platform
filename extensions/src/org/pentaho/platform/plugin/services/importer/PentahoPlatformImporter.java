package org.pentaho.platform.plugin.services.importer;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.repository.messages.Messages;

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
    return this.defaultHandler;
  }

  public void setDefaultHandler(IPlatformImportHandler defaultHandler) {
    this.defaultHandler = defaultHandler;
  }
  
  /**
   * To be consumed mainly by platform plugins who want to treat importing artifacts different.
   * */
  public void addHandler(String key, IPlatformImportHandler handler) {
	  this.importHandlers.put(key, handler);
  }
  
  /**
   * this is the main method that uses the mime time (from Spring) to determine which handler to invoke.
   */
  public void importFile(IPlatformImportBundle file) throws PlatformImportException {
    String mime = file.getMimeType() != null ? file.getMimeType() : mimeResolver.resolveMimeForBundle(file);
    if(mime == null){
      log.trace(messages.getString("PentahoPlatformImporter.ERROR_0001_INVALID_MIME_TYPE") + file.getName());
      return;
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
    } catch (PlatformImportException pe){
      throw pe; // if already converted - just rethrow
    }catch (Exception e1) {
      throw new PlatformImportException(messages.getString("PentahoPlatformImporter.ERROR_0005_PUBLISH_GENERAL_ERRORR"),PlatformImportException.PUBLISH_GENERAL_ERROR);
    }
  }
}
