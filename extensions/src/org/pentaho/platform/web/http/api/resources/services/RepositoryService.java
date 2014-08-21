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

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.repository.RepositoryDownloadWhitelist;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.web.http.api.resources.ContentGeneratorDescriptor;
import org.pentaho.platform.web.http.api.resources.FileResource;
import org.pentaho.platform.web.http.api.resources.GeneratorStreamingOutput;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.StringTokenizer;

public class RepositoryService {

  public static final String GENERATED_CONTENT_PERSPECTIVE = "generatedContent"; //$NON-NLS-1$

  private static final Log logger = LogFactory.getLog( RepositoryService.class );

  protected IPluginManager pluginManager;

  protected IUnifiedRepository repository;

  protected RepositoryDownloadWhitelist whitelist;

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

  protected IPluginManager getPluginManager( Class<IPluginManager> clazz, IPentahoSession session ) {
    return PentahoSystem.get( clazz, session );
  }

  public GeneratorStreamingOutput getContentGeneratorStreamingOutput( CGFactoryInterface fac ) {
    rsc( "Is [{0}] a content generator ID?", fac.getContentGeneratorId() );
    final IContentGenerator contentGenerator;
    try {
      contentGenerator = fac.create();
    } catch ( NoSuchBeanDefinitionException e ) {
      rsc( "Nope, [{0}] is not a content generator ID.", fac.getContentGeneratorId() );
      return null;
    }
    if ( contentGenerator == null ) {
      rsc( "Nope, [{0}] is not a content generator ID.", fac.getContentGeneratorId() ); //$NON-NLS-1$
      return null;
    }
    rsc(
      "Yep, [{0}] is a content generator ID. Executing (where command path is {1})..", fac.getContentGeneratorId(),
      fac.getCommand() ); //$NON-NLS-1$
    return fac.getStreamingOutput( contentGenerator );
  }

  public String getUrl( RepositoryFile file, String resourceId ) {
    String ext = file.getName().substring( file.getName().indexOf( '.' ) + 1 );
    if ( !( ext.equals( "url" ) && resourceId.equals( "generatedContent" ) ) ) {
      return null;
    }

    String url = extractUrl( file );
    if ( !url.trim().startsWith( "http" ) ) {
      // if path is relative, prepend FQSURL
      url = getFullyQualifiedServerURL() + url;
    }
    return url;
  }

  public String getRepositoryFileResourcePath( FileResource fileResource, String filePath, String relPath ) {
    rsc( "Is [{0}] a relative path to a repository file, relative to [{1}]?", relPath, filePath );

    fileResource.setWhitelist( whitelist );

    return separatorsToRepository( filePath, relPath );
  }

  public String extractUrl( RepositoryFile file ) {
    SimpleRepositoryFileData data = null;

    data = getIUnifiedRepository().getDataForRead( file.getId(), SimpleRepositoryFileData.class );
    StringWriter writer = getStringWriter();
    try {
      copy( data.getInputStream(), writer );
    } catch ( IOException e ) {
      return "";
    }

    String props = writer.toString();
    StringTokenizer tokenizer = getStringTokenizer( props, "\n" );
    while ( tokenizer.hasMoreTokens() ) {
      String line = tokenizer.nextToken();
      int pos = line.indexOf( '=' );
      if ( pos > 0 ) {
        String propname = line.substring( 0, pos );
        String value = line.substring( pos + 1 );
        if ( ( value != null ) && ( value.length() > 0 ) && ( value.charAt( value.length() - 1 ) == '\r' ) ) {
          value = value.substring( 0, value.length() - 1 );
        }
        if ( "URL".equalsIgnoreCase( propname ) ) {
          return value;
        }

      }
    }
    // No URL found
    return "";
  }

  public void ctxt( String msg, Object... args ) {
    debug( "[RESOLVING CONTEXT ID] ==> " + msg, args ); //$NON-NLS-1$
  }

  public void rsc( String msg, Object... args ) {
    debug( "[RESOLVING RESOURCE ID] ==> " + msg, args ); //$NON-NLS-1$
  }

  public void debug( String msg, Object... args ) {
    logger.debug( MessageFormat.format( msg, args ) );
  }

  public interface CGFactoryInterface extends ContentGeneratorDescriptor {
    IContentGenerator create();

    GeneratorStreamingOutput getStreamingOutput( IContentGenerator cg );

    public String getContentGeneratorId();

    public String getCommand();
  }

  public RepositoryDownloadWhitelist getWhitelist() {
    return whitelist;
  }

  public void setWhitelist( RepositoryDownloadWhitelist whitelist ) {
    this.whitelist = whitelist;
  }

  protected String getFullyQualifiedServerURL() {
    return PentahoSystem.getApplicationContext().getFullyQualifiedServerURL();
  }

  protected String separatorsToRepository( String filePath, String relPath ) {
    return RepositoryFilenameUtils
      .separatorsToRepository( RepositoryFilenameUtils.concat( filePath, "../" + relPath ) );
  }

  protected void copy( InputStream in, StringWriter out ) throws IOException {
    IOUtils.copy( in, out );
  }

  protected IPluginManager getPluginManager() {
    if ( this.pluginManager == null ) {
      this.pluginManager = PentahoSystem.get( IPluginManager.class );
    }

    return this.pluginManager;
  }

  protected IUnifiedRepository getIUnifiedRepository() {
    if ( this.repository == null ) {
      this.repository = PentahoSystem.get( IUnifiedRepository.class );
    }

    return this.repository;
  }

  protected StringWriter getStringWriter() {
    return new StringWriter();
  }

  protected StringTokenizer getStringTokenizer( String str, String delim ) {
    return new StringTokenizer( str, delim );
  }
}
