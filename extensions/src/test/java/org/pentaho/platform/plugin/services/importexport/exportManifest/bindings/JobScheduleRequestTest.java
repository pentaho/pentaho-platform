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


package org.pentaho.platform.plugin.services.importexport.exportManifest.bindings;

import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by rfellows on 10/26/15.
 */
public class JobScheduleRequestTest {

  @Test
  public void testGettersAndSetters() throws Exception {
    String[] excludes = new String[] {
      "jobParameters"
    };
    assertThat( JobScheduleRequest.class, hasValidGettersAndSettersExcluding( excludes ) );
  }

  @Test
  public void testGetJobParameters() throws Exception {
    JobScheduleRequest jsr = new JobScheduleRequest();
    assertNotNull( jsr.getJobParameters() );
    assertEquals( 0, jsr.getJobParameters().size() );
  }
}
