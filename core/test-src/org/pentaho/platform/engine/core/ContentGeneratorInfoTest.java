/*
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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.core;

import junit.framework.TestCase;
import org.pentaho.platform.engine.core.solution.ContentGeneratorInfo;
import org.pentaho.platform.engine.core.solution.FileInfo;

@SuppressWarnings( { "all" } )
public class ContentGeneratorInfoTest extends TestCase {

  public void testContentGeneratorInfo() {

    ContentGeneratorInfo cgi = new ContentGeneratorInfo();

    cgi.setClassname( "test classname" );
    cgi.setDescription( "test description" );
    cgi.setId( "test id" );
    cgi.setTitle( "test title" );
    cgi.setType( "test type" );
    cgi.setUrl( "test url" );

    assertEquals( "wrong field", "test classname", cgi.getClassname() );
    assertEquals( "wrong field", "test description", cgi.getDescription() );
    assertEquals( "wrong field", "test id", cgi.getId() );
    assertEquals( "wrong field", "test title", cgi.getTitle() );
    assertEquals( "wrong field", "test type", cgi.getType() );
    assertEquals( "wrong field", "test url", cgi.getUrl() );

  }

  public void testFileInfo() {

    FileInfo cgi = new FileInfo();

    cgi.setDescription( "test description" );
    cgi.setTitle( "test title" );
    cgi.setAuthor( "test author" );
    cgi.setIcon( "test icon" );
    cgi.setDisplayType( "test displaytype" );

    assertEquals( "wrong field", "test description", cgi.getDescription() );
    assertEquals( "wrong field", "test title", cgi.getTitle() );
    assertEquals( "wrong field", "test author", cgi.getAuthor() );
    assertEquals( "wrong field", "test icon", cgi.getIcon() );
    assertEquals( "wrong field", "test displaytype", cgi.getDisplayType() );

  }

}
