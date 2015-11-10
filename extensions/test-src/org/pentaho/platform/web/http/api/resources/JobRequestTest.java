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

package org.pentaho.platform.web.http.api.resources;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by rfellows on 11/10/15.
 */
public class JobRequestTest {

  @Test
  public void testConstructor() throws Exception {
    JobRequest jr = new JobRequest();
    jr.setJobId( "jobId" );
    assertEquals( "jobId", jr.getJobId() );
  }
}
