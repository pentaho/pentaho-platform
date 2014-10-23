/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.importer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.mimeType.MimeType;
import org.pentaho.platform.plugin.services.importexport.ExportFileNameEncoder;
import org.pentaho.platform.plugin.services.importexport.ImportSession;
import org.pentaho.platform.plugin.services.importexport.ImportSource.IRepositoryFileBundle;
import org.pentaho.platform.plugin.services.importexport.RepositoryFileBundle;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.plugin.services.importexport.exportManifest.Parameters;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMetadata;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMondrian;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.web.http.api.resources.JobScheduleRequest;
import org.pentaho.platform.web.http.api.resources.SchedulerResource;

import javax.ws.rs.core.Response;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SolutionImportHandler implements IPlatformImportHandler {

  private static final Log log = LogFactory.getLog( SolutionImportHandler.class );

  private static final String sep = ";";
  private Map<String, RepositoryFileImportBundle.Builder> cachedImports;
  private SolutionFileImportHelper solutionHelper;
  private List<MimeType> mimeTypes;

  public SolutionImportHandler( List<MimeType> mimeTypes ) {
    this.mimeTypes = mimeTypes;
    this.solutionHelper = new SolutionFileImportHelper();
  }

  public ImportSession getImportSession() {
    return ImportSession.getSession();
  }

  public void importFile( IPlatformImportBundle bundle ) throws PlatformImportException, DomainIdNullException,
      DomainAlreadyExistsException, DomainStorageException, IOException {

    RepositoryFileImportBundle importBundle = (RepositoryFileImportBundle) bundle;
    ZipInputStream zipImportStream = new ZipInputStream( bundle.getInputStream() );
    SolutionRepositoryImportSource importSource = new SolutionRepositoryImportSource( zipImportStream );
    LocaleFilesProcessor localeFilesProcessor = new LocaleFilesProcessor();

    // importSession.set(ImportSession.getSession());

    IPlatformImporter importer = PentahoSystem.get( IPlatformImporter.class );

    cachedImports = new HashMap<String, RepositoryFileImportBundle.Builder>();

    //Process Manifest Settings
    ExportManifest manifest = getImportSession().getManifest();
    String manifestVersion = null;
    if ( manifest != null ) {
      manifestVersion = manifest.getManifestInformation().getManifestVersion();
    }
    // Process Metadata
    if ( manifest != null ) {
      List<ExportManifestMetadata> metadataList = manifest.getMetadataList();
      for ( ExportManifestMetadata exportManifestMetadata : metadataList ) {

        String domainId = exportManifestMetadata.getDomainId();
        boolean overWriteInRepository = true;
        RepositoryFileImportBundle.Builder bundleBuilder =
            new RepositoryFileImportBundle.Builder().charSet( "UTF-8" ).hidden( false ).overwriteFile(
                overWriteInRepository ).mime( "text/xmi+xml" ).withParam( "domain-id", domainId );

        cachedImports.put( exportManifestMetadata.getFile(), bundleBuilder );

      }

      // Process Mondrian
      List<ExportManifestMondrian> mondrianList = manifest.getMondrianList();
      for ( ExportManifestMondrian exportManifestMondrian : mondrianList ) {

        String catName = exportManifestMondrian.getCatalogName();
        Parameters parametersMap = exportManifestMondrian.getParameters();
        StringBuilder parametersStr = new StringBuilder();
        for ( String s : parametersMap.keySet() ) {
          parametersStr.append( s ).append( "=" ).append( parametersMap.get( s ) ).append( sep );
        }

        RepositoryFileImportBundle.Builder bundleBuilder =
            new RepositoryFileImportBundle.Builder().charSet( "UTF_8" ).hidden( false ).name( catName ).overwriteFile(
                true ).mime( "application/vnd.pentaho.mondrian+xml" )
                .withParam( "parameters", parametersStr.toString() ).withParam( "domain-id", catName ); // TODO: this is
        // definitely
        // named wrong
        // at the very
        // least.
        // pass as param if not in parameters string
        String xmlaEnabled = "" + exportManifestMondrian.isXmlaEnabled();
        bundleBuilder.withParam( "EnableXmla", xmlaEnabled );

        cachedImports.put( exportManifestMondrian.getFile(), bundleBuilder );
      }
    }

    for ( IRepositoryFileBundle file : importSource.getFiles() ) {
      String fileName = file.getFile().getName();
      String actualFilePath = file.getPath();
      if ( manifestVersion != null ) {
        fileName = ExportFileNameEncoder.decodeZipFileName( fileName );
        actualFilePath = ExportFileNameEncoder.decodeZipFileName( actualFilePath );
      }
      String repositoryFilePath =
          RepositoryFilenameUtils.concat( PentahoPlatformImporter.computeBundlePath( actualFilePath ), fileName );

      if ( this.cachedImports.containsKey( repositoryFilePath ) ) {

        byte[] bytes = IOUtils.toByteArray( file.getInputStream() );
        RepositoryFileImportBundle.Builder builder = cachedImports.get( repositoryFilePath );
        builder.input( new ByteArrayInputStream( bytes ) );

        importer.importFile( build( builder ) );
        continue;
      }
      RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder();

      InputStream bundleInputStream = null;

      String decodedFilePath = file.getPath();
      RepositoryFile decodedFile = file.getFile();
      if ( manifestVersion != null ) {
        decodedFile = new RepositoryFile.Builder( decodedFile ).path( decodedFilePath ).name( fileName ).title( fileName ).build();
        decodedFilePath = ExportFileNameEncoder.decodeZipFileName( file.getPath() );
      }

      if ( file.getFile().isFolder() ) {
        bundleBuilder.mime( "text/directory" );
        bundleBuilder.file( decodedFile );
        fileName = repositoryFilePath;
        repositoryFilePath = importBundle.getPath();
      } else {
        byte[] bytes = IOUtils.toByteArray( file.getInputStream() );
        bundleInputStream = new ByteArrayInputStream( bytes );
        // If is locale file store it for later processing.
        if ( localeFilesProcessor.isLocaleFile( file, importBundle.getPath(), bytes ) ) {
          log.trace( "Skipping [" + repositoryFilePath + "], it is a locale property file" );
          continue;
        }
        bundleBuilder.input( bundleInputStream );
        bundleBuilder.mime( solutionHelper.getMime( fileName ) );

        String filePath =
            ( decodedFilePath.equals( "/" ) || decodedFilePath.equals( "\\" ) ) ? "" : decodedFilePath;
        repositoryFilePath = RepositoryFilenameUtils.concat( importBundle.getPath(), filePath );
      }

      bundleBuilder.name( fileName );
      bundleBuilder.path( repositoryFilePath );

      String sourcePath;
      if ( decodedFilePath.startsWith( "/" ) ) {
        sourcePath = RepositoryFilenameUtils.concat( decodedFilePath.substring( 1 ), fileName );
      } else {
        if ( file.getFile().isFolder() ) {
          sourcePath = fileName;
        } else {
          sourcePath = RepositoryFilenameUtils.concat( decodedFilePath, fileName );
        }
      }

      //This clause was added for processing ivb files so that it would not try process acls on folders that the user
      //may not have rights to such as /home or /public
      if ( manifest != null && manifest.getExportManifestEntity( sourcePath ) == null && file.getFile().isFolder() ) {
        continue;
      }

      getImportSession().setCurrentManifestKey( sourcePath );

      bundleBuilder.charSet( bundle.getCharset() );
      bundleBuilder.overwriteFile( bundle.overwriteInRepository() );
      bundleBuilder.hidden( isFileHidden( bundle, sourcePath ) );
      bundleBuilder.applyAclSettings( bundle.isApplyAclSettings() );
      bundleBuilder.retainOwnership( bundle.isRetainOwnership() );
      bundleBuilder.overwriteAclSettings( bundle.isOverwriteAclSettings() );
      bundleBuilder.acl( getImportSession().processAclForFile( sourcePath ) );
      IPlatformImportBundle platformImportBundle = build( bundleBuilder );
      importer.importFile( platformImportBundle );

      if ( bundleInputStream != null ) {
        bundleInputStream.close();
        bundleInputStream = null;
      }
    }
    if ( manifest != null ) {
      List<JobScheduleRequest> scheduleList = manifest.getScheduleList();
      if ( scheduleList != null ) {
        SchedulerResource schedulerResource = new SchedulerResource();
        for ( JobScheduleRequest jobScheduleRequest : scheduleList ) {
          try {
            Response response = createSchedulerJob( schedulerResource, jobScheduleRequest );
            if ( response.getStatus() == Response.Status.OK.getStatusCode() ) {
              if ( response.getEntity() != null ) {
                // get the schedule job id from the response and add it to the import session
                ImportSession.getSession().addImportedScheduleJobId( response.getEntity().toString() );
              }
            }
          } catch ( Exception e ) {
            throw new PlatformImportException( Messages.getInstance()
                .getString( "SolutionImportHandler.ERROR_0001_ERROR_CREATING_SCHEDULE", e.getMessage() ) );
          }
        }
      }

      // Add Pentaho Connections
      List<org.pentaho.database.model.DatabaseConnection> datasourceList = manifest.getDatasourceList();
      if ( datasourceList != null ) {
        IDatasourceMgmtService datasourceMgmtSvc = PentahoSystem.get( IDatasourceMgmtService.class );
        for ( org.pentaho.database.model.DatabaseConnection databaseConnection : datasourceList ) {
          try {
            IDatabaseConnection existingDBConnection =
                datasourceMgmtSvc.getDatasourceByName( databaseConnection.getName() );
            if ( existingDBConnection != null && existingDBConnection.getName() != null ) {
              databaseConnection.setId( existingDBConnection.getId() );
              datasourceMgmtSvc.updateDatasourceByName( databaseConnection.getName(), databaseConnection );
            } else {
              datasourceMgmtSvc.createDatasource( databaseConnection );
            }
          } catch ( Exception e ) {
            e.printStackTrace();
          }
        }
      }

    }
    // Process locale files.
    localeFilesProcessor.processLocaleFiles( importer );
  }

  /**
   * Determines if the file or folder should be hidden. If there is a manifest entry for the file, and we are not
   * ignoring the manifest, then set the hidden flag based on the manifest. Otherwise use the blacklist to determine if
   * it is hidden.
   *
   * @param bundle
   * @param filePath
   * @return true if file/folder should be hidden, false otherwise
   */
  private boolean isFileHidden( IPlatformImportBundle bundle, String filePath ) {
    Boolean result = getImportSession().isFileHidden( filePath );
    return ( result != null ) ? result : solutionHelper.isInHiddenList( filePath );
  }

  private boolean isSystemPath( final String bundlePath ) {
    final String[] split = StringUtils.split( bundlePath, RepositoryFile.SEPARATOR );
    return isSystemDir( split, 0 ) || isSystemDir( split, 1 );
  }

  private boolean isSystemDir( final String[] split, final int index ) {
    return ( split != null && index < split.length && ( StringUtils.equals( split[index], "system" ) || StringUtils
        .equals( split[index], "admin" ) ) );
  }

  class SolutionRepositoryImportSource {
    private ZipInputStream zipInputStream;
    private List<IRepositoryFileBundle> files;

    public SolutionRepositoryImportSource( final ZipInputStream zipInputStream ) {
      this.zipInputStream = zipInputStream;
      this.files = new ArrayList<IRepositoryFileBundle>();
      initialize();
    }

    protected void initialize() {
      try {
        ZipEntry entry = zipInputStream.getNextEntry();
        while ( entry != null ) {
          final String entryName = RepositoryFilenameUtils.separatorsToRepository( entry.getName() );
          File tempFile = null;
          boolean isDir = entry.isDirectory();
          if ( !isDir ) {
            if ( !solutionHelper.isInApprovedExtensionList( entryName ) ) {
              zipInputStream.closeEntry();
              entry = zipInputStream.getNextEntry();
              continue;
            }
            tempFile = File.createTempFile( "zip", null );
            tempFile.deleteOnExit();
            FileOutputStream fos = new FileOutputStream( tempFile );
            IOUtils.copy( zipInputStream, fos );
            fos.close();
          }
          File file = new File( entryName );
          RepositoryFile repoFile =
              new RepositoryFile.Builder( file.getName() ).folder( isDir ).hidden( false ).build();
          String parentDir =
              new File( entryName ).getParent() == null ? RepositoryFile.SEPARATOR : new File( entryName ).getParent()
                  + RepositoryFile.SEPARATOR;
          IRepositoryFileBundle repoFileBundle =
              new RepositoryFileBundle( repoFile, null, parentDir, tempFile, "UTF-8", null );

          if ( file.getName().equals( "exportManifest.xml" ) ) {
            initializeAclManifest( repoFileBundle );
          } else {
            files.add( repoFileBundle );
          }
          zipInputStream.closeEntry();
          entry = zipInputStream.getNextEntry();
        }
        zipInputStream.close();
      } catch ( IOException exception ) {
        final String errorMessage = Messages.getInstance().getErrorString( "", exception.getLocalizedMessage() );
        log.trace( errorMessage );
      }
    }

    private void initializeAclManifest( IRepositoryFileBundle file ) {
      try {
        byte[] bytes = IOUtils.toByteArray( file.getInputStream() );
        ByteArrayInputStream in = new ByteArrayInputStream( bytes );
        getImportSession().setManifest( ExportManifest.fromXml( in ) );
      } catch ( Exception e ) {
        log.trace( e );
      }
    }

    public List<IRepositoryFileBundle> getFiles() {
      return this.files;
    }
  }

  @Override
  public List<MimeType> getMimeTypes() {
    return mimeTypes;
  }

  // handlers that extend this class may override this method and perform operations
  // over the bundle prior to entering its designated importer.importFile()
  public IPlatformImportBundle build( RepositoryFileImportBundle.Builder builder ) {
    return builder != null ? builder.build() : null;
  }

  // handlers that extend this class may override this method and perform operations
  // over the job prior to its creation at scheduler.createJob()
  public Response createSchedulerJob( SchedulerResource scheduler, JobScheduleRequest jobRequest ) throws IOException {
    return scheduler != null ? scheduler.createJob( jobRequest ) : null;
  }
}
