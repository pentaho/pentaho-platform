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

package org.pentaho.mantle.client.dialogs.scheduling.validators;

import org.pentaho.gwt.widgets.client.utils.CronExpression;
import org.pentaho.mantle.client.dialogs.scheduling.CronEditor;

public class CronEditorValidator implements IUiValidator {

  private CronEditor editor = null;
  private DateRangeEditorValidator dateRangeEditorValidator = null;

  public CronEditorValidator( CronEditor editor ) {
    this.editor = editor;
    this.dateRangeEditorValidator = new DateRangeEditorValidator( editor.getDateRangeEditor() );
  }

  public boolean isValid() {
    boolean isValid = true;

    if ( !CronExpression.isValidExpression( editor.getCronString() ) ) {
      isValid = false;
    }
    isValid &= dateRangeEditorValidator.isValid();

    return isValid;
  }

  public void clear() {
    editor.setCronError( null );
    dateRangeEditorValidator.clear();
  }
}
