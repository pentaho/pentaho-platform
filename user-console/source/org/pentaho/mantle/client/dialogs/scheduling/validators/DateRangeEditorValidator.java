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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 *
 * Created  
 * @author Steven Barkdull
 */
package org.pentaho.mantle.client.dialogs.scheduling.validators;

import org.pentaho.gwt.widgets.client.controls.DateRangeEditor;

public class DateRangeEditorValidator implements IUiValidator {

  private DateRangeEditor dateRangeEditor = null;
  public DateRangeEditorValidator( DateRangeEditor dateRangeEditor ) {
    this.dateRangeEditor = dateRangeEditor; 
  }
  
  public boolean isValid() {
    boolean isValid = true;
    
    if ( null == dateRangeEditor.getStartDate() ) {
      isValid = false;
    }

    if ( dateRangeEditor.isEndBy() 
        && ( null == dateRangeEditor.getEndDate() ) ) {
      isValid = false;
    }
    return isValid;
  }

  public void clear() {
    dateRangeEditor.setStartDateError( null );
    dateRangeEditor.setEndByError( null );
  }
}
