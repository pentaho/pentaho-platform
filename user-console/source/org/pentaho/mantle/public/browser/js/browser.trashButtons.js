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
                    id: "purge",
                    text: this.i18n.prop('contextAction_purge'),
                    handler: $.proxy(that.purgeHandler, that)
                }
            ];

            that.initEventHandlers();
        },

        buttons: [],

        initEventHandlers: function(){
            // listen for file action events
            if(window.top.mantle_addHandler != undefined)
                window.top.mantle_addHandler("SolutionFolderActionEvent", this.eventLogger);
        },

        buildParameter: function(fileList,mode){
            return {
                fileList: fileList,
                mode:mode
            };
        },

        onTrashSelect: function(empty){
           //if trash can is empty grey out purge button
           if(empty){
            $("#purge").attr("disabled", "disabled");
           }
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

        purgeHandler: function(files, type, mode){
            window.top.executeCommand("DeletePermanentFileCommand", this.buildParameter(files, mode));
        }
    };

    var TrashButtons = function(i18n){
        this.i18n=i18n;
        this.init();
    }
    TrashButtons.prototype = local;
    return TrashButtons;
});