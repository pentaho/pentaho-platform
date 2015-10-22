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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.repository2.unified;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;

public class RepositoryFileTest {

  private static final String NAME = "name";
  private static final String ID = "id";
  private static final String CREATOR_ID = anyString();
  private static final Date CREATED_DATE = any( Date.class );
  private static final Date LAST_MODIFIED_DATE = any( Date.class );
  private static final Long FILE_SIZE = anyLong();
  private static final Boolean FOLDER = anyBoolean();
  private static final String PATH = anyString();
  private static final Boolean HIDDEN = anyBoolean();
  private static final Boolean VERSIONED = anyBoolean();
  private static final String VERSION_ID = anyString();
  private static final Boolean LOCKED = anyBoolean();
  private static final String LOCK_OWNER = anyString();
  private static final String LOCK_MESSAGE = anyString();
  private static final Date LOCK_DATE = any( Date.class );
  private static final String TITLE = anyString();
  private static final String DESCRIPTION = anyString();
  private static final Map<String, Properties> LOCALE_PROP = new HashMap<String, Properties>();
  private static final String LOCALE = anyString();
  private static final String PARENT_FOLDER = anyString();
  private static final Date DELETED_DATE = any( Date.class );

  private RepositoryFile file;

  @Before
  public void setUp() {
    if ( !LOCALE_PROP.containsKey( RepositoryFile.DEFAULT_LOCALE ) ) {
      LOCALE_PROP.put( RepositoryFile.DEFAULT_LOCALE, null );
    }
    file = new RepositoryFile( ID, NAME, FOLDER, HIDDEN, VERSIONED, VERSION_ID, PATH, CREATED_DATE, LAST_MODIFIED_DATE,
      LOCKED, LOCK_OWNER, LOCK_MESSAGE, LOCK_DATE, LOCALE, TITLE, DESCRIPTION, PARENT_FOLDER, DELETED_DATE, FILE_SIZE,
      CREATOR_ID, LOCALE_PROP );
  }

  @Test
  public void testRepositoryFile() {
    checkRepositoryFile( file );
    RepositoryFile dupFile = file.clone();
    checkRepositoryFile( dupFile );

    try {
      file.compareTo( null );
      fail( "Shoudld throw an exception" );
    } catch ( Exception e ) {
      // Pass
    }

    assertNotEquals( 29791, dupFile.hashCode() );

    // Coverage for equals
    assertTrue( file.equals( dupFile ) );
    assertTrue( file.equals( file ) );
    assertFalse( file.equals( null ) );
  }

  private void checkRepositoryFile( RepositoryFile theFile ) {
    assertEquals( NAME, theFile.getName() );
    assertEquals( ID, theFile.getId() );
    assertEquals( CREATOR_ID, theFile.getCreatorId() );
    assertEquals( CREATED_DATE, theFile.getCreatedDate() );
    assertEquals( LAST_MODIFIED_DATE, theFile.getLastModifiedDate() );
    assertEquals( 0, theFile.getFileSize(), .0000001 );
    assertEquals( FOLDER, theFile.isFolder() );
    assertEquals( PATH, theFile.getPath() );
    assertEquals( HIDDEN, theFile.isHidden() );
    assertEquals( VERSIONED, theFile.isVersioned() );
    assertEquals( VERSION_ID, theFile.getVersionId() );
    assertEquals( LOCKED, theFile.isLocked() );
    assertEquals( LOCK_OWNER, theFile.getLockOwner() );
    assertEquals( LOCK_MESSAGE, theFile.getLockMessage() );
    assertEquals( LOCK_DATE, theFile.getLockDate() );
    assertEquals( TITLE, theFile.getTitle() );
    assertEquals( DESCRIPTION, theFile.getDescription() );
    assertEquals( LOCALE_PROP, theFile.getLocalePropertiesMap() );
    assertEquals( LOCALE, theFile.getLocale() );
    assertEquals( PARENT_FOLDER, theFile.getOriginalParentFolderPath() );
    assertEquals( DELETED_DATE, theFile.getDeletedDate() );
    assertEquals( false, theFile.isAclNode() );
    assertNotNull( theFile.toString() );
  }

}
