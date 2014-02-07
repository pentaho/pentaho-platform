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

import com.google.gwt.i18n.client.DateTimeFormat;
import org.pentaho.mantle.client.dialogs.scheduling.RunOnceEditor;
import org.pentaho.mantle.client.messages.Messages;

import java.util.Date;

public class RunOnceEditorValidator implements IUiValidator {

  private RunOnceEditor editor = null;

  public RunOnceEditorValidator( RunOnceEditor editor ) {
    this.editor = editor;
  }

  public boolean isValid() {
    boolean isValid = true;
    if ( null == editor.getStartDate() ) {
      isValid = false;
    } else {
      final DateTimeFormat format = DateTimeFormat.getFormat( "MM-dd-yyyy" ); //$NON-NLS-1$
      final String date = format.format( editor.getStartDate() );
      final String dateTime = date + " " + editor.getStartTime(); //$NON-NLS-1$

      if ( DateTimeFormat.getFormat( "MM-dd-yyyy hh:mm:ss a" ).parse( dateTime ).before( new Date() ) ) { //$NON-NLS-1$
        isValid = false;
        editor.setStartDateError( Messages.getString( "datePastNotAvailable" ) ); //$NON-NLS-1$
      }
    }
    return isValid;
  }

  public void clear() {
    editor.setStartDateError( null );
  }
}
