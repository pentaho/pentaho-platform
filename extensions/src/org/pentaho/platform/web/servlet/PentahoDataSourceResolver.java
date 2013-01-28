package org.pentaho.platform.web.servlet;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.servlet.messages.Messages;

import mondrian.spi.DataSourceResolver;

/**
 * This class provides SPI functionality to Mondrian.
 * It resolves relational data sources by their name.
 * It uses the {@link PentahoSessionHolder}.
 * @author Luc Boudreau
 */
public class PentahoDataSourceResolver implements DataSourceResolver {
  Logger logger = Logger.getLogger(PentahoDataSourceResolver.class);
  public DataSource lookup(String dataSourceName) throws Exception {
    try {
      IDBDatasourceService datasourceSvc =
        PentahoSystem.getObjectFactory().get(
          IDBDatasourceService.class,
          PentahoSessionHolder.getSession());
      javax.sql.DataSource datasource =
        datasourceSvc.getDataSource(
          datasourceSvc.getDSUnboundName(dataSourceName));
      return datasource;
    } catch (ObjectFactoryException e) {
      logger.error(Messages.getInstance().getErrorString("PentahoXmlaServlet.ERROR_0002_UNABLE_TO_INSTANTIATE"), e); //$NON-NLS-1$
      throw e;
    } catch (DBDatasourceServiceException e) {
      logger.error(Messages.getInstance().getErrorString("PentahoXmlaServlet.ERROR_0002_UNABLE_TO_INSTANTIATE"), e); //$NON-NLS-1$
      throw e;
    }
  }
}
