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

    init: function () {

      // retrieve i18n map
      var that = this; // trap this

      // initialize buttons definitions
      that.buttons = [
        {
          id: "restore",
          text: this.i18n.prop('contextAction_restore'),
          handler: $.proxy(that.restoreHandler, that)
        },

        {
          id: "permDel",
          text: this.i18n.prop('contextAction_permDelete'),
          handler: $.proxy(that.permDeleteHandler, that)
        }
      ];

      that.initEventHandlers();
    },

    buttons: [],

    initEventHandlers: function () {
      // listen for file action events
      if (window.top.mantle_addHandler != undefined)
        window.top.mantle_addHandler("SolutionFileActionEvent", this.eventLogger);
    },

    buildParameter: function (fileList, type, mode) {
      return {
        fileList: fileList,
        type: type,
        mode: mode
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

    eventLogger: function (event) {
      console.log(event.action + " : " + event.message);
    },

    restoreHandler: function (fileList, type, mode) {
      window.top.executeCommand("RestoreFileCommand", this.buildParameter(fileList, type, null));
    },

    permDeleteHandler: function (fileList, type, mode) {
      window.top.executeCommand("DeletePermanentFileCommand", this.buildParameter(fileList, type, mode));
    }
  };

  var TrashItemButtons = function (i18n) {
    this.i18n = i18n;
    this.init();
  }
  TrashItemButtons.prototype = local;
  return TrashItemButtons;
});
