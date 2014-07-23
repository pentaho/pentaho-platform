package org.pentaho.platform.web.http.api.resources.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.web.http.messages.Messages;

public class FileResourceService {

  private static final Log logger = LogFactory.getLog( FileResourceService.class );

  protected static DefaultUnifiedRepositoryWebService repoWs;

  public void doDeleteFiles( String params ) throws Exception {
    String[] sourceFileIds = params.split( "[,]" );
    try {
      for ( int i = 0; i < sourceFileIds.length; i++ ) {
        getRepoWs().deleteFile( sourceFileIds[i], null );
      }
    } catch ( Exception t ) {
      t.printStackTrace();
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), t );
      throw t;
    }
  }

  public static DefaultUnifiedRepositoryWebService getRepoWs() {
    if ( repoWs == null ) {
      repoWs = new DefaultUnifiedRepositoryWebService();
    }
    return repoWs;
  }

}
