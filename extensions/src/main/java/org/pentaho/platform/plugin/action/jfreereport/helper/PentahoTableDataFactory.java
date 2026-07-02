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


package org.pentaho.platform.plugin.action.jfreereport.helper;

import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.data.IPreparedComponent;
import org.pentaho.reporting.engine.classic.core.AbstractDataFactory;
import org.pentaho.reporting.engine.classic.core.DataRow;
import org.pentaho.reporting.engine.classic.core.util.CloseableTableModel;

import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The PentahoTableDataFactory class implements JFreeReport's data factory and manages the TableModels provided to
 * JFreeReport. The primary difference between this class and JFreeReport's standard TableDataFactory is the
 * "getTableIterator" method, which allows the Platform to clean up and table model resources after their use. Also, we
 * support Pentaho's IPreparedComponent interface which allows a prepared component to generate a result set when
 * requested.
 * 
 * @author Will Gorman
 */
public class PentahoTableDataFactory extends AbstractDataFactory {

  private static final long serialVersionUID = -33882557376609479L;

  /** map of tables to keep track of */
  private HashMap<String, TableModel> tables;

  private HashMap<String, IPreparedComponent> components;

  /**
   * default constructor
   * 
   */
  public PentahoTableDataFactory() {
    this.tables = new HashMap<String, TableModel>();
    this.components = new HashMap<String, IPreparedComponent>();
  }

  /**
   * constructor with one time call to addTable for convenience.
   * 
   * @param name
   *          table name
   * @param tableModel
   *          instance of table model
   */
  public PentahoTableDataFactory( final String name, final TableModel tableModel ) {
    this();
    addTable( name, tableModel );
  }

  /**
   * add a table to the map
   * 
   * @param name
   *          table name
   * @param tableModel
   *          instance of table model
   */
  public void addTable( final String name, final TableModel tableModel ) {
    tables.put( name, tableModel );
  }

  /**
   * add a prepared component to the map
   * 
   * @param name
   *          prepared component name
   * @param component
   *          instance of prepared component
   */
  public void addPreparedComponent( final String name, final IPreparedComponent component ) {
    components.put( name, component );
  }

  /**
   * remove a table from the map
   * 
   * @param name
   *          table name
   */
  public void removeTable( final String name ) {
    tables.remove( name );
  }

  /**
   * Queries a datasource. The string 'query' defines the name of the query. The Parameterset given here may contain
   * more data than actually needed.
   * <p/>
   * The dataset may change between two calls, do not assume anything!
   * 
   * @param query
   *          the name of the table.
   * @param parameters
   *          are ignored for this factory.
   * @return the report data or null.
   */
  public TableModel queryData( final String query, final DataRow parameters ) {
    TableModel model = tables.get( query );
    if ( model == null ) {
      final IPreparedComponent component = components.get( query );
      if ( component != null ) {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        if ( parameters != null ) {
          String[] columnNames = parameters.getColumnNames();
          for ( String columnName : columnNames ) {
            map.put( columnName, parameters.get( columnName ) );
          }
        }
        final IPentahoResultSet rs = component.executePrepared( map );
        model = new PentahoTableModel( rs );
      }
    }
    return model;
  }

  public void close() {
    // this gets called too frequently for the old implementation
    // the reporting engine seems to call this method during each stage
    // of report generation, breaking our data
  }

  public void closeTables() {
    // this is the old implementation of 'close'
    for ( TableModel model : tables.values() ) {
      if ( model instanceof CloseableTableModel ) {
        final CloseableTableModel closeableTableModel = (CloseableTableModel) model;
        closeableTableModel.close();
      }
    }
    tables.clear();
  }

  /**
   * Derives a freshly initialized report data factory, which is independend of the original data factory. Opening or
   * Closing one data factory must not affect the other factories.
   * 
   * @return
   */
  public PentahoTableDataFactory derive() {
    return clone();
  }

  @Override
  public PentahoTableDataFactory clone() {
    final PentahoTableDataFactory dataFactory = (PentahoTableDataFactory) super.clone();
    dataFactory.tables = (HashMap) tables.clone();
    dataFactory.components = (HashMap) components.clone();
    return dataFactory;
  }

  public String[] getQueryNames() {
    final List<String> queryNameList = new ArrayList<String>();
    queryNameList.addAll( tables.keySet() );
    queryNameList.addAll( components.keySet() );
    final String[] queryNames = queryNameList.toArray( new String[queryNameList.size()] );
    return queryNames;
  }

  public boolean isQueryExecutable( String query, DataRow parameters ) {
    boolean queryExecutable = tables.containsKey( query ) || components.containsKey( query );
    return queryExecutable;
  }

  public void cancelRunningQuery() {
  }
}
