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
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.pentaho.commons.util.repository.type.CmisObject;
import org.springframework.util.Assert;

public class GetCheckedoutDocsResponseTest {

  @Test
  public void testGetSetDocs() {
    GetCheckedoutDocsResponse response = new GetCheckedoutDocsResponse();
    List<CmisObject> docList = Arrays.asList( new CmisObject[] { mock( CmisObject.class ), mock( CmisObject.class ), mock( CmisObject.class ) } );
    response.setDocs( docList );
    Assert.notEmpty( response.getDocs(), "The document list must not be empty" );
  }

  @Test
  public void testIsHasMoreItems() {
    GetCheckedoutDocsResponse response = new GetCheckedoutDocsResponse();
    assertFalse( response.isHasMoreItems() );
  }

  @Test
  public void testSetHasMoreItems() {
    GetCheckedoutDocsResponse response = new GetCheckedoutDocsResponse();
    response.setHasMoreItems( true );
    assertEquals( true, response.isHasMoreItems() );
  }

}
