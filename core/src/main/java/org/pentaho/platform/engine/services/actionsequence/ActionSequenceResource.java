/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services.actionsequence;

import org.apache.commons.io.FilenameUtils;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.web.HttpUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

public class ActionSequenceResource implements org.pentaho.platform.api.engine.IActionSequenceResource {

  private String name;

  private String mimeType;

  private String address;

  private int sourceType;

  private static IUnifiedRepository repository;

  public ActionSequenceResource( final String name, final int sourceType,
                                 final String mimeType, final String address ) {

    this.name = name;
    this.mimeType = mimeType;
    this.sourceType = sourceType;
    this.address = address;
  }

  public ActionSequenceResource( final String name, final int sourceType, final String mimeType,
      final String solutionName, final String solutionPath, final String location ) {

    this( name, sourceType, mimeType, null );
  }

  private static IUnifiedRepository getRepository() {
    if ( repository == null ) {
      repository = PentahoSystem.get( IUnifiedRepository.class, null );
    }
    return repository;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.solution.IActionResource#getName()
   */
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.solution.IActionResource#getMimeType()
   */
  public String getMimeType() {
    return mimeType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.solution.IActionResource#getSourceType()
   */
  public int getSourceType() {
    return sourceType;
  }

  public String getAddress() {
    return address;
  }

  public static int getResourceType( final String sourceTypeName ) {
    if ( "solution-file".equals( sourceTypeName ) ) { //$NON-NLS-1$
      return IActionSequenceResource.SOLUTION_FILE_RESOURCE;
    } else if ( "file".equals( sourceTypeName ) ) { //$NON-NLS-1$
      return IActionSequenceResource.FILE_RESOURCE;
    } else if ( "url".equals( sourceTypeName ) ) { //$NON-NLS-1$
      return IActionSequenceResource.URL_RESOURCE;
    } else if ( "xml".equals( sourceTypeName ) ) { //$NON-NLS-1$
      return IActionSequenceResource.XML;
    } else if ( "string".equals( sourceTypeName ) ) { //$NON-NLS-1$
      return IActionSequenceResource.STRING;
    } else {
      return IActionSequenceResource.UNKNOWN_RESOURCE;
    }
  }

  public static long getLastModifiedDate( String filePath, Locale locale ) {
    if ( filePath.startsWith( "system" ) ) {
      File file = null;
      filePath = PentahoSystem.getApplicationContext().getSolutionPath( filePath );
      if ( locale == null ) {
        file = new File( filePath );
      } else {
        String extension = FilenameUtils.getExtension( filePath );
        String baseName = FilenameUtils.removeExtension( filePath );
        if ( extension.length() > 0 ) {
          extension = "." + extension; //$NON-NLS-1$
        }
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();
        if ( !variant.equals( "" ) ) { //$NON-NLS-1$
          file = new File( baseName + "_" + language + "_" + country + "_" + variant + extension ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        if ( ( file == null ) || !file.exists() ) {
          file = new File( baseName + "_" + language + "_" + country + extension ); //$NON-NLS-1$//$NON-NLS-2$
        }
        if ( ( file == null ) || !file.exists() ) {
          file = new File( baseName + "_" + language + extension ); //$NON-NLS-1$
        }
        if ( ( file == null ) || !file.exists() ) {
          file = new File( filePath );
        }
      }
      if ( file != null ) {
        return file.lastModified();
      }
    } else {
      RepositoryFile repositoryFile = null;
      if ( locale == null ) {
        repositoryFile = getRepository().getFile( filePath );
      } else {
        String extension = FilenameUtils.getExtension( filePath );
        String baseName = FilenameUtils.removeExtension( filePath );
        if ( extension.length() > 0 ) {
          extension = "." + extension; //$NON-NLS-1$
        }
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();
        if ( !variant.equals( "" ) ) { //$NON-NLS-1$
          repositoryFile =
              getRepository().getFile( baseName + "_" + language + "_" + country + "_" + variant + extension ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        if ( repositoryFile == null ) {
          repositoryFile = getRepository().getFile( baseName + "_" + language + "_" + country + extension ); //$NON-NLS-1$//$NON-NLS-2$
        }
        if ( repositoryFile == null ) {
          repositoryFile = getRepository().getFile( baseName + "_" + language + extension ); //$NON-NLS-1$
        }
        if ( repositoryFile == null ) {
          repositoryFile = getRepository().getFile( filePath );
        }
      }
      if ( repositoryFile != null ) {
        return repositoryFile.getLastModifiedDate().getTime();
      }
    }
    return -1L;
  }

  @SuppressWarnings( { "resource", "deprecation" } )
  public static InputStream getInputStream( String filePath, Locale locale ) {
    InputStream inputStream = null;
    
    if ( filePath.startsWith( "system" ) ) {
      File file = null;
      filePath = PentahoSystem.getApplicationContext().getSolutionPath( filePath );
      if ( locale == null ) {
        file = new File( filePath );
      } else {
        String extension = FilenameUtils.getExtension( filePath );
        String baseName = FilenameUtils.removeExtension( filePath );
        if ( extension.length() > 0 ) {
          extension = "." + extension; //$NON-NLS-1$
        }
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();
        if ( !variant.equals( "" ) ) { //$NON-NLS-1$
          file = new File( baseName + "_" + language + "_" + country + "_" + variant + extension ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        if ( ( file == null ) || !file.exists() ) {
          file = new File( baseName + "_" + language + "_" + country + extension ); //$NON-NLS-1$//$NON-NLS-2$
        }
        if ( ( file == null ) || !file.exists() ) {
          file = new File( baseName + "_" + language + extension ); //$NON-NLS-1$
        }
        if ( ( file == null ) || !file.exists() ) {
          file = new File( filePath );
        }
      }
      if ( file != null ) {
        try {
          inputStream = new FileInputStream( file );
        } catch ( FileNotFoundException ex ) {
          // Do nothing we'll just return a null input stream;
        }
      }
    } else {
      // This is not a file from the system folder. User is trying to access a resource in the repository.
      // Get the RepositoryContentConverterHandler
      IRepositoryContentConverterHandler converterHandler = PentahoSystem.get( IRepositoryContentConverterHandler.class);
      RepositoryFile repositoryFile = null;
      if ( locale == null ) {
        repositoryFile = getRepository().getFile( filePath );
        String extension = FilenameUtils.getExtension( filePath );
        try {
          // Try to get the converter for the extension. If there is not converter available then we will
          //assume simple type and will get the data that way
          if(converterHandler != null) {
            Converter converter = converterHandler.getConverter( extension );
            if(converter != null) {
              inputStream = converter.convert( repositoryFile.getId() );
            }
          }
          if(inputStream == null) {
            inputStream =
              getRepository().getDataForRead( repositoryFile.getId(), SimpleRepositoryFileData.class ).getStream();
          }
        } catch ( UnifiedRepositoryException ure ) {
          //ignored
        }
      } else {
        String extension = FilenameUtils.getExtension( filePath );
        String baseName = FilenameUtils.removeExtension( filePath );
        if ( extension.length() > 0 ) {
          extension = "." + extension; //$NON-NLS-1$
        }
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();
        if ( !variant.equals( "" ) ) { //$NON-NLS-1$
          repositoryFile =
              getRepository().getFile( baseName + "_" + language + "_" + country + "_" + variant + extension ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          try {
            if ( repositoryFile != null ) {
              // Try to get the converter for the extension. If there is not converter available then we will
              //assume simple type and will get the data that way
              if(converterHandler != null) {
                Converter converter = converterHandler.getConverter( FilenameUtils.getExtension( filePath ) );
                if(converter != null) {
                  inputStream = converter.convert( repositoryFile.getId() );
                }
              }
              if(inputStream == null) {
                inputStream =
                  getRepository().getDataForRead( repositoryFile.getId(), SimpleRepositoryFileData.class ).getStream();
              } 
            }
          } catch ( UnifiedRepositoryException ure ) {
            //ignored
          }
        }
        if ( inputStream == null ) {
          repositoryFile = getRepository().getFile( baseName + "_" + language + "_" + country + extension ); //$NON-NLS-1$//$NON-NLS-2$
          try {
            if ( repositoryFile != null ) {
              // Try to get the converter for the extension. If there is not converter available then we will
              //assume simple type and will get the data that way
              if(converterHandler != null) {
                Converter converter = converterHandler.getConverter( FilenameUtils.getExtension( filePath ) );
                if(converter != null) {
                  inputStream = converter.convert( repositoryFile.getId() );
                }
              }
              if(inputStream == null) {
                inputStream =
                  getRepository().getDataForRead( repositoryFile.getId(), SimpleRepositoryFileData.class ).getStream();
              }             }
          } catch ( UnifiedRepositoryException ure ) {
            //ignored
          }
        }
        if ( inputStream == null ) {
          repositoryFile = getRepository().getFile( baseName + "_" + language + extension ); //$NON-NLS-1$
          try {
            if ( repositoryFile != null ) {
              // Try to get the converter for the extension. If there is not converter available then we will
              //assume simple type and will get the data that way
              if(converterHandler != null) {
                Converter converter = converterHandler.getConverter( FilenameUtils.getExtension( filePath ) );
                if(converter != null) {
                  inputStream = converter.convert( repositoryFile.getId() );
                }
              }
              if(inputStream == null) {
                inputStream =
                  getRepository().getDataForRead( repositoryFile.getId(), SimpleRepositoryFileData.class ).getStream();
              }
            }
          } catch ( UnifiedRepositoryException ure ) {
            //ignored
          }
        }
        if ( inputStream == null ) {
          repositoryFile = getRepository().getFile( filePath );
          try {
            if ( repositoryFile != null ) {
              // Try to get the converter for the extension. If there is not converter available then we will
              //assume simple type and will get the data that way
              if(converterHandler != null) {
                Converter converter = converterHandler.getConverter( FilenameUtils.getExtension( filePath ) );
                if(converter != null) {
                  inputStream = converter.convert( repositoryFile.getId() );
                }
              }
              if(inputStream == null) {
                inputStream =
                  getRepository().getDataForRead( repositoryFile.getId(), SimpleRepositoryFileData.class ).getStream();
              } 
            }
          } catch ( UnifiedRepositoryException ure ) {
            //ignored
          }
        }
      }
    }
    return inputStream;
  }

  public long getLastModifiedDate( Locale locale ) {
    int resourceSource = getSourceType();
    if ( resourceSource == IActionSequenceResource.URL_RESOURCE ) {
      return -1L;
    } else if ( ( resourceSource == IActionSequenceResource.SOLUTION_FILE_RESOURCE )
        || ( resourceSource == IActionSequenceResource.FILE_RESOURCE ) ) {
      return getLastModifiedDate( getAddress(), locale );
    } else if ( ( resourceSource == IActionSequenceResource.STRING )
        || ( resourceSource == IActionSequenceResource.XML ) ) {
      return -1L;
    }
    return -1L;
  }

  public InputStream getInputStream( RepositoryFilePermission actionoperation, Locale locale ) {
    int resourceSource = getSourceType();
    InputStream inputStream = null;
    if ( resourceSource == IActionSequenceResource.URL_RESOURCE ) {
      inputStream = HttpUtil.getURLInputStream( getAddress() );
    } else if ( ( resourceSource == IActionSequenceResource.SOLUTION_FILE_RESOURCE )
        || ( resourceSource == IActionSequenceResource.FILE_RESOURCE ) ) {
      inputStream = getInputStream( getAddress(), locale );
    } else if ( ( resourceSource == IActionSequenceResource.STRING )
        || ( resourceSource == IActionSequenceResource.XML ) ) {
      String s = getAddress();
      if ( s == null ) {
        s = "";
      }
      inputStream = new ByteArrayInputStream( s.getBytes() );
    }
    return inputStream;
  }

  public InputStream getInputStream( RepositoryFilePermission actionOperation ) {
    return getInputStream( actionOperation, null );
  }

}
