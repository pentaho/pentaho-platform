/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.engine.services.metadata;

import static org.junit.Assert.*;

import org.junit.Test;
import static org.mockito.Mockito.*;
import org.pentaho.platform.api.engine.IPentahoSession;

public class MetadataPublisherTest {

  @Test
  public void test() {
    MetadataPublisher publisher = new MetadataPublisher();
    assertNotNull( publisher.getLogger() );
    assertNotNull( publisher.getName() );
    assertNotNull( publisher.getDescription() );
    IPentahoSession session = mock( IPentahoSession.class );
    assertNotEquals( "", publisher.publish( session ) );
  }
}
