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
			jQuery.i18n.properties({
	      name: 'messages',
	      mode: 'map',
	      callback: function () {
	      	// replace default text with locale properties
					$(that.buttons).each(function(idx, fb){
						if(fb.i18n){
							var localeString = jQuery.i18n.prop(fb.i18n);
							if(localeString){
								fb.text = localeString;
							}
						}	
					});
	      }
	    });

			// initialize buttons definitions
			that.buttons = [
				{
					id: "openButton",	
					text: "Open",	
					i18n: "contextAction_open", 							
					handler: $.proxy(that.openButtonHandler, that)
				},
				{
					id: "openNewButton", 
					text: "Open in a new window", 
					i18n: "contextAction_openNewWindow", 			
					handler: $.proxy(that.openNewButtonHandler, that)
				},
				{
					id: "runButton", 
					text: "Run in background", 
					i18n: "contextAction_runBackground",
					handler: $.proxy(that.runInBackgroundHandler, that)
				},
				{
					id: "editButton", 
					text: "Edit", 
					i18n: "contextAction_edit",
					handler: $.proxy(that.editHandler, that)
				},
				{id: "separator"},
				{
					id: "deleteButton", 
					text: "Delete",	
					i18n: "contextAction_delete",
					handler: $.proxy(that.deleteHandler, that)
				},
				{
					id: "cutbutton", 
					text: "Cut", 
					i18n: "contextAction_cut",
					handler: $.proxy(that.cutHandler, that)
				},
				{
					id: "copyButton", 
					text: "Copy", 
					i18n: "contextAction_copy",
					handler: $.proxy(that.copyHandler, that)
				},
				{id: "separator"},
				{
					id: "downloadButton",	
					text: "Download...", 
					i18n: "contextAction_download",
					handler: $.proxy(that.downloadHandler, that)
				},
				{id: "separator"},
				{
					id: "shareButton", 
					text: "Share...", 
					i18n: "contextAction_share",
					handler: $.proxy(that.shareHandler, that)
				},
				{
					id: "scheduleButton",	
					text: "Schedule...", 
					i18n: "contextAction_schedule",
					handler: $.proxy(that.scheduleHandler, that)
				},
				{
					id: "showButton", 
					text: "Show Generated Content...", 
					i18n: "contextAction_showGeneratedContent",
					handler: $.proxy(that.showHandler, that)
				},
				{
					id: "favoritesButton", 
					text: "Add to Favorites", 
					i18n: "contextAction_addToFavorites",
					handler: $.proxy(that.favoritesHandler, that)
				},
				{
					id: "propertiesButton", 
					text: "Properties",	
					i18n: "contextAction_properties",
					handler: $.proxy(that.propertiesHandler, that)
				}
			];

      that.initEventHandlers();

    },

		buttons: [],

    initEventHandlers: function(){
      // listen for file action events
      window.top.mantle_addHandler("SolutionFileActionEvent", this.eventLogger);
    },

    buildParameter: function(path, title){
      return {
        solutionPath: (path == null ? ":" : path.replace(/\//g, ":")),
        solutionTitle: (title ? null : title)
      };
    },

    eventLogger: function(event){
      console.log(event.action + " : " + event.message);
    },

		openButtonHandler: function(path){
      window.top.mantle_openRepositoryFile(path, "RUN");
		},

		openNewButtonHandler: function(path){
      window.top.mantle_openRepositoryFile(path, "NEWWINDOW");
		},

		runInBackgroundHandler: function(path, title){
      window.top.executeCommand("RunInBackgroundCommand", this.buildParameter(path, title));
		},

		editHandler: function(path){
      window.top.mantle_openRepositoryFile(path, "EDIT");
		},

    deleteHandler: function(path){
      window.top.executeCommand("DeleteFileCommand", this.buildParameter(path));
    },

    cutHandler: function(path){
      window.top.executeCommand("CutFilesCommand", this.buildParameter(path));
    },

    copyHandler: function(path){
      window.top.executeCommand("CopyFilesCommand", this.buildParameter(path));
    },

    downloadHandler: function(path){
      window.top.executeCommand("ExportFileCommand", this.buildParameter(path));
    },

		shareHandler: function(path){
      window.top.executeCommand("ShareFileCommand", this.buildParameter(path));
    },

		scheduleHandler: function(path){
      window.top.mantle_openRepositoryFile(path, "SCHEDULE_NEW");
		},

		showHandler: function(path){
      window.top.executeCommand("ShowGeneratedContentCommand", this.buildParameter(path));
		},

    favoritesHandler: function(path, title){
      window.top.mantle_addFavorite(path, title);
    },

		propertiesHandler: function(path){
      window.top.executeCommand("FilePropertiesCommand", this.buildParameter(path));
		}
	};

	var FileButtons = function(){
    this.init();
  }
  FileButtons.prototype = local;

  return FileButtons;

});