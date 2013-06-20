/*
 * ******************************************************************************
 * Pentaho
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 */

pen.define([
  "js/browser.utils.js",
  "common-ui/jquery-i18n",
  "common-ui/jquery"
], function(BrowserUtils) {

	var local = {

		init: function() {

			// retrieve i18n map
			var that = this; // trap this

      that.browserUtils = new BrowserUtils();

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
      that.refreshFavoritesList();
    },

    browserUtils: null,

		buttons: [],

		favoriteItems: [],

		updateFavoriteItems: true,

		isFavorite: false,

    initEventHandlers: function(){
    	var that = this; // trap this

      // listen for file action events
      if(window.top.mantle_addHandler != undefined){
      	window.top.mantle_addHandler("SolutionFileActionEvent", $.proxy(that.eventLogger, that));
      	window.top.mantle_addHandler("FavoritesChangedEvent", $.proxy(that.onFavoritesChanged, that));
      	window.top.mantle_addHandler("GenericEvent", $.proxy(that.onNewFavoritesItems, that));		
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

    // Listen for changes in favorites. Do not update list immediately.
    onFavoritesChanged: function(event){
    	this.updateFavoriteItems = true;
    	console.log("Favorite items marked for update..." );
    },

    // Handle response from FavoritesController. Update favorite items.
    onNewFavoritesItems: function(event){
    	if(event.eventSubType == 'favoritesListResponse'){
        this.favoriteItems = JSON.parse(event.stringParam);
        this.updateFavoriteItems = false;
        console.log('Favorite items updated...');
      }
    },

    isItemAFavorite: function(path){
    	var isFave = false;
      $.each(this.favoriteItems, function(idx, fave) {
        if(path == fave.fullPath) {
          isFave = true;
          return false; // break the $.each loop
        }
      });
      return isFave;
    },

    // Send request to FavoritesController to get the current list of favorites.
    refreshFavoritesList: function(){
			if(this.updateFavoriteItems && window.top.mantle_fireEvent){
        window.top.mantle_fireEvent('GenericEvent', {"eventSubType": "favoritesListRequest"});  
      }
    },

    // Respond to changes in selected file
    onFileSelect: function(path){
    	this.refreshFavoritesList(); // refresh if necessary
    	this.toggleFavoriteContext(path);

      // BISERVER-9415
      var extension = path.substring(path.lastIndexOf('.') + 1, path.length);
      if(this.browserUtils.isScheduleAllowed(extension)){
        $('#runButton').show();
        $('#scheduleButton').show();
      }
      else{
        $('#runButton').hide();
        $('#scheduleButton').hide();
      }

      // BISERVER-9435
      if(this.browserUtils.isFileExecutable(extension)){
        $('#editButton').show();
      }
      else{
        $('#editButton').hide();
      }
    },

    toggleFavoriteContext: function(path){
    	if(this.isItemAFavorite(path)){
    		this.isFavorite = true;
    		this.updateFavoritesButton();
    	}
    	else{
    		this.isFavorite = false;
    		this.updateFavoritesButton();
    	}
    },

    updateFavoritesButton: function(){
    	if(this.isFavorite){
    		$favoritesButton = $('#favoritesButton');
    		$favoritesButton.text(jQuery.i18n.prop("contextAction_removeFromFavorites"));
    	}
    	else{
    		$favoritesButton = $('#favoritesButton');
    		$favoritesButton.text(jQuery.i18n.prop("contextAction_addToFavorites"));
    	}
    },

		openButtonHandler: function(path){
      window.top.mantle_openRepositoryFile(path, "RUN");
		},

		openNewButtonHandler: function(path){
      window.top.mantle_openRepositoryFile(path, "NEWWINDOW");
      window.top.mantle_setPerspective('browser.perspective');
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

    favoritesHandler: function(path, title){
    	if(this.isFavorite){
    		window.top.mantle_removeFavorite(path);	
    	}
    	else{
    		window.top.mantle_addFavorite(path, title);
    	}
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