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
 * Copyright (c) 2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.util.xml.dom4j;

import org.junit.Test;

import javax.xml.transform.TransformerFactoryConfigurationError;

import static org.mockito.Mockito.mock;

public class XmlDom4JHelperTest {

  @Test ( expected = TransformerFactoryConfigurationError.class )
  public void testConvertToDom4JDocTrowTransformerFactoryConfigurationErrorException() throws Exception {
    final org.w3c.dom.Document doc = mock( org.w3c.dom.Document.class );
    XmlDom4JHelper.convertToDom4JDoc( doc );
  }
}
