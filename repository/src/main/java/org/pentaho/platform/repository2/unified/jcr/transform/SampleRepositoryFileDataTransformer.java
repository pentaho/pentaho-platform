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
