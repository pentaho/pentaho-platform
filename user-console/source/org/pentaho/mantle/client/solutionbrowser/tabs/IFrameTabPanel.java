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

    public native void removeEventListeners( Element ele )
    /*-{
        var wind = ele.contentWindow;
        try {
          wind.onmouseup = null;
          wind.onmousedown = null;
          wind.onmousemove = null;          
          wind.onunload = null;
        } catch(e) {
          // Swallow. Most probably due to Same Domain Origin Policy.
        }
        $wnd.watchWindow = null;
    }-*/;

    public native void attachEventListeners( Element ele, CustomFrame frame )
    /*-{
      var iwind = ele.contentWindow; //IFrame's window instance
      
      var funct = function(event){
        event = iwind.parent.translateInnerMouseEvent(ele, event);
        iwind.parent.sendMouseEvent(event);
      }  
      
      // Hooks up mouse and unload events
      $wnd.hookEvents = function(wind){
        try{
          if(wind == null){
            wind = $wnd.watchWindow
          }
          wind.onmouseup = funct;
          wind.onmousedown = funct;
          wind.onmousemove = funct;
          
          wind.onunload = unloader;
          wind.mantleEventsIn = true;
          $wnd.watchWindow = null;
        } catch(e){
          //You're most likely here because of Cross-site scripting permissions... consuming
        }
      }
      
      // IFrame URL watching code
      
      // Called on iFrame unload, calls containing Window to start monitoring it for Url change
      var unloader = function(event){
        //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
        frame.@org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel$CustomFrame::addHistory(Ljava/lang/String;)(iwind.location.href);
        $wnd.startIFrameWatcher(iwind);
      }
      
      // Starts the watching loop.
      $wnd.startIFrameWatcher = function(wind){
        $wnd.watchWindow = wind;
        $wnd.setTimeout("rehookEventsTimer()", 300);
      }
    
      // loop that's started when an iFrame unloads, when the url changes it adds back in the hooks
      $wnd.rehookEventsTimer = function(){
        try {
          if($wnd.watchWindow.mantleEventsIn == undefined){
            //location changed hook back up event interceptors
            $wnd.setTimeout("hookEvents()", 300);
          } else {
            $wnd.setTimeout("rehookEventsTimer()", 300);
          }
        } catch(e) {
          // Swallow. Most probably due to Same Domain Origin Policy.
        }
      }
      
      // Scope helper funct.
      function rehookEventsTimer(){
        $wnd.rehookEventsTimer();
      }
      
      //Hook up the mouse and unload event handlers for iFrame being created
      $wnd.hookEvents(iwind);
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
