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
  "pentaho/csrf/service"
], function(csrfService) {

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

    getContent: function (serviceUrl, successCallback, errorCallback, beforeSendCallback, isProtected) {
      this._makeAjaxCall("GET", "json", serviceUrl, true, successCallback, errorCallback, beforeSendCallback, isProtected);
    },

    putContent: function (serviceUrl, successCallback, errorCallback, beforeSendCallback, isProtected) {
      this._makeAjaxCall("POST", "json", serviceUrl, true, successCallback, errorCallback, beforeSendCallback, isProtected);
    },

    _makeAjaxCall: function (type, dataType, serviceUrl, async, successCallback, errorCallback, beforeSendCallback, isProtected) {
      var now = new Date();
      var url = serviceUrl + (serviceUrl.indexOf("?") > -1 ? "&" : "?") + "ts=" + now.getTime();

      var headers = {};
      if(isProtected) {
        var csrfToken = csrfService.getToken(url);
        if(csrfToken !== null) {
          headers[csrfToken.header] = csrfToken.token;
        }
      }

      $.ajax({
        type: type,
        dataType: dataType,
        url: url,
        async: async,
        headers: headers,
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
          canSchedule = type.canSchedule;
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
          canEdit = type.canEdit;
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
