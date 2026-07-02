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
          id: "purge",
          text: this.i18n.prop('contextAction_purge'),
          handler: $.proxy(that.purgeHandler, that)
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

    buildParameter: function (fileList, mode) {
      return {
        fileList: fileList,
        mode: mode
      };
    },

    onTrashSelect: function (empty) {
      //if trash can is empty grey out purge button
      if (empty) {
        $("#purge").attr("disabled", "disabled");
      }
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

    purgeHandler: function (files, type, mode) {
      window.parent.executeCommand("DeletePermanentFileCommand", this.buildParameter(files, mode));
    }
  };

  var TrashButtons = function (i18n) {
    this.i18n = i18n;
    this.init();
  }
  TrashButtons.prototype = local;
  return TrashButtons;
});
