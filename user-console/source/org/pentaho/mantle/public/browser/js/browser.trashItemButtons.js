/*
 * ******************************************************************************
 * Pentaho
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 */

pen.define([
    "common-ui/jquery-i18n",
    "common-ui/jquery"
], function() {

    var local = {

        init: function() {

            // retrieve i18n map
            var that = this; // trap this

            // initialize buttons definitions
            that.buttons = [
                {
                    id: "restore",
                    text: "Restore",
                    i18n: "contextAction_restore",
                    handler: $.proxy(that.restoreHandler, that)
                },

                {
                    id: "permDel",
                    text: "Permanently Delete",
                    i18n: "contextAction_permDelete",
                    handler: $.proxy(that.permDeleteHandler, that)
                }
            ];

            // retrieve i18n map
            jQuery.i18n.properties({
                name: 'messages',
                mode: 'map',
                language: that.urlParam('locale'),
                callback: function () {
                    // replace default text with locale properties
                    $(that.buttons).each(function(idx, fb){
                        if(fb.i18n){
                            var localeString = jQuery.i18n.prop(fb.i18n);
                            if(localeString && (localeString != '['+fb.i18n+']')){
                                fb.text = localeString;
                            }
                        }
                    });
                }
            });
            that.initEventHandlers();
        },

        buttons: [],

        initEventHandlers: function(){
            // listen for file action events
            if(window.top.mantle_addHandler != undefined)
                window.top.mantle_addHandler("SolutionFileActionEvent", this.eventLogger);
        },

        buildParameter: function(fileList, type, mode){
            return {
                fileList: fileList,
                type: type,
                mode: mode
            };
        },

        urlParam: function(paramName){
            var value = new RegExp('[\\?&]' + paramName + '=([^&#]*)').exec(window.top.location.href);
            if(value){
                return value[1];
            }
            else{
                return null;
            }
        },

        eventLogger: function(event){
            console.log(event.action + " : " + event.message);
        },

        restoreHandler: function(fileList, type, mode){
            window.top.executeCommand("RestoreFileCommand", this.buildParameter(fileList, type, null));
        },

        permDeleteHandler: function(fileList, type, mode){
            window.top.executeCommand("DeletePermanentFileCommand", this.buildParameter(fileList, type, mode));
        }
    };

    var TrashItemButtons = function(){
        this.init();
    }
    TrashItemButtons.prototype = local;
    return TrashItemButtons;
});