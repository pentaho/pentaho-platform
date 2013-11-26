/*
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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.webservices;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@XmlRootElement
public class RepositoryFileTreeDto implements Serializable {
  private static final long serialVersionUID = -4222089807149018286L;

  RepositoryFileDto file;

  List<RepositoryFileTreeDto> children;

  public RepositoryFileTreeDto() {
  }

  public RepositoryFileDto getFile() {
    return file;
  }

  public void setFile( RepositoryFileDto file ) {
    this.file = file;
  }

  public List<RepositoryFileTreeDto> getChildren() {
    return children;
  }

  public void setChildren( List<RepositoryFileTreeDto> children ) {
    this.children = children;
  }

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return "RepositoryFileTreeDto [file=" + file + ", children=" + children + "]";
  }

  public void afterUnmarshal( Unmarshaller unmarshaller, Object parent ) {
    if ( children == null ) {
      children = Collections.<RepositoryFileTreeDto>emptyList();
    }
  }
}
