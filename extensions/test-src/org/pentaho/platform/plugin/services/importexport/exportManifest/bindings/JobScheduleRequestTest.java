/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
