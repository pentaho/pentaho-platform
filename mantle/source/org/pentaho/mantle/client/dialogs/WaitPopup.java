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
 */
package org.pentaho.mantle.client.dialogs;

import org.pentaho.gwt.widgets.client.dialogs.GlassPane;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class WaitPopup extends SimplePanel{

  static private WaitPopup instance = new WaitPopup();
  private static FocusPanel pageBackground = null;
  private static int clickCount = 0;
  
  private WaitPopup(){
    setStyleName("waitPopup"); //$NON-NLS-1$
    VerticalPanel vp = new VerticalPanel();
    Label lbl = new Label(Messages.getString("pleaseWait")); //$NON-NLS-1$
    lbl.setStyleName("waitPopup_title"); //$NON-NLS-1$
    vp.add(lbl);
    lbl = new Label(Messages.getString("waitMessage")); //$NON-NLS-1$
    lbl.setStyleName("waitPopup_msg"); //$NON-NLS-1$
    vp.add(lbl);
    vp.setStyleName("waitPopup_table"); //$NON-NLS-1$
    this.add(vp);
    
    if (pageBackground == null) {
      pageBackground = new FocusPanel();
      pageBackground.getElement().setId("pageBackground");//$NON-NLS-1$
      pageBackground.setHeight("100%"); //$NON-NLS-1$
      pageBackground.setWidth("100%"); //$NON-NLS-1$
      pageBackground.setStyleName("glasspane"); //$NON-NLS-1$
      pageBackground.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          clickCount++;
          if (clickCount > 1) {
            clickCount = 0;
            setVisible(false);
          }
        }
      });
      RootPanel.get().add(pageBackground, 0, 0);
      this.setVisible(false);
    }
  }
  
  public static WaitPopup getInstance(){
    return instance;
  }

  @Override
  public void setVisible(boolean visible) {
    try {
      super.setVisible(visible);
      pageBackground.setVisible(visible);

      if (visible) {
        getElement().getStyle().setDisplay(Display.BLOCK);
        pageBackground.getElement().getStyle().setDisplay(Display.BLOCK);
      } else {
        getElement().getStyle().setDisplay(Display.NONE);
        pageBackground.getElement().getStyle().setDisplay(Display.NONE);
      }
      
      // Notify listeners that this wait dialog is shown (hide pdfs, flash, etc.)
      if(visible){
        GlassPane.getInstance().show();
      } else {
        GlassPane.getInstance().hide();
      }
    } catch (Throwable t) {
    }
  }
  
  
}
