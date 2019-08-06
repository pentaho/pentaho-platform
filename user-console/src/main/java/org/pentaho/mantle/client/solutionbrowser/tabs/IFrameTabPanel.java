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
 * Copyright (c) 2002-2019 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.mantle.client.solutionbrowser.tabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;

import java.util.Set;
import java.util.Stack;

public class IFrameTabPanel extends VerticalPanel {

  private String url;
  private String deepLinkUrl;
  private String name;
  private CustomFrame frame;
  private SolutionFileInfo fileInfo;
  private FormPanel form;
  private boolean saveEnabled, editContentEnabled, editContentSelected;

  // We hold onto a Javascript object that gets various notifications of PUC events.
  // (edit content button clicked, etc.)
  protected JavaScriptObject jsCallback;

  private Set<String> overlayIds;

  public IFrameTabPanel() {
    this.name = "" + System.currentTimeMillis();
    this.frame = new CustomFrame( name, "about:blank" );
    add( frame );
  }

  public IFrameTabPanel( String name ) {
    this.name = name;

    frame = new CustomFrame( name );
    frame.getElement().setAttribute( "id", name + System.currentTimeMillis() ); //$NON-NLS-1$
    frame.setWidth( "100%" ); //$NON-NLS-1$
    frame.setHeight( "100%" ); //$NON-NLS-1$

    add( frame );
  }

  public void setName( String name ) {
    this.name = name;
    frame.getElement().setAttribute( "name", name );
    frame.getElement().setAttribute( "id", name + System.currentTimeMillis() ); //$NON-NLS-1$
  }

  public void reload() {
    if ( form != null ) {
      form.submit();
    } else {
      // frame.setUrl(getCurrentUrl());
      reloadFrame( frame.getElement() );
    }
  }

  public native void reloadFrame( Element frameElement )
  /*-{
    frameElement.contentWindow.location.reload();
  }-*/;

  public void back() {
    frame.back();
  }

  public void setFileInfo( SolutionFileInfo info ) {
    fileInfo = info;
  }

  public void setFileInfo( FileItem item ) {
    SolutionFileInfo fileInfo = new SolutionFileInfo();
    fileInfo.setName( item.getName() );
    fileInfo.setPath( item.getPath() );
    setFileInfo( fileInfo );
  }

  public SolutionFileInfo getFileInfo() {
    return fileInfo;
  }

  /*
   * frame.getUrl returns the original URL, but not the current one. This method accesses the DOM directly to get
   * that URL
   */
  private String getCurrentUrl() {
    return IFrameElement.as( this.frame.getElement() ).getContentDocument().getURL();
  }

  public String getUrl() {
    return url;
  }

  public void setUrl( String url ) {
    this.url = url;
    setSaveEnabled( url.contains( "analysisview.xaction" ) ); //$NON-NLS-1$
    setUrl( frame.getElement(), url );
  }

  public static native void setUrl( Element f, String url )/*-{
    try{
      f.contentWindow.location.href = url;
    } catch(e){
      // XSS error or frame not yet on document, set attribute instead
      f.src = url;
    }
  }-*/;

  public void openTabInNewWindow() {
    Window.open( getCurrentUrl(), "_blank", "" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public NamedFrame getFrame() {
    return frame;
  }

  public void setFrame( CustomFrame frame ) {
    this.frame = frame;
  }

  public FormPanel getForm() {
    return form;
  }

  public void setForm( FormPanel form ) {
    this.form = form;
  }

  public class CustomFrame extends NamedFrame {
    private boolean ignoreNextHistoryAdd = false;
    private Stack<String> history = new Stack<String>();
    private static final String ALLOW_TRANSPARENCY_ATTRIBUTE = "allowTransparency";

    private CustomFrame( String name ) {
      super( name );
      this.getElement().setAttribute( ALLOW_TRANSPARENCY_ATTRIBUTE, "true" );
    }

    private CustomFrame( String name, String url ) {
      super( name );
      setUrl( url );
      IFrameTabPanel.setUrl( this.getElement(), url );
      this.getElement().setAttribute( ALLOW_TRANSPARENCY_ATTRIBUTE, "true" );
    }

    public void back() {
      if ( !history.empty() ) {
        ignoreNextHistoryAdd = true;
        IFrameTabPanel.setUrl( frame.getElement(), history.pop() );
      }
    }

    public void addHistory( String url ) {
      if ( ignoreNextHistoryAdd || url.equals( "about:blank" ) ) { //$NON-NLS-1$
        ignoreNextHistoryAdd = false;
        return;
      }
      history.add( url );
    }

    @Override
    protected void onAttach() {
      super.onAttach();
      attachEventListeners( frame.getElement(), this );
    }

    // Called when closing a tab. See MantleTabPanel closeTab.
    public native void removeEventListeners( Element frameElem )
    /*-{
        var iframeWindow = frameElem.contentWindow;

        // console.log("IFrameTabPanel.CustomFrame.removeEventListeners url=" + iframeWindow.location.href + " name=" + iframeWindow.name);

        try {
          iframeWindow.onmouseup = null;
          iframeWindow.onmousedown = null;
          iframeWindow.onmousemove = null;
          iframeWindow.onunload = null;
        } catch(e) {
          // Swallow. Most probably due to Same Domain Origin Policy.
        }

        $wnd.watchWindow = null;
    }-*/;

    public native void attachEventListeners( Element frameElem, CustomFrame frame )
    /*-{
      // IFrame's window instance.
      var iframeWindow = frameElem.contentWindow;

      // console.log("IFrameTabPanel.CustomFrame.attachEventListeners url=" + iframeWindow.location.href + " name=" + iframeWindow.name);

      function onChildWindowMouseEvent(event) {
        // console.log("IFrameTabPanel.CustomFrame.onChildWindowMouseEvent url=" + this.location.href + " name=" +  this.name);

        var mantle = iframeWindow.parent;

        event = mantle.translateInnerMouseEvent(frameElem, event);
        mantle.sendMouseEvent(event);
      }

      // IFrame URL watching code.
      
      // Called on IFrame unload, calls containing Window to start monitoring it for URL change.
      // When a tab is closed through the UI, this handler is first unregistered, and so this is only
      // called for internal navigation.
      function onChildWindowUnload(event) {
        // console.log("IFrameTabPanel.CustomFrame.onChildWindowUnload url=" + this.location.href + " name=" +  this.name);

        //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
        frame.@org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel$CustomFrame::addHistory(Ljava/lang/String;)(iframeWindow.location.href);

        // When actually closing the tab, this is redundant with what closeTab already performs.
        // However, when navigating within a tab, this avoids the memory leak.

        //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
        frame.@org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel$CustomFrame::removeEventListeners(Lcom/google/gwt/dom/client/Element;)(frameElem);

        startIFrameWatcher(iframeWindow);
      }
      
      // Starts the watching loop.
      function startIFrameWatcher(wind) {
        // console.log("IFrameTabPanel.CustomFrame.startIFrameWatcher url=" + wind.location.href + " name=" +  wind.name);

        $wnd.watchWindow = wind;
        $wnd.setTimeout(rehookEventsTimer, 300);
      }
    
      // Loop that's started when an IFrame unloads.
      // When the url changes it adds back in the hooks.
      function rehookEventsTimer() {
        // console.log("IFrameTabPanel.CustomFrame.rehookEventsTimer url=" + ($wnd.watchWindow && $wnd.watchWindow.location.href) + " name=" + ($wnd.watchWindow && $wnd.watchWindow.name));

        try {
          if($wnd.watchWindow.mantleEventsIn == undefined) {
            // location changed hook back up event interceptors
            $wnd.setTimeout(hookEvents, 300);
          } else {
            $wnd.setTimeout(rehookEventsTimer, 300);
          }
        } catch(e) {
          // Swallow. Most probably due to Same Domain Origin Policy.
        }
      }

      // Hooks up mouse and unload events.
      function hookEvents(wind) {
        if(wind == null) {
            wind = $wnd.watchWindow;
        }

        // console.log("IFrameTabPanel.CustomFrame.hookEvents url=" + (wind && wind.location.href) + " name=" + (wind && wind.name));
        try {
          wind.onmouseup = onChildWindowMouseEvent;
          wind.onmousedown = onChildWindowMouseEvent;
          wind.onmousemove = onChildWindowMouseEvent;
          wind.onunload = onChildWindowUnload;

          wind.mantleEventsIn = true;
          $wnd.watchWindow = null;
        } catch(e) {
          // You're most likely here because of Cross-site scripting permissions... consuming
        }
      }
      
      // Hook up the mouse and unload event handlers for IFrame being created.
      // Usually, this frame will start at about:blank and is redirected to another url.
      // So, the sequence is:
      // 1. attachEventListeners about:blank
      // 2. hookEvents about:blank
      // 3. onChildWindowUnload about:blank
      // 4. removeEventListeners about:blank
      // 5. startIFramweWatcher about:blank
      // ...
      // 6. rehookEventsTimer new-url
      // 7. hookEvents new-url.
      hookEvents(iframeWindow);
    }-*/;
  }

  public boolean isSaveEnabled() {
    return saveEnabled;
  }

  public void setSaveEnabled( boolean enabled ) {
    saveEnabled = enabled;
  }

  public Set<String> getOverlayIds() {
    return overlayIds;
  }

  public void addOverlay( String id ) {
    overlayIds.add( id );
  }

  public void setEditEnabled( boolean enable ) {
    this.editContentEnabled = enable;
  }

  public boolean isEditEnabled() {
    return editContentEnabled;
  }

  public void setEditSelected( boolean selected ) {
    this.editContentSelected = selected;
  }

  public boolean isEditSelected() {
    return this.editContentSelected;
  }

  public boolean isPrintVisible() {
    return checkFrameWindowPrintVisible( frame.getElement() );
  }

  private native boolean checkFrameWindowPrintVisible( Element frame )/*-{
                                                                      try {
                                                                      return frame.contentWindow.printVisible;
                                                                      } catch (e) {
                                                                      }
                                                                      return false;
                                                                      }-*/;

  public void setId( String id ) {
    frame.getElement().setAttribute( "id", id ); //$NON-NLS-1$
  }

  public String getDeepLinkUrl() {
    return deepLinkUrl;
  }

  public void setDeepLinkUrl( String deepLinkUrl ) {
    this.deepLinkUrl = deepLinkUrl;
  }
}
