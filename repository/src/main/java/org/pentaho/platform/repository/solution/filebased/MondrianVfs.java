/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.repository.solution.filebased;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileProvider;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.MondrianSchemaAnnotator;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.Collection;

/**
 * @author Ezequiel Cuellar
 */
public class MondrianVfs extends AbstractFileProvider {

  public static final String SCHEMA_XML = "schema.xml";
  public static final String ANNOTATIONS_XML = "annotations.xml";
  public static final String ANNOTATOR_KEY = "inlineModeling";

  @Override
  public FileObject createFileSystem( String arg0, FileObject arg1,
                                      FileSystemOptions arg2 ) throws FileSystemException {
    // TODO Auto-generated method stub
    return null;
  }

  public FileObject findFile( FileObject arg0, String catalog, FileSystemOptions arg2 ) throws FileSystemException {
    // Resolves mondrian:/<catalog> to /etc/mondrian/<catalog>/schema.xml
    catalog = catalog.substring( catalog.indexOf( ":" ) + 1 ); // removes mondrian:
    FileObject schemaFile = getCatalogFileObject( catalog, SCHEMA_XML );
    FileObject annotationsFile = getCatalogFileObject( catalog, ANNOTATIONS_XML );
    MondrianSchemaAnnotator annotator = getAnnotator();
    if ( annotationsFile.exists() && annotator != null ) {
      return new MondrianFileObject( schemaFile, annotationsFile, annotator );
    }
    return schemaFile;
  }

  MondrianSchemaAnnotator getAnnotator() {
    return PentahoSystem.get( MondrianSchemaAnnotator.class, ANNOTATOR_KEY, PentahoSessionHolder.getSession() );
  }

  FileObject getCatalogFileObject( final String catalog, final String fileName ) {
    return new SolutionRepositoryVfsFileObject( RepositoryFile.SEPARATOR + "etc" + RepositoryFile.SEPARATOR
      + "mondrian" + catalog + RepositoryFile.SEPARATOR + fileName );
  }

  @Override
  public Collection getCapabilities() {
    return null;
  }

  @Override
  public FileSystemConfigBuilder getConfigBuilder() {
    return null;
  }

  @Override
  public FileName parseUri( FileName arg0, String arg1 ) throws FileSystemException {
    return null;
  }
}
