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
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

define([
  "common-ui/jquery-pentaho-i18n",
  "common-ui/jquery"
], function () {

  var local = {

    init: function () {

      // retrieve i18n map
      var that = this; // trap this

      // initialize buttons definitions
      that.buttons = [
        {
          id: "openButton",
          text: this.i18n.prop('contextAction_open'),
          handler: $.proxy(that.openButtonHandler, that)
        },
        {id: "separator"},
        {
          id: "cutButton",
          text: this.i18n.prop('contextAction_cut'),
          handler: $.proxy(that.cutHandler, that)
        },
        {
          id: "copyButton",
          text: this.i18n.prop('contextAction_copy'),
          handler: $.proxy(that.copyHandler, that)
        },
        {
          id: "deleteButton",
          text: this.i18n.prop('contextAction_delete'),
          handler: $.proxy(that.deleteHandler, that)
        }
      ];

      that.initEventHandlers();
    },

    buttons: [],

    initEventHandlers: function () {
      // listen for file action events
      if (window.parent.mantle_addHandler != undefined)
        window.parent.mantle_addHandler("SolutionFolderActionEvent", this.eventLogger);
    },

    openButtonHandler: function (path) {
      for(var i=0;i<path.length;i++){
        window.parent.mantle_openRepositoryFile(path[i], "RUN");
      }
    },

    cutHandler: function (path, title, id) {
      window.parent.executeCommand("CutFilesCommand", this.buildParameter(path, title, id));
    },

    copyHandler: function (path, title, id) {
      window.parent.executeCommand("CopyFilesCommand", this.buildParameter(path, title, id));
    },

    deleteHandler: function (path, title, id) {
      window.parent.executeCommand("DeleteFileCommand", this.buildParameter(path, title, id));
    },

      buildParameter: function (path, title, id) {
          for (var i=0;i<path.length;i++){
              var tmpPath=path[i];
              var tmpTitle=title[i];
              var tmpId=id[i];
              path[i] = (tmpPath == null ? "/" : tmpPath );
              title[i] = (tmpTitle == null ? "" : tmpTitle );
              id[i] = (tmpId == null ? "" : tmpId );
          }
          var tabbedPaths=path.join("\n");
          var tabbedTitles=title.join("\n");
          var tabbedIds=id.join("\n");

          var retObj= {
              solutionPath: tabbedPaths,
              fileNames: tabbedTitles,
              fileIds: tabbedIds
          };
          return retObj;
     },

    urlParam: function (paramName) {
      var value = new RegExp('[\\?&]' + paramName + '=([^&#]*)').exec(window.parent.location.href);
      if (value) {
        return value[1];
      }
      else {
        return null;
      }
    },

    eventLogger: function (event) {
      console.log(event.action + " : " + event.message);
    },

    enableButtons: function (enableButtons) {
      this.buttons.forEach((buttonDef) => {
        if (buttonDef.id !== "openButton") {
          let target;
          if (buttonDef.id == "separator") {
            target = $(".separator");
          } else {
            target = $("#" + buttonDef.id);
          }

          target.prop("disabled", !enableButtons);

          if (!enableButtons) {
            target.hide();
          } else {
            target.show();
          }
        }
      });
    }

  };

  var MultiSelectButtons = function (i18n) {
    this.i18n = i18n;
    this.init();
  }
  MultiSelectButtons.prototype = local;
  return MultiSelectButtons;
});
