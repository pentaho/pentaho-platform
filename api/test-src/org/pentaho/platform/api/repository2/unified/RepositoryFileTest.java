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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
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
      Assert.fail( "Shoudld throw an exception" );
    } catch ( Exception e ) {
      // Pass
    }

    Assert.assertEquals( 0, file.compareTo( file ) );

    RepositoryFile.Builder builder = new RepositoryFile.Builder( file );
    builder.title( "diffTitle" );
    builder.id( null );
    Assert.assertNotEquals( 0, file.compareTo( builder.build() ) );

    Assert.assertNotEquals( 29791, dupFile.hashCode() );
  }

  @Test
  public void testBuilderNulls() {
    RepositoryFile nullFile =
        new RepositoryFile( ID, NAME, FOLDER, HIDDEN, SCHEDULABLE, VERSIONED, VERSION_ID, PATH, null, null,
      LOCKED, LOCK_OWNER, LOCK_MESSAGE, null, LOCALE, null, DESCRIPTION, PARENT_FOLDER, null, FILE_SIZE,
      CREATOR_ID, null );

    Assert.assertNull( nullFile.getCreatedDate() );
    Assert.assertNull( nullFile.getLastModifiedDate() );
    Assert.assertNull( nullFile.getLockDate() );
    Assert.assertEquals( NAME, nullFile.getTitle() );
    Assert.assertNull( nullFile.getLocalePropertiesMap() );
    Assert.assertNull( nullFile.getDeletedDate() );
  }

  @Test
  public void testEquals() {
    RepositoryFile dupFile = file.clone();
    Assert.assertTrue( file.equals( dupFile ) );
    Assert.assertTrue( file.equals( file ) );
    Assert.assertFalse( file.equals( null ) );

    RepositoryFile.Builder builder = new RepositoryFile.Builder( file );
    // equals version ID
    builder.versionId( "diffVersionId" );
    RepositoryFile newFile = builder.build();
    Assert.assertFalse( newFile.equals( file ) );

    builder.versionId( null );
    newFile = builder.build();
    Assert.assertFalse( newFile.equals( file ) );


    RepositoryFile secondNewFile = builder.build();
    Assert.assertTrue( newFile.equals( secondNewFile ) );

    // equals locale
    builder.locale( "diffLocale" );
    newFile = builder.build();
    Assert.assertFalse( newFile.equals( file ) );

    builder.locale( null );
    newFile = builder.build();
    Assert.assertFalse( newFile.equals( file ) );

    secondNewFile = builder.build();
    Assert.assertTrue( newFile.equals( secondNewFile ) );

    // equals ID
    builder.id( "diffId" );
    newFile = builder.build();
    Assert.assertFalse( newFile.equals( file ) );

    builder.id( null );
    newFile = builder.build();
    Assert.assertFalse( newFile.equals( file ) );

    secondNewFile = builder.build();
    Assert.assertTrue( newFile.equals( secondNewFile ) );

    builder.path( "diffPath" );
    secondNewFile = builder.build();
    Assert.assertFalse( newFile.equals( secondNewFile ) );
  }

  @Test
  public void testBuilder() {
    RepositoryFile.Builder builder = new RepositoryFile.Builder( file );
    Assert.assertTrue( file.getLocalePropertiesMap().containsKey( RepositoryFile.DEFAULT_LOCALE ) );
    Assert.assertNull( file.getLocalePropertiesMap().get( RepositoryFile.DEFAULT_LOCALE ) );
    builder.clearLocalePropertiesMap();
    RepositoryFile newFile = builder.build();
    Assert.assertTrue( newFile.getLocalePropertiesMap().containsKey( RepositoryFile.DEFAULT_LOCALE ) );
    Assert.assertNotNull( newFile.getLocalePropertiesMap().get( RepositoryFile.DEFAULT_LOCALE ) );

    builder.localeProperties( null, new Properties() );
    newFile = builder.build();
    Assert.assertEquals( 2, newFile.getLocalePropertiesMap().size() );
    Assert.assertNotNull( newFile.getLocalePropertiesMap().get( null ) );

    String localTitle = "newLocalTitle";
    String newTitle = "newTitle";
    builder.title( localTitle, newTitle );
    newFile = builder.build();
    Assert.assertTrue( newFile.getLocalePropertiesMap().containsKey( localTitle ) );
    Assert.assertEquals( newTitle, newFile.getLocalePropertiesMap().get( localTitle ).getProperty( RepositoryFile.TITLE ) );
    Assert.assertEquals( newTitle, newFile.getLocalePropertiesMap().get( localTitle ).getProperty( RepositoryFile.FILE_TITLE ) );

    String localDesc = "newLocalDesc";
    String newDesc = "newDesc";
    builder.description( localDesc, newDesc );
    newFile = builder.build();
    Assert.assertTrue( newFile.getLocalePropertiesMap().containsKey( localDesc ) );
    Assert.assertEquals( newDesc, newFile.getLocalePropertiesMap().get( localDesc ).getProperty( RepositoryFile.DESCRIPTION ) );
    Assert.assertEquals( newDesc, newFile.getLocalePropertiesMap().get( localDesc ).getProperty( RepositoryFile.FILE_DESCRIPTION ) );

    builder = new RepositoryFile.Builder( ID, NAME );
    newFile = builder.build();
    Assert.assertEquals( NAME, newFile.getName() );
    Assert.assertEquals( ID, newFile.getId() );

    String newString = "newString";
    builder.name( newString );
    newFile = builder.build();
    Assert.assertEquals( newString, newFile.getName() );

    // Test that Default Locale gets set
    builder.localePropertiesMap( new HashMap<String, Properties>() );
    newFile = builder.build();
    Assert.assertTrue( newFile.getLocalePropertiesMap().containsKey( RepositoryFile.DEFAULT_LOCALE ) );

    builder.localePropertiesMap( null );
    builder.title( LOCALE, "newTitle" );
    newFile = builder.build();
    Assert.assertTrue( newFile.getLocalePropertiesMap().containsKey( RepositoryFile.DEFAULT_LOCALE ) );

    try {
      builder = new RepositoryFile.Builder( null, NAME );
      Assert.fail( "Null pointer exception should of be thrown." );
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
    Assert.assertNull( newFile.getLocalePropertiesMap() );
    try {
      builder.clearLocalePropertiesMap();
    } catch ( Exception e ) {
      Assert.fail( "NPE should not have occurred." );
    }
    newFile = builder.build();
    Assert.assertNull( newFile.getLocalePropertiesMap() );

    // Test overwriting existing properties
    builder = new RepositoryFile.Builder( file );
    newFile = builder.build();
    Assert.assertFalse( newFile.getLocalePropertiesMap().containsKey( LOCALE ) );
    builder.title( LOCALE, TITLE );
    newFile = builder.build();
    Assert.assertEquals( TITLE, newFile.getLocalePropertiesMap().get( LOCALE ).getProperty( RepositoryFile.TITLE ) );

    builder.title( LOCALE, newTitle );
    newFile = builder.build();
    Assert.assertEquals( newTitle, newFile.getLocalePropertiesMap().get( LOCALE ).getProperty( RepositoryFile.TITLE ) );

    builder.description( LOCALE, DESCRIPTION );
    newFile = builder.build();
    Assert.assertEquals( DESCRIPTION, newFile.getLocalePropertiesMap().get( LOCALE ).getProperty( RepositoryFile.DESCRIPTION ) );

    builder.description( LOCALE, newDesc );
    newFile = builder.build();
    Assert.assertEquals( newDesc, newFile.getLocalePropertiesMap().get( LOCALE ).getProperty( RepositoryFile.DESCRIPTION ) );
  }

  private void checkRepositoryFile( RepositoryFile theFile ) {
    Assert.assertEquals( NAME, theFile.getName() );
    Assert.assertEquals( ID, theFile.getId() );
    Assert.assertEquals( CREATOR_ID, theFile.getCreatorId() );
    Assert.assertEquals( CREATED_DATE, theFile.getCreatedDate() );
    Assert.assertEquals( LAST_MODIFIED_DATE, theFile.getLastModifiedDate() );
    Assert.assertEquals( FILE_SIZE, theFile.getFileSize(), .0000001 );
    Assert.assertEquals( FOLDER, theFile.isFolder() );
    Assert.assertEquals( PATH, theFile.getPath() );
    Assert.assertEquals( HIDDEN, theFile.isHidden() );
    Assert.assertEquals( VERSIONED, theFile.isVersioned() );
    Assert.assertEquals( VERSION_ID, theFile.getVersionId() );
    Assert.assertEquals( LOCKED, theFile.isLocked() );
    Assert.assertEquals( LOCK_OWNER, theFile.getLockOwner() );
    Assert.assertEquals( LOCK_MESSAGE, theFile.getLockMessage() );
    Assert.assertEquals( LOCK_DATE, theFile.getLockDate() );
    Assert.assertEquals( TITLE, theFile.getTitle() );
    Assert.assertEquals( DESCRIPTION, theFile.getDescription() );
    Assert.assertEquals( LOCALE_PROP, theFile.getLocalePropertiesMap() );
    Assert.assertEquals( LOCALE, theFile.getLocale() );
    Assert.assertEquals( PARENT_FOLDER, theFile.getOriginalParentFolderPath() );
    Assert.assertEquals( DELETED_DATE, theFile.getDeletedDate() );
    Assert.assertEquals( false, theFile.isAclNode() );
    Assert.assertNotNull( theFile.toString() );
  }

}
