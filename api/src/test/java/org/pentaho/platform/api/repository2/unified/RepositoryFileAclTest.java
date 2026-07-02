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


package org.pentaho.platform.api.repository2.unified;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Created by bgroves on 10/23/15.
 */
public class RepositoryFileAclTest {

  private static final String ID = "id";
  private static final RepositoryFileSid SID = new RepositoryFileSid( "SID" );
  private static final boolean INHERITING = false;
  private static final RepositoryFileAce ACE_FILE_ONE = new RepositoryFileAce( SID, RepositoryFilePermission.READ );
  private static final RepositoryFileAce ACE_FILE_TWO = new RepositoryFileAce( SID, RepositoryFilePermission.WRITE );
  private static final List<RepositoryFileAce> ACES = Arrays.asList( ACE_FILE_ONE, ACE_FILE_TWO );

  private RepositoryFileAcl acl;

  @BeforeEach
  public void setUp() {
    acl = new RepositoryFileAcl( ID, SID, INHERITING, ACES );
  }

  @Test
  public void testAcl() {
    assertEquals( ID, acl.getId() );
    assertEquals( SID, acl.getOwner() );
    assertEquals( INHERITING, acl.isEntriesInheriting() );
    assertTrue( acl.getAces().contains( ACE_FILE_ONE ) );
    assertTrue( acl.getAces().contains( ACE_FILE_TWO ) );

    assertNotEquals( 923521, acl.hashCode() );
    assertNotNull( acl.toString() );

    // Testing Exceptions
    try {
      new RepositoryFileAcl( null, null, true, null );
      fail( "Should of thrown and illegal arguement exception" );
    } catch ( Exception e ) {
      // Pass
    }
  }

  @Test
  public void testBuilderAndEquals() {
    RepositoryFileAcl.Builder builder = new RepositoryFileAcl.Builder( SID );
    RepositoryFileAcl newAcl = builder.build();
    assertEquals( SID, newAcl.getOwner() );
    assertNull( newAcl.getId() );
    assertTrue( newAcl.isEntriesInheriting() );
    assertEquals( 0, newAcl.getAces().size() );

    // Testing equals
    assertFalse( acl.equals( null ) );
    assertFalse( acl.equals( new String() ) );

    assertTrue( acl.equals( acl ) );
    builder = new RepositoryFileAcl.Builder( acl );
    RepositoryFileAcl dupAcl = builder.build();
    assertTrue( acl.equals( dupAcl ) );

    builder = new RepositoryFileAcl.Builder( acl );
    dupAcl = builder.build();
    assertEquals( acl.getAces().size(), dupAcl.getAces().size() );

    builder.owner( new RepositoryFileSid( "diffSid" ) );
    RepositoryFileAcl diffAcl = builder.build();
    assertFalse( acl.equals( diffAcl ) );

    builder.id( new String( "DiffId" ) );
    diffAcl = builder.build();
    assertFalse( acl.equals( diffAcl ) );
    builder.id( null );
    diffAcl = builder.build();
    assertFalse( diffAcl.equals( acl ) );

    builder.entriesInheriting( !acl.isEntriesInheriting() );
    diffAcl = builder.build();
    assertFalse( acl.equals( diffAcl ) );

    builder.aces( Arrays.asList( ACE_FILE_ONE ) );
    diffAcl = builder.build();
    assertFalse( acl.equals( diffAcl ) );

    // Testing build constructors
    String newOwner = "newOwner";
    builder = new RepositoryFileAcl.Builder( newOwner );
    newAcl = builder.build();
    assertEquals( newOwner, newAcl.getOwner().getName() );
    assertEquals( RepositoryFileSid.Type.USER, newAcl.getOwner().getType() );

    String newName = "newName";
    builder = new RepositoryFileAcl.Builder( newName, RepositoryFileSid.Type.ROLE );
    newAcl = builder.build();
    assertEquals( newName, newAcl.getOwner().getName() );
    assertEquals( RepositoryFileSid.Type.ROLE, newAcl.getOwner().getType() );

    String newId = "newId";
    builder = new RepositoryFileAcl.Builder( newId, newName, RepositoryFileSid.Type.ROLE );
    newAcl = builder.build();
    assertEquals( newName, newAcl.getOwner().getName() );
    assertEquals( RepositoryFileSid.Type.ROLE, newAcl.getOwner().getType() );

    builder.ace( SID, RepositoryFilePermission.DELETE );
    newAcl = builder.build();
    assertTrue( newAcl.getAces().get( 0 ).getPermissions().contains( RepositoryFilePermission.DELETE ) );

    EnumSet<RepositoryFilePermission> permissions = EnumSet.of( RepositoryFilePermission.READ,
      RepositoryFilePermission.WRITE );
    builder.clearAces();
    builder.ace( SID, permissions );
    newAcl = builder.build();
    assertTrue( newAcl.getAces().get( 0 ).getPermissions().contains( RepositoryFilePermission.READ ) );
    assertTrue( newAcl.getAces().get( 0 ).getPermissions().contains( RepositoryFilePermission.WRITE ) );

    builder.clearAces();
    builder.ace( "SID", RepositoryFileSid.Type.ROLE, RepositoryFilePermission.DELETE );
    newAcl = builder.build();
    assertEquals( "SID", newAcl.getAces().get( 0 ).getSid().getName() );
    assertEquals( RepositoryFileSid.Type.ROLE, newAcl.getAces().get( 0 ).getSid().getType() );
    assertTrue( newAcl.getAces().get( 0 ).getPermissions().contains( RepositoryFilePermission.DELETE ) );

    builder.clearAces();
    builder.ace( "SID", RepositoryFileSid.Type.ROLE, permissions );
    newAcl = builder.build();
    assertEquals( "SID", newAcl.getAces().get( 0 ).getSid().getName() );
    assertEquals( RepositoryFileSid.Type.ROLE, newAcl.getAces().get( 0 ).getSid().getType() );
    assertTrue( newAcl.getAces().get( 0 ).getPermissions().contains( RepositoryFilePermission.READ ) );
    assertTrue( newAcl.getAces().get( 0 ).getPermissions().contains( RepositoryFilePermission.WRITE ) );
  }
}
