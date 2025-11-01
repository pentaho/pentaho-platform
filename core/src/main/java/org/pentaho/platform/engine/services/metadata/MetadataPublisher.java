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


package org.pentaho.platform.engine.services.metadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.BasePublisher;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;

import java.util.ArrayList;
import java.util.List;

public class MetadataPublisher extends BasePublisher {

  private static final long serialVersionUID = 1843038346011563927L;

  private static final Log logger = LogFactory.getLog( MetadataPublisher.class );

  public static final int NO_ERROR = 0;

  public static final int UNABLE_TO_DELETE = (int) Math.pow( 2, 0 );

  public static final int UNABLE_TO_IMPORT = (int) Math.pow( 2, 1 );

  public static final int NO_META = (int) Math.pow( 2, 2 );

  public static String XMI_FILENAME = "metadata.xmi"; //$NON-NLS-1$

  private static int numberUpdated = 0;

  @Override
  public Log getLogger() {
    return MetadataPublisher.logger;
  }

  public String getName() {
    return Messages.getInstance().getString( "MetadataPublisher.USER_PUBLISHER_NAME" ); //$NON-NLS-1$
  }

  public String getDescription() {
    return Messages.getInstance().getString( "MetadataPublisher.USER_PUBLISHER_DESCRIPTION" ); //$NON-NLS-1$
  }

  @Override
  public String publish( final IPentahoSession session ) {
    MetadataPublisher.numberUpdated = 0;
    List<String> messages = new ArrayList<String>();
    // refresh new metadata domains
    try {
      IMetadataDomainRepository repo = PentahoSystem.get( IMetadataDomainRepository.class, session );
      repo.reloadDomains();
      MetadataPublisher.numberUpdated = repo.getDomainIds().size();
      return Messages.getInstance().getString(
          "MetadataPublisher.USER_METADATA_RELOADED", Integer.toString( MetadataPublisher.numberUpdated ) ); //$NON-NLS-1$
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getErrorString( "MetadataPublisher.ERROR_0001_USER_IMPORT_META_FAILED" ), e ); //$NON-NLS-1$
      messages.add( Messages.getInstance().getString( "MetadataPublisher.ERROR_0001_USER_IMPORT_META_FAILED" ) ); //$NON-NLS-1$
    }

    StringBuffer buffer = new StringBuffer();
    buffer.append( "<small>" ); //$NON-NLS-1$
    for ( String str : messages ) {
      buffer.append( "<br/>" + str ); //$NON-NLS-1$
    }
    buffer
        .append( "<br/><b>" + Messages.getInstance().getString( "MetadataPublisher.INFO_0001_CHECK_LOG" ) + "</b></small>" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    return buffer.toString();
  }
}
