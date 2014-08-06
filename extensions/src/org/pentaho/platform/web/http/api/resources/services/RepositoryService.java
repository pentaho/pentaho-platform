package org.pentaho.platform.web.http.api.resources.services;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginOperation;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.api.resources.utils.FileUtils;

public class RepositoryService {

  public static final String GENERATED_CONTENT_PERSPECTIVE = "generatedContent"; //$NON-NLS-1$
  
  public URI doExecuteDefault( String pathId, StringBuffer buffer, String queryString ) throws FileNotFoundException,
    MalformedURLException, URISyntaxException {
    String perspective = null;
    String url = null;

    String path = FileUtils.idToPath( pathId );
    String extension = path.substring( path.lastIndexOf( '.' ) + 1 );
    IPluginManager pluginManager = getPluginManager( IPluginManager.class, PentahoSessionHolder.getSession() );
    IContentInfo info = pluginManager.getContentTypeInfo( extension );
    for ( IPluginOperation operation : info.getOperations() ) {
      if ( operation.getId().equalsIgnoreCase( "RUN" ) ) { //$NON-NLS-1$
        perspective = operation.getPerspective();
        break;
      }
    }
    if ( perspective == null ) {
      perspective = GENERATED_CONTENT_PERSPECTIVE;
    }

    url = buffer.substring( 0, buffer.lastIndexOf( "/" ) + 1 ) + perspective + //$NON-NLS-1$
        ( ( queryString != null && queryString.length() > 0 ) ? "?" + queryString : "" );
    return new URL( url ).toURI();
  }

  protected IPluginManager getPluginManager( Class<IPluginManager> clazz, IPentahoSession session) {
    return PentahoSystem.get( clazz, session );
  }
}
