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
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.repository2.unified.jcr.ITransformer;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.springframework.util.StringUtils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.InputStream;
import java.util.Calendar;

/**
 * An {@link ITransformer} that can read and write {@code nt:resource} nodes.
 * 
 * @author mlowery
 */
public class SimpleRepositoryFileDataTransformer implements ITransformer<SimpleRepositoryFileData> {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  // ~ Constructors
  // ====================================================================================================

  public SimpleRepositoryFileDataTransformer() {
    super();
  }

  // ~ Methods
  // =========================================================================================================

  /**
   * {@inheritDoc}
   */
  public boolean canRead( final String contentType, final Class<? extends IRepositoryFileData> clazz ) {
    return IRepositoryFileData.SIMPLE_CONTENT_TYPE.equals( contentType )
        && clazz.isAssignableFrom( SimpleRepositoryFileData.class );
  }

  /**
   * {@inheritDoc}
   */
  public boolean canWrite( final Class<? extends IRepositoryFileData> clazz ) {
    return SimpleRepositoryFileData.class.equals( clazz );
  }

  /**
   * {@inheritDoc}
   */
  public String getContentType() {
    return IRepositoryFileData.SIMPLE_CONTENT_TYPE;
  }

  /**
   * {@inheritDoc}
   */
  public void createContentNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final SimpleRepositoryFileData data, final Node fileNode ) throws RepositoryException {

    Node resourceNode = fileNode.addNode( pentahoJcrConstants.getJCR_CONTENT(), pentahoJcrConstants.getNT_RESOURCE() );

    // mandatory property on nt:resource; give them a value to satisfy Jackrabbit
    resourceNode.setProperty( pentahoJcrConstants.getJCR_LASTMODIFIED(), fileNode.getProperty(
        pentahoJcrConstants.getJCR_CREATED() ).getDate() );

    if ( StringUtils.hasText( data.getEncoding() ) ) {
      resourceNode.setProperty( pentahoJcrConstants.getJCR_ENCODING(), data.getEncoding() );
    }
    resourceNode.setProperty( pentahoJcrConstants.getJCR_DATA(), session.getValueFactory().createBinary(
        data.getStream() ) );
    resourceNode.setProperty( pentahoJcrConstants.getJCR_MIMETYPE(), data.getMimeType() );
  }

  /**
   * {@inheritDoc}
   */
  public SimpleRepositoryFileData fromContentNode( final Session session,
      final PentahoJcrConstants pentahoJcrConstants, final Node fileNode ) throws RepositoryException {
    String encoding = null;
    Node resourceNode = fileNode.getNode( pentahoJcrConstants.getJCR_CONTENT() );
    if ( resourceNode.hasProperty( pentahoJcrConstants.getJCR_ENCODING() ) ) {
      encoding = resourceNode.getProperty( pentahoJcrConstants.getJCR_ENCODING() ).getString();
    }
    InputStream data = resourceNode.getProperty( pentahoJcrConstants.getJCR_DATA() ).getBinary().getStream();
    String mimeType = resourceNode.getProperty( pentahoJcrConstants.getJCR_MIMETYPE() ).getString();
    return new SimpleRepositoryFileData( data, encoding, mimeType );
  }

  /**
   * {@inheritDoc}
   */
  public void updateContentNode( final Session session, final PentahoJcrConstants pentahoJcrConstants,
      final SimpleRepositoryFileData data, final Node fileNode ) throws RepositoryException {
    Node resourceNode = fileNode.getNode( pentahoJcrConstants.getJCR_CONTENT() );

    // mandatory property on nt:resource; give them a value to satisfy Jackrabbit
    resourceNode.setProperty( pentahoJcrConstants.getJCR_LASTMODIFIED(), Calendar.getInstance() );

    if ( StringUtils.hasText( data.getEncoding() ) ) {
      resourceNode.setProperty( pentahoJcrConstants.getJCR_ENCODING(), data.getEncoding() );
    }
    resourceNode.setProperty( pentahoJcrConstants.getJCR_DATA(), session.getValueFactory().createBinary(
        data.getInputStream() ) );
    resourceNode.setProperty( pentahoJcrConstants.getJCR_MIMETYPE(), data.getMimeType() );
  }

}
