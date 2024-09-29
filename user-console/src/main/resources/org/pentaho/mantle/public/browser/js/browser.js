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
 * Copyright (c) 2002-2023 Hitachi Vantara..  All rights reserved.
 */
define([
  "./browser.fileButtons",
  "./browser.folderButtons",
  "./browser.trashButtons",
  "./browser.trashItemButtons",
  "./browser.utils",
  "./browser.multiSelectButtons",
  "./dialogs/browser.dialog.rename",
  "common-ui/util/spin",
  "common-ui/util/PentahoSpinner",
  "./browser.templates",
  "common-ui/util/URLEncoder",
  "common-ui/util/_a11y",
  "common-ui/bootstrap",
  "common-ui/handlebars",
  "common-ui/jquery-pentaho-i18n",
  "common-ui/jquery"
], function (FileButtons, FolderButtons, TrashButtons, TrashItemButtons, BrowserUtils, MultiSelectButtons, RenameDialog, Spinner, spin, templates, Encoder, a11yUtil) {

  const REPOSITORY_ROOT_PATH = "/";

  if (window.parent.mantle_isBrowseRepoDirty == undefined) {
    window.parent.mantle_isBrowseRepoDirty = false;
  }
  this.FileBrowser = {};

  FileBrowser.urlParam = function (paramName) {
    var value = new RegExp('[\\?&]' + paramName + '=([^&#]*)').exec(window.parent.location.href);
    if (value) {
      return value[1];
    } else {
      return null;
    }
  };

  var locale = FileBrowser.urlParam('locale');
  if( !locale ) {
    // look to see if locale is set on the page in a meta tag
    var localeMeta = $("meta[name='locale']");
    if(localeMeta) {
      locale = localeMeta.attr("content");
    }
  }

  // retrieve i18n map
  jQuery.i18n.properties({
    name: 'messages',
    mode: 'map',
    language: locale
  });

  var renameDialog = new RenameDialog(jQuery.i18n);
  var fileButtons = new FileButtons(jQuery.i18n);
  var folderButtons = new FolderButtons(jQuery.i18n);
  var trashButtons = new TrashButtons(jQuery.i18n);
  var trashItemButtons = new TrashItemButtons(jQuery.i18n);
  var browserUtils = new BrowserUtils();
  var multiSelectButtons = new MultiSelectButtons(jQuery.i18n);
  // BACKLOG-40444 -- After the addition of vfs connections to fetch all the folders with depth of 3 has become a
  // performance issue. Changing the depth to 1 to fetch the folders lazily. For the last clicked folders if it's depth
  // is more than one we are using extendedPath param to fetch the tree structure of last clicked folder and remaining
  // all folders loads with depth of 1.
  var depth = 1;

  fileButtons.renameDialog = renameDialog;
  folderButtons.renameDialog = renameDialog;

  FileBrowser.$container = null;
  FileBrowser.fileBrowserModel = null;
  FileBrowser.fileBrowserView = null;
  FileBrowser.openFileHandler = undefined;
  FileBrowser.showHiddenFiles = false;
  FileBrowser.showDescriptions = false;
  FileBrowser.canDownload = false;
  FileBrowser.canPublish = false;
  FileBrowser.canRead = false;
  FileBrowser.canCreate = false;

  /**
   * Encode a path that has the slashes converted to colons
   **/
  FileBrowser.encodePathComponents = function (path) {
    return Encoder.encode("{0}", path);
  };

  FileBrowser.encodeGenericFilePathComponents = function (path) {
    const encodedFilePath = Encoder.encodeGenericFilePath(path);
    return Encoder.encode("{0}", encodedFilePath);
  }

  FileBrowser.setShowHiddenFiles = function (value) {
    this.showHiddenFiles = value;
  };

  FileBrowser.setShowDescriptions = function (value) {
    this.showDescriptions = value;
  };

  FileBrowser.setCanDownload = function (value) {
    this.canDownload = value;
  }

  FileBrowser.setCanPublish = function (value) {
    this.canPublish = value;
  }

  FileBrowser.setCanRead = function (value) {
    this.canRead = value;
  }

  FileBrowser.setCanCreate = function (value) {
    this.canCreate = value;
  }

  FileBrowser.updateShowDescriptions = function (value) {
    this.setShowDescriptions(value);
    this.fileBrowserModel.set("showDescriptions", value);
  };

  FileBrowser.setContainer = function ($container) {
    this.$container = $container;
  };

  FileBrowser.setOpenFileHandler = function (handler) {
    this.openFileHandler = handler;
  };

  FileBrowser.update = function (initialPath, showDescriptions) {
    this.redraw(initialPath, showDescriptions);
  };

  FileBrowser.updateFolder = function (clickedPath, showDescriptions) {
    // Sets initialPath as the clicked folder
    if (this.fileBrowserModel && clickedPath) {
      this.fileBrowserModel.set("clickedFolder", {
        obj: $("[path='" + escapeCssSelector(clickedPath) + "']"),
        time: (new Date()).getTime()
      });
    }
    this.redraw(clickedPath, showDescriptions);
  };

  FileBrowser.updateData = function () {
    if (this.fileBrowserModel != null && this.fileBrowserModel.get('fileListModel') != null) {
      this.fileBrowserModel.get('fileListModel').updateData();
    }
  };

  FileBrowser.concatArray = function (arr1, arr2) {
    for (var i = 0; i < arr2.length; i++) {
      FileBrowser.pushUnique(arr1, arr2[i]);
    }
    return arr1;
  };

  FileBrowser.pushUnique = function (array, item) {
    var exists = false;
    for (var i = 0; i < array.length; i++) {
      if (item.obj.attr("id") == array[i].obj.attr("id")) {
        exists = true;
        break;
      }
    }
    if (!exists) {
      array.push(item);
    }
  };

  FileBrowser.redraw = function (initialPath, _showDescriptions) {
    var myself = this;
    var _clikedFolder = undefined;
    var _clickedFile = undefined;
    var _lastClick = "folder";
    if ( this.fileBrowserModel ) {
      _clikedFolder = {
        obj: this.fileBrowserModel.getFolderClicked(),
        time: (new Date()).getTime()
      };
      _clickedFile = {
        obj: this.fileBrowserModel.getFileClicked(),
        time: (new Date()).getTime()
      };
      _lastClick = this.fileBrowserModel.getLastClick();
    }
    //if we have not new parameter, than save previous
    if ( _showDescriptions == undefined ) {
      _showDescriptions = myself.showDescriptions;
    }
    myself.fileBrowserModel = new FileBrowserModel({
      spinConfig: spin,
      openFileHandler: myself.openFileHandler,
      showHiddenFiles: myself.showHiddenFiles,
      showDescriptions: _showDescriptions,
      canDownload: myself.canDownload,
      canPublish: myself.canPublish,
      canRead: myself.canRead,
      canCreate: myself.canCreate,
      startFolder: initialPath,
      clickedFolder: _clikedFolder,
      clickedFile: _clickedFile,
      lastClick: _lastClick
    });
    myself.FileBrowserView = new FileBrowserView({
      model: myself.fileBrowserModel,
      el: myself.$container
    });

    //BISERVER-10586 - need to run listener after fileBrowserModel listener
    myself.fileBrowserModel.on("change:clickedFolder", myself.fileBrowserModel.updateFileList, myself.fileBrowserModel);

    //Kill text selection in all IE browsers for the browse perspective
    $("#fileBrowser").bind("selectstart", function(){return false});
  };

  FileBrowser.openFolder = function (path) {
    //first select folder
    var $folder = $("[path='"+escapeCssSelector(path)+"']"),
        $parentFolder = $folder.parent(".folders");
    while(!$parentFolder.hasClass("body") && $parentFolder.length > 0){
      $parentFolder.show();
      $parentFolder.parent().addClass("open");
      $parentFolder = $parentFolder.parent().parent();
    }
    $folder.find("> .element .name").trigger("click");
  };

  FileBrowser.clearTreeCache = function () {
    let url = CONTEXT_PATH + "plugin/scheduler-plugin/api/generic-files/tree/cache";

    $.ajax({
      async: false,
      cache: false, // prevent IE from caching the request
      type: 'DELETE',
      dataType: "json",
      url: url,
      success: function (response) {
      },
      error: function () {
        //TODO indicate error via dialog?
      },
      beforeSend: function (xhr) {
      }
    });
  };

  var FileBrowserModel = Backbone.Model.extend({
    defaults: {
      showHiddenFilesURL: CONTEXT_PATH + "api/user-settings/MANTLE_SHOW_HIDDEN_FILES",
      fileButtons: fileButtons,
      folderButtons: folderButtons,
      trashButtons: trashButtons,
      trashItemButtons: trashItemButtons,
      browserUtils: browserUtils,
      multiSelectButtons: multiSelectButtons,

      foldersTreeModel: undefined,
      fileListModel: undefined,

      clickedFolder: undefined,
      clickedFile: undefined,
      startFolder: window.parent.HOME_FOLDER,
      lastClick: "folder",
      data: undefined,
      openFileHandler: undefined,

      showHiddenFiles: false,
      showDescriptions: false,

      canDownload: false,
      canPublish: false,
      spinConfig: undefined
    },

    initialize: function () {
      var myself = this,
          foldersTreeModel = myself.get("foldersTreeModel"),
          fileListModel = myself.get("fileListModel");
      //handle data
      var foldersObj = {}
      //get spinner and give a new to each browser
      var config = myself.get("spinConfig").getLargeConfig();
      var spinner1 = new Spinner(config),
          spinner2 = new Spinner(config);

      function getClickedFolder(model) {
        return model && model.get("clickedFolder") && model.get("clickedFolder").obj;
      }

      function getClickedFile(model) {
        return model && model.get("clickedFile") && model.get("clickedFile").obj;
      }

      var _clickedFolderObj = getClickedFolder(foldersTreeModel) ?? getClickedFolder(myself);
      var _clickedFolder = _clickedFolderObj ? {obj: _clickedFolderObj, time: (new Date()).getTime()} : null;

      var _clickedFileObj = getClickedFile(fileListModel) ?? getClickedFile(myself);
      var _clickedFile = _clickedFileObj ? {obj: _clickedFileObj, time: (new Date()).getTime()} : null;

      //create two models
      foldersTreeModel = new FileBrowserFolderTreeModel({
        spinner: spinner1,
        showHiddenFiles: myself.get("showHiddenFiles"),
        showDescriptions: myself.get("showDescriptions"),
        startFolder: myself.get("startFolder"),
        clickedFolder: _clickedFolder
      });
      fileListModel = new FileBrowserFileListModel({
        spinner: spinner2,
        openFileHandler: myself.get("openFileHandler"),
        showHiddenFiles: myself.get("showHiddenFiles"),
        showDescriptions: myself.get("showDescriptions"),
        clickedFile: _clickedFile
      });

      //assign backbone events
      foldersTreeModel.on("change:clicked", myself.updateClicked, myself);
      foldersTreeModel.on("change:clickedFolder", myself.updateFolderClicked, myself);

      fileListModel.on("change:clickedFile", myself.updateFileClicked, myself);

      //handlers for buttons header update
      foldersTreeModel.on("change:clicked", myself.updateFolderLastClick, myself);
      fileListModel.on("change:clicked", myself.updateFileLastClick, myself);

      myself.set("foldersTreeModel", foldersTreeModel);
      myself.set("fileListModel", fileListModel);

      myself.on("change:showDescriptions", myself.updateDescriptions, myself);

      window.parent.mantle_addHandler("FavoritesChangedEvent", $.proxy(myself.onFavoritesChanged, myself));
    },
    onFavoritesChanged: function () {
      // BISERVER-9127  - Reselect current file
      var that = this;
      setTimeout(function () {
        that.get('fileListModel').trigger("change:clickedFile");
      }, 100);
    },

    updateClicked: function () {
      this.set("clicked", true);
    },

    updateFolderClicked: function () {
      var clickedFolder = this.get("foldersTreeModel").get("clickedFolder");
      var folderPath = clickedFolder.obj.attr("path");
      if (folderPath == ".trash") {
        this.updateTrashLastClick();
      }
      this.set("clickedFolder", clickedFolder);
      this.updateFolderButtons(folderPath);
    },

    updateFolderButtons: function( _folderPath) {
      const isRepositoryFolderPath = !!_folderPath && isRepositoryPath(_folderPath) && !isRepositoryRootPath(_folderPath);

      var userHomePath = Encoder.encodeRepositoryPath(window.parent.HOME_FOLDER);
      var model = FileBrowser.fileBrowserModel; // trap model
      var folderPath = Encoder.encodeRepositoryPath(_folderPath);

      // BACKLOG-40475, BACKLOG-41134: Disable and hide all folder actions for roots "Repository" and "VFS Connections", along with all PVFS folders.
      // "Repository" and "VFS Connections" are technically folders in the tree, but shouldn't be considered regular "folders" from a user perspective.
      // They should be considered "folder categories" or something of the like. We have called them "provider roots" in the scope of this code.
      folderButtons.enableButtons(isRepositoryFolderPath);

      // BACKLOG-23730: server+client side code uses centralized logic to check if user can download/upload

      //Ajax request to check if user can download
      if (isRepositoryFolderPath) {
        $.ajax({
          url: CONTEXT_PATH + "api/repo/files/canDownload?dirPath=" + encodeURIComponent(_folderPath),
          type: "GET",
          async: true,
          success: function (response) {
            folderButtons.canDownload(response == "true");
          },
          error: function (response) {
            folderButtons.canDownload(false);
          }
        });

        // Ajax request to check if user can upload (a.k.a. publish)
        $.ajax({
          url: CONTEXT_PATH + "api/repo/files/canUpload?dirPath=" + encodeURIComponent(_folderPath),
          type: "GET",
          async: true,
          success: function (response) {
            folderButtons.canPublish(response == "true");
          },
          error: function (response) {
            folderButtons.canPublish(false);
          }
        });

        //Ajax request to check write permissions for folder
        $.ajax({
          url: CONTEXT_PATH + 'api/repo/files/' + FileBrowser.encodePathComponents(folderPath) + '/canAccessMap',
          type: "GET",
          beforeSend: function (request) {
            request.setRequestHeader('accept', 'application/json');
          },
          data: {"permissions": "1"}, //check write permissions for the given folder
          async: true,
          cache: false,
          success: function (response) {
            folderButtons.updateFolderPermissionButtons(response, model.get('browserUtils').multiSelectItems, !(folderPath == userHomePath));
          },
          error: function (response) {
            folderButtons.updateFolderPermissionButtons(false, model.get('browserUtils').multiSelectItems, false);
          }
        });
      } //else {
        // This is a VFS Connection path
        //TODO BACKLOG-40086: refactor when we've implemented permissions for vfs connections folders/files
        //}
    },

    updateFileClicked: function () {
      var clickedFile = this.get("fileListModel").get("clickedFile");
      this.set("clickedFile", clickedFile);

      var clickedFolder = this.get("clickedFolder");

      var filePath = clickedFile.obj.attr("path");
      const isRepoPath = isRepositoryPath(filePath);

      //Repository Only Logic
      //Ajax request to check write permissions for file
      if( isRepoPath ) {
        var didClickTrash = (!!clickedFolder) && clickedFolder.obj.attr("path") == ".trash";
        if (didClickTrash) {
          this.updateTrashItemLastClick();
        }

        if ( clickedFile == null ) {
          fileButtons.updateFilePermissionButtons(false);
          fileButtons.canDownload(false);
          return;
        }

        if (!didClickTrash) {
          // BISERVER-9127 - Provide the selected path to the FileButtons object
          fileButtons.onFileSelect(clickedFile.obj.attr("path"));
        }

        fileButtons.canDownload(this.get("canDownload"));

        filePath = Encoder.encodeRepositoryPath(filePath);

        $.ajax({
          url: CONTEXT_PATH + 'api/repo/files/' + FileBrowser.encodePathComponents(filePath) + '/canAccessMap',
          type: "GET",
          beforeSend: function (request) {
            request.setRequestHeader('accept', 'application/json');
          },
          data: {"permissions": "1|2"}, //check write and delete permissions for the given file
          async: true,
          cache: false,
          success: function (response) {
            fileButtons.updateFilePermissionButtons(response);
          },
          error: function (response) {
            fileButtons.updateFilePermissionButtons(false);
          }
        });
      } //else {
        // this is a VFS connection path
        //TODO BACKLOG-40086: refactor when we've implemented permissions for vfs connections folders/files
        //}
    },

    updateFolderLastClick: function () {
      this.set("lastClick", "folder");
    },

    updateFileLastClick: function () {
      this.set("lastClick", "file");
    },

    updateTrashLastClick: function () {
      this.set("lastClick", "trash");
    },

    updateTrashItemLastClick: function () {
      this.set("lastClick", "trashItem");
    },

    getLastClick: function () {
      return this.get("lastClick");
    },

    getFolderClicked: function () {
      return this.get("clickedFolder") == null || this.get("clickedFolder") == undefined ? null : this.get("clickedFolder").obj;
    },

    getFileClicked: function () {
      return this.get("clickedFile") == null || this.get("clickedFile") == undefined ? null : this.get("clickedFile").obj; // [BISERVER-9128] - wrap in jquery object
    },

    updateFileList: function () {
      var myself = this;
      //trigger file list update. Force event in case path was not changed
      myself.get("fileListModel").set("path", myself.get("clickedFolder").obj.attr("path"), {silent:true});
      myself.get("fileListModel").trigger('change:path');
    },

    updateDescriptions: function () {
      var myself = this;
      myself.get("fileListModel").set("showDescriptions", myself.get("showDescriptions"));
      myself.get("foldersTreeModel").set("showDescriptions", myself.get("showDescriptions"));
    }
  });

  var FileBrowserFolderTreeModel = Backbone.Model.extend({
    defaults: {
      clicked: false,
      clickedFolder: undefined,

      data: undefined,
      updateData: false,

      runSpinner: false,
      spinner: undefined,

      showHiddenFiles: false,
      showDescriptions: false,
      sequenceNumber: 0
    },

    initialize: function () {
      var myself = this;
      myself.on("change:updateData", myself.updateData, myself);
    },

    updateData: function () {
      var myself = this;
      const trashName = jQuery.i18n.prop('trash');
      const trashFolder = {
        "file": {
          "trash": "trash",
          "createdDate": "1365427106132",
          "fileSize": "0",
          "folder": true,
          "hidden": "false",
          "objectId:": jQuery.i18n.prop('trash'),
          "locale": "en",
          "locked": "false",
          "name": trashName,
          "nameDecoded": trashName,
          "ownerType": "-1",
          "path": ".trash",
          "title": trashName,
          "versioned": "false"
        }
      };

      myself.set("runSpinner", true);
      myself.fetchTreeRootData(function (response) {

        //Add the trash folder once to the first Repository folder
        myself.getRepositoryFolderChildren(response).push(trashFolder);

        response.children = reformatResponse(response).children;
        myself.set("data", response);
      });
    },

    fetchTreeRootData: function (callback) {
      var myself = this,
          localSequenceNumber = myself.get("sequenceNumber"),
          url = this.getFolderTreeRootRequest();

      FileBrowser.clearTreeCache();

      $.ajax({
        async: true,
        cache: false, // prevent IE from caching the request
        dataType: "json",
        url: url,
        success: function (response) {
          if (localSequenceNumber == myself.get("sequenceNumber") && callback != undefined) {
            myself.set("sequenceNumber", localSequenceNumber + 1);
            callback(customSort(response));
          }
        },
        error: function () {
        },
        beforeSend: function (xhr) {
          myself.set("runSpinner", true);
        }
      });
    },

    _getResolvedClickedFolder: function() {
       const clickedFolder = this.get("clickedFolder")?.obj?.attr("path");
       return clickedFolder && clickedFolder !== '.trash' ? clickedFolder : null;
    },

    _getResolvedStartFolder: function() {
      const startFolder = FileBrowser.fileBrowserModel.get("startFolder");
      return startFolder && startFolder !== '.trash' ? startFolder : null;
    },

    getFolderTreeRootRequest: function(path) {
      const expandedPath = this._getResolvedClickedFolder() || this._getResolvedStartFolder();
      const expandedPathParam = expandedPath ? (`&expandedPath=${encodeURIComponent(expandedPath)}`) : "";

      /**
       * TODO BACKLOG-40086: In order to prevent regressions in current behavior we include the depth parameter here
       * like it was for repository api.
       * This is expensive, however, fetching the entire folder tree to whatever depth, just to view a specific folder.
       */
      return `${CONTEXT_PATH}plugin/scheduler-plugin/api/generic-files/tree?depth=${depth}&filter=FOLDERS&showHidden=${
        this.get("showHiddenFiles")}${expandedPathParam}`;
    },

    getRepositoryFolderChildren: function (response) {
      for (let i = 0; i < response.children.length; i++) {
        const childFolder = response.children[i];
        if (isRepositoryRootPath(childFolder.file.path)) {
          return childFolder.children;
        }
      }
      return response.children;
    }
  });

  var FileBrowserFileListModel = Backbone.Model.extend({
    defaults: {
      clicked: false,
      clickedFile: undefined,
      anchorPoint: undefined,
      multiSelect: [],
      shiftLasso: [],
      clipboard: [],

      data: undefined,
      cachedData: {},
      path: "/",

      runSpinner: false,
      spinner: undefined,

      openFileHander: undefined,

      showHiddenFiles: false,
      showDescriptions: false,
      deletedFiles: "",

      sequenceNumber: 0,
      keysSoFar: ""
    },

    initialize: function () {
      var myself = this;
      myself.set("cachedData", {}); // clear cached data on initialization - Backbone relates to old cachedData when new object is created
      myself.on("change:path", myself.updateData, myself);
    },

    reformatTrashResponse: function (myself, response) {
      var newResp = {
        children: []
      }
      if (response && response.repositoryFileDto) {
        myself.set("deletedFiles", "");
        for (var i = 0; i < response.repositoryFileDto.length; i++) {
          var obj = {
            file: response.repositoryFileDto[i]
          };

          obj.file.nameDecoded = obj.file.name;
          obj.file.trash = true;
          obj.file.pathText = jQuery.i18n.prop('originText') + " " //i18n

          /* BACKLOG-40086: we've updated the browser.templates.js to match the objects returned by generic-files endpoints
             Trash is still using /api/repo/files and repositoryFileDto. To match template change:
             - Convert "folder" from string to boolean
             - Convert "id" to "objectId"
           */
          obj.file.folder = (obj.file.folder === "true");
          obj.file.objectId = obj.file.id;

          if (obj.file.id) {
            if (myself.get("deletedFiles") == "") {
              myself.set("deletedFiles", obj.file.id + ",");
            } else {
              myself.set("deletedFiles", myself.get("deletedFiles") + obj.file.id + ",");
            }
          }
          newResp.children.push(obj);
        }
      }
      return newResp;
    },

    updateData: function () {
      var myself = this;
      myself.set("runSpinner", true);
      myself.fetchData(myself.get("path"), function (response) {
        //If we have trash data we reformat it to match the handlebar templates
        if (myself.get("path") == ".trash") {
          var newResp = myself.reformatTrashResponse(myself, response);
          myself.set("data", newResp);
          if (myself.get("deletedFiles") == "") {
            FileBrowser.fileBrowserModel.get("trashButtons").onTrashSelect(true);
          }
        } else {
          var newResp = reformatResponse(response);
          newResp.ts = new Date(); // force backbone to trigger onchange event even if response is the same
          myself.set("data", newResp);
        }
      });
    },

    fetchData: function (path, callback) {
      var myself = this,
          url = this.getFileListRequest(FileBrowser.encodeGenericFilePathComponents(path == null ? "/" : path)),
          localSequenceNumber = myself.get("sequenceNumber");

      $.ajax({
        async: true,
        cache: false, // prevent IE from caching the request
        dataType: "json",
        url: url,
        success: function (response) {
          if (localSequenceNumber == myself.get("sequenceNumber") && callback != undefined) {
            myself.set("sequenceNumber", localSequenceNumber + 1);
            callback(customSort(response));
          }

          if (window.parent.mantle_isBrowseRepoDirty == true) {
            window.parent.mantle_isBrowseRepoDirty = false;
            //clear the cache
            myself.set("cachedData", {});
          }
          //cache the folder contents
          if (FileBrowser.fileBrowserModel.getFolderClicked()) {
            if (FileBrowser.fileBrowserModel.getFolderClicked().attr("path") == ".trash") {
              myself.get("cachedData")[FileBrowser.fileBrowserModel.getFolderClicked().attr("path")]
                  = FileBrowser.fileBrowserModel.attributes.fileListModel.reformatTrashResponse(myself, response);
            }
            else {
              myself.get("cachedData")[FileBrowser.fileBrowserModel.getFolderClicked().attr("path")] = reformatResponse(response);
            }
          }
        },
        error: function () {
        },
        beforeSend: function (xhr) {
          myself.set("runSpinner", true);

          if (!window.parent.mantle_isBrowseRepoDirty) {
            if (myself.get("cachedData") != undefined) {
              if (myself.get("cachedData")[FileBrowser.fileBrowserModel.getFolderClicked().attr("path")] != undefined) {
                if (_.isEqual(myself.get("cachedData")[FileBrowser.fileBrowserModel.getFolderClicked().attr("path")], myself.get("data"))) {
                  myself.trigger('change:data');
                } else {
                  //force event in case data was not changed
                  myself.set("data", myself.get("cachedData")[FileBrowser.fileBrowserModel.getFolderClicked().attr("path")], {silent:true});
                  myself.trigger("change:data");
                }
                xhr.abort();
                if (myself.get("path") == ".trash" && myself.get("deletedFiles") == "") {
                  FileBrowser.fileBrowserModel.get("trashButtons").onTrashSelect(true);
                }
                myself.set("runSpinner", false);
              }
            }
          } else {
            // BACKLOG-40086: Clear the server-side tree cache if isBrowseRepoDirty flag is set.
            FileBrowser.clearTreeCache();
          }
        }
      });
    },

    /*
     * Path has already been converted to colons
     */
    getFileListRequest: function (path) {
      var request;
      if (path == ".trash") {
        request = CONTEXT_PATH + "api/repo/files/deleted";
      } else {
        request = CONTEXT_PATH + "plugin/scheduler-plugin/api/generic-files/" + path + "/tree?depth=1&filter=FILES&showHidden=" + this.get("showHiddenFiles");
      }
      return request;
    }
  });

  var FileBrowserView = Backbone.View.extend({
    attributes: {
      buttonsEnabled: false
    },

    initialize: function () {
      this.initializeLayout();
      this.initializeOptions();
      this.configureListeners();
      this.render();
    },

    configureListeners: function () {
      //update buttons when changed folder/file
      this.model.on("change:clickedFile", this.updateButtons, this);
      this.model.on("change:lastClick", this.updateButtons, this);

      //update buttons header on folder/file selection
      this.model.on("change:clickedFolder", this.updateButtonsHeader, this);
      this.model.on("change:clickedFile", this.updateButtonsHeader, this);

      //update folder and file browser headers on folder selection change
      this.model.on("change:clickedFolder", this.updateFolderBrowserHeader, this);
      this.model.on("change:clickedFolder", this.updateFileBrowserHeader, this);

      //check buttons enabled
      this.model.on("change:clickedFolder", this.checkButtonsEnabled, this);
      this.model.on("change:clickedFile", this.checkButtonsEnabled, this);
    },

    initializeLayout: function () {
      var myself = this;
      myself.$el.empty();
      myself.$el.append($(templates.structure({})));
    },

    initializeOptions: function () {
      var myself = this;

      foldersTreeView = undefined;
      fileListView = undefined;

      this.foldersTreeView = new FileBrowserFolderTreeView({
        model: myself.model.get("foldersTreeModel"),
        data: myself.model.get("foldersTreeModel").get("data"),
        el: myself.$el.find("#fileBrowserFolders .body")
      });

      this.fileListView = new FileBrowserFileListView({
        model: myself.model.get("fileListModel"),
        data: myself.model.get("fileListModel").get("data"),
        el: myself.$el.find("#fileBrowserFiles .body")
      });
    },

    render: function () {
      var myself = this;

      myself.updateButtons();
      myself.updateButtonsHeader();
      myself.updateFolderBrowserHeader();
      myself.updateFileBrowserHeader();

      //disable all buttons on start
      $("button.btn.btn-block").each(function () {
        $(this).attr("disabled", "disabled");
      });
    },

    updateButtonsHeader: function () {
      var myself = this,
          $buttonsContainer = myself.$el.find($("#fileBrowserButtons"));

      $buttonsContainer.find($(".header")).detach();

      var lastClick = myself.model.getLastClick(),
          folderClicked = myself.model.getFolderClicked(),
          fileClicked = myself.model.getFileClicked();

      var obj = {};

      if (lastClick == "file" && fileClicked != undefined) {
        obj.folderName = undefined;
        obj.fileName = $(fileClicked.find('.title')[0]).text();
      } else if (lastClick == "folder" && folderClicked != undefined) {
        obj.folderName = $(folderClicked.find('.title')[0]).text();
        obj.fileName = undefined;
      } else if ($(folderClicked).attr('path') == ".trash") {
        obj.trashHeader = jQuery.i18n.prop('trash_actions'); //i18n
      }
      obj.i18n = jQuery.i18n;
      //require buttons header template
      $buttonsContainer.prepend($(templates.buttonsHeader(obj)));
    },

    updateFolderBrowserHeader: function () {
      var $el = $(this.el),
          $folderBrowserContainer = $el.find($("#fileBrowserFolders"));

      $folderBrowserContainer.find($(".header")).detach();

      var folderClicked = this.model.getFolderClicked();
      var obj = {
        folderBreadcrumb: folderClicked && folderClicked.attr("path") ?
            folderClicked.attr("path").split("/").slice(1).join(" > ") : undefined,
        i18n: jQuery.i18n,
        refreshHandler: function () {
          if (window.parent.mantle_fireEvent) {
            window.parent.mantle_fireEvent('GenericEvent', {"eventSubType": "RefreshBrowsePerspectiveEvent",
              "booleanParam": FileBrowser.fileBrowserModel.get("showDescriptions") });
          }
        }
      };

      if (this.model.getLastClick() == "trash") {
        obj.trashHeader = jQuery.i18n.prop('browsing_trash'); //i18n
      }
      //require folders header template
      $folderBrowserContainer.prepend($(templates.folderBrowserHeader(obj)));
      if ($folderBrowserContainer.find("#refreshBrowserIcon").length > 0){
        a11yUtil.makeAccessibleActionButton($folderBrowserContainer.find("#refreshBrowserIcon")[0]);
      }
    },

    updateFileBrowserHeader: function () {
      var $el = $(this.el),
          $folderBrowserContainer = $el.find($("#fileBrowserFiles"));

      $folderBrowserContainer.find($(".header")).detach();

      var folderClicked = this.model.getFolderClicked();

      var obj = {
        folderName: folderClicked != undefined ? folderClicked.find("> .element .title").text() : undefined,
        i18n: jQuery.i18n
      }

      if (this.model.getLastClick() == "trash") {
        obj.trashHeader = jQuery.i18n.prop('trash_contents');
      }

      //require files header template
      $folderBrowserContainer.prepend($(templates.fileBrowserHeader(obj)));
    },

    updateButtons: function () {
      var $el = $(this.el),
          $buttonsContainer = $el.find($("#fileBrowserButtons .body"));

      $buttonsContainer.empty();
      var lastClick = this.model.getLastClick(),
          folderClicked = this.model.getFolderClicked(),
          fileClicked = this.model.getFileClicked();
      var buttonsType;
      if (lastClick == "file") {
        buttonsType = this.model.defaults.fileButtons;
      } else if (lastClick == "folder") {
        //convert path/title to arrays
        buttonsType = this.model.defaults.folderButtons;
      } else if (lastClick == "trash") {
        buttonsType = this.model.defaults.trashButtons;
      } else if (lastClick == "trashItem") {
        buttonsType = this.model.defaults.trashItemButtons;
      }

      var model = this.model; // trap model

      $buttonsContainer.append($(templates.buttons(buttonsType)));

      // add onClick handler to each button
      $(buttonsType.buttons).each(function (idx, fb) {
        $('#' + fb.id).on("click", { model: model, handler: fb.handler }, function (event) {
          var path = null;
          var title = null;
          var fileList = "";
          var id = "";
          var type = null;
          var mode = null;

          var multiSelectItems = FileBrowser.concatArray(model.get("fileListModel").get("multiSelect"), model.get("fileListModel").get("shiftLasso"));
          if (model.getLastClick() == "file") {
            path = $(model.getFileClicked()[0]).attr("path");
            title = $(model.getFileClicked()[0]).children('.title').text();
            id = $(model.getFileClicked()[0]).attr("id");
          } else if (model.getLastClick() == "folder") {
            path = $(model.getFolderClicked()[0]).attr("path");
            title = $(model.getFolderClicked()[0]).children('.title').text();
          } else if (model.getLastClick() == "trash") {
            fileList = model.get("fileListModel").get("deletedFiles");
            mode = "purge";
          } else if (model.getLastClick() == "trashItem") {
            for (var i = 0; i < multiSelectItems.length; i++) {
              fileList += multiSelectItems[i].obj.attr("id") + ",";
            }
            type ="file";
          }
          if ((path != null) && event.data.handler) {
            event.data.handler(path, title, id, multiSelectItems, model.get("browserUtils"));
            event.stopPropagation();
          } else {
            event.data.handler(fileList, type, mode);
            event.stopPropagation();
          }
        });
      });

      // TODO BACKLOG-40086: for now, disable all file actions for VFS Connections, i.e. isRepoPath = false
      const selectedFilePath = fileClicked ? fileClicked.attr("path") : null;
      if (selectedFilePath != null && buttonsType.enableButtons) {
        const isRepoPath = isRepositoryPath(selectedFilePath);
        buttonsType.enableButtons(isRepoPath);
      }

      model.updateFolderButtons( folderClicked == undefined ? window.parent.HOME_FOLDER : folderClicked.attr("path") );
    },

    updateButtonsMulti: function () {
      var $el = $(this.el),
          $buttonsContainer = $el.find($("#fileBrowserButtons .body"));

      $buttonsContainer.empty();
      var lastClick = this.model.getLastClick(),
          folderClicked = this.model.getFolderClicked(),
          fileClicked = this.model.getFileClicked();

      var buttonsType = this.model.get("multiSelectButtons");

      var model = this.model; // trap model

      //require buttons template
      $buttonsContainer.append($(templates.buttons(buttonsType)));

      // add onClick handler to each button
      $(buttonsType.buttons).each(function (idx, fb) {
        $('#' + fb.id).on("click", { model: model, handler: fb.handler }, function (event) {
          var path = [];
          var title = [];
          var id = [];
          var fileList = null;
          var type = null;
          var mode = null;
          var returnModel = null;

          var multiSelectItems = FileBrowser.concatArray(model.get("fileListModel").get("multiSelect"), model.get("fileListModel").get("shiftLasso"));

          if (model.getLastClick() == "file") {
            for (var i = 0; i < multiSelectItems.length; i++) {
              path[i] = multiSelectItems[i].obj.attr("path");
              title[i] = multiSelectItems[i].obj.attr("title");
              id[i] = multiSelectItems[i].obj.attr("id");
            }
          } else if (model.getLastClick() == "folder") {
            path = $(model.getFolderClicked()[0]).attr("path");
            title = $(model.getFolderClicked()[0]).children('.title')
          } else if (model.getLastClick() == "trash") {
            fileList = model.get("fileListModel").get("deletedFiles");
            mode = "purge";
          } else if (model.getLastClick() == "trashItem") {
            fileList = $(model.getFileClicked()[0]).attr("id") + ",";
            type = $(model.getFileClicked()[0]).attr("type");
          }
          if ((path != null) && event.data.handler) {
            event.data.handler(path, title, id, multiSelectItems, model.get("browserUtils"));
            event.stopPropagation();
          } else {
            event.data.handler(fileList, type, mode);
            event.stopPropagation();
          }
        });
      });

      // TODO BACKLOG-40086: for now, disable all file actions for VFS Connections, i.e. isRepoPath = false
      const selectedFilePath = fileClicked ? fileClicked.attr("path") : null;
      if (selectedFilePath != null && buttonsType.enableButtons) {
        const isRepoPath = isRepositoryPath(selectedFilePath);
        buttonsType.enableButtons(isRepoPath);
      }
    },

    checkButtonsEnabled: function () {
      //disable all buttons on start
      $("button.btn.btn-block[disabled=disabled]").each(function () {
        $(this).removeAttr("disabled");
      });
    }
  });


  var FileBrowserFolderTreeView = Backbone.View.extend({

    events: {
      "click .folder .expandCollapse": "expandFolder",

      "click .folder .icon": "clickFolder",
      "dblclick .folder .icon": "expandFolder",

      "click .folder .title": "clickFolder",
      "dblclick .folder .title": "expandFolder",

      "keydown .folder .element": "keyDownFolder",
    },

    initialize: function () {
      var myself = this,
          data = myself.model.get("data"),
          spinner = myself.model.get("spinner");

      myself.model.on("change:runSpinner", myself.manageSpinner, myself);
      myself.model.on("change:data", myself.render, myself);
      myself.model.on("change:showDescriptions", this.updateDescriptions, this);
      if (data == undefined) { //update data
        //start spinner
        myself.$el.html(spinner.spin());
        myself.model.set("updateData", true);
      }

    },

    render: function () {
      var myself = this,
          data = myself.model.get("data");

      //stop spinner
      myself.model.set("runSpinner", false);

      //append content
      myself.$el.append(templates.folders(data));


      //fix folder widths
      $(".folder").each(function () {
        $(this).addClass("selected");
      });

      $(".element").each(function () {
        var $this = $(this);

        //BISERVER-10784 - limit the amount of attempts to widen the column due to rendering
        //issues on google chrome
        var tries = 0;
        while ($this.height() > 20 && tries < 250) {
          $this.width($this.width() + 20);
          tries++;
        }
      });

      $(".folder").each(function () {
        $(this).removeClass("selected");
      });

      //close all children folders
      myself.$el.find(".folders").hide();

      //remove padding of first folder
      myself.$el.children().each(function () {
        $(this).addClass("first");
      });

      //hide expand button from trash
      $(".trash").addClass("empty");

      // Checks if any folder is visible
      var $firstVisibleFolder = myself.getFirstVisibleFolder();

      if( $firstVisibleFolder ) {
        // open last clicked folder or start folder (home folder)
        // if startFolder is not visible, use first one that it is instead
        var $folder = undefined;

        // verify if clicked folder is visible
        if ( FileBrowser.fileBrowserModel.getFolderClicked() &&
            FileBrowser.fileBrowserModel.getFolderClicked().attr("path") &&
            ( $( "div[path='" + escapeCssSelector(FileBrowser.fileBrowserModel.getFolderClicked().attr("path")) + "']" ).length !== 0 ) ) {
          $folder = $("[path='" + escapeCssSelector(FileBrowser.fileBrowserModel.getFolderClicked().attr("path")) + "']");
        } else if ( FileBrowser.fileBrowserModel.get("startFolder") &&
            ( $( "div[path='" + escapeCssSelector(FileBrowser.fileBrowserModel.get("startFolder")) + "']" ).length !== 0 ) ) {
          $folder = $( "[path='" + escapeCssSelector(FileBrowser.fileBrowserModel.get("startFolder")) + "']" );
        } else {
          $folder = myself.getFirstVisibleFolder();
        }

        var $parentFolder = $folder.parent(".folders");
        while (!$parentFolder.hasClass("body") && $parentFolder.length > 0) {
          $parentFolder.show();
          $parentFolder.parent().addClass("open");
          $parentFolder = $parentFolder.parent().parent();
        }
        FileBrowser.fileBrowserModel.set("clickedFolder", {
          obj: $folder,
          time: (new Date()).getTime()
        });
        var $clickedFile = FileBrowser.fileBrowserModel.getFileClicked();
        if ($clickedFile != undefined && FileBrowser.fileBrowserModel.getLastClick() == "file") {
          FileBrowser.fileBrowserModel.get("fileListModel").set("clickedFile", {
            obj: FileBrowser.fileBrowserModel.getFileClicked(),
            time: (new Date()).getTime()
          });
          FileBrowser.fileBrowserModel.updateFileClicked();
          $folder.addClass("secondarySelected");
          $folder.children(".element").attr("tabindex", 0).attr("aria-selected", true);
          $folder.removeClass("selected");
          $clickedFile.addClass("selected");
        } else {
          $folder.children(".element").attr("aria-expanded", true);
          $folder.addClass("selected");
          $folder.children(".element").attr("tabindex", 0).attr("aria-selected", true);
          $folder.find("> .folders").show();
        }
        FileBrowser.fileBrowserModel.updateFolderButtons($folder.attr("path"));
        myself.updateDescriptions();

        //scroll the first selected folder div into view
        const selectedFolders = $("div.folders .selected").get();
        if (selectedFolders.length > 0) {
          selectedFolders[0].scrollIntoView();
        }
      }
    },

    getFirstVisibleFolder: function () {
      var myself = this;
      var firstVisibleFolder = undefined;
      var foldersList = myself.model.get( "data" ).children;

      for ( var i = 0; i < foldersList.length; i++ ) {
        var elem = foldersList[i];
        if (elem && elem.file && elem.file.folder && elem.file.path &&
          $("div[path=\"" + escapeCssSelector(elem.file.path) + "\"]").length != 0) {
          firstVisibleFolder = elem;
          break;
        }
      }

      if ( firstVisibleFolder ) {
        return $( "[path='" + escapeCssSelector(firstVisibleFolder.file.path) + "']" );
      }
    },

    expandFolder: function (event) {
      const LOADING_FOLDER_CLASS = "loading";
      const ERROR_FOLDER_CLASS = "error";

      let $target;
      if ($(event.currentTarget).hasClass("element")) {
        $target = $(event.currentTarget).parent();
      } else {
        $target = $(event.currentTarget).parent().parent();
      }

      if($target.hasClass("trash")){
        //ignore expand events for trash
        event.stopPropagation();
        return;
      }

      // If target has class open, it is already opened and showing children...close it and hide children
      if ($target.hasClass("open")) {
        $target.children(".element").attr("aria-expanded", false);
        $target.removeClass("open").find("> .folders").hide();
        if ($target.find("[tabindex=0]").length > 0) {
          $target.find("[tabindex=0]").attr("tabindex", -1);
          $target.children(".element").attr("tabindex", 0);
        }
      // Else if the children are already part of the DOM, there is no need to make a rest call to get them
      // Simply add .open class to target, and show children (we've already made a call to get them)
      } else if ($target.find("> .folders").children().length > 0) {
        $target.children(".element").attr("aria-expanded", true);
        $target.addClass("open").find("> .folders").show();
      // else, we must make a call to get the children of the target folder (if they exist) and add them to DOM
      } else {
        var path = $target.attr("path");
        var myself = this;

        var url = CONTEXT_PATH + "plugin/scheduler-plugin/api/generic-files/"
            + FileBrowser.encodeGenericFilePathComponents(path)
            + "/tree?depth=1&showHidden=" + myself.model.get("showHiddenFiles") + "&filter=FOLDERS";
        $.ajax({
          async: true,
          cache: false, // prevent IE from caching the request
          dataType: "json",
          url: url,
          success: function (response) {

            if (response.children) {
              response = customSort(response);

              var toAppend = templates.folders(reformatResponse(response));
              $target.find("> .folders").append(toAppend ? toAppend : "");
            }

            // set the widths of new folder descriptions
            $target.find(".element").each(function () {
              var $this = $(this);
              var tries = 0;
              while ($this.height() > 20 && tries < 250) {
                $this.width($this.width() + 20);
                tries++;
              }
            });
            $target.removeClass(ERROR_FOLDER_CLASS);
            $target.removeClass(LOADING_FOLDER_CLASS);
            $target.children(".element").attr("aria-expanded", true);
            $target.addClass("open").find("> .folders").show();
          },
          error: function () {
            $target.removeClass(LOADING_FOLDER_CLASS);
            $target.addClass(ERROR_FOLDER_CLASS);
            //TODO indicate some failure via dialog?
          },
          beforeSend: function (xhr) {
            $target.removeClass(ERROR_FOLDER_CLASS);
            $target.addClass(LOADING_FOLDER_CLASS);
          }
        });
      }
      event.stopPropagation();
    },

    clickFolder: function (event) {
      let $target;
      if ($(event.currentTarget).hasClass("element")) {
        $target = $(event.currentTarget).parent();
      } else {
        $target = $(event.currentTarget).parent().parent();
      }
      //BISERVER-9259 - added time parameter to force change event
      this.model.set("clicked", {
        obj: $target.attr("id"),
        time: (new Date()).getTime()
      });
      this.model.set("clickedFolder", {
        obj: $target,
        time: (new Date()).getTime()
      });
      $(".folder.selected").children(".element").attr("tabindex", -1).attr("aria-selected", false);
      $(".folder.selected").removeClass("selected");
      $(".folder.secondarySelected").removeClass("secondarySelected");
      $(".folder").find("[tabindex=0]").attr("tabindex", -1);
      $target.addClass("selected");
      $target.children(".element").attr("tabindex", 0).attr("aria-selected", true);
      //deselect any files
      $("#fileBrowserFiles").children("[role=listbox]").removeAttr("aria-activedescendant");
      $(".file.selected").removeClass("selected");
      event.stopPropagation();
    },

    keyDownFolder: function (event) {
      let keyCode = event.which || event.keyCode;
      if (keyCode === a11yUtil.keyCodes.enter || keyCode === a11yUtil.keyCodes.space) {
        // ENTER , SPACE
        this.clickFolder(event);
      } else if (keyCode === a11yUtil.keyCodes.arrowDown) {
        // DOWN Arrow
        let nextElement = this.getNextElementToFocus($(event.currentTarget));
        if (nextElement !== null) {
          nextElement.focus();
        }
      } else if (keyCode === a11yUtil.keyCodes.arrowUp) {
        // UP Arrow
        let prevElement = this.getPreviousElementToFocus($(event.currentTarget));
        if (prevElement !== null) {
          prevElement.focus();
        }
      } else if (keyCode === a11yUtil.keyCodes.arrowRight) {
        // RIGHT Arrow
        if (!$(event.currentTarget).parent().hasClass("open")) {
          this.expandFolder(event);
        }
      } else if (keyCode === a11yUtil.keyCodes.arrowLeft) {
        // LEFT Arrow
        if ($(event.currentTarget).parent().hasClass("open")) {
          this.expandFolder(event);
        }
      }
      event.stopPropagation();
    },

    getNextElementToFocus: function (currentElement) {
      let lastFolder = $("#fileBrowserFolders").find(".folder").last();
      let firstChildFolder = currentElement.next().children().first();

      if (currentElement.parent().hasClass("open") && firstChildFolder.length > 0) {
        return firstChildFolder.children(".element");
      }
      return this.getNextAvailableElement(currentElement.parent(), lastFolder);
    },

    getNextAvailableElement: function (currentFolder, lastFolder) {
      if (currentFolder.attr("title") === lastFolder.attr("title")) {
        return null;
      }

      if (currentFolder.next().length > 0) {
        return currentFolder.next().children(".element");
      } else {
        return this.getNextAvailableElement(currentFolder.parent().parent(), lastFolder);
      }
    },

    getPreviousElementToFocus: function (currentElement) {
      let previousFolder = currentElement.parent().prev();

      if (previousFolder.length === 0) {
        let rootFolder = $("#fileBrowserFolders").find(".folder").first();
        if (rootFolder.attr("id") === currentElement.parent().attr("id")) {
          return null;
        }
        return currentElement.parent().parent().parent().children(".element");
      } else {
        return this.getPreviousAvailableElement(previousFolder);
      }
    },

    getPreviousAvailableElement: function (currentFolder) {
      let currentFolderChildren = currentFolder.children(".folders").children();

      if (currentFolder.hasClass("open") && currentFolderChildren.length > 0) {
        return this.getPreviousAvailableElement(currentFolderChildren.last())
      }
      return currentFolder.children(".element");
    },

    manageSpinner: function () {
      var myself = this,
          runSpinner = this.model.get("runSpinner"),
          spinner = this.model.get("spinner");

      if (runSpinner) {
        if (spinner != undefined) {
          myself.$el.html(spinner.spin().el);
        } else {  }
      } else {
        myself.model.get("spinner").stop();
      }
    },

    updateDescriptions: function () {
      var $folders = $(".folder"),
          showDescriptions = this.model.get("showDescriptions");

      $folders.each(function () {
        var $this = $(this);
        var desc = $this.attr("desc");
        if (showDescriptions && desc != "") {
          $this.attr("title", desc);
        } else {
          $this.attr("title", $this.attr("ext"));
        }
      });
    }
  });

  var FileBrowserFileListView = Backbone.View.extend({
    events: {
      "click div.file": "clickFile",
      "dblclick div.file": "doubleClickFile",
      "click": "clickBody",
      "keydown": "keyDownFile"
    },

    initialize: function () {
      var myself = this,
          data = myself.model.get("data");
      this.model.on("change:data", this.updateFileList, this);
      myself.model.on("change:runSpinner", myself.manageSpinner, myself);
      myself.model.on("change:showDescriptions", this.updateDescriptions, this);
      this.keyClearTimeoutHandle = null;
    },

    render: function () {
      var myself = this,
          data = myself.model.get("data");

      //require file list template
      myself.$el.empty().append(templates.files(data));

      if (myself.$el.children().length > 0) {
        $(".file").each(function () {
          var $this = $(this);

          //BISERVER-10784 - limit the amount of attempts to widen the column due to rendering
          //issues on google chrome
          var tries = 0;
          while ($this.height() > 20 && tries < 250) {
            $this.width($this.width() + 20);
            tries++;
          }
        });
      } else {
        myself.$el.append(templates.emptyFolder({i18n: jQuery.i18n}));
      }

      myself.updateDescriptions();
      var fileSelected = false;
      if ( myself.model.attributes.clickedFile ){
        var filelist = myself.$el.children();
        for (index = 0; index < filelist.length; ++index) {
          if ( $(myself.$el.children().get(index)).attr("path") == myself.model.attributes.clickedFile.obj.attr("path") ) {
            $(myself.$el.children().get(index)).addClass("selected").attr("aria-selected", true);
            myself.$el.attr('aria-activedescendant', myself.$el.children().get(index).id);
            myself.model.attributes.anchorPoint = myself.model.attributes.clickedFile;
            fileSelected = true;
          }
        }
      }
      //could not find file, select folder for file
      if (!fileSelected) {
        var $folder = $(".folder.secondarySelected");
        if ($folder.length > 0) {
          $folder.addClass("selected");
          $folder.removeClass("secondarySelected");
          FileBrowser.fileBrowserModel.updateFolderLastClick();
          FileBrowser.FileBrowserView.updateButtonsHeader();
        }
      }
      setTimeout(function () {
        myself.model.set("runSpinner", false);
      }, 100);

      //scroll the first selected file div into view
      const selectedFiles = $("div.file.selected").get();
      if (selectedFiles.length > 0) {
        selectedFiles[0].scrollIntoView();
      }
    },

    /*!
    * This software or document includes material copied from or derived from https://www.w3.org/WAI/content-assets/wai-aria-practices/patterns/listbox/examples/js/listbox.js
    * Copyright © 2023 World Wide Web Consortium. https://www.w3.org/Consortium/Legal/2023/doc-license
    *
    !*/
    keyDownFile: function (evt) {
      var key = evt.which || evt.keyCode;
      var listboxNode = this.$el[0];
      var activeDescendant = listboxNode.getAttribute("aria-activedescendant");
      var lastActiveId = activeDescendant;
      var allOptions = listboxNode.querySelectorAll('[role="option"]');
      var currentItem = document.getElementById(activeDescendant) || allOptions[0];
      var nextItem = currentItem;

      if (!currentItem) {
        return;
      }

      switch (key) {
        case a11yUtil.keyCodes.arrowUp:
        case a11yUtil.keyCodes.arrowDown:
          evt.preventDefault();
          if (!activeDescendant) {
            // focus first option if no option was previously focused, and perform no other actions
            activeDescendant = this.focusItem(currentItem, activeDescendant, listboxNode);
            break;
          }

          if (key === a11yUtil.keyCodes.arrowUp) {
            nextItem = this.findPreviousOption(currentItem, listboxNode);
          } else {
            nextItem = this.findNextOption(currentItem, listboxNode);
          }

          if (nextItem) {
            activeDescendant = this.focusItem(nextItem, activeDescendant, listboxNode);

            if (evt.shiftKey) {
              this.clickFile(evt);
            }
          }
          break;
        case a11yUtil.keyCodes.home:
          evt.preventDefault();
          var firstItem = listboxNode.querySelector('[role="option"]');

          if (firstItem) {
            activeDescendant = this.focusItem(firstItem, activeDescendant, listboxNode);

            if (evt.shiftKey && evt.ctrlKey) {
              this.clickFile(evt);
            }
          }
          break;
        case a11yUtil.keyCodes.end:
          evt.preventDefault();
          var itemList = listboxNode.querySelectorAll('[role="option"]');

          if (itemList.length > 0) {
            activeDescendant = this.focusItem(itemList[itemList.length - 1], activeDescendant, listboxNode);

            if (evt.shiftKey && evt.ctrlKey) {
              this.clickFile(evt);
            }
          }
          break;
        case a11yUtil.keyCodes.space:
          if ($(evt.currentTarget).children(".active-descendant").length > 0) {
            evt.preventDefault();
            this.clickFile(evt);
          }
          break;
        case a11yUtil.keyCodes.A:
          // control + A
          if (evt.ctrlKey || evt.metaKey) {
            evt.preventDefault();
            this.clickFile(evt);
            break;
          }
        // fall through
        default:
          var itemToFocus = this.findItemToFocus(key, activeDescendant, listboxNode);
          if (itemToFocus) {
            activeDescendant = this.focusItem(itemToFocus, activeDescendant, listboxNode);
          }
          break;
      }

      if (activeDescendant !== lastActiveId) {
        this.updateScroll(activeDescendant, listboxNode);
      }
    },

    /**
     *  Focus on the specified item - element and return its ID
     */
    focusItem: function (element, activeDescendant, listboxNode) {
      var previouslyFocusedElement = document.getElementById(activeDescendant);
      if (previouslyFocusedElement) {
        previouslyFocusedElement.classList.remove("active-descendant");
      }

      element.classList.add("active-descendant");
      listboxNode.setAttribute("aria-activedescendant", element.id);
      return element.id;
    },

    /**
     * Return the previous listbox option, if it exists; otherwise, returns null
     */
    findPreviousOption: function (currentOption, listboxNode) {
      var allOptions = Array.prototype.slice.call(listboxNode.querySelectorAll('[role="option"]')); // get options array
      var currentOptionIndex = allOptions.indexOf(currentOption);
      var previousOption = null;

      if (currentOptionIndex > 0) {
        previousOption = allOptions[currentOptionIndex - 1];
      }

      return previousOption;
    },

    /**
     * Return the next listbox option, if it exists; otherwise, returns null
     */
    findNextOption: function (currentOption, listboxNode) {
      var allOptions = Array.prototype.slice.call(listboxNode.querySelectorAll('[role="option"]')); // get options array
      var currentOptionIndex = allOptions.indexOf(currentOption);
      var nextOption = null;

      if (currentOptionIndex > -1 && currentOptionIndex < allOptions.length - 1) {
        nextOption = allOptions[currentOptionIndex + 1];
      }

      return nextOption;
    },

    /**
     * Check if the selected option is in view, and scroll if not
     */
    updateScroll: function (activeDescendant, listboxNode) {
      var selectedOption = document.getElementById(activeDescendant);

      if (selectedOption && listboxNode.scrollHeight > listboxNode.clientHeight) {
        var scrollBottom = listboxNode.clientHeight + listboxNode.scrollTop;
        var elementBottom = selectedOption.offsetTop + selectedOption.offsetHeight;

        if (elementBottom > scrollBottom) {
          listboxNode.scrollTop = elementBottom - listboxNode.clientHeight;
        } else if ((selectedOption.offsetTop - 2*selectedOption.offsetHeight) < listboxNode.scrollTop) {
          listboxNode.scrollTop = selectedOption.offsetTop - 2*selectedOption.offsetHeight;
        }

      }
    },

    findItemToFocus: function (key, activeDescendant, listboxNode) {
      var itemList = listboxNode.querySelectorAll('[role="option"]');
      var character = String.fromCharCode(key).toUpperCase();
      var searchIndex = 0;
      var keysSoFar = this.model.get("keysSoFar");

      if (!keysSoFar) {
        for (var i = 0; i < itemList.length; i++) {
          if (itemList[i].getAttribute("id") === activeDescendant) {
            searchIndex = i;
          }
        }
      }
      keysSoFar += character;
      this.model.set("keysSoFar", keysSoFar);
      this.clearKeysSoFarAfterDelay();

      var nextMatch = this.findMatchInRange(itemList, searchIndex + 1, itemList.length);
      if (!nextMatch) {
        nextMatch = this.findMatchInRange(itemList, 0, searchIndex);
      }
      return nextMatch;
    },

    findMatchInRange: function (list, startIndex, endIndex) {
      // Find the first item starting with the keysSoFar substring, searching in
      // the specified range of items
      for (var n = startIndex; n < endIndex; n++) {
        var label = list[n].innerText;
        if (label && label.toUpperCase().indexOf(this.model.get("keysSoFar")) === 0) {
          return list[n];
        }
      }
      return null;
    },

    clearKeysSoFarAfterDelay: function () {
      if (this.keyClearTimeoutHandle) {
        clearTimeout(this.keyClearTimeoutHandle);
        this.keyClearTimeoutHandle = null;
      }
      this.keyClearTimeoutHandle = setTimeout(
        function () {
          this.model.set("keysSoFar", "");
          this.keyClearTimeoutHandle = null;
        }.bind(this),
        500
      );
    },

    clickFile: function (event) {
      var prevClicked = this.model.get("clickedFile");
      if (this.model.get("anchorPoint")) {
        prevClicked = this.model.get("anchorPoint");
      }

      //don't want to stop propagation of the event, but need to notify clickBody listener
      //that the event was handled and we don't need to deselect a file
      this.model.set("desel", 1);
      let $target;
      if ($(event.currentTarget).attr("role") === "listbox") {
        if (event.ctrlKey && event.keyCode === a11yUtil.keyCodes.A) {
          $target = $(event.currentTarget).find(".file").first();
        } else {
          $target = $(event.currentTarget).find(".active-descendant");
        }
      } else {
        $target = $(event.currentTarget).eq(0);
      }

      //BISERVER-9259 - added time parameter to force change event
      this.model.set("clicked", {
        obj: $target,
        time: (new Date()).getTime()
      });

      this.model.set("clickedFile", {
        obj: $target,
        time: (new Date()).getTime()
      });

      if (!event.shiftKey) {
        this.model.set("anchorPoint", this.model.get("clickedFile"));
      }

      if (event.ctrlKey && event.keyCode === a11yUtil.keyCodes.A) {
        this.multiSelectFile($target, $(event.currentTarget).find(".file").last());
        //Control Click
      } else if ((event.ctrlKey || event.metaKey) && !(event.ctrlKey && event.shiftKey)) {
        //Control click will reset the shift lasso and merge its contents into main array
        this.model.set("multiSelect", FileBrowser.concatArray(this.model.get("multiSelect"), this.model.get("shiftLasso")));
        this.model.set("shiftLasso", []);

        //If item is already selected, deselect it.
        var clickedFileIndex = -1;
        var index;
        for (index = 0; index < this.model.get("multiSelect").length; ++index) {
          var clickedFileId = this.model.get("clickedFile").obj.attr("id")
          var multiSelectId = this.model.get("multiSelect")[index].obj.attr("id");
          if (clickedFileId == multiSelectId) {
            clickedFileIndex = index;
            break;
          }
        }

        //We are cntrl clicking an existing selection
        if (clickedFileIndex > -1) {
          this.model.get("multiSelect").splice(clickedFileIndex, 1);
          //Remove selected style from deselected item
          $target.removeClass("selected").removeAttr("aria-selected");
        }
        //We are cntrl clicking a new selection
        else {
          FileBrowser.pushUnique(this.model.get("multiSelect"), this.model.get("clickedFile"));
          $target.addClass("selected").attr("aria-selected",true);
        }
        //Shift Click
      } else if (event.shiftKey) {
        this.multiSelectFile(prevClicked.obj, $target);
        //Single Click
      } else {
        //Clear the multiselect array
        this.model.set("multiSelect", []);
        this.model.set("shiftLasso", []);
        FileBrowser.pushUnique(this.model.get("multiSelect"), this.model.get("clickedFile"));

        //reset all file selected styles
        $(".file.selected").removeClass("selected").removeAttr("aria-selected");
        $target.addClass("selected").attr("aria-selected",true);
      }

      var tempModel = [];
      $(".file.selected").each(function (i, ele) {
        tempModel.push({obj: $(ele)});
      });

      this.model.set("multiSelect", tempModel);
      //If more than one file is selected add multiselect button options
      if (!(this.model.get("path") == ".trash") && this.model.get("multiSelect").length > 1) {
        FileBrowser.FileBrowserView.updateButtonsMulti();
      }
      //Add secondary selection to folder
      $(".folder.selected").addClass("secondarySelected");
      $(".folder.selected").removeClass("selected");
    },

    multiSelectFile: function (from, target) {
      //reset lasso file selected styles
      for (var i = 0; i < this.model.get("shiftLasso").length; i++) {
        this.model.get("shiftLasso")[i].obj.removeClass("selected").removeAttr("aria-selected");
      }
      //Clear the Lasso array
      this.model.set("shiftLasso", []);
      target.addClass("selected").attr("aria-selected",true);
      from.addClass("selected").attr("aria-selected",true);

      if (from.attr("id") != target.attr("id")) {
        //Model title
        this.model.get("data").children[0].file.title;
        var files = this.model.get("data").children;
        var inRange = false;
        var secondMatch = false;
        for (var i = 0; i < files.length; i++) {
          if (files[i].file.folder === false) {
            if ((files[i].file.id == from.attr("id") || files[i].file.id == target.attr("id"))) {
              if (inRange == true) {
                secondMatch = true;
              } else {
                inRange = true;
              }
            }
            if (inRange == true) {
              var item = {
                obj: $("div[id=\"" + files[i].file.id + "\"]")
              }
              item.obj.addClass("selected").attr("aria-selected",true);
              FileBrowser.pushUnique(this.model.get("shiftLasso"), item);
              if (secondMatch) {
                inRange = false;
              }
            }
          }
        }
      }
      //target title
      target.attr("title");
      //prev Clicked title
      from.attr("title");
    },

    doubleClickFile: function (event) {
      var path = $(event.currentTarget).attr("path");
      //if not trash item, try to open the file.

      if (FileBrowser.fileBrowserModel.getLastClick() != "trashItem") {
        this.model.get("openFileHandler")(path, "run");
      }
    },

    clickBody: function (event) {
      if(!this.model.get("desel")){
        $(".file.selected").removeClass("selected");
        if(FileBrowser.fileBrowserModel.getLastClick() == 'file'){
          FileBrowser.fileBrowserModel.set("lastClick", "folder");
          $(".file.selected").removeClass("selected");
          $(".folder.secondarySelected").addClass("selected");
          $(".folder.secondarySelected").removeClass("secondarySelected");
          FileBrowser.FileBrowserView.updateButtonsHeader();
        }
      }
      this.model.set("desel", 0);
    },

    updateFileList: function () {
      var myself = this;
      this.render();

      setTimeout(function () {
        myself.model.set("runSpinner", false);
      }, 100);
    },

    manageSpinner: function () {
      var myself = this,
          runSpinner = this.model.get("runSpinner"),
          spinner = this.model.get("spinner");

      if (runSpinner) {
        if (spinner != undefined) {
          myself.$el.html(spinner.spin().el);
        } else {

        }
      } else {
        myself.model.get("spinner").stop();
      }
    },

    updateDescriptions: function () {
      var $files = $(".file"),
          showDescriptions = this.model.get("showDescriptions");

      $files.each(function () {
        var $this = $(this);
        var desc = $this.attr("desc");
        if (showDescriptions && desc != "") {
          $this.attr("title", desc);
        }
        else {
          $this.attr("title", $this.attr("ext"));
        }
      });

    }

  });

  function customSort(response) {

    // Code should be in sync with org.pentaho.gwt.widgets.client.filechooser.TreeItemComparator#compare.
    var localeCompare = function(a, b) {
      const aLowerCase = a.toLowerCase();
      const bLowerCase = b.toLowerCase();

      if(aLowerCase.localeCompare(bLowerCase) === 0) {
        // if values are equal, case ignored, use original values for comparison.
        return ((a < b) ? -1 : ((a > b) ? 1 : 0));
      }

      return aLowerCase.localeCompare(bLowerCase);
    };

    var sortFunction = function (a, b) {
      //if the file doesn't have a title, sort by its name
      //this is expected to only be the case for root folders
      const aCompare = a.file.title || a.file.name;
      const bCompare = b.file.title || b.file.name;
      return localeCompare(aCompare, bCompare);
    };

    var recursivePreorder = function(node) {
      if(node != null) {
        if(node.children == null || node.children.length === 0) {
          // do nothing if node is not a parent
        } else {
          for(var i = 0; i < node.children.length; i++) {
            // recursively sort children
            recursivePreorder(node.children[i]);
          }

          node.children.sort(sortFunction);
        }
      }
    };

    recursivePreorder(response);

    return response;
  }

  function isRepositoryPath(path) {
    return path.charAt(0) === REPOSITORY_ROOT_PATH;
  }

  function isProviderRootPath(file) {
    return file.parentPath === null;
  }

  function isRepositoryRootPath(path) {
    return path === REPOSITORY_ROOT_PATH;
  }

  /**
   * Reformat response data to handle conditionals before passing off to templates.
   * We can do conditional logic in templates, but it is better to do as much as possible here.
   */
  function reformatResponse(response) {
    var newResp = {
      children: []
    }
    if (response && response.children) {
      for (var i = 0; i < response.children.length; i++) {
        var obj = {
          file: Object
        }

        obj.file = response.children[i].file;

        if (obj.file.hasChildren){
          const childFolderTree = {
            file: obj.file,
            children: response.children[i].children
          }

          obj.children = reformatResponse(childFolderTree).children;
        }

        obj.file.pathText = jQuery.i18n.prop('originText') + " " //i18n

        if( isProviderRootPath(obj.file) ){
          obj.file.isProviderRootPath = true;
        }

        // Default title to non-encoded name.
        if (!obj.file.title) {
          obj.file.title = obj.file.nameDecoded;
        }

        if(!obj.file.objectId){
          obj.file.objectId = obj.file.path;
        }

        obj.file.id = obj.file.objectId;

        newResp.children.push(obj);
      }
    }
    return newResp;
  }

  function decodePvfsFileAttribute(attribute) {
    try {
      return decodeURI(attribute).replace("%23", "#");
    } catch (error) {
      // if there is an error, simply return the value. This should only impact the UI visually, not functionally.
      // we can show an error if we'd like, but have opted not to at this time.
      // let errorMessage = "Error decoding file attribute: " + attribute + "\n" + error;
      // window.parent.mantle_showGenericError(errorMessage);
      return attribute;
    }
  }


  /**
   * Escapes special characters in jquery selector
   * @param selector
   * @returns selector with special characters escaped by "\\"
   */
  function escapeCssSelector(selector) {
    return selector.replace(/[!"#$%&'()*+,.\/:;<=>?@[\\\]^`{|}~]/g, "\\$&");
  }

  return {
    encodePathComponents: FileBrowser.encodePathComponents,
    encodeGenericFilePathComponents: FileBrowser.encodeGenericFilePathComponents,
    setContainer: FileBrowser.setContainer,
    setOpenFileHandler: FileBrowser.setOpenFileHandler,
    setShowHiddenFiles: FileBrowser.setShowHiddenFiles,
    setShowDescriptions: FileBrowser.setShowDescriptions,
    setCanDownload: FileBrowser.setCanDownload,
    setCanPublish: FileBrowser.setCanPublish,
    setCanRead: FileBrowser.setCanRead,
    setCanCreate: FileBrowser.setCanCreate,
    updateShowDescriptions: FileBrowser.updateShowDescriptions,
    update: FileBrowser.update,
    updateData: FileBrowser.updateData,
    updateFile: FileBrowser.updateFile,
    updateFolder: FileBrowser.updateFolder,
    redraw: FileBrowser.redraw,
    templates: FileBrowser.templates,
    openFolder: FileBrowser.openFolder,
    pushUnique: FileBrowser.pushUnique,
    concatArray: FileBrowser.concatArray,
    clearTreeCache: FileBrowser.clearTreeCache
  }
});

function perspectiveActivated() {
  window.parent.mantle_isBrowseRepoDirty = true;
  FileBrowser.update();
}
