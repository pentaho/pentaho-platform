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

define([
  "common-ui/jquery-pentaho-i18n",
  "common-ui/jquery"
], function () {

  var local = {
    renameDialog: null,

    init: function () {
      var that = this; // trap this

      // initialize buttons definitions
      that.buttons = [
        {
          id: "newFolderButton",
          text: this.i18n.prop('contextAction_newFolder'),
          handler: $.proxy(that.newFolderHandler, that)
        },
        {
          id: "deleteFolderButton",
          text: this.i18n.prop('contextAction_delete'),
          handler: $.proxy(that.deleteFolderHandler, that)
        },
        {
          id: "renameButton",
          text: this.i18n.prop('contextAction_rename'),
          handler: $.proxy(that.renameHandler, that)
        },
        {id: "separator"},
        {
          id: "pasteButton",
          text: this.i18n.prop('contextAction_paste'),
          handler: $.proxy(that.pasteHandler, that)
        },
        {id: "separator"},
        {
          id: "uploadButton",
          text: this.i18n.prop('contextAction_upload'),
          handler: $.proxy(that.uploadHandler, that)
        },
        {
          id: "downloadButton",
          text: this.i18n.prop('contextAction_download'),
          handler: $.proxy(that.downloadHandler, that)
        },
        {id: "optional-separator"},
        {
          id: "propertiesButton",
          text: this.i18n.prop('contextAction_properties'),
          handler: $.proxy(that.propertiesHandler, that)
        }
      ];

      that.initEventHandlers();

    },

    buttons: [],

    initEventHandlers: function () {
      // listen for file action events
      if (window.top.mantle_addHandler) {
        window.top.mantle_addHandler("SolutionFolderActionEvent", this.eventLogger);
        window.top.mantle_addHandler("SolutionFileActionEvent", this.postPropertiesHandler);
      }
    },

    buildParameter: function (path, title) {
      return {
        solutionPath: (path == null ? "/" : path ),
        fileNames: (title ? null : title)
      };
    },

    urlParam: function (paramName) {
      var value = new RegExp('[\\?&]' + paramName + '=([^&#]*)').exec(window.top.location.href);
      if (value) {
        return value[1];
      }
      else {
        return null;
      }
    },

    canPublish: function (canPublish) {
      if (canPublish) {
        $('#uploadButton').show();
      }
      else {
        $('#uploadButton').hide();
      }
      if ((  $('#uploadButton').css('display') == "none" ) &&
          (  $('#downloadButton').css('display') == "none" )) {
        $('#optional-separator').hide()
      }
      else {
        $('#optional-separator').show()
      }

    },

    canDownload: function (canDownload) {
      if (canDownload) {
        $('#downloadButton').show();
      }
      else {
        $('#downloadButton').hide();
      }
      if ((  $('#uploadButton').css('display') == "none" ) &&
          (  $('#downloadButton').css('display') == "none" )) {
        $('#optional-separator').hide()
      }
      else {
        $('#optional-separator').show()
      }

    },

    updateFolderPermissionButtons: function (permissions, multiSelectItems) {
      if (permissions != false) {

        var writePerm = "false";
        for (var i = 0; i < permissions.setting.length; i++) {
          //Write Permission
          if (permissions.setting[i].name == "1") {
            writePerm = permissions.setting[i].value;
          }
        }

        if (writePerm == "true") {
          var disabled = (multiSelectItems.length > 0) ? false : true;
          $("#pasteButton").prop("disabled", disabled);
          $("#renameButton").prop("disabled", false);
        }

        else {
          $("#pasteButton").prop("disabled", true);
          $("#renameButton").prop("disabled", true);
        }
      }
    },

    eventLogger: function (event) {
      console.log(event.action + " : " + event.message);
    },

    newFolderHandler: function (path) {
      if (window.top != undefined)
        window.top.executeCommand("NewFolderCommand", this.buildParameter(path));
    },

    deleteFolderHandler: function (path) {
      window.top.executeCommand("DeleteFolderCommand", this.buildParameter(path));
    },

    pasteHandler: function (path, title, id,  multiSelectItems, browserUtils) {
      if (browserUtils.multiSelectItems.length > 0 ) {
        window.top.executeCommand("PasteFilesCommand", this.buildParameter(path));
      }
    },

    uploadHandler: function (path) {
      window.top.executeCommand("ImportFileCommand", this.buildParameter(path));
    },

    downloadHandler: function (path) {
      window.top.executeCommand("ExportFileCommand", this.buildParameter(path));
    },

    propertiesHandler: function (path) {
      window.top.executeCommand("FilePropertiesCommand", this.buildParameter(path));
    },

    postPropertiesHandler: function () {
      $(window.parent.document).find(".pentaho-dialog:not(.modal)").attr("id", "browse-properties-dialog");
    },
    renameHandler: function (path) {
      this.renameDialog.init(path, "folder");
    }

  };

  var FolderButtons = function (i18n) {
    this.i18n = i18n;
    this.init();
  }
  FolderButtons.prototype = local;
  return FolderButtons;
});
