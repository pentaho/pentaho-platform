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
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 *
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.ui.tabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.tabs.PentahoTab;
import org.pentaho.gwt.widgets.client.utils.FrameUtils;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.dialogs.WaitPopup;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserListener;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel;
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel.CustomFrame;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class MantleTabPanel extends org.pentaho.gwt.widgets.client.tabs.PentahoTabPanel {

  final PopupPanel waitPopup = new PopupPanel(false, true);

  private static final String FRAME_ID_PRE = "frame_"; //$NON-NLS-1$
  private static int frameIdCount = 0;

  public MantleTabPanel() {
    this(false);
  }

  public MantleTabPanel(boolean setupNativeHooks) {
    super();
    if (setupNativeHooks) {
      setupNativeHooks(this);
    }
    // add window close listener
    Window.addWindowClosingHandler(new ClosingHandler() {

      public void onWindowClosing(ClosingEvent event) {
        // close only if we have stuff open
        if (getTabCount() > 0) {
          for (int i = 0; i < getTabCount(); i++) {
            Element frameElement = getFrameElement(getTab(i));
            if (hasUnsavedChanges(frameElement)) {
              event.setMessage(Messages.getString("windowCloseWarning")); //$NON-NLS-1$
              return;
            }
          }
        }
      }
    });

  }

  public void addTab(String text, String tooltip, boolean closeable, Widget content) {
    MantleTab tab = new MantleTab(text, tooltip, this, content, closeable);
    getTabBar().add(tab);
    getTabDeck().add(content);
    if (getSelectedTab() == null) {
      selectTab(tab);
    }
  }

  public void showNewURLTab(String tabName, String tabTooltip,  String url, boolean setFileInfoInFrame, String frameName) {

    showLoadingIndicator();

    // Because Frames are being generated with the window.location object, relative URLs will be generated differetly
    // than if set with the src attribute. This detects the relative paths are prepends them appropriately.
    if(url.indexOf("http") != 0 && url.indexOf("/") != 0){
      url = GWT.getHostPageBaseURL() + url;
    }
    
    if(!url.contains("?")) {
    	url = url + "?ts=" + System.currentTimeMillis();
    } else {
    	url = url + "&ts=" + System.currentTimeMillis();
    }

    final int elementId = getTabCount();
    if (frameName == null || "".equals(frameName.trim())) {
      frameName = getUniqueFrameName();
    }

    // check for other tabs with this name
    if (existingTabMatchesName(tabName)) {
      int counter = 2;
      while (true) {
        // Loop until a unique tab name is not found
        // i.e. get the last counter number and then add 1 to it for the new tab
        // name
        if (existingTabMatchesName(tabName + " (" + counter + ")")) { // unique //$NON-NLS-1$ //$NON-NLS-2$
          counter++;
          continue;
        } else {
          tabName = tabName + " (" + counter + ")"; //$NON-NLS-1$ //$NON-NLS-2$
          tabTooltip = tabTooltip + " (" + counter + ")"; //$NON-NLS-1$ //$NON-NLS-2$
          break;
        }
      }
    }

    IFrameTabPanel panel = new IFrameTabPanel(frameName);
    addTab(tabName, tabTooltip, true, panel);
    selectTab(elementId);

    // plugins will define their background color, if any
    // all other content is expected, for backwards compatibility to
    // be set on a white background (default for web browsers)
    // I have defined a CSS class for this background if someone
    // wants to change or remove the color
    if (url.indexOf("/content") > -1 || url.indexOf("/generatedContent") > -1) {
    	panel.getElement().addClassName("mantle-white-tab-background"); // white background
    } else {
    	panel.getElement().addClassName("mantle-default-tab-background"); // transparent background
    }

    final ArrayList<com.google.gwt.dom.client.Element> parentList = new ArrayList<com.google.gwt.dom.client.Element>();
    com.google.gwt.dom.client.Element parent = panel.getFrame().getElement();
    while (parent != getElement()) {
      parentList.add(parent);
      parent = parent.getParentElement();
    }
    Collections.reverse(parentList);
    for (int i = 1; i < parentList.size(); i++) {
      parentList.get(i).getStyle().setProperty("height", "100%"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    SolutionBrowserPanel.getInstance().showContent();
    SolutionBrowserPanel.getInstance().fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.OPEN, getSelectedTabIndex());

    // if showContent is the thing that turns on our first tab, which is entirely possible, then we
    // would encounter the same timing issue as before
    panel.setUrl(url);

    SolutionBrowserPanel.getInstance().fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.SELECT, getSelectedTabIndex());
    
    if (setFileInfoInFrame && SolutionBrowserPanel.getInstance().getFilesListPanel().getSelectedFileItems().size() > 0) {
      setFileInfoInFrame(SolutionBrowserPanel.getInstance().getFilesListPanel().getSelectedFileItems().get(0));
    }

    // create a timer to check the readyState
    Timer t = new Timer() {
      public void run() {
        Element frameElement = getFrameElement(getSelectedTab());
        if (supportsReadyFeedback(frameElement)) {
          // cancel the timer, the content will hide the loading indicator itself
          cancel();
        } else {
          if ("complete".equalsIgnoreCase(getReadyState(frameElement))) {
            // the content is not capable of giving us feedback so when the
            // readyState is "complete" we hide/cancel
            hideLoadingIndicator();
            cancel();
          } else if (StringUtils.isEmpty(getReadyState(frameElement)) || "undefined".equals(getReadyState(frameElement))) {
            hideLoadingIndicator();
            cancel();
          }
        }
      }
    };
    t.scheduleRepeating(1000);
  }

  /*
   * This should only ever get invoked via JSNI now
   */
  private void showNewURLTab(String tabName, String tabTooltip, final String url) {
    showNewURLTab(tabName, tabTooltip, url, false);
  }

  private void showNewNamedFrameURLTab(String tabName, String tabTooltip, String frameName, final String url) {
    showNewURLTab(tabName, tabTooltip, url, false, frameName);
  }

  public void showNewURLTab(String tabName, String tabTooltip, final String url, boolean setFileInfoInFrame) {
    showNewURLTab(tabName, tabTooltip, url, setFileInfoInFrame, null);
  }

  private String getUniqueFrameName() {
    return FRAME_ID_PRE + frameIdCount++;
  }

  public boolean existingTabMatchesName(String name) {

    // TODO: remove once a more elegant tab solution is in place
    // Must escape name before attempting to match it in HTML
    name = name
        .replaceAll("&", "&amp;") //$NON-NLS-1$ //$NON-NLS-2$
        .replaceAll(">", "&gt;") //$NON-NLS-1$ //$NON-NLS-2$
        .replaceAll("<", "&lt;") //$NON-NLS-1$ //$NON-NLS-2$
        .replaceAll("\"", "&quot;"); //$NON-NLS-1$ //$NON-NLS-2$

    String key = ">" + name + "<"; //$NON-NLS-1$ //$NON-NLS-2$

    NodeList<com.google.gwt.dom.client.Element> divs = getTabBar().getElement().getElementsByTagName("div"); //$NON-NLS-1$

    for (int i = 0; i < divs.getLength(); i++) {
      String tabHtml = divs.getItem(i).getInnerHTML();
      // TODO: remove once a more elegant tab solution is in place
      if (tabHtml.indexOf(key) > -1) {
        return true;
      }
    }
    return false;
  }

  private native void setupNativeHooks(MantleTabPanel tabPanel)
  /*-{  
    
    $wnd.enableContentEdit = function(enable) { 
      tabPanel.@org.pentaho.mantle.client.ui.tabs.MantleTabPanel::enableContentEdit(Z)(enable);      
    }
    $wnd.setContentEditSelected = function(enable) { 
      tabPanel.@org.pentaho.mantle.client.ui.tabs.MantleTabPanel::setContentEditSelected(Z)(enable);      
    }
    $wnd.registerContentOverlay = function(id) { 
      tabPanel.@org.pentaho.mantle.client.ui.tabs.MantleTabPanel::registerContentOverlay(Ljava/lang/String;)(id);      
    }
    $wnd.enableSave = function(enable) {
      tabPanel.@org.pentaho.mantle.client.ui.tabs.MantleTabPanel::setCurrentTabSaveEnabled(Z)(enable);
    }
    $wnd.closeTab = function(url) {
      tabPanel.@org.pentaho.mantle.client.ui.tabs.MantleTabPanel::closeTab(Ljava/lang/String;)(url);
    }    
    $wnd.mantle_openTab = function(name, title, url) {
      tabPanel.@org.pentaho.mantle.client.ui.tabs.MantleTabPanel::showNewURLTab(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(name, title, url);
    }    
    $wnd.openURL = function(name, tooltip, url){
      if(url.indexOf('http') != 0 && url.indexOf('/') != 0){
        // relative url. Prepend with root to fix issue with cross frame calls
        url = $wnd.CONTEXT_PATH + url;
      }
      tabPanel.@org.pentaho.mantle.client.ui.tabs.MantleTabPanel::showNewURLTab(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(name, tooltip, url);
    }    
    $wnd.mantle_openNamedFrameTab = function(name, title, frameName, url) {
      tabPanel.@org.pentaho.mantle.client.ui.tabs.MantleTabPanel::showNewNamedFrameURLTab(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(name, title, frameName, url);
    }
    $wnd.hideLoadingIndicator = function() {
      tabPanel.@org.pentaho.mantle.client.ui.tabs.MantleTabPanel::hideLoadingIndicator()();
    }
    $wnd.showLoadingIndicator = function() {
      tabPanel.@org.pentaho.mantle.client.ui.tabs.MantleTabPanel::showLoadingIndicator()();
    }
  }-*/;

  public void showLoadingIndicator() {
    WaitPopup.getInstance().setVisible(true);
  }

  public void hideLoadingIndicator() {
    WaitPopup.getInstance().setVisible(false);
  }

  public void setCurrentTabSaveEnabled(boolean enabled) {
    IFrameTabPanel panel = getCurrentFrame();
    if (panel != null) {
      panel.setSaveEnabled(enabled);
      SolutionBrowserPanel.getInstance().fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.SELECT, getSelectedTabIndex());
    }
  }

  /*
   * registerContentOverlay - register the overlay with the panel. Once the registration is done it fires a soultion browser event passing the current tab index
   * and the type of event
   */
  public void registerContentOverlay(String id) {
    IFrameTabPanel panel = getCurrentFrame();
    if (panel != null) {
      panel.addOverlay(id);
      SolutionBrowserPanel.getInstance().fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.OPEN, getSelectedTabIndex());
    }
  }

  public void enableContentEdit(boolean enable) {
    IFrameTabPanel panel = getCurrentFrame();
    if (panel != null) {
      panel.setEditEnabled(enable);
      SolutionBrowserPanel.getInstance().fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.UNDEFINED, getSelectedTabIndex());
    }
  }

  public void setContentEditSelected(boolean selected) {
    IFrameTabPanel panel = getCurrentFrame();
    if (panel != null) {
      panel.setEditSelected(selected);
      SolutionBrowserPanel.getInstance().fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.UNDEFINED, getSelectedTabIndex());
    }
  }

  /**
   * Store representation of file in the frame for reference later when save is called
   * 
   * @param selectedFileItem
   */
  public void setFileInfoInFrame(FileItem selectedFileItem) {
    IFrameTabPanel tp = getCurrentFrame();
    if (tp != null && selectedFileItem != null) {
      SolutionFileInfo fileInfo = new SolutionFileInfo();
      fileInfo.setName(selectedFileItem.getName());
      fileInfo.setPath(selectedFileItem.getPath());
      tp.setFileInfo(fileInfo);
    }
  }

  public IFrameTabPanel getCurrentFrame() {
    return getFrame(getSelectedTab());
  }

  public IFrameTabPanel getFrame(PentahoTab tab) {
    if (tab != null && tab.getContent() instanceof IFrameTabPanel) {
      return ((IFrameTabPanel) tab.getContent());
    }
    return null;
  }

  public Element getFrameElement(PentahoTab tab) {
    if (getFrame(tab) != null && getFrame(tab) instanceof IFrameTabPanel) {
      return getFrame(tab).getFrame().getElement();
    }
    return null;
  }

  /**
   * This method returns the current frame element id.
   * 
   * @return
   */
  public String getCurrentFrameElementId() {
    if (getCurrentFrame() == null) {
      return null;
    }
    return getCurrentFrame().getFrame().getElement().getAttribute("id"); //$NON-NLS-1$
  }

  public static native String getReadyState(Element frameElement)
  /*-{
    try {
      return frameElement.contentDocument.readyState;
    } catch (e) {
      // probably cross-site security
      return 'complete';
    }
  }-*/;

  public static native boolean supportsReadyFeedback(Element frameElement)
  /*-{
    try {
      if (!frameElement.contentWindow.supportsReadyFeedback) {
        return false;
      }
      return frameElement.contentWindow.supportsReadyFeedback;
    } catch (e) {
      return false;
    }
  }-*/;

  public static native boolean hasUnsavedChanges(Element frameElement)
  /*-{
    try {
      if (!frameElement.contentWindow.hasUnsavedChanges) {
        return false;
      }
      return frameElement.contentWindow.hasUnsavedChanges();
    } catch (e) {
      return false;
    }
  }-*/;

  public static native boolean preTabCloseHook(Element frameElement)
  /*-{
    try {
      if (!frameElement.contentWindow.preTabCloseHook) {
        return true;
      }
      return frameElement.contentWindow.preTabCloseHook();
    } catch (e) {
      return true;
    }
  }-*/;

  public void closeTab(final PentahoTab closeTab, final boolean invokePreTabCloseHook) {
    if (closeTab.getContent() instanceof IFrameTabPanel) {
      final Element frameElement = ((IFrameTabPanel) closeTab.getContent()).getFrame().getElement();
      if (invokePreTabCloseHook && hasUnsavedChanges(frameElement)) {
        // prompt user
        VerticalPanel vp = new VerticalPanel();
        vp.add(new Label(Messages.getString("confirmTabClose"))); //$NON-NLS-1$
        final PromptDialogBox confirmDialog = new PromptDialogBox(
            Messages.getString("confirm"), Messages.getString("yes"), Messages.getString("no"), false, true, vp); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        confirmDialog.setCallback(new IDialogCallback() {
          public void cancelPressed() {
          }

          public void okPressed() {
            clearClosingFrame(frameElement);
            MantleTabPanel.super.closeTab(closeTab, invokePreTabCloseHook);
            if (getTabCount() == 0) {
              SolutionBrowserPanel.getInstance().showContent();
              SolutionBrowserPanel.getInstance().fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.CLOSE, -1);
            }
          }
        });
        confirmDialog.center();
        return;
      }

      clearClosingFrame(frameElement);
    }
    super.closeTab(closeTab, invokePreTabCloseHook);

    if (getTabCount() == 0) {
      SolutionBrowserPanel.getInstance().showContent();
      SolutionBrowserPanel.getInstance().fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.CLOSE, -1);
    }
  }

  public static native void clearClosingFrame(Element frame)/*-{
    try{
      frame.contentWindow.document.write("");
    } catch(e){
      // ignore XSS
    }
  }-*/;

  /**
   * Called by JSNI call from parameterized xaction prompt pages to "cancel". The only 'key' to pass up is the URL. To handle the possibility of multiple tabs
   * with the same url, this method first checks the assumption that the current active tab initiates the call. Otherwise it checks from tail up for the first
   * tab with a matching url and closes that one. *
   * 
   * @param url
   */
  private void closeTab(String url) {
    int curpos = getSelectedTabIndex();
    if (StringUtils.isEmpty(url)) {
      // if the url was not provided, simply remove the currently selected tab
      // and then remove
      if (curpos >= 0 && getTabCount() > 0) {
        closeTab(curpos, true);
      }
      return;
    }
    IFrameTabPanel curPanel = (IFrameTabPanel) getTab(curpos).getContent();
    if (url.contains(curPanel.getUrl())) {
      closeTab(curpos, true);
      return;
    }

    for (int i = getTabCount() - 1; i >= 0; i--) {
      curPanel = (IFrameTabPanel) getTab(i).getContent();

      if (url.contains(curPanel.getUrl())) {
        closeTab(i, true);
        return;
      }
    }
  }

  public void closeOtherTabs(PentahoTab exceptThisTab) {
    // remove from 0 -> me
    while (exceptThisTab != getTab(0)) {
      closeTab(getTab(0), true);
    }
    // remove from END -> me
    while (exceptThisTab != getTab(getTabCount() - 1)) {
      closeTab(getTab(getTabCount() - 1), true);
    }
    selectTab(exceptThisTab);
  }

  public void closeAllTabs() {
    // get a copy of the tabs to create a separate list
    ArrayList<PentahoTab> tabs = new ArrayList<PentahoTab>(getTabCount());
    for (int i = 0; i < getTabCount(); i++) {
      tabs.add(getTab(i));
    }
    for (PentahoTab tab : tabs) {
      closeTab(tab, true);
    }
  }

  public void selectTab(final PentahoTab selectedTab) {
    super.selectTab(selectedTab);

    if (selectedTab == null) {
      return;
    }

    SolutionBrowserPanel.getInstance().fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.SELECT, getSelectedTabIndex());
    Window.setTitle(Messages.getString("productName") + " - " + selectedTab.getLabelText()); //$NON-NLS-1$ //$NON-NLS-2$

    // first turn off all tabs that should be
    for (int i = 0; i < getTabCount(); i++) {
      final PentahoTab tab = getTab(i);
      if (tab.getContent() instanceof IFrameTabPanel) {
        if (tab.getContent() != selectedTab.getContent()) {
          FrameUtils.setEmbedVisibility(((IFrameTabPanel) tab.getContent()).getFrame(), false);
        }
      }
    }

    // now turn on the select tab
    if (selectedTab.getContent() instanceof IFrameTabPanel) {
      FrameUtils.setEmbedVisibility(((IFrameTabPanel) selectedTab.getContent()).getFrame(), true);
      // fix for BISERVER-6027 - on selection, set the focus into a textbox
      // element to allow IE mouse access in these elements
      // this was made native due to BISERVER-7400
      ieFix(((IFrameTabPanel) selectedTab.getContent()).getFrame().getElement());
    }
  }

  private native void ieFix(Element frame)/*-{
      try {
      var inputElements = frame.contentWindow.document.getElementsByTagName("input");
      for (var i = 0; i < inputElements.length; i++) {
        if (inputElements[i].getAttribute("type") != null && "TEXT" === inputElements[i].getAttribute("type").toUpperCase()) {
          if (inputElements[i].getAttribute("paramType") == null || !("DATE" === inputElements[i].getAttribute("paramType").toUpperCase())) { 
            inputElements[i].focus();
                break;
              }
            }
          }
    } catch (e) {    
        }
  }-*/;
}
