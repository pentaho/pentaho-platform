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

package org.pentaho.platform.api.engine;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IAuthorizationActionTest {

  static class SelfAction implements IAuthorizationAction {
    @Override
    public String getName() {
      return "self";
    }

    @Override
    public String getLocalizedDisplayName( String locale ) {
      return "Self";
    }

    @Override
    public String getLocalizedDescription( String locale ) {
      return "Self action";
    }
    // getResourceTypes() default: empty set
  }

  static class ResourceAction implements IAuthorizationAction {
    @Override
    public String getName() {
      return "resource";
    }

    @Override
    public String getLocalizedDisplayName( String locale ) {
      return "Resource";
    }

    @Override
    public String getLocalizedDescription( String locale ) {
      return "Resource action";
    }

    @Override
    @NonNull
    public Set<String> getResourceTypes() {
      return Set.of( "typeA", "typeB" );
    }
  }

  static class DefaultsAction implements IAuthorizationAction {
    @Override
    public String getName() {
      return "defaults";
    }

    @Override
    public String getLocalizedDisplayName( String locale ) {
      return "Defaults";
    }

    @Override
    public String getLocalizedDescription( String locale ) {
      return "Defaults action";
    }
  }

  @Test
  void testDefaultResourceTypesAreEmpty() {
    IAuthorizationAction action = new DefaultsAction();
    assertNotNull( action.getResourceTypes() );
    assertTrue( action.getResourceTypes().isEmpty() );
  }

  @Test
  void testIsSelfActionIfHasNoResourceTypes() {
    IAuthorizationAction selfAction = new SelfAction();
    IAuthorizationAction resourceAction = new ResourceAction();
    IAuthorizationAction defaultsAction = new DefaultsAction();

    assertTrue( selfAction.isSelfAction() );
    assertFalse( resourceAction.isSelfAction() );
    assertTrue( defaultsAction.isSelfAction() );
  }

  @Test
  void testIsResourceActionIfHasResourceTypes() {
    IAuthorizationAction selfAction = new SelfAction();
    IAuthorizationAction resourceAction = new ResourceAction();
    IAuthorizationAction defaultsAction = new DefaultsAction();

    assertFalse( selfAction.isResourceAction() );
    assertTrue( resourceAction.isResourceAction() );
    assertFalse( defaultsAction.isResourceAction() );
  }

  @Test
  void testPerformsOnResourceType() {
    IAuthorizationAction resourceAction = new ResourceAction();

    assertTrue( resourceAction.performsOnResourceType( "typeA" ) );
    assertTrue( resourceAction.performsOnResourceType( "typeB" ) );
    assertFalse( resourceAction.performsOnResourceType( "typeC" ) );

    // --

    IAuthorizationAction selfAction = new SelfAction();

    assertFalse( selfAction.performsOnResourceType( "typeA" ) );
    assertFalse( selfAction.performsOnResourceType( "typeB" ) );
    assertFalse( selfAction.performsOnResourceType( "typeC" ) );
  }

  @SuppressWarnings( "DataFlowIssue" )
  @Test
  void testPerformsOnResourceTypeNullThrows() {
    IAuthorizationAction action = new DefaultsAction();

    assertThrows( NullPointerException.class, () -> {
        action.performsOnResourceType( null );
      });
  }
}
