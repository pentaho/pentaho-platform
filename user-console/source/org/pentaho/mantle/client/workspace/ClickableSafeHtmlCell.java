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

package org.pentaho.mantle.client.workspace;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * @author Rowell Belen
 */
public class ClickableSafeHtmlCell extends AbstractCell<SafeHtml> {

  public ClickableSafeHtmlCell() {
    super( "click" );
  }

  @Override
  public void onBrowserEvent( Context context, Element parent, SafeHtml value, NativeEvent event,
      ValueUpdater<SafeHtml> valueUpdater ) {

    // use default implementation for all events other than the click event
    super.onBrowserEvent( context, parent, value, event, valueUpdater );

    if ( "click".equals( event.getType() ) ) {

      // Ignore clicks that occur outside of the outermost element.
      EventTarget eventTarget = event.getEventTarget();
      if ( parent.getFirstChildElement().isOrHasChild( Element.as( eventTarget ) ) ) {
        onEnterKeyDown( context, parent, value, event, valueUpdater );
      }
    }
  }

  @Override
  protected void onEnterKeyDown( Context context, Element parent, SafeHtml value, NativeEvent event,
      ValueUpdater<SafeHtml> valueUpdater ) {
    if ( valueUpdater != null ) {
      valueUpdater.update( value );
    }
  }

  @Override
  public void render( Context context, SafeHtml value, SafeHtmlBuilder sb ) {
    if ( value != null ) {
      sb.append( value );
    }
  }
}
