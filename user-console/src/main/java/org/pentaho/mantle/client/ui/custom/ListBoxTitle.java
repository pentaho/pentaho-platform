/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.mantle.client.ui.custom;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.i18n.client.HasDirection;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Listbox wrapper, sets title attribute of the option element
 */
public class ListBoxTitle extends ListBox {

  public ListBoxTitle() {
    super();
  }

  public ListBoxTitle( boolean isMultipleSelect ) {
    super( isMultipleSelect );
  }

  protected ListBoxTitle( Element element ) {
    super( element );
  }

  @Override
  public void insertItem( String item, HasDirection.Direction dir, String value, int index ) {
    SelectElement select = (SelectElement) this.getElement().cast();
    OptionElement option = Document.get().createOptionElement();
    this.setOptionText( option, item, dir );
    option.setValue( value );
    option.setTitle( value );
    int itemCount = select.getLength();
    if ( index < 0 || index > itemCount ) {
      index = itemCount;
    }

    if ( index == itemCount ) {
      select.add( option, (OptionElement) null );
    } else {
      OptionElement before = (OptionElement) select.getOptions().getItem( index );
      select.add( option, before );
    }
  }
}
