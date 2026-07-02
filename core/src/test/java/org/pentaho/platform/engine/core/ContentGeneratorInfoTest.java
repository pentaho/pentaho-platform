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
