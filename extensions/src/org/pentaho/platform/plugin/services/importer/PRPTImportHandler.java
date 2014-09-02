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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.mimeType.MimeType;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.libraries.base.util.StringUtils;
import org.pentaho.reporting.libraries.docbundle.DocumentMetaData;
import org.pentaho.reporting.libraries.docbundle.ODFMetaAttributeNames;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

/**
 * This is a special handler that will extract the title and description from the meta.xml - uses the parent class s to
 * do the rest of the lifting. (changes to importexport.xml application/prpt) to use this class
 *
 * @author tband Apr 2013 [BIServer 5499]
 */
public class PRPTImportHandler extends RepositoryFileImportFileHandler implements IPlatformImportHandler {

  public PRPTImportHandler( List<MimeType> mimeTypes ) {
    super( mimeTypes );
  }

  private static final Log log = LogFactory.getLog( PRPTImportHandler.class );
  private final String rootElement = "/office:document-meta/office:meta";

  @Override
  public void importFile( IPlatformImportBundle bundle ) throws PlatformImportException {
    RepositoryFileImportBundle importBundle = (RepositoryFileImportBundle) bundle;
    LocaleFilesProcessor localeFilesProcessor = new LocaleFilesProcessor();

    IPlatformImporter importer = PentahoSystem.get( IPlatformImporter.class );
    String fileName = importBundle.getName();

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
      boolean hidden = extractMetaData( localeFilesProcessor, bytes, filePath, fileName, importBundle.getFile() );
      importBundle.setHidden( hidden );
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
   * @return true if this report is hidden. The report is hidden if the visible attribute is set to 'false'
   * (with case sensitive check to filter out garbage).
   * @throws IOException
   */
  private boolean extractMetaData( LocaleFilesProcessor localeFilesProcessor,
                                   byte[] bytes, String filePath, String fileName,
                                   RepositoryFile rf )
    throws IOException, PlatformImportException {

    try {
      ResourceManager mgr = new ResourceManager();
      MasterReport report = (MasterReport) mgr.createDirectly( bytes, MasterReport.class ).getResource();
      DocumentMetaData metaData = report.getBundle().getMetaData();
      String description = (String) metaData.getBundleAttribute(ODFMetaAttributeNames.DublinCore.NAMESPACE, ODFMetaAttributeNames.DublinCore.DESCRIPTION );
      if ( StringUtils.isEmpty( description, true ) ) {
        // make sure that empty strings and strings with only whitespace are not used as description.
        description = null;
      }
      String title = (String) metaData.getBundleAttribute( ODFMetaAttributeNames.DublinCore.NAMESPACE, ODFMetaAttributeNames.DublinCore.TITLE );
      if ( StringUtils.isEmpty( title, true ) ) {
        // make sure that empty strings and strings with only whitespace are not used as description.
        title = null;
      }
      if ( title != null || description != null ) {
        localeFilesProcessor.createLocaleEntry( filePath, fileName, title, description, rf,
            new ByteArrayInputStream( "".getBytes() ) );
      }
      // we are conservative here. Only if the string matches 'true' with this spelling.
      return "false".equals( metaData.getBundleAttribute( ClassicEngineBoot.METADATA_NAMESPACE, "visible" ) );
    } catch ( ResourceException e ) {
      throw new PlatformImportException( "An unexpected error occurred while parsing a report definition", e );
    }
  }
}
