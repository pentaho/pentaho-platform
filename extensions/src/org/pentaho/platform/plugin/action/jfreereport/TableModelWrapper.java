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
