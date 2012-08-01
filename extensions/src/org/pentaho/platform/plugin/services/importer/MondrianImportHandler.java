package org.pentaho.platform.plugin.services.importer;

/**
 * Used by REST Services to handle mulit part form upload from Schema WorkBench 
 * 
 * @author tband
 * @date 6/27/12
 * 
 */
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import mondrian.xmla.DataSourcesConfig.DataSource;

import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogServiceException;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogServiceException.Reason;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianDataSource;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianSchema;
import org.xml.sax.SAXException;

public class MondrianImportHandler implements IPlatformImportHandler {

  private static final String PARAMETERS = "parameters";

  private static final String ENABLE_XMLA = "EnableXmla";

  private static final String DOMAIN_ID = "domain-id";

  private static final String DATA_SOURCE = "DataSource";

  IMondrianCatalogService mondrianRepositoryImporter;

  public MondrianImportHandler(final IMondrianCatalogService mondrianImporter) {
    if (mondrianImporter == null) {
      throw new IllegalArgumentException();
    }
    this.mondrianRepositoryImporter = mondrianImporter;
  }

  /**
   * ****************************************
   * Main entry point from the Spring Interface
   * @param IPlatformImportBundle
   * @throws IOException 
   * @throws DomainStorageException 
   * @throws DomainAlreadyExistsException 
   * @throws DomainIdNullException 
   * @throws PlatformImportException 
   * @throws SAXException 
   * @throws ParserConfigurationException 
   */

  public void importFile(IPlatformImportBundle bundle) throws PlatformImportException, DomainIdNullException,
      DomainAlreadyExistsException, DomainStorageException, IOException {
    boolean overwriteInRepossitory = bundle.overwriteInRepossitory();
    boolean xmla = "true".equalsIgnoreCase(findParameterPropertyValue(bundle, ENABLE_XMLA)) ? true : false;
    final String domainId = (String) bundle.getProperty(DOMAIN_ID);

    if (domainId == null) {
      throw new PlatformImportException("Bundle missing required domain-id property");
    }
    try {
      String ds = findParameterPropertyValue(bundle, DATA_SOURCE);
      MondrianCatalog catalog = this.createCatalogObject(domainId, ds, xmla);
      mondrianRepositoryImporter.addCatalog(bundle.getInputStream(), catalog, overwriteInRepossitory,
          PentahoSessionHolder.getSession());
    } catch (MondrianCatalogServiceException mse) {
      int statusCode = convertExceptionToStatus(mse);
      throw new PlatformImportException(mse.getMessage(), statusCode);
    } catch (Exception e) {
      throw new PlatformImportException(e.getMessage(), PlatformImportException.PUBLISH_GENERAL_ERROR);
    }
  }

  /**
   * helper method to find the value in the bundle from either the property or parameter list
   * @param bundle
   * @param key
   * @return
   */
  private String findParameterPropertyValue(IPlatformImportBundle bundle, String key) {
    String value = (String) bundle.getProperty(key);
    if (value == null) {
      mondrian.olap.Util.PropertyList propertyList = mondrian.olap.Util.parseConnectString((String) bundle
          .getProperty(PARAMETERS));
      value = propertyList.get(key);
    }
    return value;
  }

  /**
   * convert the catalog service exception to a platform exception and get the proper status code
   * @param mse
   * @return
   */
  private int convertExceptionToStatus(MondrianCatalogServiceException mse) {
    int statusCode = PlatformImportException.PUBLISH_TO_SERVER_FAILED;
    if (mse.getReason().equals(Reason.GENERAL)) {
      statusCode = PlatformImportException.PUBLISH_GENERAL_ERROR;
    } else {
      if (mse.getReason().equals(Reason.ACCESS_DENIED)) {
        statusCode = PlatformImportException.PUBLISH_TO_SERVER_FAILED;
      } else {
        if (mse.getReason().equals(Reason.ALREADY_EXISTS)) {
          statusCode = PlatformImportException.PUBLISH_SCHEMA_EXISTS_ERROR;
        } else {
          if (mse.getReason().equals(Reason.XMLA_SCHEMA_NAME_EXISTS)) {
            statusCode = PlatformImportException.PUBLISH_XMLA_CATALOG_EXISTS;
          }
        }
      }
    }
    return statusCode;
  }

  /**
   * Helper method to create a catalog object 
   * @param domainId
   * @param datasource
   * @return
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  protected MondrianCatalog createCatalogObject(String domainId, String datasource, boolean xmlaEnabled)
      throws ParserConfigurationException, SAXException, IOException {

    String catName = domainId;
    MondrianSchema schema = new MondrianSchema(catName, null);
    String dsProvider = xmlaEnabled ? DataSource.PROVIDER_TYPE_MDP : "None:";
    MondrianDataSource ds = new MondrianDataSource(catName, "", "", "Provider=mondrian;DataSource=" + datasource,
        "Provider=Mondrian", dsProvider, DataSource.AUTH_MODE_UNAUTHENTICATED, null); 
    MondrianCatalog catalog = new MondrianCatalog(catName, "Provider=mondrian;DataSource=" + datasource + ";",
        "mondrian:" +  RepositoryFile.SEPARATOR + catName, ds, schema);
    return catalog;
  }

}
