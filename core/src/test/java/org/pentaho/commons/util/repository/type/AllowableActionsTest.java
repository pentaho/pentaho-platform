/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.commons.util.repository.type;

import static org.junit.Assert.*;
import org.junit.Test;

public class AllowableActionsTest {

  @Test
  public void testAllowableActionsBean() {
    AllowableActions aa = new AllowableActions();
    aa.setCanAddPolicy( true );
    aa.setCanAddToFolder( true );
    aa.setCanCancelCheckout( true );
    aa.setCanCheckin( true );
    aa.setCanCheckout( true );
    aa.setCanCreateDocument( true );
    aa.setCanCreateFolder( true );
    aa.setCanCreatePolicy( true );
    aa.setCanCreateRelationship( true );
    aa.setCanDelete( true );
    aa.setCanDeleteContent( true );
    aa.setCanDeleteTree( true );
    aa.setCanDeleteVersion( true );
    aa.setCanGetAllVersions( true );
    aa.setCanGetAppliedPolicies( true );
    aa.setCanGetChildren( true );
    aa.setCanGetDescendants( true );
    aa.setCanGetFolderParent( true );
    aa.setCanGetParents( true );
    aa.setCanGetProperties( true );
    aa.setCanGetRelationships( true );
    aa.setCanMove( true );
    aa.setCanRemoveFromFolder( true );
    aa.setCanRemovePolicy( true );
    aa.setCanSetContent( true );
    aa.setCanUpdateProperties( true );
    aa.setCanViewContent( true );
    assertTrue( aa.isCanAddPolicy() );
    assertTrue( aa.isCanAddToFolder() );
    assertTrue( aa.isCanCancelCheckout() );
    assertTrue( aa.isCanCheckin() );
    assertTrue( aa.isCanCheckout() );
    assertTrue( aa.isCanCreateDocument() );
    assertTrue( aa.isCanCreateFolder() );
    assertTrue( aa.isCanCreatePolicy() );
    assertTrue( aa.isCanCreateRelationship() );
    assertTrue( aa.isCanDelete() );
    assertTrue( aa.isCanDeleteContent() );
    assertTrue( aa.isCanDeleteTree() );
    assertTrue( aa.isCanDeleteVersion() );
    assertTrue( aa.isCanGetAllVersions() );
    assertTrue( aa.isCanGetAppliedPolicies() );
    assertTrue( aa.isCanGetChildren() );
    assertTrue( aa.isCanGetDescendants() );
    assertTrue( aa.isCanGetFolderParent() );
    assertTrue( aa.isCanGetParents() );
    assertTrue( aa.isCanGetProperties() );
    assertTrue( aa.isCanGetRelationships() );
    assertTrue( aa.isCanMove() );
    assertTrue( aa.isCanRemoveFromFolder() );
    assertTrue( aa.isCanRemovePolicy() );
    assertTrue( aa.isCanSetContent() );
    assertTrue( aa.isCanUpdateProperties() );
    assertTrue( aa.isCanViewContent() );

    aa.setCanAddPolicy( false );
    aa.setCanAddToFolder( false );
    aa.setCanCancelCheckout( false );
    aa.setCanCheckin( false );
    aa.setCanCheckout( false );
    aa.setCanCreateDocument( false );
    aa.setCanCreateFolder( false );
    aa.setCanCreatePolicy( false );
    aa.setCanCreateRelationship( false );
    aa.setCanDelete( false );
    aa.setCanDeleteContent( false );
    aa.setCanDeleteTree( false );
    aa.setCanDeleteVersion( false );
    aa.setCanGetAllVersions( false );
    aa.setCanGetAppliedPolicies( false );
    aa.setCanGetChildren( false );
    aa.setCanGetDescendants( false );
    aa.setCanGetFolderParent( false );
    aa.setCanGetParents( false );
    aa.setCanGetProperties( false );
    aa.setCanGetRelationships( false );
    aa.setCanMove( false );
    aa.setCanRemoveFromFolder( false );
    aa.setCanRemovePolicy( false );
    aa.setCanSetContent( false );
    aa.setCanUpdateProperties( false );
    aa.setCanViewContent( false );
    assertFalse( aa.isCanAddPolicy() );
    assertFalse( aa.isCanAddToFolder() );
    assertFalse( aa.isCanCancelCheckout() );
    assertFalse( aa.isCanCheckin() );
    assertFalse( aa.isCanCheckout() );
    assertFalse( aa.isCanCreateDocument() );
    assertFalse( aa.isCanCreateFolder() );
    assertFalse( aa.isCanCreatePolicy() );
    assertFalse( aa.isCanCreateRelationship() );
    assertFalse( aa.isCanDelete() );
    assertFalse( aa.isCanDeleteContent() );
    assertFalse( aa.isCanDeleteTree() );
    assertFalse( aa.isCanDeleteVersion() );
    assertFalse( aa.isCanGetAllVersions() );
    assertFalse( aa.isCanGetAppliedPolicies() );
    assertFalse( aa.isCanGetChildren() );
    assertFalse( aa.isCanGetDescendants() );
    assertFalse( aa.isCanGetFolderParent() );
    assertFalse( aa.isCanGetParents() );
    assertFalse( aa.isCanGetProperties() );
    assertFalse( aa.isCanGetRelationships() );
    assertFalse( aa.isCanMove() );
    assertFalse( aa.isCanRemoveFromFolder() );
    assertFalse( aa.isCanRemovePolicy() );
    assertFalse( aa.isCanSetContent() );
    assertFalse( aa.isCanUpdateProperties() );
    assertFalse( aa.isCanViewContent() );
  }

}
