/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
    Assert.notEmpty( response.getDocs() );
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
