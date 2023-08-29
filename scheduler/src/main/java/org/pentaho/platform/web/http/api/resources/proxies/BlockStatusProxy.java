/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources.proxies;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author wseyler
 * 
 */
@XmlRootElement
public class BlockStatusProxy {
  Boolean totallyBlocked;
  Boolean partiallyBlocked;

  public BlockStatusProxy() {
    this( false, false );
  }

  public BlockStatusProxy( Boolean totallyBlocked, Boolean partiallyBlocked ) {
    super();
    this.totallyBlocked = totallyBlocked;
    this.partiallyBlocked = partiallyBlocked;
  }

  public Boolean getTotallyBlocked() {
    return totallyBlocked;
  }

  public void setTotallyBlocked( Boolean totallyBlocked ) {
    this.totallyBlocked = totallyBlocked;
  }

  public Boolean getPartiallyBlocked() {
    return partiallyBlocked;
  }

  public void setPartiallyBlocked( Boolean partiallyBlocked ) {
    this.partiallyBlocked = partiallyBlocked;
  }

  @Override public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }

    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    BlockStatusProxy that = (BlockStatusProxy) o;

    return new EqualsBuilder()
      .append( totallyBlocked, that.totallyBlocked )
      .append( partiallyBlocked, that.partiallyBlocked )
      .isEquals();
  }

  @Override public int hashCode() {
    return new HashCodeBuilder( 17, 37 )
      .append( totallyBlocked )
      .append( partiallyBlocked )
      .toHashCode();
  }

  @Override public String toString() {
    return new ToStringBuilder( this )
      .append( "partiallyBlocked", partiallyBlocked )
      .append( "totallyBlocked", totallyBlocked )
      .toString();
  }
}
