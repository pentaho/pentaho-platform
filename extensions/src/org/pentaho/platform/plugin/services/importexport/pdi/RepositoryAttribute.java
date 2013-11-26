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

package org.pentaho.platform.plugin.services.importexport.pdi;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryAttributeInterface;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.DataProperty;

import java.util.List;

public class RepositoryAttribute implements RepositoryAttributeInterface, java.io.Serializable {

  private static final long serialVersionUID = -5787096049770518000L; /* EESOURCE: UPDATE SERIALVERUID */

  private DataNode dataNode;
  private List<DatabaseMeta> databases;

  public RepositoryAttribute( DataNode dataNode, List<DatabaseMeta> databases ) {
    this.dataNode = dataNode;
    this.databases = databases;
  }

  public void setAttribute( String code, String value ) {
    dataNode.setProperty( code, value );
  }

  public String getAttributeString( String code ) {
    DataProperty property = dataNode.getProperty( code );
    if ( property != null ) {
      return property.getString();
    }
    return null;
  }

  public void setAttribute( String code, boolean value ) {
    dataNode.setProperty( code, value );
  }

  public boolean getAttributeBoolean( String code ) {
    DataProperty property = dataNode.getProperty( code );
    if ( property != null ) {
      return property.getBoolean();
    }
    return false;
  }

  public void setAttribute( String code, long value ) {
    dataNode.setProperty( code, value );
  }

  public long getAttributeInteger( String code ) {
    DataProperty property = dataNode.getProperty( code );
    if ( property != null ) {
      return property.getLong();
    }
    return 0L;
  }

  public void setAttribute( String code, DatabaseMeta databaseMeta ) {
    dataNode.setProperty( code, databaseMeta.getObjectId().getId() );
  }

  public DatabaseMeta getAttributeDatabaseMeta( String code ) {
    DataProperty property = dataNode.getProperty( code );
    if ( property == null || Const.isEmpty( property.getString() ) ) {
      return null;
    }
    ObjectId id = new StringObjectId( property.getString() );
    return DatabaseMeta.findDatabase( databases, id );
  }
}
