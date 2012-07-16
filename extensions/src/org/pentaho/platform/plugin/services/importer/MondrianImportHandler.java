package org.pentaho.platform.plugin.services.importer;

/**
 * Used by REST Services to handle mulit part form upload from Schema WorkBench 
 * 
 * @author tband
 * @date 6/27/12
 * 
 */
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.repository.messages.Messages;

public class MondrianImportHandler implements IPlatformImportHandler {

  private static final String DOMAIN_ID = "domain-id";

  private static final String MONDRIAN_MIME_TYPE =  "application/vnd.pentaho.mondrian+xml";

  private static final String UTF_8 = "UTF-8";

  private static final Log logger = LogFactory.getLog(MondrianImportHandler.class);

  private static final Messages messages = Messages.getInstance();

  IMondrianCatalogService mondrianRepositoryImporter;

  public MondrianImportHandler(final IMondrianCatalogService mondrianImporter) {
    if (mondrianImporter == null) {
      throw new IllegalArgumentException();
    }
    this.mondrianRepositoryImporter = mondrianImporter;
  }



  /**
   * overloaded method from original - default to false (do not overwrite)
   * @throws IOException 
   * @throws DomainStorageException 
   * @throws DomainAlreadyExistsException 
   * @throws DomainIdNullException 
   * @throws PlatformImportException 
   */
 
  public void importFile(IPlatformImportBundle bundle) throws DomainIdNullException,
      DomainAlreadyExistsException, DomainStorageException, IOException, PlatformImportException {
    boolean overwriteInRepossitory = bundle.overwriteInRepossitory();
    boolean xmlaEnabled = ("True".equalsIgnoreCase((String)bundle.getProperty("xmlaEnabled")))?true:false;
    logger.debug("Importing as metadata - [domain=" + bundle.getName() + "]");
    logger.debug("importFile start " + bundle.getName() + " overwriteInRepossitory:" + overwriteInRepossitory);
    final String domainId = (String) bundle.getProperty("domain-id");

    if (domainId == null) {
      throw new DomainIdNullException("Bundle missing required domain-id property");
    }
     String datasource = (String) bundle.getProperty("datasource");
   
     mondrianRepositoryImporter.storeDomain(bundle.getInputStream(), domainId, datasource, overwriteInRepossitory, xmlaEnabled);
  }


}
