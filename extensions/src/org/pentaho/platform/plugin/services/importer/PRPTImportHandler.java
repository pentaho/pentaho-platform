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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.ImportSource.IRepositoryFileBundle;
import org.pentaho.platform.plugin.services.importexport.InitializationException;
import org.pentaho.platform.plugin.services.importexport.legacy.ZipSolutionRepositoryImportSource;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 * This is a special handler that will extract the title and description from the meta.xml - uses the parent class s to
 * do the rest of the lifting. (changes to importexport.xml application/prpt) to use this class
 * 
 * @author tband Apr 2013 [BIServer 5499]
 * 
 */
public class PRPTImportHandler extends RepositoryFileImportFileHandler implements IPlatformImportHandler {

  public PRPTImportHandler( List<String> approvedExtensionList, List<String> hiddenExtensionList ) {
    super( approvedExtensionList, hiddenExtensionList );
  }

  private static final Log log = LogFactory.getLog( PRPTImportHandler.class );
  private final String rootElement = "/office:document-meta/office:meta";

  @Override
  public void importFile( IPlatformImportBundle bundle ) throws PlatformImportException {
    RepositoryFileImportBundle importBundle = (RepositoryFileImportBundle) bundle;
    LocaleFilesProcessor localeFilesProcessor = new LocaleFilesProcessor();

    IPlatformImporter importer = PentahoSystem.get( IPlatformImporter.class );
    String fileName = importBundle.getName();

    String repositoryFilePath =
        RepositoryFilenameUtils.concat( PentahoPlatformImporter.computeBundlePath( importBundle.getPath() ), fileName );
    String filePath =
        ( importBundle.getPath().equals( "/" ) || importBundle.getPath().equals( "\\" ) ) ? "" : importBundle.getPath();

    // If is locale file store it for later processing.
    // need to extract this from meta.xml
    try {
      // copy the inputstream first
      byte[] bytes = IOUtils.toByteArray( bundle.getInputStream() );
      InputStream bundleInputStream = new ByteArrayInputStream( bytes );
      // Process locale file from meta.xml.
      importBundle.setInputStream( bundleInputStream );
      convertStreamToProperties( localeFilesProcessor, bytes, filePath, fileName );
      super.importFile( importBundle );
      localeFilesProcessor.processLocaleFiles( importer );
    } catch ( Exception ex ) {
      throw new PlatformImportException( ex.getMessage(), ex );
    }
  }

  /**
   * extract the contents of the file meta.xml and place in the locales process entry
   * 
   * @param localeFilesProcessor
   * @param bytes
   * @throws InitializationException
   * @throws IOException
   * @throws DocumentException
   */
  private void convertStreamToProperties( LocaleFilesProcessor localeFilesProcessor, byte[] bytes, String filePath,
      String fileName ) throws InitializationException, IOException, DocumentException {

    InputStream zipInput = new ByteArrayInputStream( bytes );
    ZipInputStream zipInputStream = new ZipInputStream( zipInput );
    ZipSolutionRepositoryImportSource zip = new ZipSolutionRepositoryImportSource( zipInputStream, "UTF-8" );
    for ( IRepositoryFileBundle fileBundle : zip.getFiles() ) {
      RepositoryFile rf = fileBundle.getFile();
      if ( rf.getName().equals( "meta.xml" ) ) {
        try {
          Document doc = XmlDom4JHelper.getDocFromStream( fileBundle.getInputStream() );

          String description = doc.selectSingleNode( rootElement + "/dc:description" ).getStringValue();
          String title = doc.selectSingleNode( rootElement + "/dc:title" ).getStringValue();
          localeFilesProcessor.createLocaleEntry( filePath, fileName, title, description, rf, new ByteArrayInputStream(
            "".getBytes() ) );
        } catch ( Exception ex ) {
          // not all meta.xml have xpath entries
        }
        break;
      }
    }
  }
}
