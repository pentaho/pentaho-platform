/*
 * Copyright 2017 Pentaho Corporation.  All rights reserved.
 *
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.platform.web.http.api.resources;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.util.Assert;

import javax.ws.rs.core.Response;



public class SystemResourceTest {
  SystemResource systemResource;

  @Before
  public void setup() {
    PentahoSystem.init();
    PentahoSessionHolder.setSession( null );
    systemResource = new SystemResource();
  }

  @After
  public void teardown() {
    systemResource = null;
    PentahoSystem.shutdown();
  }

  @Test
  public void testSetLocaleOverride() {
    Response resp = null;
    try {
      resp = systemResource.setLocaleOverride( "en_US" );
    } catch ( Exception e ) {

    }
    Assert.notNull( resp );
  }
}
