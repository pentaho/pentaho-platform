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
