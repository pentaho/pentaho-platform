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


package org.pentaho.platform.repository2.unified.jcr;

import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * A pluggable method for reading and writing {@link IRepositoryFileData} implementations.
 * 
 * @param <T>
 *          type which this transformer reads and writes
 * @author mlowery
 */
public interface ITransformer<T extends IRepositoryFileData> {

  /**
   * Returns {@code true} if this transformer can read data for files with the given content type and return the
   * data in the given form.
   * 
   * @param contentType
   *          content type to check
   * @param clazz
   *          class to check
   * @return {@code true} if this transformer can read data for files with the given content type and return the
   *         data in the given form
   */
  boolean canRead( final String contentType, final Class<? extends IRepositoryFileData> clazz );

  /**
   * Returns {@code true} if this transformer can write data of the form {@code clazz}.
   * 
   * @param clazz
   *          class to check
   * @return {@code true} if this transformer can write data of the form {@code clazz}
   */
  boolean canWrite( final Class<? extends IRepositoryFileData> clazz );

  /**
   * Returns the content type string for this transformer. This gets set on the file and allows the file's data to
   * be read regardless of the requested data class.
   * 
   * @return content type
   */
  String getContentType();

  /**
   * Transforms a JCR node subtree into an {@link IRepositoryFileData}.
   * 
   * @param session
   *          JCR session
   * @param pentahoJcrConstants
   *          constants
   * @param escapeHelper
   *          escape helper
   * @param fileNode
   *          node of type pho_nt:pentahoFile or pho_nt:pentahoLinkedFile
   * @return an {@link IRepositoryFileData} instance
   * @throws RepositoryException
   *           if anything goes wrong
   */
  T fromContentNode( final Session session, final PentahoJcrConstants pentahoJcrConstants, final Node fileNode )
    throws RepositoryException;

  /**
   * Creates a JCR node subtree representing the given {@code content}.
   * 
   * @param session
   *          JCR session
   * @param pentahoJcrConstants
   *          constants
   * @param escapeHelper
   *          escape helper
   * @param data
   *          data to create
   * @param fileNode
   *          node of type pho_nt:pentahoFile or pho_nt:pentahoLinkedFile
   * @return an {@link IRepositoryFileData} instance
   * @throws RepositoryException
   *           if anything goes wrong
   */
  void createContentNode( final Session session, final PentahoJcrConstants pentahoJcrConstants, final T data,
      final Node fileNode ) throws RepositoryException;

  /**
   * Updates a JCR node subtree representing the given {@code content}.
   * 
   * @param session
   *          JCR session
   * @param pentahoJcrConstants
   *          constants
   * @param escapeHelper
   *          escape helper
   * @param data
   *          data to update
   * @param fileNode
   *          node of type pho_nt:pentahoFile or pho_nt:pentahoLinkedFile
   * @return an {@link IRepositoryFileData} instance
   * @throws RepositoryException
   *           if anything goes wrong
   */
  void updateContentNode( final Session session, final PentahoJcrConstants pentahoJcrConstants, final T data,
      final Node fileNode ) throws RepositoryException;

}
