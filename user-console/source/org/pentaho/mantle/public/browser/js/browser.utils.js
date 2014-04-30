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

    resetCutItemsStyle: function () {
      var that = this; // trap this
      for (var i=0;i<that.cutItems.length;i++){
        that.cutItems[i].obj.removeClass("hiddenFileLabel");
      }
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
