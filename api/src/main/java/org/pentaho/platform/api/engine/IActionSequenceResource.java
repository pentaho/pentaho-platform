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


package org.pentaho.platform.api.engine;

import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;

import java.io.InputStream;
import java.util.Locale;

/**
 * The ActionResource interface represents one resource in an ActionSequence. Resources are elements in a solution
 * that exist outside of the action sequence document, such as images, icons, additional definition documents, etc.
 */
public interface IActionSequenceResource {

  // TODO sbarkdull, may want to refactor this list of ints to enum when we move to java 5
  /**
   * The Resource is a solution file
   */
  public static final int SOLUTION_FILE_RESOURCE = 1;

  /**
   * The resource is a URL
   */
  public static final int URL_RESOURCE = 2;

  /**
   * The resource is an arbitrary file
   */
  public static final int FILE_RESOURCE = 3;

  /**
   * The resource type is unknown
   */
  public static final int UNKNOWN_RESOURCE = 4;

  /**
   * The resource type is an embedded string
   */
  public static final int STRING = 5;

  /**
   * The resource type is embedded xml
   */
  public static final int XML = 6;

  /**
   * Return the xml node name of the resource
   * 
   * @return name of the resource
   */
  public String getName();

  /**
   * Returns the mime type of the resource. Since resources are external, they can take on many different formats
   * ie., text/xml, image/jpg, etc.
   * 
   * @return the mime type of the resource
   */
  public String getMimeType();

  /**
   * Get the type of external resource that this ActionResource is derived from.
   * <p>
   * Valid source types are SOLUTION_FILE_RESOURCE, URL_RESOURCE, FILE_RESOURCE and UNKNOWN_RESOURCE
   * 
   * @return the resource source type
   */
  public int getSourceType();

  /**
   * Depending on the resource source type, returns the address to the resource as a path or a URL.
   * 
   * @return address of resource
   */
  // public String getLocation();
  public String getAddress();

  public InputStream getInputStream( RepositoryFilePermission actionOperation, Locale locale );

  public InputStream getInputStream( RepositoryFilePermission actionOperation );
}
