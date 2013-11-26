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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.security.userroledao;

import java.io.Serializable;

public class PentahoUserRoleMapping {

  private Id id;

  public Id getId() {
    return id;
  }

  public void setId( Id id ) {
    this.id = id;
  }

  public static class Id implements Serializable {
    private static final long serialVersionUID = -2387185346376315677L;

    private String user;

    private String role;

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ( ( role == null ) ? 0 : role.hashCode() );
      result = prime * result + ( ( user == null ) ? 0 : user.hashCode() );
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
      Id other = (Id) obj;
      if ( role == null ) {
        if ( other.role != null ) {
          return false;
        }
      } else if ( !role.equals( other.role ) ) {
        return false;
      }
      if ( user == null ) {
        if ( other.user != null ) {
          return false;
        }
      } else if ( !user.equals( other.user ) ) {
        return false;
      }
      return true;
    }

    public String getUser() {
      return user;
    }

    public String getRole() {
      return role;
    }

    public void setUser( String user ) {
      this.user = user;
    }

    public void setRole( String role ) {
      this.role = role;
    }

  }

}
