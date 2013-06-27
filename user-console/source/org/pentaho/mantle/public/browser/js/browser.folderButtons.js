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
					id: "newFolderButton",	
					text: this.i18n.prop('contextAction_newFolder'),
					handler: $.proxy(that.newFolderHandler, that)
				},
				{
					id: "deleteFolderButton",	
					text: this.i18n.prop('contextAction_delete'),
					handler: $.proxy(that.deleteFolderHandler, that)
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

    initEventHandlers: function(){
    	// listen for file action events
    	if(window.top.mantle_addHandler) {
    		window.top.mantle_addHandler("SolutionFolderActionEvent", this.eventLogger);
    		window.top.mantle_addHandler("SolutionFileActionEvent", this.postPropertiesHandler);
    	}
    },

    buildParameter: function(path, title){
      return {
        solutionPath: (path == null ? ":" : path.replace(/\//g, ":")),
        solutionTitle: (title ? null : title)
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

    canPublish: function(canPublish) {
    	if (canPublish) {
            $('#uploadButton').show();
        }
        else{
        	$('#uploadButton').hide();
        }
		if  ( (  $('#uploadButton').css('display') == "none" ) &&
				  (  $('#downloadButton').css('display') == "none" )  ) {
				$('#optional-separator').hide() }
			else {
				$('#optional-separator').show() 
			}

    },

    canDownload: function(canDownload) {
    	if (canDownload) {
            $('#downloadButton').show();
        }
        else{
        	$('#downloadButton').hide();
        }
		if  ( (  $('#uploadButton').css('display') == "none" ) &&
				  (  $('#downloadButton').css('display') == "none" )  ) {
				$('#optional-separator').hide() }
			else {
				$('#optional-separator').show() 
			}

    },

    eventLogger: function(event){
      console.log(event.action + " : " + event.message);
    },

		newFolderHandler: function(path){
			if(window.top != undefined)
      			window.top.executeCommand("NewFolderCommand", this.buildParameter(path));
		},

		deleteFolderHandler: function(path){
			window.top.executeCommand("DeleteFolderCommand", this.buildParameter(path));
		},

		pasteHandler: function(path){
			window.top.executeCommand("PasteFilesCommand", this.buildParameter(path));
		},

		uploadHandler: function(path){
			window.top.executeCommand("ImportFileCommand", this.buildParameter(path));
		},

		downloadHandler: function(path){
			window.top.executeCommand("ExportFileCommand", this.buildParameter(path));
		},

		propertiesHandler: function(path){
			window.top.executeCommand("FilePropertiesCommand", this.buildParameter(path));
		},

		postPropertiesHandler: function() {
			$(window.parent.document).find(".pentaho-dialog").attr("id", "browse-properties-dialog");
		}

	};

	var FolderButtons = function(i18n){
        this.i18n=i18n;
        this.init();
  }
  FolderButtons.prototype = local;
  return FolderButtons;
});