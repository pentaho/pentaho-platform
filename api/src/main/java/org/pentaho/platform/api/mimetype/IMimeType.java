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
* Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
*/
package org.pentaho.platform.api.mimetype;

import java.util.List;

import org.pentaho.platform.api.repository2.unified.Converter;

public interface IMimeType {
  
  public String getName();

  public void setName( String name );
  
  public List<String> getExtensions();

  public boolean isHidden();

  public void setHidden( boolean hidden );

  public boolean isLocale();

  public void setLocale( boolean locale );

  public boolean isVersionEnabled();

  public void setVersionEnabled( boolean versionEnabled );

  public boolean isVersionCommentEnabled();

  public void setVersionCommentEnabled( boolean versionCommentEnabled );
  
  public Converter getConverter();

  public void setConverter( Converter converter );

  public void setExtensions( List<String> extensions );

  public String toString();

}
