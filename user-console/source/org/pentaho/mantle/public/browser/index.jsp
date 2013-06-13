<!DOCTYPE html>
<%@page import="org.pentaho.platform.engine.core.system.PentahoSessionHolder"%>
<%
    String userName = PentahoSessionHolder.getSession().getName();
%>
<html lang="en"  class="bootstrap">
<head>
    <meta charset="utf-8" class="bootstrap">
    <title>Browse Files</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <!-- Le styles -->
    <link href="css/browser.css" rel="stylesheet">

    <!-- We need web context for requirejs and css -->
    <script type="text/javascript" src="webcontext.js?context=mantle&cssOnly=true"></script>

    <!-- Require File Browser -->
    <script type="text/javascript">
        function openRepositoryFile(path, mode) {
            if(!path) {
                return;
            }
            if(!mode) {
                mode = "edit";
            }

            // show the opened perspective
            parent.mantle_setPerspective('opened.perspective');
            window.parent.mantle_openRepositoryFile(path, mode);
        }

        var FileBrowser = null;

        function initBrowser(showHiddenFiles, showDescriptions){
            pen.require(["js/browser"], function(pentahoFileBrowser) {
                FileBrowser = pentahoFileBrowser;
                FileBrowser.setOpenFileHandler(openRepositoryFile);
                FileBrowser.setContainer($("#fileBrowser"));
                FileBrowser.setShowHiddenFiles(showHiddenFiles);
                FileBrowser.setShowDescriptions(showDescriptions);
                FileBrowser.update(window.top.HOME_FOLDER);

                if(window.top.mantle_addHandler == undefined) return;

                window.top.mantle_addHandler("ShowHiddenFilesEvent", function(event){
                    if(event.value != undefined){
                        FileBrowser.setShowHiddenFiles(event.value);
                        FileBrowser.update(window.top.HOME_FOLDER);
                    }
                });

                window.top.mantle_addHandler("ShowDescriptionsEvent", function(event){
                    if(event.value != undefined){
                        FileBrowser.updateShowDescriptions(!event.value);
                    }
                });


                // refresh file list on successful delete
                window.top.mantle_addHandler("SolutionFileActionEvent", function(event){
                    if(event.action.indexOf('DeleteFileCommand') >= 0 ||
                            (event.action.indexOf('RestoreFileCommand') >= 0)||
                            (event.action.indexOf('DeletePermanentFileCommand')>= 0)){
                        if(event.message == 'Success'){
                            //Refresh folders to parent path if restoring a file or folder
                            if((event.action.indexOf('RestoreFileCommand')>= 0)){
                                var parentPath=FileBrowser.fileBrowserModel.getFileClicked().attr("originalParentFolderPath");
                                FileBrowser.update(parentPath); // refresh folder list
                            }
                            FileBrowser.updateData(); // refresh file list
                        }
                        else{
                            window.top.mantle_showMessage('Error', event.message);
                        }
                    }
                });

                window.top.mantle_addHandler("SolutionFileActionEvent", function(event){
                    if((event.action.indexOf('ScheduleHelper') >= 0)    ||
                            (event.action.indexOf('ShareFileCommand') >= 0)){
                        if(event.message == 'Open' || event.message == 'Success'){
                            window.top.mantle_setPerspective('browser.perspective'); // change to browse perspective
                        }
                    }
                });

                // refresh folder list on create new folder / delete folder / import
                window.top.mantle_addHandler("SolutionFolderActionEvent", function(event){
                    if(event.action.indexOf('NewFolderCommand') >= 0   ||
                            event.action.indexOf('DeleteFolderCommand') >= 0 ||
                            event.action.indexOf('ImportFileCommand') >= 0   ||
                            event.action.indexOf('PasteFilesCommand') >= 0   ||
                            event.action.indexOf('DeleteFolderCommand') >= 0 ) {
                        if(event.message == 'Success'){
                            if(FileBrowser.fileBrowserModel.getFolderClicked()){

                                //Refresh folders to parent path if deleting a folder
                                if((event.action.indexOf('DeleteFolderCommand')>= 0)){
                                    var path=FileBrowser.fileBrowserModel.getFolderClicked().attr("path");
                                    var parentPath= path.substring(0, path.lastIndexOf("/"));
                                    FileBrowser.update(parentPath); // refresh folder list
                                }
                                //Restore to last clicked folder path for all other actions
                                else {
                                    FileBrowser.update(FileBrowser.fileBrowserModel.getFolderClicked().attr("path")); // refresh folder list
                                }
                            }
                            //If no last clicked folder, restore to home folder
                            else{
                                FileBrowser.update(window.top.HOME_FOLDER);
                            }
                        }
                        else{
                            window.top.mantle_showMessage('Error', event.message);
                        }
                }
            });

            window.top.mantle_addHandler("GenericEvent", function(paramJson){
          if(paramJson.eventSubType == "OpenFolderEvent"){
                    FileBrowser.openFolder(paramJson.stringParam);
          }
          else if(paramJson.eventSubType == "RefreshBrowsePerspectiveEvent"){
            FileBrowser.update(window.top.HOME_FOLDER); // refresh folder list
          }
          else if(paramJson.eventSubType == "RefreshCurrentFolderEvent"){
            FileBrowser.updateData();
          }
            });
        });
        }

        function checkShowHiddenFiles(){
            $.ajax({
                url: "/pentaho/api/user-settings/MANTLE_SHOW_HIDDEN_FILES",
                type: "GET",
                async: true,
                success: function(response){
                    checkShowDescriptions(response == "true");
                },
                error: function(response){
                    checkShowDescriptions(false);
                }
            });
        }

        function checkShowDescriptions(showHiddenFiles){
            $.ajax({
                url: "/pentaho/api/user-settings/MANTLE_SHOW_DESCRIPTIONS_FOR_TOOLTIPS",
                type: "GET",
                async: true,
                success: function(response){
                    initBrowser(showHiddenFiles, !(response == "true"));
                },
                error: function(response){
                    initBrowser(showHiddenFiles, false);
                }
            });
        }

        //init component

        checkShowHiddenFiles();

        function openFolder(path){

        }

    </script>

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="bootstrap/js/html5shiv.js"></script>
    <![endif]-->

</head>

<body data-spy="scroll" data-target=".sidebar">


<div class="container-fluid main-container fill-absolute">
    <div id="fileBrowser" class="row-fluid fill-absolute">
    </div>


    <!-- libs -->
    <script type="text/javascript" src="lib/jquery/jquery-1.9.1.js"></script>
    <script type="text/javascript" src="lib/underscore/underscore-min.js"></script>
    <script type="text/javascript" src="lib/backbone/backbone.js"></script>
</div>
</body>
</html>