/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.jcr;

import junit.framework.TestCase;

import org.junit.Test;

public class JcrStringHelperTest {

  @Test
  public void testLetterDigitIDEncode(){
    String id = "a243423d-adasdasdasd-asdasd";
    String encodedId = JcrStringHelper.idEncode( id );
    TestCase.assertEquals( id, encodedId );
  }
  
  @Test
  public void testDigitLetterIDEncode(){
    String id = "5a243423d-adasdasdasd-asdasd";
    String encodedId = JcrStringHelper.idEncode( id );
    TestCase.assertEquals( id, encodedId );
  }
 
  @Test
  public void testFilePathpecEncode(){
    String path="/asdf/3err";
    String encodedPath = JcrStringHelper.pathEncode( path );
    TestCase.assertTrue( ! path.equals( encodedPath ) );
    TestCase.assertEquals( "/asdf/_x0033_err", encodedPath );
  }
  
  @Test
  public void testFilePathSpecEncodeDecode(){
    String path="/asdf/3err";
    String encodedPath = JcrStringHelper.pathEncode( path );
    TestCase.assertTrue( ! path.equals( encodedPath ) );
    String decodedPath = JcrStringHelper.fileNameDecode( encodedPath );
    TestCase.assertEquals( path, decodedPath );
  }
  
  @Test
  public void testFilePathEncode(){
    String path="/asdf/err";
    String encodedPath = JcrStringHelper.pathEncode( path );
    TestCase.assertEquals( path, encodedPath );
  }
  
  @Test
  public void testFilePathEncodeDecode(){
    String path="/asdf/err";
    String encodedPath = JcrStringHelper.pathEncode( path );
    TestCase.assertEquals( path, encodedPath );
    String decodedPath = JcrStringHelper.fileNameDecode( encodedPath );
    TestCase.assertEquals( path, decodedPath );
  }
}
