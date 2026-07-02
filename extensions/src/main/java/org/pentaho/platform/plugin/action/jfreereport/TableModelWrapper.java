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


package org.pentaho.platform.plugin.action.jfreereport;

import org.pentaho.reporting.engine.classic.core.util.CloseableTableModel;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * @deprecated This class is no longer used.
 */
@Deprecated
public class TableModelWrapper implements TableModel, CloseableTableModel {

  private TableModel tableModel;

  public TableModelWrapper( final TableModel tableModel ) {
    this.tableModel = tableModel;
  }

  public int getColumnCount() {
    return tableModel.getColumnCount();
  }

  public int getRowCount() {
    return tableModel.getRowCount();
  }

  public boolean isCellEditable( final int rowIndex, final int columnIndex ) {
    return tableModel.isCellEditable( rowIndex, columnIndex );
  }

  public Class getColumnClass( final int columnIndex ) {
    return tableModel.getColumnClass( columnIndex );
  }

  public Object getValueAt( final int rowIndex, final int columnIndex ) {
    return tableModel.getValueAt( rowIndex, columnIndex );
  }

  public void setValueAt( final Object aValue, final int rowIndex, final int columnIndex ) {
    tableModel.setValueAt( aValue, rowIndex, columnIndex );
  }

  public String getColumnName( final int columnIndex ) {
    return tableModel.getColumnName( columnIndex );
  }

  public void addTableModelListener( final TableModelListener l ) {
    tableModel.addTableModelListener( l );
  }

  public void removeTableModelListener( final TableModelListener l ) {
    tableModel.removeTableModelListener( l );
  }

  public void close() {
    tableModel = null;
  }

}
