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

/**
 * Used by REST Services to handle mulit part form upload from Schema WorkBench 
 *
 * @author tband
 * @date 6/27/12
 *
 */

import mondrian.util.Pair;

import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogServiceException;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogServiceException.Reason;
import org.pentaho.platform.plugin.services.importer.mimeType.MimeType;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MondrianImportHandler implements IPlatformImportHandler {

  private static final String PARAMETERS = "parameters";

  private static final String ENABLE_XMLA = "EnableXmla";

  private static final String DOMAIN_ID = "domain-id";

  private static final String DATA_SOURCE = "DataSource";
  private static final String PROVIDER = "Provider";

  private List<MimeType> mimeTypes;
  IMondrianCatalogService mondrianRepositoryImporter;

  public MondrianImportHandler( List<MimeType> mimeTypes, final IMondrianCatalogService mondrianImporter ) {
    if ( mondrianImporter == null ) {
      throw new IllegalArgumentException();
    }
    this.mimeTypes = mimeTypes;
    this.mondrianRepositoryImporter = mondrianImporter;
  }

  /**
   * **************************************** Main entry point from the Spring Interface
   *
   * @param IPlatformImportBundle
   * @throws IOException
   * @throws DomainStorageException
   * @throws DomainAlreadyExistsException
   * @throws DomainIdNullException
   * @throws PlatformImportException
   * @throws SAXException
   * @throws ParserConfigurationException
   */

  public void importFile( IPlatformImportBundle bundle ) throws PlatformImportException, DomainIdNullException,
      DomainAlreadyExistsException, DomainStorageException, IOException {
    boolean overwriteInRepossitory = bundle.overwriteInRepository();
    boolean xmla = "false".equalsIgnoreCase( findParameterPropertyValue( bundle, ENABLE_XMLA ) ) ? false : true;
    final String domainId = (String) bundle.getProperty( DOMAIN_ID );

    if ( domainId == null ) {
      throw new PlatformImportException( "Bundle missing required domain-id property" );
    }
    try {
      MondrianCatalog catalog = this.createCatalogObject( domainId, xmla, bundle );
      mondrianRepositoryImporter.addCatalog( bundle.getInputStream(), catalog, overwriteInRepossitory,
          PentahoSessionHolder.getSession() );
    } catch ( MondrianCatalogServiceException mse ) {
      int statusCode = convertExceptionToStatus( mse );
      throw new PlatformImportException( mse.getMessage(), statusCode );
    } catch ( Exception e ) {
      throw new PlatformImportException( e.getMessage(), PlatformImportException.PUBLISH_GENERAL_ERROR );
    }
  }

  /**
   * helper method to find the value in the bundle from either the property or parameter list
   *
   * @param bundle
   * @param key
   * @return
   */
  private String findParameterPropertyValue( IPlatformImportBundle bundle, String key ) {
    String value = (String) bundle.getProperty( key );
    if ( value == null ) {
      mondrian.olap.Util.PropertyList propertyList =
          mondrian.olap.Util.parseConnectString( (String) bundle.getProperty( PARAMETERS ) );
      value = propertyList.get( key );
    }
    return value;
  }

  private Map<String, String> findParameters( IPlatformImportBundle bundle ) {
    mondrian.olap.Util.PropertyList propertyList =
        mondrian.olap.Util.parseConnectString( (String) bundle.getProperty( PARAMETERS ) );
    final Map<String, String> parameters = new HashMap<String, String>();
    for ( Pair<String, String> prop : propertyList ) {
      parameters.put( prop.left, prop.right );
    }
    return parameters;
  }

  /**
   * convert the catalog service exception to a platform exception and get the proper status code
   *
   * @param mse
   * @return
   */
  private int convertExceptionToStatus( MondrianCatalogServiceException mse ) {
    int statusCode = PlatformImportException.PUBLISH_TO_SERVER_FAILED;
    if ( mse.getReason().equals( Reason.GENERAL ) ) {
      statusCode = PlatformImportException.PUBLISH_GENERAL_ERROR;
    } else {
      if ( mse.getReason().equals( Reason.ACCESS_DENIED ) ) {
        statusCode = PlatformImportException.PUBLISH_TO_SERVER_FAILED;
      } else {
        if ( mse.getReason().equals( Reason.ALREADY_EXISTS ) ) {
          statusCode = PlatformImportException.PUBLISH_SCHEMA_EXISTS_ERROR;
        } else {
          if ( mse.getReason().equals( Reason.XMLA_SCHEMA_NAME_EXISTS ) ) {
            statusCode = PlatformImportException.PUBLISH_XMLA_CATALOG_EXISTS;
          }
        }
      }
    }
    return statusCode;
  }

  /**
   * Helper method to create a catalog object
   */
  protected MondrianCatalog createCatalogObject( String catName, boolean xmlaEnabled, IPlatformImportBundle bundle )
    throws ParserConfigurationException, SAXException, IOException, PlatformImportException {
    final Map<String, String> parameters = findParameters( bundle );
    final String dsName = findParameterPropertyValue( bundle, DATA_SOURCE );

    final String provider;
    if ( parameters.containsKey( PROVIDER ) ) {
      provider = findParameterPropertyValue( bundle, PROVIDER );
    } else {
      // Defaults to 'mondrian'
      provider = "mondrian";
    }

    StringBuilder sb = new StringBuilder();

    if ( dsName != null ) {
      sb.append( "DataSource=" + dsName + ";" );
    }
    if ( !parameters.containsKey( "EnableXmla" ) ) {
      sb.append( "EnableXmla=" + xmlaEnabled + ";" );
    }
    sb.append( "Provider=" + provider );

    // Build a list of the remaining properties
    for ( Entry<String, String> parameter : parameters.entrySet() ) {
      if ( !parameter.getKey().equals( DATA_SOURCE ) && !parameter.getKey().equals( PROVIDER ) ) {
        sb.append( ";" );
        sb.append( parameter.getKey() );
        sb.append( "=\"" );
        sb.append( parameter.getValue() );
        sb.append( "\"" );
      }
    }

    MondrianCatalog catalog =
        new MondrianCatalog( catName, sb.toString(), provider + ":" + RepositoryFile.SEPARATOR + catName, null, null );

    return catalog;
  }

  @Override
  public List<MimeType> getMimeTypes() {
    return mimeTypes;
  }
}
