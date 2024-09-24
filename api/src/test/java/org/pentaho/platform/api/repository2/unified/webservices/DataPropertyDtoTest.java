/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.api.repository2.unified.webservices;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by efreitas on 27-07-2018.
 */
public class DataPropertyDtoTest {

  @Test
  public void testDto() {

    DataPropertyDto dto = new DataPropertyDto();
    String mockValue = "mockID";
    dto.setValue( mockValue );
    assertEquals( dto.getValue(), mockValue );
    String mockName = "mockName";
    dto.setName( mockName );
    assertEquals( dto.getName(), mockName );
    int mockType = 999;
    dto.setType( mockType );
    assertEquals( dto.getType(), mockType );

  }

}
