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
 * @created Jun 30, 2008 
 * @author wseyler
 */
package org.pentaho.mantle.client.solutionbrowser.fileproperties;

import java.util.ArrayList;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.objects.SubscriptionSchedule;
import org.pentaho.mantle.client.objects.SubscriptionState;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.client.solutionbrowser.IFileSummary;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.Document;

/**
 * @author wseyler
 * 
 */
public class SubscriptionsPanel extends VerticalPanel implements IFileModifier {
  private boolean dirty = false;
  private boolean wasEnabled = false;
  private CheckBox enableSubscriptions = new CheckBox(Messages.getString("enableSubscription")); //$NON-NLS-1$

  private ListBox availableLB = new ListBox(true);
  private ListBox appliedLB = new ListBox(true);

  private Button moveRightBtn = new Button();
  private Button moveLeftBtn = new Button();
  private Button moveAllRightBtn = new Button();
  private Button moveAllLeftBtn = new Button();

  private IFileSummary fileSummary = null;

  public SubscriptionsPanel() {
    layout();
    dirty = false;

    moveRightBtn.setStylePrimaryName("pentaho-button");
    moveLeftBtn.setStylePrimaryName("pentaho-button");
    moveAllRightBtn.setStylePrimaryName("pentaho-button");
    moveAllLeftBtn.setStylePrimaryName("pentaho-button");
    
    enableSubscriptions.getElement().setId("subscriptionPanelEnableCheck");
    availableLB.getElement().setId("subscriptionPanelAvailableList");
    appliedLB.getElement().setId("subscriptionPanelAppliedList");
    moveRightBtn.getElement().setId("subscriptionPanelMoveRightButton");
    moveLeftBtn.getElement().setId("subscriptionPanelMoveLeftButton");
    moveAllRightBtn.getElement().setId("subscriptionPanelMoveAllRightButton");
    moveAllLeftBtn.getElement().setId("subscriptionPanelMoveAllLeftButton");

  }

  public void layout() {
    this.setSize("100%", "100%"); //$NON-NLS-1$//$NON-NLS-2$
    enableSubscriptions.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        dirty = true;
        updateControls();
      }
    });
    this.add(enableSubscriptions);
    this.setSpacing(10);

    CaptionPanel scheduleCaptionPanel = new CaptionPanel(Messages.getString("schedule")); //$NON-NLS-1$

    FlexTable schedulePanel = new FlexTable();
    schedulePanel.setSize("100%", "100%"); //$NON-NLS-1$//$NON-NLS-2$

    VerticalPanel availablePanel = new VerticalPanel();
    availablePanel.add(new Label(Messages.getString("available"))); //$NON-NLS-1$
    availablePanel.add(availableLB);
    availableLB.setVisibleItemCount(9);
    availableLB.setWidth("100%"); //$NON-NLS-1$

    VerticalPanel appliedPanel = new VerticalPanel();
    appliedPanel.add(new Label(Messages.getString("current"))); //$NON-NLS-1$
    appliedPanel.add(appliedLB);
    appliedLB.setVisibleItemCount(9);
    appliedLB.setWidth("100%"); //$NON-NLS-1$

    // Add the buttons
    VerticalPanel buttonGrid = new VerticalPanel();
    buttonGrid.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    buttonGrid.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
    buttonGrid.setSpacing(2);
    moveRightBtn.setText(">"); //$NON-NLS-1$
    moveRightBtn.setTitle(Messages.getString("add")); //$NON-NLS-1$
    moveRightBtn.setWidth("30px"); //$NON-NLS-1$
    moveRightBtn.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        moveSelectedToRight();
      }
    });
    buttonGrid.add(moveRightBtn);
    moveAllRightBtn.setText(">>"); //$NON-NLS-1$
    moveAllRightBtn.setTitle(Messages.getString("addAll")); //$NON-NLS-1$
    moveAllRightBtn.setWidth("30px"); //$NON-NLS-1$
    moveAllRightBtn.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        moveAllToRight();
      }
    });
    buttonGrid.add(moveAllRightBtn);
    moveLeftBtn.setText("<"); //$NON-NLS-1$
    moveLeftBtn.setTitle(Messages.getString("remove")); //$NON-NLS-1$
    moveLeftBtn.setWidth("30px"); //$NON-NLS-1$
    moveLeftBtn.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        moveSelectedToLeft();
      }
    });

    buttonGrid.add(moveLeftBtn);
    moveAllLeftBtn.setText("<<"); //$NON-NLS-1$
    moveAllLeftBtn.setTitle(Messages.getString("removeAll")); //$NON-NLS-1$
    moveAllLeftBtn.setWidth("30px"); //$NON-NLS-1$
    moveAllLeftBtn.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        moveAllToLeft();
      }
    });

    buttonGrid.add(moveAllLeftBtn);

    // Add the list boxes
    schedulePanel.setWidget(0, 0, availablePanel);
    schedulePanel.setWidget(0, 1, buttonGrid);
    schedulePanel.setWidget(0, 2, appliedPanel);

    scheduleCaptionPanel.add(schedulePanel);
    this.add(scheduleCaptionPanel);
    this.setCellHorizontalAlignment(schedulePanel, HasHorizontalAlignment.ALIGN_CENTER);

    availablePanel.setWidth("100%"); //$NON-NLS-1$
    buttonGrid.setWidth("100%"); //$NON-NLS-1$
    appliedPanel.setWidth("100%"); //$NON-NLS-1$
    schedulePanel.getCellFormatter().setWidth(0, 0, "50%"); //$NON-NLS-1$
    schedulePanel.getCellFormatter().setWidth(0, 2, "50%"); //$NON-NLS-1$

  }

  protected void moveSelectedToRight() {
    moveItems(availableLB, appliedLB, false);
  }

  protected void moveAllToRight() {
    moveItems(availableLB, appliedLB, true);
  }

  protected void moveSelectedToLeft() {
    moveItems(appliedLB, availableLB, false);
  }

  protected void moveAllToLeft() {
    moveItems(appliedLB, availableLB, true);
  }

  protected void moveItems(ListBox srcLB, ListBox destLB, boolean moveAll) {
    dirty = true;
    int itemCount = srcLB.getItemCount();
    int srcSelectionIndex = srcLB.getSelectedIndex();

    deselectAll(destLB);
    for (int i = 0; i < itemCount; i++) {
      if (moveAll || srcLB.isItemSelected(i)) {
        String value = srcLB.getValue(i);
        String name = srcLB.getItemText(i);
        destLB.addItem(name, value); // adds it to the bottom of the destination List
        destLB.setItemSelected(destLB.getItemCount() - 1, true);
      }
    }
    removeItems(srcLB, moveAll);
    if (srcLB.getItemCount() > 0) {
      if ((srcLB.getItemCount() - 1) < srcSelectionIndex) {
        srcLB.setSelectedIndex(srcLB.getItemCount() - 1);
      } else {
        srcLB.setSelectedIndex(srcSelectionIndex);
      }
    }
  }

  private void removeItems(ListBox targetListBox, boolean removeAll) {
    int itemCount = targetListBox.getItemCount();
    for (int i = itemCount - 1; i >= 0; i--) {
      if (removeAll || targetListBox.isItemSelected(i)) {
        targetListBox.removeItem(i);
      }
    }
  }

  /**
   * @param destLB
   */
  private void deselectAll(ListBox targetListBox) {
    for (int i = 0; i < targetListBox.getItemCount(); i++) {
      targetListBox.setItemSelected(i, false);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.mantle.client.solutionbrowser.fileproperties.IFileModifier#apply()
   */
  public void apply() {
    if (dirty) {
      if ((wasEnabled && !enableSubscriptions.getValue() && Window.confirm(Messages.getString("appliedSchedulesWillBeLost"))) || (!wasEnabled)) { // We're
        AsyncCallback<Void> callback = new AsyncCallback<Void>() {

          public void onFailure(Throwable caught) {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), caught.toString(), false, false, true); //$NON-NLS-1$
            dialogBox.center();
          }

          public void onSuccess(Void nothing) {
            dirty = false; // I don't know if this even gets back here
          }

        };
        ArrayList<SubscriptionSchedule> currentSchedules = new ArrayList<SubscriptionSchedule>();
        for (int i = 0; i < appliedLB.getItemCount(); i++) {
          SubscriptionSchedule subSchedule = new SubscriptionSchedule();
          subSchedule.title = appliedLB.getItemText(i);
          subSchedule.id = appliedLB.getValue(i);
          currentSchedules.add(subSchedule);
        }
        MantleServiceCache.getService().setSubscriptions(fileSummary.getSolution(), fileSummary.getPath(), fileSummary.getName(), enableSubscriptions.isChecked(), currentSchedules, callback); //$NON-NLS-1$
      }
    }
  }

  public void init(RepositoryFile fileSummary, Document fileInfo) {
    
  }
  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.mantle.client.solutionbrowser.fileproperties.IFileModifier#init(org.pentaho.mantle.client.solutionbrowser.FileItem,
   * org.pentaho.mantle.client.objects.SolutionFileInfo)
   */
  public void init(IFileSummary fileSummary, SolutionFileInfo fileInfo) {
    this.fileSummary = fileSummary;

    updateState();
    wasEnabled = enableSubscriptions.getValue();
  }

  protected void updateControls() {
    availableLB.setEnabled(enableSubscriptions.getValue());
    appliedLB.setEnabled(enableSubscriptions.getValue());
    moveAllLeftBtn.setEnabled(enableSubscriptions.getValue());
    moveAllRightBtn.setEnabled(enableSubscriptions.getValue());
    moveLeftBtn.setEnabled(enableSubscriptions.getValue());
    moveRightBtn.setEnabled(enableSubscriptions.getValue());
  }

  /**
   * 
   */
  private void updateState() {
    AsyncCallback<SubscriptionState> callBack = new AsyncCallback<SubscriptionState>() {

      public void onSuccess(SubscriptionState state) {
        enableSubscriptions.setValue(state.subscriptionsEnabled);
        for (SubscriptionSchedule schedule : state.availableSchedules) {
          availableLB.addItem(schedule.title, schedule.id);
        }
        for (SubscriptionSchedule schedule : state.appliedSchedules) {
          appliedLB.addItem(schedule.title, schedule.id);
        }
        updateControls();
      }

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), caught.toString(), false, false, true); //$NON-NLS-1$
        dialogBox.center();
      }

    };
    MantleServiceCache.getService().getSubscriptionState(fileSummary.getSolution(), fileSummary.getPath(), fileSummary.getName(), callBack); //$NON-NLS-1$
  }

}
