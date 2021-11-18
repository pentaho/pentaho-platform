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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.apache.jackrabbit.core.security.authorization.acl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;

import org.apache.jackrabbit.core.SessionImpl;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.mockito.ArgumentMatchers.any;

public class MagicAceDefinitionTest {
  @Test
  public void parseYamlMagicAceDefinitionsTest() throws Exception {
    List<MagicAceDefinition> aces = parseYamlFile( "./src/test/resources/MagicAceTest.yaml" );
    assertEquals( 3, aces.size() );
    boolean[] caseHit = new boolean[3];
    for ( MagicAceDefinition ace : aces ) {
      switch ( ace.path ) {
        case "{0}":
          caseHit[0] = true;
          assertEquals( "org.pentaho.security.administerSecurity", ace.logicalRole );
          assertNotNull( ace.privileges );
          assertEquals( 1, ace.privileges.length );
          assertEquals( true, ace.applyToTarget );
          assertEquals( true, ace.applyToChildren );
          assertEquals( false, ace.applyToAncestors );
          assertNull( ace.exceptChildren );
          break;
        case "{0}/etc":
          caseHit[1] = true;
          assertEquals( "org.pentaho.repository.read", ace.logicalRole );
          assertNotNull( ace.privileges );
          assertEquals( 2, ace.privileges.length );
          assertEquals( true, ace.applyToTarget );
          assertEquals( false, ace.applyToChildren );
          assertEquals( false, ace.applyToAncestors );
          assertNotNull( ace.exceptChildren );
          assertEquals( 1, ace.exceptChildren.length );
          assertEquals( "{0}/etc/pdi/databases", ace.exceptChildren[0] );
          break;
        case "{0}/foo":
          caseHit[2] = true;
          assertEquals( "org.pentaho.repository.create", ace.logicalRole );
          assertNotNull( ace.privileges );
          assertEquals( 7, ace.privileges.length );
          assertEquals( true, ace.applyToTarget );
          assertEquals( false, ace.applyToChildren );
          assertEquals( true, ace.applyToAncestors );
          assertNotNull( ace.exceptChildren );
          assertEquals( 2, ace.exceptChildren.length );
          assertEquals( "one", ace.exceptChildren[0] );
          assertEquals( "two", ace.exceptChildren[1] );
          break;
        default:
          fail( "Unknown Ace path" );
      }
    }
    assertTrue( "All aces in yaml file were not present", caseHit[0] && caseHit[1] && caseHit[2] );
  }

  @Test
  /*
   * This test ensures the actual yaml file in use will parse properly
   */
  public void parseActualYamlMagicAceDefinitionsTest() throws Exception {
    parseYamlFile( "./src/main/resources/jcr/config.yaml" );
  }

  private List<MagicAceDefinition> parseYamlFile( String filePath ) throws Exception {
    SessionImpl mockSessionImpl = mock( SessionImpl.class );
    AccessControlManager mockAccessControlManager = mock( AccessControlManager.class );
    Privilege mockPrivilege = mock( Privilege.class );
    when( mockSessionImpl.getAccessControlManager() ).thenReturn( mockAccessControlManager );
    when( mockAccessControlManager.privilegeFromName( any() ) ).thenReturn( mockPrivilege );

    InputStream input = new FileInputStream( new File( filePath ) );
    return MagicAceDefinition.parseYamlMagicAceDefinitions( input, mockSessionImpl );
  }

}
