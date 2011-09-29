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
package org.pentaho.mantle.client.solutionbrowser.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFileTree;
import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.gwt.widgets.client.utils.string.StringTokenizer;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.dialogs.WaitPopup;
import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.FolderCommand;
import org.pentaho.mantle.client.solutionbrowser.IRepositoryFileProvider;
import org.pentaho.mantle.client.solutionbrowser.IRepositoryFileTreeListener;
import org.pentaho.mantle.client.solutionbrowser.MantlePopupPanel;
import org.pentaho.mantle.client.solutionbrowser.RepositoryFileTreeManager;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.usersettings.IMantleUserSettingsConstants;
import org.pentaho.mantle.client.usersettings.IUserSettingsListener;
import org.pentaho.mantle.client.usersettings.UserSettingsManager;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class SolutionTree extends Tree implements IRepositoryFileTreeListener, IUserSettingsListener, IRepositoryFileProvider {
  private boolean showLocalizedFileNames = true;
  private boolean showHiddenFiles = false;
  private boolean isAdministrator = false;
  private boolean createRootNode = false;
  private boolean useDescriptionsForTooltip = false;
  public RepositoryFileTree repositoryFileTree;
  public static final String ETC_FOLDER = "etc";//$NON-NLS-1$
  private FileTreeItem selectedItem = null;

  FocusPanel focusable = new FocusPanel();

  public SolutionTree() {
    super(MantleImages.images, true);
    setAnimationEnabled(true);
    sinkEvents(Event.ONDBLCLICK);
    // popupMenu.setAnimationEnabled(false);
    DOM.setElementAttribute(getElement(), "oncontextmenu", "return false;"); //$NON-NLS-1$ //$NON-NLS-2$

    DOM.setStyleAttribute(focusable.getElement(), "fontSize", "0"); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setStyleAttribute(focusable.getElement(), "position", "absolute"); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setStyleAttribute(focusable.getElement(), "outline", "0px"); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setStyleAttribute(focusable.getElement(), "width", "1px"); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setStyleAttribute(focusable.getElement(), "height", "1px"); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setElementAttribute(focusable.getElement(), "hideFocus", "true"); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setIntStyleAttribute(focusable.getElement(), "zIndex", -1); //$NON-NLS-1$
    DOM.appendChild(getElement(), focusable.getElement());
    DOM.sinkEvents(focusable.getElement(), Event.FOCUSEVENTS);

    // By default, expanding a node does not select it. Add that in here
    this.addOpenHandler(new OpenHandler<TreeItem>() {
      public void onOpen(OpenEvent<TreeItem> event) {
        SolutionTree.this.setSelectedItem(event.getTarget());
      }
    });

    getElement().setId("solutionTree");
    getElement().getStyle().setProperty("margin", "29px 0px 10px 0px"); //$NON-NLS-1$ //$NON-NLS-2$

    RepositoryFileTreeManager.getInstance().addRepositoryFileTreeListener(this);
    UserSettingsManager.getInstance().addUserSettingsListener(this);
  }

  public void onFetchUserSettings(ArrayList<IUserSetting> settings) {
    if (settings == null) {
      return;
    }

    for (IUserSetting setting : settings) {
      if (IMantleUserSettingsConstants.MANTLE_SHOW_LOCALIZED_FILENAMES.equals(setting.getSettingName())) {
        boolean showLocalizedFileNames = "true".equals(setting.getSettingValue()); //$NON-NLS-1$
        setShowLocalizedFileNames(showLocalizedFileNames);
      } else if (IMantleUserSettingsConstants.MANTLE_SHOW_DESCRIPTIONS_FOR_TOOLTIPS.equals(setting.getSettingName())) {
        boolean useDescriptions = "true".equals(setting.getSettingValue()); //$NON-NLS-1$
        setUseDescriptionsForTooltip(useDescriptions);
      } else if (IMantleUserSettingsConstants.MANTLE_SHOW_HIDDEN_FILES.equals(setting.getSettingName())) {
        boolean showHiddenFiles = "true".equals(setting.getSettingValue()); //$NON-NLS-1$
        setShowHiddenFiles(showHiddenFiles);
      }
    }

    SolutionBrowserPerspective.getInstance().updateViewMenu();
  }

  public void onBrowserEvent(Event event) {
    int eventType = DOM.eventGetType(event);
    switch (eventType) {
    case Event.ONMOUSEDOWN:
    case Event.ONMOUSEUP:
    case Event.ONCLICK: {
      try {
        int[] scrollOffsets = ElementUtils.calculateScrollOffsets(getElement());
        int[] offsets = ElementUtils.calculateOffsets(getElement());
        DOM.setStyleAttribute(focusable.getElement(), "top", (event.getClientY() + scrollOffsets[1] - offsets[1]) + "px"); //$NON-NLS-1$ //$NON-NLS-2$
      } catch (Exception ignored) {
        // ignore any exceptions fired by this. Most likely a result of the element
        // not being on the DOM
      }
      break;
    }
    }

    try {
      if (DOM.eventGetButton(event) == NativeEvent.BUTTON_RIGHT) {
        // load menu (Note: disabled as Delete and Properties have no meaning for Folders now
        int left = Window.getScrollLeft() + DOM.eventGetClientX(event);
        int top = Window.getScrollTop() + DOM.eventGetClientY(event);
        final PopupPanel popupMenu = MantlePopupPanel.getInstance(true);
        popupMenu.setPopupPosition(left, top);
        MenuBar menuBar = new MenuBar(true);
        menuBar.setAutoOpen(true);
        menuBar.addItem(new MenuItem(Messages.getString("createNewFolderEllipsis"), new FolderCommand(FolderCommand.COMMAND.CREATE_FOLDER, popupMenu, ((RepositoryFileTree)this.getSelectedItem().getUserObject()).getFile())));//$NON-NLS-1$
        menuBar.addItem(new MenuItem(Messages.getString("delete"), new FolderCommand(FolderCommand.COMMAND.DELETE, popupMenu, getRepositoryFiles().get(0)))); //$NON-NLS-1$
        menuBar.addSeparator();
        MenuItem pasteMenuItem = menuBar.addItem(new MenuItem(Messages.getString("paste"), new FolderCommand(FolderCommand.COMMAND.PASTE, popupMenu, ((RepositoryFileTree)this.getSelectedItem().getUserObject()).getFile()))); //$NON-NLS-1$
        pasteMenuItem.setStyleName( SolutionBrowserPerspective.getInstance().getClipboard().hasContent() ? "gwt-MenuItem" : "disabledMenuItem");
        menuBar.addSeparator();
        if (SolutionBrowserPerspective.getInstance().isAdministrator()) {
          menuBar.addItem(new MenuItem(Messages.getString("exportRepositoryFiles"), new FolderCommand(FolderCommand.COMMAND.EXPORT, popupMenu, ((RepositoryFileTree)this.getSelectedItem().getUserObject()).getFile()))); //$NON-NLS-1$
          menuBar.addItem(new MenuItem(Messages.getString("importRepositoryFilesElipsis"), new FolderCommand(FolderCommand.COMMAND.IMPORT, popupMenu, ((RepositoryFileTree)this.getSelectedItem().getUserObject()).getFile()))); //$NON-NLS-1$
          menuBar.addSeparator();
        }
        menuBar.addItem(new MenuItem(Messages.getString("propertiesEllipsis"), new FolderCommand(FolderCommand.COMMAND.PROPERTIES, popupMenu, ((RepositoryFileTree) this.getSelectedItem().getUserObject()).getFile()))); //$NON-NLS-1$
        popupMenu.setWidget(menuBar);
        popupMenu.hide();
        popupMenu.show();
      } else if (DOM.eventGetType(event) == Event.ONDBLCLICK) {
        getSelectedItem().setState(!getSelectedItem().getState(), true);
      } else {
        super.onBrowserEvent(event);
      }
    } catch (Throwable t) {
      // death to this browser event
    }
    TreeItem selItem = getSelectedItem();
    if (selItem != null) {
      DOM.scrollIntoView(selItem.getElement());
    }
  }

  public void beforeFetchRepositoryFileTree() {
    WaitPopup.getInstance().setVisible(true);
    if (getSelectedItem() != null) {
      selectedItem = (FileTreeItem) getSelectedItem();
    }
    clear();
    addItem(new TreeItem(Messages.getString("loadingEllipsis"))); //$NON-NLS-1$
    WaitPopup.getInstance().setVisible(false);
  }

  public void onFetchRepositoryFileTree(RepositoryFileTree repositoryFileTree) {

    if (repositoryFileTree == null) {
      WaitPopup.getInstance().setVisible(false);
      return;
    }
    this.repositoryFileTree = repositoryFileTree;
    // remember selectedItem, so we can reselect it after the tree is loaded
    clear();
    // get document root item
    RepositoryFile rootRepositoryFile = (RepositoryFile) repositoryFileTree.getFile();
    FileTreeItem rootItem = null;
    if (createRootNode) {
      rootItem = new FileTreeItem();
      rootItem.setText(rootRepositoryFile.getPath()); 
      rootItem.setTitle(rootRepositoryFile.getPath()); 
      rootItem.getElement().setId(rootRepositoryFile.getId());
      // added so we can traverse the true names
      rootItem.setFileName("/"); //$NON-NLS-1$
      addItem(rootItem);
      buildSolutionTree(rootItem, repositoryFileTree);
    } else {
      buildSolutionTree(null, repositoryFileTree);
      // sort the root elements
      ArrayList<TreeItem> roots = new ArrayList<TreeItem>();
      for (int i = 0; i < getItemCount(); i++) {
        roots.add(getItem(i));
      }
      Collections.sort(roots, new Comparator<TreeItem>() {
        public int compare(TreeItem o1, TreeItem o2) {
          return o1.getText().compareTo(o2.getText());
        }
      });
      clear();
      for (TreeItem myRootItem : roots) {
        addItem(myRootItem);
      }
    }
    if (selectedItem != null) {
      ArrayList<FileTreeItem> parents = new ArrayList<FileTreeItem>();
      while (selectedItem != null) {
        parents.add(selectedItem);
        selectedItem = (FileTreeItem) selectedItem.getParentItem();
      }
      Collections.reverse(parents);
      selectFromList(parents);
    } else {
      for (int i = 0; i < getItemCount(); i++) {
        ((FileTreeItem) getItem(i)).setState(true);
      }
    }
    WaitPopup.getInstance().setVisible(false);
  }

  public ArrayList<FileTreeItem> getAllNodes() {
    ArrayList<FileTreeItem> nodeList = new ArrayList<FileTreeItem>();
    for (int i = 0; i < this.getItemCount(); i++) {
      nodeList.add((FileTreeItem) this.getItem(i));
      getAllNodes((FileTreeItem) this.getItem(i), nodeList);
    }
    return nodeList;
  }

  private void getAllNodes(FileTreeItem parent, ArrayList<FileTreeItem> nodeList) {
    for (int i = 0; i < parent.getChildCount(); i++) {
      FileTreeItem child = (FileTreeItem) parent.getChild(i);
      nodeList.add(child);
      getAllNodes(child, nodeList);
    }
  }

  public ArrayList<String> getPathSegments(String path) {
    ArrayList<String> pathSegments = new ArrayList<String>();
    if (path != null) {
      if (path.startsWith("/")) { //$NON-NLS-1$
        path = path.substring(1);
      }
      StringTokenizer st = new StringTokenizer(path, '/');
      for (int i = 0; i < st.countTokens(); i++) {
        pathSegments.add(st.tokenAt(i));
      }
    }
    return pathSegments;
  }

  public FileTreeItem getTreeItem(final ArrayList<String> pathSegments) {
    if (pathSegments.size() > 0) {
      // the first path segment is going to be a 'root' in the tree
      String rootSegment = pathSegments.get(0);
      for (int i = 0; i < getItemCount(); i++) {
        FileTreeItem root = (FileTreeItem) getItem(i);
        if (root.getFileName().equalsIgnoreCase(rootSegment)) {
          ArrayList<String> tmpPathSegs = (ArrayList<String>) pathSegments.clone();
          tmpPathSegs.remove(0);
          return getTreeItem(root, tmpPathSegs);
        }
      }
    }
    return null;
  }

  private FileTreeItem getTreeItem(final FileTreeItem root, final ArrayList<String> pathSegments) {
    int depth = 0;
    FileTreeItem currentItem = root;
    while (depth < pathSegments.size()) {
      String pathSegment = pathSegments.get(depth);
      for (int i = 0; i < currentItem.getChildCount(); i++) {
        FileTreeItem childItem = (FileTreeItem) currentItem.getChild(i);
        if (childItem.getFileName().equalsIgnoreCase(pathSegment)) {
          currentItem = childItem;
        }
      }
      depth++;
    }
    // let's check if the currentItem matches our segments (it might point to the last item before
    // we eventually failed to find the complete match)
    FileTreeItem tmpItem = currentItem;
    depth = pathSegments.size()-1;
    while (tmpItem != null && depth >= 0) {
      if (tmpItem.getFileName().equalsIgnoreCase(pathSegments.get(depth))) {
        tmpItem = (FileTreeItem) tmpItem.getParentItem();
        depth--;
      } else {
        // every item must match
        return null;
      }
    }

    return currentItem;
  }

  private void selectFromList(ArrayList<FileTreeItem> parents) {
    FileTreeItem pathDown = null;
    for (int i = 0; i < parents.size(); i++) {
      FileTreeItem parent = parents.get(i);
      if (pathDown == null) {
        for (int j = 0; j < getItemCount(); j++) {
          FileTreeItem possibleItem = (FileTreeItem) getItem(j);
          if (parent.getFileName().equals(possibleItem.getFileName())) {
            pathDown = possibleItem;
            pathDown.setState(true, true);
            pathDown.setSelected(true);
            break;
          }
        }
      } else {
        for (int j = 0; j < pathDown.getChildCount(); j++) {
          FileTreeItem possibleItem = (FileTreeItem) pathDown.getChild(j);
          if (parent.getFileName().equals(possibleItem.getFileName())) {
            pathDown = possibleItem;
            pathDown.setState(true, true);
            break;
          }
        }
      }
    }
    if (pathDown != null) {
      setSelectedItem(pathDown);
      pathDown.setState(true, true);
    }
  }

  @SuppressWarnings("unchecked")
  private void buildSolutionTree(FileTreeItem parentTreeItem, RepositoryFileTree repositoryFileTree) {
    List<RepositoryFileTree> children = repositoryFileTree.getChildren();
    for (RepositoryFileTree treeItem:children) {
      RepositoryFile file = (RepositoryFile) treeItem.getFile();
      boolean isVisible = !file.isHidden();
      boolean isDirectory = file.isFolder();
      String fileName = file.getName();
      if ((isVisible || showHiddenFiles) && !(!StringUtils.isEmpty(fileName)
          && fileName.equals(ETC_FOLDER)) ) {
        
        // TODO Mapping Title to LocalizedName
        String localizedName = file.getTitle();
        String description = file.getDescription();
        FileTreeItem childTreeItem = new FileTreeItem();
        childTreeItem.getElement().setAttribute("id", file.getId());//$NON-NLS-1$
        childTreeItem.setUserObject(treeItem);
        ElementUtils.killAllTextSelection(childTreeItem.getElement());
        childTreeItem.setURL(fileName); 
        if (showLocalizedFileNames) {
          childTreeItem.setText(localizedName);
          if (isUseDescriptionsForTooltip() && !StringUtils.isEmpty(description)) {
            childTreeItem.setTitle(description);
          } else {
            childTreeItem.setTitle(fileName);
          }
        } else {
          childTreeItem.setText(fileName);
          if (isUseDescriptionsForTooltip() && !StringUtils.isEmpty(description)) {
            childTreeItem.setTitle(description);
          } else {
            childTreeItem.setTitle(localizedName);
          }
        }
        childTreeItem.setFileName(fileName);
        if (parentTreeItem == null && isDirectory) {
          addItem(childTreeItem);
        } else {

          try {
            // find the spot in the parentTreeItem to insert the node (based on showLocalizedFileNames)
            if (parentTreeItem.getChildCount() == 0) {
              parentTreeItem.addItem(childTreeItem);
            } else {
              // this does sorting
              boolean inserted = false;
              for (int j = 0; j < parentTreeItem.getChildCount(); j++) {
                FileTreeItem kid = (FileTreeItem) parentTreeItem.getChild(j);
                if (showLocalizedFileNames) {
                  if (childTreeItem.getText().compareTo(kid.getText()) <= 0) {
                    // leave all items ahead of the insert point
                    // remove all items between the insert point and the end
                    // add the new item
                    // add back all removed items
                    ArrayList<FileTreeItem> removedItems = new ArrayList<FileTreeItem>();
                    for (int x = j; x < parentTreeItem.getChildCount(); x++) {
                      FileTreeItem removedItem = (FileTreeItem) parentTreeItem.getChild(x);
                      removedItems.add(removedItem);
                    }
                    for (FileTreeItem removedItem : removedItems) {
                      parentTreeItem.removeItem(removedItem);
                    }
                    parentTreeItem.addItem(childTreeItem);
                    inserted = true;
                    for (FileTreeItem removedItem : removedItems) {
                      parentTreeItem.addItem(removedItem);
                    }
                    break;
                  }
                } else {
                  parentTreeItem.addItem(childTreeItem);
                  inserted = true;
                }
              }
              if (!inserted) {
                parentTreeItem.addItem(childTreeItem);
              }
            }
          } catch (Exception e) { /* Error with FF */
          }
        }
        FileTreeItem tmpParent = childTreeItem;
        String pathToChild = tmpParent.getFileName();
        while (tmpParent.getParentItem() != null) {
          tmpParent = (FileTreeItem) tmpParent.getParentItem();
          pathToChild = tmpParent.getFileName() + "/" + pathToChild; //$NON-NLS-1$
        }
        /* TODO Not sure what to do here
         * if (parentTreeItem != null) {
          ArrayList<FileChooserRepositoryFile> files = (ArrayList<FileChooserRepositoryFile>) parentTreeItem.getUserObject();
          if (files == null) {
            files = new ArrayList<FileChooserRepositoryFile>();
            parentTreeItem.setUserObject(files);
          }
          files.add(file);
        }*/
        if (isDirectory) {
          buildSolutionTree(childTreeItem, (RepositoryFileTree) treeItem);
        } else {
          if (parentTreeItem != null) {
            parentTreeItem.removeItem(childTreeItem);
          }
        }
      }
    }
  }

  
  public void setShowLocalizedFileNames(boolean showLocalizedFileNames) {
    this.showLocalizedFileNames = showLocalizedFileNames;
    // use existing tree and switch text/title
    for (int i = 0; i < getItemCount(); i++) {
      toggleLocalizedFileNames((FileTreeItem) getItem(i));
    }
  }

  private void toggleLocalizedFileNames(FileTreeItem parentTreeItem) {
    String title = parentTreeItem.getTitle();
    String text = parentTreeItem.getText();
    parentTreeItem.setTitle(text);
    parentTreeItem.setText(title);
    for (int i = 0; i < parentTreeItem.getChildCount(); i++) {
      toggleLocalizedFileNames((FileTreeItem) parentTreeItem.getChild(i));
    }
  }

  public boolean isShowHiddenFiles() {
    return showHiddenFiles;
  }

  public void setShowHiddenFiles(boolean showHiddenFiles) {
    this.showHiddenFiles = showHiddenFiles;
    onFetchRepositoryFileTree(repositoryFileTree);
  }

  public boolean isShowLocalizedFileNames() {
    return showLocalizedFileNames;
  }

  public boolean isUseDescriptionsForTooltip() {
    return useDescriptionsForTooltip;
  }

  public void setUseDescriptionsForTooltip(boolean useDescriptionsForTooltip) {
    this.useDescriptionsForTooltip = useDescriptionsForTooltip;
    onFetchRepositoryFileTree(repositoryFileTree);
  }

  public boolean isAdministrator() {
    return isAdministrator;
  }

  public void setAdministrator(boolean isAdministrator) {
    this.isAdministrator = isAdministrator;
  }

  public boolean isCreateRootNode() {
    return createRootNode;
  }

  Focusable getFocusable() {
    return this.focusable;
  }

  public List<RepositoryFile> getRepositoryFiles() {
    final FileTreeItem selectedTreeItem = (FileTreeItem) getSelectedItem();
    List<RepositoryFile> values = new ArrayList<RepositoryFile>();
    values.add((RepositoryFile) ((RepositoryFileTree) selectedTreeItem.getUserObject()).getFile());
    return values;
  }

}
