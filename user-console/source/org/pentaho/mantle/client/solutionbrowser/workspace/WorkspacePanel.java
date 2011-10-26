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
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.solutionbrowser.workspace;

import java.util.ArrayList;
import java.util.Iterator;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.JobDetail;
import org.pentaho.mantle.client.objects.JobSchedule;
import org.pentaho.mantle.client.objects.SimpleMessageException;
import org.pentaho.mantle.client.objects.WorkspaceContent;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.login.client.MantleLoginDialog;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class WorkspacePanel extends ScrollPanel {
  private static final int WAITING = 0;
  private static final int COMPLETE = 1;
  private static final int MYSCHEDULES = 2;
  private static final int ALLSCHEDULES = 3;
  private static final String DELETE = "delete"; //$NON-NLS-1$

  private DisclosurePanel allScheduledContentDisclosure = new DisclosurePanel(MantleImages.images.disclosurePanelOpen(),
      MantleImages.images.disclosurePanelClosed(), Messages.getString("allSchedulesAdminOnly")); //$NON-NLS-1$
  private DisclosurePanel subscriptionsContentDisclosure = new DisclosurePanel(MantleImages.images.disclosurePanelOpen(),
      MantleImages.images.disclosurePanelClosed(), Messages.getString("publicSchedules")); //$NON-NLS-1$
  private DisclosurePanel myScheduledContentDisclosure = new DisclosurePanel(MantleImages.images.disclosurePanelOpen(),
      MantleImages.images.disclosurePanelClosed(), Messages.getString("mySchedules")); //$NON-NLS-1$
  private DisclosurePanel waitingContentDisclosure = new DisclosurePanel(MantleImages.images.disclosurePanelOpen(),
      MantleImages.images.disclosurePanelClosed(), Messages.getString("waiting")); //$NON-NLS-1$
  private DisclosurePanel completedContentDisclosure = new DisclosurePanel(MantleImages.images.disclosurePanelOpen(),
      MantleImages.images.disclosurePanelClosed(), Messages.getString("complete")); //$NON-NLS-1$

  private FlexTable allScheduledContentTable;
  private FlexTable subscriptionsContentTable;
  private FlexTable myScheduledContentTable;
  private FlexTable waitingContentTable;
  private FlexTable completedContentTable;
  private FlexTable workspaceTable = new FlexTable();

  public WorkspacePanel(boolean isAdministrator) {
    getElement().setAttribute("id", "workspacePanel");
    setStyleName("workspacePanel");
    allScheduledContentDisclosure.setOpen(false);
    subscriptionsContentDisclosure.setOpen(false);
    myScheduledContentDisclosure.setOpen(false);
    waitingContentDisclosure.setOpen(false);
    completedContentDisclosure.setOpen(false);
    buildScheduledAndCompletedContentPanel(isAdministrator);
  }

  private FlexTable buildEmptyBackgroundItemTable(int tableType) {
    FlexTable table = new FlexTable();
    table.setWidth("100%"); //$NON-NLS-1$
    table.setStyleName("backgroundContentTable"); //$NON-NLS-1$
    table.setWidget(0, 0, new Label(Messages.getString("name"))); //$NON-NLS-1$
    table.setWidget(0, 1, new Label(Messages.getString("date"))); //$NON-NLS-1$
    if (tableType == COMPLETE) {
      table.setWidget(0, 2, new Label(Messages.getString("size"))); //$NON-NLS-1$
      table.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_RIGHT);
      table.setWidget(0, 3, new Label(Messages.getString("type"))); //$NON-NLS-1$
      table.setWidget(0, 4, new Label(Messages.getString("actions"))); //$NON-NLS-1$
      table.getCellFormatter().setStyleName(0, 0, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
      table.getCellFormatter().setStyleName(0, 1, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
      table.getCellFormatter().setStyleName(0, 2, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
      table.getCellFormatter().setStyleName(0, 3, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
      table.getCellFormatter().setStyleName(0, 4, "backgroundContentHeaderTableCellRight"); //$NON-NLS-1$
    } else {
      table.setWidget(0, 2, new Label(Messages.getString("actions"))); //$NON-NLS-1$
      table.getCellFormatter().setStyleName(0, 0, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
      table.getCellFormatter().setStyleName(0, 1, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
      table.getCellFormatter().setStyleName(0, 2, "backgroundContentHeaderTableCellRight"); //$NON-NLS-1$
    }
    return table;
  }

  private FlexTable buildEmptyScheduleTable() {
    FlexTable table = new FlexTable();
    table.setWidth("100%"); //$NON-NLS-1$
    table.setStyleName("backgroundContentTable"); //$NON-NLS-1$
    table.setWidget(0, 0, new Label(Messages.getString("jobName"))); //$NON-NLS-1$
    table.setWidget(0, 1, new Label(Messages.getString("jobGroup"))); //$NON-NLS-1$
    table.setWidget(0, 2, new Label(Messages.getString("description"))); //$NON-NLS-1$
    table.setWidget(0, 3, new Label(Messages.getString("lastRunNextRun"))); //$NON-NLS-1$
    table.setWidget(0, 4, new Label(Messages.getString("state"))); //$NON-NLS-1$
    table.setWidget(0, 5, new Label(Messages.getString("actions"))); //$NON-NLS-1$
    table.getCellFormatter().setStyleName(0, 0, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
    table.getCellFormatter().setStyleName(0, 1, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
    table.getCellFormatter().setStyleName(0, 2, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
    table.getCellFormatter().setStyleName(0, 3, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
    table.getCellFormatter().setStyleName(0, 4, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
    table.getCellFormatter().setStyleName(0, 5, "backgroundContentHeaderTableCellRight"); //$NON-NLS-1$
    return table;
  }

  private FlexTable buildEmptySubscriptionsTable() {
    FlexTable table = new FlexTable();
    table.setWidth("100%"); //$NON-NLS-1$
    table.setStyleName("backgroundContentTable"); //$NON-NLS-1$
    table.setWidget(0, 0, new Label(Messages.getString("name"))); //$NON-NLS-1$
    table.setWidget(0, 1, new Label(Messages.getString("scheduleDate"))); //$NON-NLS-1$
    table.setWidget(0, 2, new Label(Messages.getString("type"))); //$NON-NLS-1$
    table.setWidget(0, 3, new Label(Messages.getString("size"))); //$NON-NLS-1$
    table.setWidget(0, 4, new Label(Messages.getString("actions"))); //$NON-NLS-1$
    table.getFlexCellFormatter().setWidth(1, 0, "200em"); //$NON-NLS-1$
    table.getFlexCellFormatter().setWidth(1, 1, "200em"); //$NON-NLS-1$
    table.getFlexCellFormatter().setWidth(1, 2, "100em"); //$NON-NLS-1$
    table.getFlexCellFormatter().setWidth(1, 3, "100em"); //$NON-NLS-1$
    table.getFlexCellFormatter().setWidth(1, 4, "450em"); //$NON-NLS-1$

    table.getCellFormatter().setStyleName(0, 0, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
    table.getCellFormatter().setStyleName(0, 1, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
    table.getCellFormatter().setStyleName(0, 2, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
    table.getCellFormatter().setStyleName(0, 3, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
    table.getCellFormatter().setStyleName(0, 4, "backgroundContentHeaderTableCell"); //$NON-NLS-1$
    return table;
  }

  public void setAdministrator(boolean isAdministrator) {
    buildScheduledAndCompletedContentPanel(isAdministrator);
  }

  private void buildScheduledAndCompletedContentPanel(boolean isAdministrator) {
    workspaceTable = new FlexTable();

    HTML workspaceMessage = new HTML(Messages.getString("workspaceMessage"));
    workspaceMessage.setStylePrimaryName("workspaceMessage");

    workspaceTable.setWidget(0, 0, workspaceMessage); //$NON-NLS-1$
    workspaceTable.setWidget(1, 0, waitingContentDisclosure);
    workspaceTable.setWidget(2, 0, completedContentDisclosure);
    workspaceTable.setWidget(3, 0, myScheduledContentDisclosure);
    if (isAdministrator) {
      workspaceTable.setWidget(4, 0, allScheduledContentDisclosure);
    }

    workspaceTable.setWidget(5, 0, subscriptionsContentDisclosure);
    DOM.setStyleAttribute(workspaceTable.getElement(), "margin", "10px"); //$NON-NLS-1$ //$NON-NLS-2$
    setWidget(workspaceTable);
  }

  
  /*
   * Deletes the given public schedule and all the contents belonging to it.
   * 
   * @param currSubscr Current public schedule to be deleted
   */

  private void buildScheduleTable(ArrayList<JobSchedule> scheduleDetails, FlexTable scheduleTable, DisclosurePanel disclosurePanel, final int jobSource) {
    disclosurePanel.setOpen(scheduleDetails != null && scheduleDetails.size() > 0);
    for (int row = 0; row < scheduleDetails.size(); row++) {
      final JobSchedule jobSchedule = scheduleDetails.get(row);
      HorizontalPanel actionPanel = new HorizontalPanel();
      Label suspendJobLabel = new Label(Messages.getString("suspend")); //$NON-NLS-1$
      suspendJobLabel.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          suspendJob(jobSchedule.jobName, jobSchedule.jobGroup, jobSource);
        }

      });
      suspendJobLabel.setStyleName("backgroundContentAction"); //$NON-NLS-1$
      suspendJobLabel.setTitle(Messages.getString("suspendThisJob")); //$NON-NLS-1$

      Label resumeJobLabel = new Label(Messages.getString("resume")); //$NON-NLS-1$
      resumeJobLabel.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          resumeJob(jobSchedule.jobName, jobSchedule.jobGroup, jobSource);
        }

      });
      resumeJobLabel.setStyleName("backgroundContentAction"); //$NON-NLS-1$
      resumeJobLabel.setTitle(Messages.getString("resumeThisJob")); //$NON-NLS-1$

      Label runJobLabel = new Label(Messages.getString("run")); //$NON-NLS-1$
      runJobLabel.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          runJob(jobSchedule.jobName, jobSchedule.jobGroup, jobSource);
        }

      });
      runJobLabel.setStyleName("backgroundContentAction"); //$NON-NLS-1$
      runJobLabel.setTitle(Messages.getString("runThisJob")); //$NON-NLS-1$

      Label deleteJobLabel = new Label(Messages.getString("delete")); //$NON-NLS-1$
      deleteJobLabel.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          deleteJob(jobSchedule.jobName, jobSchedule.jobGroup, jobSource);
        }

      });
      deleteJobLabel.setStyleName("backgroundContentAction"); //$NON-NLS-1$
      deleteJobLabel.setTitle(Messages.getString("deleteThisJob")); //$NON-NLS-1$

      if (jobSchedule.triggerState == 0) {
        actionPanel.add(suspendJobLabel);
        actionPanel.add(new HTML("&nbsp;|&nbsp;")); //$NON-NLS-1$
      }
      if (jobSchedule.triggerState == 1) {
        actionPanel.add(resumeJobLabel);
        actionPanel.add(new HTML("&nbsp;|&nbsp;")); //$NON-NLS-1$
      }
      if (jobSchedule.triggerState != 2) {
        actionPanel.add(runJobLabel);
        // actionPanel.add(new HTML("&nbsp;|&nbsp;")); //$NON-NLS-1$
      }
      // actionPanel.add(deleteJobLabel);

      if (actionPanel.getWidgetCount() == 0) {
        actionPanel.add(new HTML("&nbsp;")); //$NON-NLS-1$
      }

      scheduleTable.setWidget(row + 1, 0, new HTML(jobSchedule.jobName));
      scheduleTable.setWidget(row + 1, 1, new HTML(jobSchedule.jobGroup));
      scheduleTable.setWidget(row + 1, 2, new HTML(
          jobSchedule.jobDescription == null || jobSchedule.jobDescription.trim().length() == 0 ? "&nbsp;" : jobSchedule.jobDescription)); //$NON-NLS-1$
      scheduleTable.setWidget(row + 1, 3,
          new HTML((jobSchedule.previousFireTime == null ? Messages.getString("never") : jobSchedule.previousFireTime.toString()) + "<BR>" //$NON-NLS-1$ //$NON-NLS-2$
              + (jobSchedule.nextFireTime == null ? "-" : jobSchedule.nextFireTime.toString()))); //$NON-NLS-1$
      scheduleTable.setWidget(row + 1, 4, new HTML(getTriggerStateName(jobSchedule.triggerState)));
      scheduleTable.setWidget(row + 1, 5, actionPanel);
      scheduleTable.getCellFormatter().setStyleName(row + 1, 0, "backgroundContentTableCell"); //$NON-NLS-1$
      scheduleTable.getCellFormatter().setStyleName(row + 1, 1, "backgroundContentTableCell"); //$NON-NLS-1$
      scheduleTable.getCellFormatter().setStyleName(row + 1, 2, "backgroundContentTableCell"); //$NON-NLS-1$
      scheduleTable.getCellFormatter().setStyleName(row + 1, 3, "backgroundContentTableCell"); //$NON-NLS-1$
      scheduleTable.getCellFormatter().setStyleName(row + 1, 4, "backgroundContentTableCell"); //$NON-NLS-1$
      scheduleTable.getCellFormatter().setStyleName(row + 1, 5, "backgroundContentTableCellRight"); //$NON-NLS-1$
      if (row == scheduleDetails.size() - 1) {
        // last
        scheduleTable.getCellFormatter().setStyleName(row + 1, 0, "backgroundContentTableCellBottom"); //$NON-NLS-1$
        scheduleTable.getCellFormatter().setStyleName(row + 1, 1, "backgroundContentTableCellBottom"); //$NON-NLS-1$
        scheduleTable.getCellFormatter().setStyleName(row + 1, 2, "backgroundContentTableCellBottom"); //$NON-NLS-1$
        scheduleTable.getCellFormatter().setStyleName(row + 1, 3, "backgroundContentTableCellBottom"); //$NON-NLS-1$
        scheduleTable.getCellFormatter().setStyleName(row + 1, 4, "backgroundContentTableCellBottom"); //$NON-NLS-1$
        scheduleTable.getCellFormatter().setStyleName(row + 1, 5, "backgroundContentTableCellBottomRight"); //$NON-NLS-1$
      }
    }
  }

  private String getTriggerStateName(int state) {
    if (state == 0) {
      return Messages.getString("normal"); //$NON-NLS-1$
    } else if (state == 1) {
      return Messages.getString("paused"); //$NON-NLS-1$
    } else if (state == 2) {
      return Messages.getString("running"); //$NON-NLS-1$
    }
    return Messages.getString("unknown"); //$NON-NLS-1$
  }

  public void refreshWorkspace() {
  }

  private void suspendJob(final String jobName, final String jobGroup, final int jobSource) {
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

      public void onSuccess(Boolean result) {
        AsyncCallback<Void> callback = new AsyncCallback<Void>() {

          public void onFailure(Throwable caught) {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), //$NON-NLS-1$
                Messages.getString("couldNotSuspendJob"), false, false, true); //$NON-NLS-1$
            dialogBox.center();
          }

          public void onSuccess(Void result) {
            refreshWorkspace();
          }
        };
        MantleServiceCache.getService().suspendJob(jobName, jobGroup, callback);
      }

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {
          }

          public void onSuccess(Boolean result) {
            suspendJob(jobName, jobGroup, jobSource);
          }

        });
      }
    };
    MantleServiceCache.getService().isAuthenticated(callback);
  }

  private void resumeJob(final String jobName, final String jobGroup, final int jobSource) {
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

      public void onSuccess(Boolean result) {
        AsyncCallback<Void> callback = new AsyncCallback<Void>() {

          public void onFailure(Throwable caught) {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), //$NON-NLS-1$
                Messages.getString("couldNotResumeJob"), false, false, true); //$NON-NLS-1$
            dialogBox.center();
          }

          public void onSuccess(Void nothing) {
            refreshWorkspace();
          }
        };
        MantleServiceCache.getService().resumeJob(jobName, jobGroup, callback);
      }

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {

          }

          public void onSuccess(Boolean result) {
            resumeJob(jobName, jobGroup, jobSource);
          }
        });
      }
    };
    MantleServiceCache.getService().isAuthenticated(callback);
  }

  private void deleteJob(final String jobName, final String jobGroup, final int jobSource) {
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

      public void onSuccess(Boolean result) {
        AsyncCallback<Void> callback = new AsyncCallback<Void>() {

          public void onFailure(Throwable caught) {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), //$NON-NLS-1$
                Messages.getString("couldNotDeleteJob"), false, false, true); //$NON-NLS-1$
            dialogBox.center();
          }

          public void onSuccess(Void nothing) {
            refreshWorkspace();
          }
        };
        MantleServiceCache.getService().deleteJob(jobName, jobGroup, callback);
      }

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {

          }

          public void onSuccess(Boolean result) {
            deleteJob(jobName, jobGroup, jobSource);
          }

        });
      }
    };
    MantleServiceCache.getService().isAuthenticated(callback);
  }

  private void runJob(final String jobName, final String jobGroup, final int jobSource) {
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

      public void onSuccess(Boolean result) {
        AsyncCallback<Void> callback = new AsyncCallback<Void>() {

          public void onFailure(Throwable caught) {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), //$NON-NLS-1$
                Messages.getString("couldNotBackgroundExecute"), false, false, true); //$NON-NLS-1$
            dialogBox.center();
          }

          public void onSuccess(Void nothing) {
            refreshWorkspace();
          }
        };
        MantleServiceCache.getService().runJob(jobName, jobGroup, callback);
      }

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {

          }

          public void onSuccess(Boolean result) {
            runJob(jobName, jobGroup, jobSource);
          }

        });
      }
    };
    MantleServiceCache.getService().isAuthenticated(callback);
  }

  // Event classes
  private class RunAndArchiveClickHandler implements ClickHandler {
    String subscriptionId;

    public RunAndArchiveClickHandler(String subscriptionID) {
      this.subscriptionId = subscriptionID;
    }

    public void onClick(ClickEvent event) {
    }
  }


}
