<%--
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
--%>

<!DOCTYPE html>
<%@page import="org.pentaho.platform.engine.core.system.PentahoSessionHolder" %>
<%
  String userName = PentahoSessionHolder.getSession().getName();
%>
<html lang="en" class="bootstrap">
<head>
<meta charset="utf-8" class="bootstrap">
<title>Browse Files</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<!-- Le styles -->
<link href="css/browser.css" rel="stylesheet">

<!-- We need web context for requirejs and css -->
<script type="text/javascript" src="webcontext.js?context=mantle&cssOnly=true"></script>

<!-- Avoid 'console' errors in browsers that lack a console. -->
<script type="text/javascript">
  if (!(window.console && console.log)) {
    (function () {
      var noop = function () {
      };
      var methods = ['assert', 'debug', 'error', 'info', 'log', 'trace', 'warn'];
      var length = methods.length;
      var console = window.console = {};
      while (length--) {
        console[methods[length]] = noop;
      }
    }());
  }
</script>

<!-- libs -->
<script type="text/javascript" src="lib/jquery/jquery-1.9.1.js"></script>
<script type="text/javascript" src="lib/underscore/underscore-min.js"></script>
<script type="text/javascript" src="lib/backbone/backbone.js"></script>

<!-- Require File Browser -->
<script type="text/javascript">
  function openRepositoryFile(path, mode) {
    if (!path) {
      return;
    }
    if (!mode) {
      mode = "edit";
    }

    // show the opened perspective
    var extension = path.split(".").pop();

    // force to open pdf files in another window due to issues with pdf readers in IE browsers
    // via class added on themeResources for IE browsers
    if (!($("body").hasClass("pdfReaderEmbeded") && extension == "pdf")) {
      parent.mantle_setPerspective('opened.perspective');
    }
    window.parent.mantle_openRepositoryFile(path, mode);
  }

  var FileBrowser = null;

  function initBrowser(canDownload, showHiddenFiles, showDescriptions, canPublish) {
    require(["js/browser"], function (pentahoFileBrowser) {
      FileBrowser = pentahoFileBrowser;
      FileBrowser.setOpenFileHandler(openRepositoryFile);
      FileBrowser.setContainer($("#fileBrowser"));
      FileBrowser.setShowHiddenFiles(showHiddenFiles);
      FileBrowser.setShowDescriptions(showDescriptions);
      FileBrowser.setCanDownload(canDownload);
      FileBrowser.setCanPublish(canPublish);
      
      var open_dir = window.top.HOME_FOLDER;
      
      $.ajax({
				url: CONTEXT_PATH + "api/mantle/session-variable?key=scheduler_folder",
				type: "GET",
				cache: false,
				async: true,
				success: function (response) {
					if(response != null && response.length > 0){
						open_dir = decodeURIComponent(response);
							$.ajax({
							url: CONTEXT_PATH + "api/mantle/session-variable?key=scheduler_folder",
							type: "DELETE",
							cache: false,
							async: true,
							success: function (response) {
							}
						});
					}
					FileBrowser.update(open_dir);
				}
			});
      
      if (window.top.mantle_addHandler == undefined) return;

      window.top.mantle_addHandler("ShowHiddenFilesEvent", function (event) {
        if (event.value != undefined) {
          //Clear the Browse Perspective cache
          window.top.mantle_isBrowseRepoDirty = true;
          FileBrowser.setShowHiddenFiles(event.value);
          FileBrowser.update(window.top.HOME_FOLDER);
        }
      });

      window.top.mantle_addHandler("ShowDescriptionsEvent", function (event) {
        if (event.value != undefined) {
          FileBrowser.updateShowDescriptions(event.value);
        }
      });


      // refresh file list on successful delete
      window.top.mantle_addHandler("SolutionFileActionEvent", function (event) {
        if (event.action.indexOf('DeleteFileCommand') >= 0 ||
            (event.action.indexOf('RestoreFileCommand') >= 0) ||
            (event.action.indexOf('DeletePermanentFileCommand') >= 0)) {
          if (event.message == 'Success') {
            window.top.mantle_isBrowseRepoDirty = true;
            refreshFileBrowser(event);
          }
          else {
            window.top.mantle_showMessage('Error', event.message);
          }
        }
        else if ((event.action.indexOf('ScheduleHelper') >= 0) ||
                (event.action.indexOf('ShareFileCommand') >= 0)) {
            if (event.message == 'Open' || event.message == 'Success') {
                window.top.mantle_setPerspective('browser.perspective'); // change to browse perspective
            }
        }
        else if (event.action.indexOf('CutFilesCommand') >= 0 ||
                event.action.indexOf('CopyFilesCommand') >= 0) {

            var model = FileBrowser.fileBrowserModel;
            var browserUtils = model.get('browserUtils');
            var fileListModel = model.get('fileListModel');
            var multiSelectItems = cutItems = FileBrowser.concatArray(fileListModel.get("multiSelect"), fileListModel.get("shiftLasso"));

            switch (event.message) {
                case 'Click':
                    switch (event.action) {
                        case "org.pentaho.mantle.client.commands.CutFilesCommand":
                            browserUtils.trackItems(cutItems, multiSelectItems);
                            browserUtils.uiButtonFeedback([$("#cutButton"), $("#cutbutton")], [$("#copyButton")]);
                            break;
                        case "org.pentaho.mantle.client.commands.CopyFilesCommand":
                            browserUtils.trackItems(new Array(), multiSelectItems);
                            browserUtils.uiButtonFeedback([$("#copyButton")], [$("#cutButton"), $("#cutbutton")]);
                            break;
                    }
                    break;
                case 'Success':
                    switch (event.action) {
                        case "org.pentaho.mantle.client.commands.CutFilesCommand":
                            break;
                        case "org.pentaho.mantle.client.commands.CopyFilesCommand":
                            break;
                    }
                    break;
                default:
                    switch (event.action) {
                        case "org.pentaho.mantle.client.commands.PasteFilesCommand":
                            //Handle errors
                            break;
                    }
                    break;
            }
        }
      });

      window.top.mantle_addHandler("SolutionFolderActionEvent", function (event) {
          // refresh folder list on create new folder / delete folder / import
          if (event.action.indexOf('NewFolderCommand') >= 0 ||
                  event.action.indexOf('DeleteFolderCommand') >= 0 ||
                  event.action.indexOf('ImportFileCommand') >= 0 ||
                  event.action.indexOf('DeleteFolderCommand') >= 0) {
              if (event.message == 'Success') {
                  refreshFileBrowser(event);
              }
              else {
                  window.top.mantle_showMessage('Error', event.message);
              }
          }
          else if (event.action.indexOf('PasteFilesCommand') >= 0) {
              var model = FileBrowser.fileBrowserModel;
              var browserUtils = model.get('browserUtils');
              var fileListModel = model.get('fileListModel');

              switch (event.message) {
                  case 'Success':
                      switch (event.action) {
                          case "org.pentaho.mantle.client.commands.PasteFilesCommand":
                              refreshFileBrowser(event);
                              break;
                      }
                      break;
                  case 'Click':
                      switch (event.action) {
                          case "org.pentaho.mantle.client.commands.PasteFilesCommand":
                              browserUtils.uiSpinnerFeedback([], [fileListModel]);
                              break;
                      }
                      break;
                  case 'Cancel':
                      switch (event.action) {
                          case "org.pentaho.mantle.client.commands.PasteFilesCommand":
                              browserUtils.uiSpinnerFeedback([fileListModel], []);
                              break;
                      }
                      break;
                  default:
                      switch (event.action) {
                          case "org.pentaho.mantle.client.commands.PasteFilesCommand":
                              //Handle errors
                              browserUtils.uiSpinnerFeedback([fileListModel], []);
                              break;
                      }
                      break;
              }
          }
      });

      window.top.mantle_addHandler("GenericEvent", function (paramJson) {
        if (paramJson.eventSubType == "OpenFolderEvent") {
          FileBrowser.openFolder(paramJson.stringParam);
        }
        else if (paramJson.eventSubType == "RefreshBrowsePerspectiveEvent") {
          //Clear the Browse Perspective cache
          window.top.mantle_isBrowseRepoDirty = true;
          FileBrowser.update(window.top.HOME_FOLDER); // refresh folder list
        }
        else if (paramJson.eventSubType == "RefreshFolderEvent") {
          FileBrowser.update(paramJson.stringParam); // refresh specified folder
        }
        else if (paramJson.eventSubType == "RefreshCurrentFolderEvent") {
          FileBrowser.updateData();
        }
        else if (paramJson.eventSubType == "ImportDialogEvent") {
          FileBrowser.update(FileBrowser.fileBrowserModel.getFolderClicked().attr("path")); // refresh folder list
        } else if (paramJson.eventSubType == "RefreshFileEvent") {
            FileBrowser.updateFile(paramJson.stringParam);
        }
      });
    });
  }

  function refreshFileBrowser(event) {
    if (FileBrowser.fileBrowserModel.getFolderClicked()) {

        //Refresh folders to parent path if deleting a folder
        if ((event.action.indexOf('DeleteFolderCommand') >= 0)) {
            var path = FileBrowser.fileBrowserModel.getFolderClicked().attr("path");
            var parentPath = path.substring(0, path.lastIndexOf("/"));
            FileBrowser.update(parentPath); // refresh folder list
        }
        //Restore to last clicked folder path for all other actions
        else {
            FileBrowser.update(FileBrowser.fileBrowserModel.getFolderClicked().attr("path")); // refresh folder list
        }
    }
    //If no last clicked folder, restore to home folder
    else {
        FileBrowser.update(window.top.HOME_FOLDER);
    }
  }

  function checkDownload() {
    $.ajax({
      url: CONTEXT_PATH + "api/authorization/action/isauthorized?authAction=org.pentaho.security.administerSecurity",
      type: "GET",
      async: true,
      success: function (response) {
        checkShowHiddenFiles(response == "true");
      },
      error: function (response) {
        checkShowHiddenFiles(false);
      }
    });
  }

  function checkShowHiddenFiles(canDownload) {
    $.ajax({
      url: CONTEXT_PATH + "api/user-settings/MANTLE_SHOW_HIDDEN_FILES",
      type: "GET",
      async: true,
      success: function (response) {
        checkShowDescriptions(canDownload, response == "true");
      },
      error: function (response) {
        checkShowDescriptions(canDownload, false);
      }
    });
  }

  function checkShowDescriptions(canDownload, showHiddenFiles) {
    $.ajax({
      url: CONTEXT_PATH + "api/user-settings/MANTLE_SHOW_DESCRIPTIONS_FOR_TOOLTIPS",
      type: "GET",
      async: true,
      success: function (response) {
        checkPublish(canDownload, showHiddenFiles, (response == "true"));
      },
      error: function (response) {
        checkPublish(canDownload, showHiddenFiles, false);
      }
    });
  }

  function checkPublish(canDownload, showHiddenFiles, showDescriptions) {
    $.ajax({
      url: CONTEXT_PATH + "api/authorization/action/isauthorized?authAction=org.pentaho.security.administerSecurity",
      type: "GET",
      async: true,
      success: function (response) {
        initBrowser(canDownload, showHiddenFiles, showDescriptions, (response == "true"));
      },
      error: function (response) {
        initBrowser(canDownload, showHiddenFiles, showDescriptions, false);
      }
    });
  }

  //init component
  checkDownload();

  function openFolder(path) {

  }

</script>

</head>

<body data-spy="scroll" data-target=".sidebar">


<div class="container-fluid main-container fill-absolute">
  <div id="fileBrowser" class="row-fluid fill-absolute">
  </div>
</div>
</body>
</html>
