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


package org.apache.jackrabbit.core.security.authorization.acl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.security.Privilege;

import org.apache.jackrabbit.core.SessionImpl;
import org.yaml.snakeyaml.Yaml;

/**
 * A configuration entry that defines a "magic ACE" rule. This is the object representation of rules that reside in
 * {@code repository.xml}.
 * 
 * @author mlowery
 */
public class MagicAceDefinition {

  public String path;

  public String logicalRole;

  public Privilege[] privileges;

  public boolean applyToChildren;

  public boolean applyToAncestors;

  public boolean applyToTarget;

  public String[] exceptChildren;

  public MagicAceDefinition( final String path, final String logicalRole, final Privilege[] privileges,
      final boolean applyToTarget, final boolean applyToChildren, final boolean applyToAncestors,
      final String[] exceptChildren ) {
    super();
    this.path = path;
    this.logicalRole = logicalRole;
    this.privileges = privileges;
    this.applyToChildren = applyToChildren;
    this.applyToAncestors = applyToAncestors;
    this.applyToTarget = applyToTarget;
    this.exceptChildren = exceptChildren;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( logicalRole == null ) ? 0 : logicalRole.hashCode() );
    result = prime * result + ( ( path == null ) ? 0 : path.hashCode() );
    result = prime * result + Arrays.hashCode( privileges );
    result = prime * result + ( applyToChildren ? 1231 : 1237 );
    result = prime * result + ( applyToAncestors ? 8 : 9 );
    result = prime * result + ( applyToTarget ? 6 : 7 );
    result = prime * result + Arrays.hashCode( exceptChildren );
    return result;
  }

  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( obj == null ) {
      return false;
    }
    if ( getClass() != obj.getClass() ) {
      return false;
    }
    MagicAceDefinition other = (MagicAceDefinition) obj;
    if ( logicalRole == null ) {
      if ( other.logicalRole != null ) {
        return false;
      }
    } else if ( !logicalRole.equals( other.logicalRole ) ) {
      return false;
    }
    if ( path == null ) {
      if ( other.path != null ) {
        return false;
      }
    } else if ( !path.equals( other.path ) ) {
      return false;
    }
    if ( !Arrays.equals( privileges, other.privileges ) ) {
      return false;
    }
    if ( applyToChildren != other.applyToChildren ) {
      return false;
    }
    if ( applyToAncestors != other.applyToAncestors ) {
      return false;
    }
    if ( applyToTarget != other.applyToTarget ) {
      return false;
    }
    if ( !Arrays.equals( exceptChildren, other.exceptChildren ) ) {
      return false;
    }
    return true;
  }

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return "MagicAceDefinition [path=" + path + ", logicalRole=" + logicalRole + ", privileges="
        + Arrays.toString( privileges ) + ", applyToTarget=" + applyToTarget + ", applyToChildren=" + applyToChildren
        + ", applyToAncestors=" + applyToAncestors + "]";
  }

  @SuppressWarnings( "unchecked" )
  public static List<MagicAceDefinition> parseYamlMagicAceDefinitions( InputStream yamlFileInputStream,
      SessionImpl systemSession ) throws RepositoryException {
    List<MagicAceDefinition> magicAceDefinitions = new ArrayList<MagicAceDefinition>();
    try {
      Yaml yaml = new Yaml();
      Map<String, Object> map = (Map<String, Object>) yaml.load( yamlFileInputStream );
      List<Map<String, Object>> magicAceList = (List<Map<String, Object>>) map.get( "MagicAces" );
      for ( Map<String, Object> magicAceMap : magicAceList ) {
        MagicAceDefinition pam = parseMagicAceDefinition( magicAceMap, systemSession );
        magicAceDefinitions.add( pam );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( "Could not parse magic ace configurations from Yaml file.", e );
    }
    return magicAceDefinitions;
  }

  /**
   * Parses a single magic ACE definition from a yaml file.
   */
  @SuppressWarnings( "unchecked" )
  private static MagicAceDefinition
      parseMagicAceDefinition( Map<String, Object> magicAceMap, SessionImpl systemSession ) throws RepositoryException {
    String path = magicAceMap.get( "path" ).toString();
    String logicalRole = magicAceMap.get( "logicalRole" ).toString();
    boolean applyToTarget = Boolean.valueOf( magicAceMap.get( "applyToTarget" ).toString() );
    boolean applyToChildren = Boolean.valueOf( magicAceMap.get( "applyToChildren" ).toString() );
    boolean applyToAncestors = Boolean.valueOf( magicAceMap.get( "applyToAncestors" ).toString() );

    List<String> privilegeList = (List<String>) magicAceMap.get( "privileges" );
    List<Privilege> privileges = new ArrayList<Privilege>();
    for ( String privilegeToken : privilegeList ) {
      privileges.add( systemSession.getAccessControlManager().privilegeFromName( privilegeToken ) );
    }

    String[] exceptChildren = null;
    List<String> exceptChildrenArray = (List<String>) magicAceMap.get( "exceptChildren" );
    if ( exceptChildrenArray != null ) {
      exceptChildren = exceptChildrenArray.toArray( new String[0] );
    }

    return new MagicAceDefinition( path, logicalRole, privileges.toArray( new Privilege[0] ), applyToTarget,
        applyToChildren, applyToAncestors, exceptChildren );
  }
}
