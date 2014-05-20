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
], function () {

  var local = {

    executableTypesUrl: 'api/repos/executableTypes',
    executableTypes: [],
    cutItems: [],
    multiSelectItems: [],

    _init: function () {

      var that = this; // trap this

      var serviceUrl = that.getUrlBase() + that.executableTypesUrl;
      that.getContent(serviceUrl, function (result) {
        that.executableTypes = result.executableFileTypeDto;
        console.log('Executable Types Loaded...');
      }, function (error) {
        console.log(error);
      });
    },

    applyHiddenFileLabelStyle: function (elementsArray) {
      for (var i=0;i<elementsArray.length;i++){
          $("#" + elementsArray[i].attr('id')).addClass("hiddenFileLabel");
      }
    },

    removeHiddenFileLabelStyle: function (elementsArray) {
      for (var i=0;i<elementsArray.length;i++){
          $("#" + elementsArray[i].attr('id')).removeClass("hiddenFileLabel");
      }
    },

    enableElements: function (elementsArray) {
      for (var i=0;i<elementsArray.length;i++){
          elementsArray[i].prop("disabled", false);
      }
    },

    disableElements: function (elementsArray) {
      for (var i=0;i<elementsArray.length;i++){
          elementsArray[i].prop("disabled", true);
      }
    },

    enableSpinners: function (modelsArray) {
      for (var i=0;i<modelsArray.length;i++){
          modelsArray[i].set("runSpinner", true);
      }
    },

    disableSpinners: function (modelsArray) {
      for (var i=0;i<modelsArray.length;i++){
          modelsArray[i].set("runSpinner", false);
          modelsArray[i].updateData();
      }
    },

    resetCutItemsStyle: function () {
      var that = this; // trap this
      var elementsArray = [];
      for (var i=0;i<that.cutItems.length;i++){
          elementsArray.push(that.cutItems[i].obj);
      }
      that.removeHiddenFileLabelStyle(elementsArray);
    },

    applyCutItemsStyle: function () {
        var that = this; // trap this
        var elementsArray = [];
        for (var i=0;i<that.cutItems.length;i++){
            elementsArray.push(that.cutItems[i].obj);
        }
        that.applyHiddenFileLabelStyle(elementsArray);
    },

    trackItems: function (cutItems, multiSelectItems) {
      var that = this; // trap this
      // remove hiddenFileLabel class
      that.resetCutItemsStyle();
      //reset cutItems to currently cut items
      that.cutItems = cutItems;
      //keep track of previously selected items
      that.multiSelectItems = multiSelectItems;
      //apply hiddenFileLable class to currently cut items
      that.applyCutItemsStyle();
    },

    uiButtonFeedback: function (elementsToDisableArray, elementsToEnableArray) {
      var that = this; // trap this
      that.enableElements(elementsToEnableArray);
      that.disableElements(elementsToDisableArray);
    },

    uiSpinnerFeedback: function (modelsToDisableSpinnerArray, modelsToEnableSpinnerArray) {
      var that = this; // trap this
      that.enableSpinners(modelsToEnableSpinnerArray);
      that.disableSpinners(modelsToDisableSpinnerArray);
    },

    getUrlBase: function () {
      return window.location.pathname.substring(0, window.location.pathname.indexOf("/mantle/")) + "/";
    },

    getContent: function (serviceUrl, successCallback, errorCallback, beforeSendCallback) {
      this._makeAjaxCall("GET", "json", serviceUrl, true, successCallback, errorCallback, beforeSendCallback);
    },

    putContent: function (serviceUrl, successCallback, errorCallback, beforeSendCallback) {
      this._makeAjaxCall("POST", "json", serviceUrl, true, successCallback, errorCallback, beforeSendCallback);
    },

    _makeAjaxCall: function (type, dataType, serviceUrl, async, successCallback, errorCallback, beforeSendCallback) {
      var now = new Date();
      var url = serviceUrl + (serviceUrl.indexOf("?") > -1 ? "&" : "?") + "ts=" + now.getTime();

      $.ajax({
        type: type,
        dataType: dataType,
        url: url,
        async: async,
        success: function (result) {
          if (successCallback) {
            successCallback(result);
          }
        },
        error: function (err) {
          console.log(err);
          if (errorCallback) {
            errorCallback(err);
          }
        },
        beforeSend: function () {
          if (beforeSendCallback) {
            beforeSendCallback();
          }
        }
      });
    },

    isFileExecutable: function (fileExtension) {
      var isExecutable = false;
      $.each(this.executableTypes, function (idx, type) {
        if (fileExtension == type.extension) {
          isExecutable = true;
          return false; // break the $.each loop
        }
      });
      return isExecutable;
    },

    isScheduleAllowed: function (fileExtension) {
      var isExecutable = this.isFileExecutable(fileExtension);
      var canSchedule = false;
      $.each(this.executableTypes, function (idx, type) {
        if (fileExtension == type.extension) {
          canSchedule = type.canSchedule == 'true';
          return false; // break the $.each loop
        }
      });
      return isExecutable && canSchedule;
    },

    isEditAllowed: function (fileExtension) {
      var isExecutable = this.isFileExecutable(fileExtension);
      var canEdit = false;
      $.each(this.executableTypes, function (idx, type) {
        if (fileExtension == type.extension) {
          canEdit = type.canEdit == 'true';
          return false; // break the $.each loop
        }
      });
      return isExecutable && canEdit;
    }
  };

  var BrowserUtils = function () {
    this._init();
  }
  BrowserUtils.prototype = local;

  return BrowserUtils;

});
