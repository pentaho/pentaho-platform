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


package org.pentaho.platform.plugin.action.jfreereport.helper;

import org.pentaho.actionsequence.dom.IActionResource;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.reporting.libraries.resourceloader.ResourceData;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceLoadingException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.reporting.libraries.resourceloader.loader.AbstractResourceData;

import java.io.InputStream;

/**
 * This class is implemented to support loading solution files from the pentaho repository into JFreeReport
 * 
 * @author Will Gorman
 */
public class PentahoResourceData extends AbstractResourceData {

  private static final long serialVersionUID = 1806026106310340013L;

  private String filename;

  private ResourceKey key;

  /**
   * constructor which takes a resource key for data loading specifics
   * 
   * @param key
   *          resource key
   */
  public PentahoResourceData( final ResourceKey key ) throws ResourceLoadingException {
    if ( key == null ) {
      throw new NullPointerException();
    }

    this.key = key;
    this.filename = (String) key.getIdentifier();
  }

  /**
   * gets a resource stream from the runtime context.
   * 
   * @param caller
   *          resource manager
   * @return input stream
   */
  public InputStream getResourceAsStream( final ResourceManager caller ) throws ResourceLoadingException {
    int resourceType = IActionResource.SOLUTION_FILE_RESOURCE;
    if ( filename.contains( "://" ) ) {
      resourceType = IActionResource.URL_RESOURCE;
    }
    final IActionSequenceResource resource =
        new ActionSequenceResource( "", resourceType, "application/binary", (String) key.getIdentifier() ); //$NON-NLS-1$ //$NON-NLS-2$
    return resource.getInputStream( RepositoryFilePermission.READ, LocaleHelper.getLocale() );
  }

  /**
   * returns a requested attribute, currently only supporting filename.
   * 
   * @param key
   *          attribute requested
   * @return attribute value
   */
  public Object getAttribute( final String lookupKey ) {
    if ( lookupKey.equals( ResourceData.FILENAME ) ) {
      return filename;
    }
    return null;
  }

  /**
   * return the version number. We don't have access to file dates or versions so return 0
   * 
   * @param caller
   *          resource manager
   * 
   * @return version
   */
  public long getVersion( final ResourceManager caller ) throws ResourceLoadingException {
    final ActionSequenceResource resource =
        new ActionSequenceResource(
            "", IActionResource.SOLUTION_FILE_RESOURCE, "application/binary", (String) key.getIdentifier() ); //$NON-NLS-1$ //$NON-NLS-2$
    return resource.getLastModifiedDate( null );
  }

  /**
   * get the resource key
   * 
   * @return resource key
   */
  public ResourceKey getKey() {
    return key;
  }
}
