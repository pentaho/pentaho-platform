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


package org.pentaho.commons.util.repository;

import static org.junit.Assert.*;

import org.junit.Test;

public class CreateDocumentRequestTest {

  @Test
  public void test() {
    CreateDocumentRequest request = new CreateDocumentRequest();
    assertNotNull( request );
  }
}
