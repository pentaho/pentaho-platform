/*!
 * Copyright 2010 - 2013 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jackrabbit.core.security.authorization.acl;

import javax.jcr.security.Privilege;
import java.util.Arrays;

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
}
