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
					text: "New Folder", 
					i18n: "contextAction_newFolder",
					handler: $.proxy(that.newFolderHandler, that)
				},
				{
					id: "deleteFolderButton",	
					text: "Delete",	
					i18n: "contextAction_delete", 							
					handler: $.proxy(that.deleteFolderHandler, that)
				},
				{id: "separator"},
				{
					id: "pasteButton",			
					text: "Paste", 			
					i18n: "contextAction_paste",
					handler: $.proxy(that.pasteHandler, that)
				},
				{id: "separator"},
				{
					id: "uploadButton",		
					text: "Upload", 		
					i18n: "contextAction_upload",
					handler: $.proxy(that.uploadHandler, that)
				},
				{
					id: "downloadButton",	
					text: "Download", 	
					i18n: "contextAction_download",
					handler: $.proxy(that.downloadHandler, that)
				},
				{id: "separator"},
				{
					id: "propertiesButton",
					text: "Properties", 
					i18n: "contextAction_properties",
					handler: $.proxy(that.propertiesHandler, that)
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

	var FolderButtons = function(){
    this.init();
  }
  FolderButtons.prototype = local;
  return FolderButtons;
});