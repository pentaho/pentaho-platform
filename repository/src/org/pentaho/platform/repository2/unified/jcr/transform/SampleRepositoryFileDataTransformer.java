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

package org.pentaho.platform.repository2.unified.jcr.transform;

import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.sample.SampleRepositoryFileData;
import org.pentaho.platform.repository2.unified.jcr.ITransformer;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

public class SampleRepositoryFileDataTransformer implements ITransformer<SampleRepositoryFileData> {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final String PROPERTY_NAME_SAMPLE_STRING = "sampleString"; //$NON-NLS-1$

  private static final String PROPERTY_NAME_SAMPLE_BOOLEAN = "sampleBoolean"; //$NON-NLS-1$

  private static final String PROPERTY_NAME_SAMPLE_INTEGER = "sampleInteger"; //$NON-NLS-1$

  // ~ Instance fields
  // =================================================================================================

  // ~ Constructors
  // ====================================================================================================

  public SampleRepositoryFileDataTransformer() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  /**
   * {@inheritDoc}
   */
  public boolean canRead( final String contentType, final Class<? extends IRepositoryFileData> clazz ) {
    return IRepositoryFileData.SAMPLE_CONTENT_TYPE.equals( contentType )
        && clazz.isAssignableFrom( SampleRepositoryFileData.class );
  }

  /**
   * {@inheritDoc}
   */
  public boolean canWrite( final Class<? extends IRepositoryFileData> clazz ) {
    return SampleRepositoryFileData.class.equals( clazz );
  }

  /**
   * {@inheritDoc}
   */
  public String getContentType() {
    return IRepositoryFileData.SAMPLE_CONTENT_TYPE;
  }

  /**
   * {@inheritDoc}
   */
  public void createContentNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final SampleRepositoryFileData data, final Node fileNode ) throws RepositoryException {
    Node unstructuredNode =
        fileNode.addNode( pentahoJcrConstants.getJCR_CONTENT(), pentahoJcrConstants.getNT_UNSTRUCTURED() );
    unstructuredNode.setProperty( PROPERTY_NAME_SAMPLE_STRING, data.getSampleString() );
    unstructuredNode.setProperty( PROPERTY_NAME_SAMPLE_BOOLEAN, data.getSampleBoolean() );
    unstructuredNode.setProperty( PROPERTY_NAME_SAMPLE_INTEGER, data.getSampleInteger() );
  }

  /**
   * {@inheritDoc}
   */
  public SampleRepositoryFileData fromContentNode( final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Node fileNode ) throws RepositoryException {
    Node unstructuredNode = fileNode.getNode( pentahoJcrConstants.getJCR_CONTENT() );
    String sampleString = unstructuredNode.getProperty( PROPERTY_NAME_SAMPLE_STRING ).getString();
    boolean sampleBoolean = unstructuredNode.getProperty( PROPERTY_NAME_SAMPLE_BOOLEAN ).getBoolean();
    int sampleInteger = (int) unstructuredNode.getProperty( PROPERTY_NAME_SAMPLE_INTEGER ).getLong();
    return new SampleRepositoryFileData( sampleString, sampleBoolean, sampleInteger );
  }

  /**
   * {@inheritDoc}
   */
  public void updateContentNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final SampleRepositoryFileData data, final Node fileNode ) throws RepositoryException {
    Node unstructuredNode = fileNode.getNode( pentahoJcrConstants.getJCR_CONTENT() );
    unstructuredNode.setProperty( PROPERTY_NAME_SAMPLE_STRING, data.getSampleString() );
    unstructuredNode.setProperty( PROPERTY_NAME_SAMPLE_BOOLEAN, data.getSampleBoolean() );
    unstructuredNode.setProperty( PROPERTY_NAME_SAMPLE_INTEGER, data.getSampleInteger() );
  }

}
