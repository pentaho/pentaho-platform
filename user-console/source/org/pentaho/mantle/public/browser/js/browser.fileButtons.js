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

    },

		buttons: [],

		openButtonHandler: function(path){
			alert('Open ' + path);
		},

		openNewButtonHandler: function(path){
			alert('Open in a new window ' + path);
		},

		runInBackgroundHandler: function(path){
			alert('Run in background ' + path);
		},

		editHandler: function(path){
			alert(path); // TODO
		},

		deleteHandler: function(path){
			alert(path); // TODO
		},

		cutHandler: function(path){
			alert(path); // TODO
		},

		copyHandler: function(path){
			alert(path); // TODO
		},

		downloadHandler: function(path){
			alert(path); // TODO
		},

		shareHandler: function(path){
			alert(path); // TODO
		},

		scheduleHandler: function(path){
			alert(path); // TODO
		},

		showHandler: function(path){
			alert(path); // TODO
		},

		favoritesHandler: function(path){
			alert(path); // TODO
		},

		propertiesHandler: function(path){
			alert(path); // TODO
		}
	};

	var FileButtons = function(){
    this.init();
  }
  FileButtons.prototype = local;

  return FileButtons;

});