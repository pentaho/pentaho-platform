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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.client.images;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.Tree.Resources;
import com.google.gwt.user.client.ui.VerticalSplitPanel;

@SuppressWarnings("deprecation")
public interface MantleImages extends ClientBundle, Resources, HorizontalSplitPanel.Resources, VerticalSplitPanel.Resources {

  public static final MantleImages images = (MantleImages) GWT.create(MantleImages.class);

  ImageResource plus();

  ImageResource minus();

  ImageResource file();

  ImageResource fileHover();

  ImageResource folder();

  ImageResource folderHover();

  ImageResource smallFolder();

  ImageResource smallFolderHover();

  ImageResource fileIcon();

  ImageResource file_url();

  ImageResource file_action();

  ImageResource file_analysis();

  ImageResource file_report();

  ImageResource treeOpen();

  ImageResource treeClosed();

  ImageResource treeLeaf();

  ImageResource closeTab();

  ImageResource closeTabHover();

  ImageResource backButton();

  ImageResource backToFirstPage();

  ImageResource forwardButton();

  ImageResource forwardToLastPage();

  ImageResource run();

  ImageResource runDisabled();

  ImageResource update();

  ImageResource updateDisabled();

  ImageResource misc();

  ImageResource miscDisabled();

  ImageResource horizontalSplitPanelThumb();

  ImageResource verticalSplitPanelThumb();

  ImageResource new_analysis_32();

  ImageResource new_report_32();

  ImageResource print_32();

  ImageResource print_32_disabled();

  ImageResource save_32();

  ImageResource saveAs_32();

  ImageResource save_32_disabled();

  ImageResource saveAs_32_disabled();

  ImageResource space1x13();

  ImageResource space1x20();

  ImageResource space1x23();

  ImageResource generic_square_32();

  ImageResource generic_square_32_disabled();

  ImageResource browser_hide_32();

  ImageResource browser_show_32();

  ImageResource workspace_32();

  ImageResource refresh();

  ImageResource open_32();

  ImageResource drop_valid();

  ImageResource drop_invalid();

  ImageResource disclosurePanelOpen();

  ImageResource disclosurePanelClosed();
  
  // workspace icons
  ImageResource datepicker16();
  ImageResource refresh16();
  ImageResource remove16();
  ImageResource edit16();
  ImageResource run16();
  ImageResource stop16();
  ImageResource start_scheduler16();
  ImageResource stop_scheduler16();
  ImageResource filter16();
  ImageResource filterActive16();
  ImageResource filterRemove16();
  ImageResource execute16();
}
