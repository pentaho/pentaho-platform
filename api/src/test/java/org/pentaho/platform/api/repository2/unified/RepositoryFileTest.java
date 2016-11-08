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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.repository2.unified;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class RepositoryFileTest {

  private static final String NAME = "name";
  private static final String ID = "id";
  private static final String CREATOR_ID = "creatorID";
  private static final Date CREATED_DATE = new Date();
  private static final Date LAST_MODIFIED_DATE = new Date();
  private static final Long FILE_SIZE = 1000L;
  private static final Boolean FOLDER = true;
  private static final String PATH = "path";
  private static final Boolean HIDDEN = false;
  private static final Boolean SCHEDULABLE = true;
  private static final Boolean VERSIONED = true;
  private static final String VERSION_ID = "versionId";
  private static final Boolean LOCKED = false;
  private static final String LOCK_OWNER = "theOwner";
  private static final String LOCK_MESSAGE = "becauseICan";
  private static final Date LOCK_DATE = new Date();
  private static final String TITLE = "title";
  private static final String DESCRIPTION = "this file";
  private static final Map<String, Properties> LOCALE_PROP = new HashMap<String, Properties>();
  private static final String LOCALE = "en_us_utf8";
  private static final String PARENT_FOLDER = "parentFolder";
  private static final Date DELETED_DATE = new Date();

  private RepositoryFile file;

  @Before
  public void setUp() {
    if ( !LOCALE_PROP.containsKey( RepositoryFile.DEFAULT_LOCALE ) ) {
      LOCALE_PROP.put( RepositoryFile.DEFAULT_LOCALE, null );
    }
    file =
        new RepositoryFile( ID, NAME, FOLDER, HIDDEN, SCHEDULABLE, VERSIONED, VERSION_ID, PATH, CREATED_DATE,
            LAST_MODIFIED_DATE,
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

    assertEquals( 0, file.compareTo( file ) );

    RepositoryFile.Builder builder = new RepositoryFile.Builder( file );
    builder.title( "diffTitle" );
    builder.id( null );
    assertNotEquals( 0, file.compareTo( builder.build() ) );

    assertNotEquals( 29791, dupFile.hashCode() );
  }

  @Test
  public void testBuilderNulls() {
    RepositoryFile nullFile =
        new RepositoryFile( ID, NAME, FOLDER, HIDDEN, SCHEDULABLE, VERSIONED, VERSION_ID, PATH, null, null,
      LOCKED, LOCK_OWNER, LOCK_MESSAGE, null, LOCALE, null, DESCRIPTION, PARENT_FOLDER, null, FILE_SIZE,
      CREATOR_ID, null );

    assertNull( nullFile.getCreatedDate() );
    assertNull( nullFile.getLastModifiedDate() );
    assertNull( nullFile.getLockDate() );
    assertEquals( NAME, nullFile.getTitle() );
    assertNull( nullFile.getLocalePropertiesMap() );
    assertNull( nullFile.getDeletedDate() );
  }

  @Test
  public void testEquals() {
    RepositoryFile dupFile = file.clone();
    assertTrue( file.equals( dupFile ) );
    assertTrue( file.equals( file ) );
    assertFalse( file.equals( null ) );

    RepositoryFile.Builder builder = new RepositoryFile.Builder( file );
    // equals version ID
    builder.versionId( "diffVersionId" );
    RepositoryFile newFile = builder.build();
    assertFalse( newFile.equals( file ) );

    builder.versionId( null );
    newFile = builder.build();
    assertFalse( newFile.equals( file ) );


    RepositoryFile secondNewFile = builder.build();
    assertTrue( newFile.equals( secondNewFile ) );

    // equals locale
    builder.locale( "diffLocale" );
    newFile = builder.build();
    assertFalse( newFile.equals( file ) );

    builder.locale( null );
    newFile = builder.build();
    assertFalse( newFile.equals( file ) );

    secondNewFile = builder.build();
    assertTrue( newFile.equals( secondNewFile ) );

    // equals ID
    builder.id( "diffId" );
    newFile = builder.build();
    assertFalse( newFile.equals( file ) );

    builder.id( null );
    newFile = builder.build();
    assertFalse( newFile.equals( file ) );

    secondNewFile = builder.build();
    assertTrue( newFile.equals( secondNewFile ) );

    builder.path( "diffPath" );
    secondNewFile = builder.build();
    assertFalse( newFile.equals( secondNewFile ) );
  }

  @Test
  public void testBuilder() {
    RepositoryFile.Builder builder = new RepositoryFile.Builder( file );
    assertTrue( file.getLocalePropertiesMap().containsKey( RepositoryFile.DEFAULT_LOCALE ) );
    assertNull( file.getLocalePropertiesMap().get( RepositoryFile.DEFAULT_LOCALE ) );
    builder.clearLocalePropertiesMap();
    RepositoryFile newFile = builder.build();
    assertTrue( newFile.getLocalePropertiesMap().containsKey( RepositoryFile.DEFAULT_LOCALE ) );
    assertNotNull( newFile.getLocalePropertiesMap().get( RepositoryFile.DEFAULT_LOCALE ) );

    builder.localeProperties( null, new Properties() );
    newFile = builder.build();
    assertEquals( 2, newFile.getLocalePropertiesMap().size() );
    assertNotNull( newFile.getLocalePropertiesMap().get( null ) );

    String localTitle = "newLocalTitle";
    String newTitle = "newTitle";
    builder.title( localTitle, newTitle );
    newFile = builder.build();
    assertTrue( newFile.getLocalePropertiesMap().containsKey( localTitle ) );
    assertEquals( newTitle, newFile.getLocalePropertiesMap().get( localTitle ).getProperty( RepositoryFile.TITLE ) );
    assertEquals( newTitle, newFile.getLocalePropertiesMap().get( localTitle ).getProperty( RepositoryFile.FILE_TITLE ) );

    String localDesc = "newLocalDesc";
    String newDesc = "newDesc";
    builder.description( localDesc, newDesc );
    newFile = builder.build();
    assertTrue( newFile.getLocalePropertiesMap().containsKey( localDesc ) );
    assertEquals( newDesc, newFile.getLocalePropertiesMap().get( localDesc ).getProperty( RepositoryFile.DESCRIPTION ) );
    assertEquals( newDesc, newFile.getLocalePropertiesMap().get( localDesc ).getProperty( RepositoryFile.FILE_DESCRIPTION ) );

    builder = new RepositoryFile.Builder( ID, NAME );
    newFile = builder.build();
    assertEquals( NAME, newFile.getName() );
    assertEquals( ID, newFile.getId() );

    String newString = "newString";
    builder.name( newString );
    newFile = builder.build();
    assertEquals( newString, newFile.getName() );

    // Test that Default Locale gets set
    builder.localePropertiesMap( new HashMap<String, Properties>() );
    newFile = builder.build();
    assertTrue( newFile.getLocalePropertiesMap().containsKey( RepositoryFile.DEFAULT_LOCALE ) );

    builder.localePropertiesMap( null );
    builder.title( LOCALE, "newTitle" );
    newFile = builder.build();
    assertTrue( newFile.getLocalePropertiesMap().containsKey( RepositoryFile.DEFAULT_LOCALE ) );

    try {
      builder = new RepositoryFile.Builder( null, NAME );
      fail( "Null pointer exception should of be thrown." );
    } catch ( Exception e ) {
      // Pass
    }

    // Test null Properties Map
    RepositoryFile nullFile =
        new RepositoryFile( ID, NAME, FOLDER, HIDDEN, SCHEDULABLE, VERSIONED, VERSION_ID, PATH, CREATED_DATE,
            LAST_MODIFIED_DATE,
      LOCKED, LOCK_OWNER, LOCK_MESSAGE, LOCK_DATE, LOCALE, TITLE, DESCRIPTION, PARENT_FOLDER, DELETED_DATE, FILE_SIZE,
      CREATOR_ID, null );
    builder = new RepositoryFile.Builder( nullFile );
    newFile = builder.build();
    assertNull( newFile.getLocalePropertiesMap() );
    try {
      builder.clearLocalePropertiesMap();
    } catch ( Exception e ) {
      fail( "NPE should not have occurred." );
    }
    newFile = builder.build();
    assertNull( newFile.getLocalePropertiesMap() );

    // Test overwriting existing properties
    builder = new RepositoryFile.Builder( file );
    newFile = builder.build();
    assertFalse( newFile.getLocalePropertiesMap().containsKey( LOCALE ) );
    builder.title( LOCALE, TITLE );
    newFile = builder.build();
    assertEquals( TITLE, newFile.getLocalePropertiesMap().get( LOCALE ).getProperty( RepositoryFile.TITLE ) );

    builder.title( LOCALE, newTitle );
    newFile = builder.build();
    assertEquals( newTitle, newFile.getLocalePropertiesMap().get( LOCALE ).getProperty( RepositoryFile.TITLE ) );

    builder.description( LOCALE, DESCRIPTION );
    newFile = builder.build();
    assertEquals( DESCRIPTION, newFile.getLocalePropertiesMap().get( LOCALE ).getProperty( RepositoryFile.DESCRIPTION ) );

    builder.description( LOCALE, newDesc );
    newFile = builder.build();
    assertEquals( newDesc, newFile.getLocalePropertiesMap().get( LOCALE ).getProperty( RepositoryFile.DESCRIPTION ) );
  }

  private void checkRepositoryFile( RepositoryFile theFile ) {
    assertEquals( NAME, theFile.getName() );
    assertEquals( ID, theFile.getId() );
    assertEquals( CREATOR_ID, theFile.getCreatorId() );
    assertEquals( CREATED_DATE, theFile.getCreatedDate() );
    assertEquals( LAST_MODIFIED_DATE, theFile.getLastModifiedDate() );
    assertEquals( FILE_SIZE, theFile.getFileSize(), .0000001 );
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
