/*
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
 * Copyright 2014 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.plugin.action.olap;

import mondrian.olap.Util;
import mondrian.xmla.XmlaHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.OlapStatement;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.MetadataElement;
import org.olap4j.metadata.Property;
import org.olap4j.metadata.Schema;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Access to XmlaExtra inside OSGI where M4 is running is tricky.  This class will delegate to the correct classes
 * using the reflection api.  Note that only the methods used inside extensions or Analyzer are implemented.  More can
 * be implemented as necessary.
 */
@SuppressWarnings( "unchecked" )
public class PlatformXmlaExtra implements XmlaHandler.XmlaExtra {
  private static final String MONDRIAN_XMLA_EXTRA_NAME = "mondrian.xmla.XmlaHandler$XmlaExtra";
  private static final String MONDRIAN_ROLAP_CONNECTION_NAME = "mondrian.rolap.RolapConnection";
  private static final Log logger = LogFactory.getLog( PlatformXmlaExtra.class );

  private Class<?> clazz;
  private Object xmlaExtra;

  public static XmlaHandler.XmlaExtra unwrapXmlaExtra( final OlapConnection olap4jConn )
    throws SQLException {
    try {
      if ( inCurrentClassLoader( olap4jConn ) ) {
        return olap4jConn.unwrap( XmlaHandler.XmlaExtra.class );
      } else if ( isMondrianConnection( olap4jConn ) ) {
        PlatformXmlaExtra platformXmlaExtra = new PlatformXmlaExtra( olap4jConn );
        if ( platformXmlaExtra.isValid() ) {
          return platformXmlaExtra;
        }
      }
    } catch ( ClassNotFoundException e ) {
      logger.warn( "Problem unwrapping XmlaExtra", e );
    }
    return null;
  }

  private boolean isValid() {
    return clazz != null && xmlaExtra != null;
  }

  private static boolean isMondrianConnection( final OlapConnection olap4jConn )
    throws SQLException, ClassNotFoundException {
    return olap4jConn.isWrapperFor(
      olap4jConn.getClass().getClassLoader().loadClass( MONDRIAN_ROLAP_CONNECTION_NAME ) );
  }

  private static boolean inCurrentClassLoader( final OlapConnection olap4jConn ) throws ClassNotFoundException {
    return olap4jConn.getClass().getClassLoader().loadClass( MONDRIAN_XMLA_EXTRA_NAME )
      .equals( XmlaHandler.XmlaExtra.class );
  }

  private PlatformXmlaExtra( final OlapConnection olap4jConn ) {
    try {
      this.clazz = olap4jConn.getClass().getClassLoader().loadClass( MONDRIAN_XMLA_EXTRA_NAME );
      this.xmlaExtra = olap4jConn.unwrap( clazz );
    } catch ( Exception e ) {
      logger.warn( "Unable to retrieve XmlaExtra", e );
    }
  }

  public Object invoke( String methodName, Class<?> paramType, Object param, Object defaultValue ) {
    try {
      return clazz.getMethod( methodName,  paramType ).invoke( xmlaExtra, param );
    } catch ( Exception e ) {
      //really hope we never get here given all the checks during object construction
      logger.warn( "Using default value for " + methodName, e );
    }
    return defaultValue;
  }

  @Override
  public ResultSet executeDrillthrough( final OlapStatement olapStatement, final String s, final boolean b,
                                        final String s2, final int[] ints )
    throws SQLException {
    throw new UnsupportedOperationException(  );
  }

  @Override public void setPreferList( final OlapConnection olapConnection ) {
    throw new UnsupportedOperationException(  );
  }

  @Override public Date getSchemaLoadDate( final Schema schema ) {
    throw new UnsupportedOperationException(  );
  }

  @Override public int getLevelCardinality( final Level level ) throws OlapException {
    throw new UnsupportedOperationException(  );
  }

  @Override
  public void getSchemaFunctionList( final List<FunctionDefinition> functionDefinitions, final Schema schema,
                                     final Util.Functor1<Boolean, String> booleanStringFunctor1 ) {
    throw new UnsupportedOperationException(  );
  }

  @Override public int getHierarchyCardinality( final Hierarchy hierarchy ) throws OlapException {
    throw new UnsupportedOperationException(  );
  }

  @Override public int getHierarchyStructure( final Hierarchy hierarchy ) {
    throw new UnsupportedOperationException(  );
  }

  @Override public boolean isHierarchyParentChild( final Hierarchy hierarchy ) {
    return (Boolean) invoke( "isHierarchyParentChild", Hierarchy.class, hierarchy, false );
  }

  @Override public int getMeasureAggregator( final Member member ) {
    throw new UnsupportedOperationException();
  }

  @Override public void checkMemberOrdinal( final Member member ) throws OlapException {
    throw new UnsupportedOperationException(  );
  }

  @Override public boolean shouldReturnCellProperty( final CellSet cellSet, final Property property, final boolean b ) {
    throw new UnsupportedOperationException(  );
  }

  @Override public List<String> getSchemaRoleNames( final Schema schema ) {
    throw new UnsupportedOperationException(  );
  }

  @Override public String getSchemaId( final Schema schema ) {
    return (String) invoke( "getSchemaId", Schema.class, schema, "" );
  }

  @Override public String getCubeType( final Cube cube ) {
    throw new UnsupportedOperationException(  );
  }

  @Override public boolean isLevelUnique( final Level level ) {
    throw new UnsupportedOperationException(  );
  }

  @Override public List<Property> getLevelProperties( final Level level ) {
    return (List<Property>) invoke( "getLevelProperties", Level.class, level, Collections.emptyList() );
  }

  @Override public boolean isPropertyInternal( final Property property ) {
    throw new UnsupportedOperationException(  );
  }

  @Override public List<Map<String, Object>> getDataSources( final OlapConnection olapConnection )
    throws OlapException {
    throw new UnsupportedOperationException(  );
  }

  @Override public Map<String, Object> getAnnotationMap( final MetadataElement metadataElement ) throws SQLException {
    return (Map<String, Object>) invoke(
      "getAnnotationMap", MetadataElement.class, metadataElement, Collections.emptyMap() );
  }

  @Override public boolean canDrillThrough( final Cell cell ) {
    return (Boolean) invoke( "canDrillThrough", Cell.class, cell, false );
  }

  @Override public int getDrillThroughCount( final Cell cell ) {
    return (Integer) invoke( "getDrillThroughCount", Cell.class, cell, 0 );

  }

  @Override public void flushSchemaCache( final OlapConnection olapConnection ) throws OlapException {
    invoke( "flushSchemaCache", OlapConnection.class, olapConnection, null );
  }

  @Override public Object getMemberKey( final Member member ) throws OlapException {
    return invoke( "getMemberKey", Member.class, member, null );
  }

  @Override public Object getOrderKey( final Member member ) throws OlapException {
    return invoke( "getOrderKey", Member.class, member, null );
  }
}
