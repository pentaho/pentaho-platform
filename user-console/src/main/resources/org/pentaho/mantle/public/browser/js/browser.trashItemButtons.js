/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
      if (window.parent.mantle_addHandler != undefined)
        window.parent.mantle_addHandler("SolutionFileActionEvent", this.eventLogger);
    },

    buildParameter: function (fileList, type, mode) {
      return {
        fileList: fileList,
        type: type,
        mode: mode
      };
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

    restoreHandler: function (fileList, type, mode) {
      window.parent.executeCommand("RestoreFileCommand", this.buildParameter(fileList, type, null));
    },

    permDeleteHandler: function (fileList, type, mode) {
      window.parent.executeCommand("DeletePermanentFileCommand", this.buildParameter(fileList, type, mode));
    }
  };

  var TrashItemButtons = function (i18n) {
    this.i18n = i18n;
    this.init();
  }
  TrashItemButtons.prototype = local;
  return TrashItemButtons;
});
