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
    renameDialog: null,

		init: function() {
			var that = this; // trap this

      that.browserUtils = new BrowserUtils();

			// initialize buttons definitions
			that.buttons = [
				{
					id: "openButton",	
					text: this.i18n.prop('contextAction_open'),
					handler: $.proxy(that.openButtonHandler, that)
				},
				{
					id: "openNewButton", 
					text: this.i18n.prop('contextAction_openNewWindow'),
					handler: $.proxy(that.openNewButtonHandler, that)
				},
				{
					id: "runButton", 
					text: this.i18n.prop('contextAction_runBackground'),
					handler: $.proxy(that.runInBackgroundHandler, that)
				},
				{
					id: "editButton", 
					text: this.i18n.prop('contextAction_edit'),
					handler: $.proxy(that.editHandler, that)
				},
				{id: "separator"},
				{
					id: "cutbutton", 
					text: this.i18n.prop('contextAction_cut'),
					handler: $.proxy(that.cutHandler, that)
				},
				{
					id: "copyButton", 
					text: this.i18n.prop('contextAction_copy'),
					handler: $.proxy(that.copyHandler, that)
				},
		        {
		          id: "deleteButton", 
		          text: this.i18n.prop('contextAction_delete'),
		          handler: $.proxy(that.deleteHandler, that)
		        },
		        {
		          id: "renameButton", 
		          text: this.i18n.prop('contextAction_rename'),
		          handler: $.proxy(that.renameHandler, that)
		        },
				{id: "separator"},
				{
					id: "downloadButton",	
					text: this.i18n.prop('contextAction_download'),
					handler: $.proxy(that.downloadHandler, that)
				},
				{id: "optional-separator"},
				{
					id: "shareButton", 
					text: this.i18n.prop('contextAction_share'),
					handler: $.proxy(that.shareHandler, that)
				},
				{
					id: "scheduleButton",	
					text: this.i18n.prop('contextAction_schedule'),
					handler: $.proxy(that.scheduleHandler, that)
				},
				{
					id: "favoritesButton", 
					text: this.i18n.prop('contextAction_addToFavorites'),
					handler: $.proxy(that.favoritesHandler, that)
				},
				{
					id: "propertiesButton", 
					text: this.i18n.prop('contextAction_properties'),
					handler: $.proxy(that.propertiesHandler, that)
				}
			];

      that.initEventHandlers();
      that.refreshFavoritesList();
    },

    browserUtils: null,

		buttons: [],

		favoriteItems: [],

		updateFavoriteItems: true,

		isFavorite: false,

    renameDialog: null,

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

    canDownload: function(canDownload) {
    	if (canDownload) {
            $('#downloadButton').show();
			$('#optional-separator').show();            
        }
        else{
        	$('#downloadButton').hide();
			$('#optional-separator').hide();
        }
    },

    eventLogger: function(event){
      console.log(event.action + " : " + event.message);
    },

    updateFilePermissionButtons: function(permissions){
      if (permissions!=false) {

        for (var i=0;i<permissions.setting.length;i++){
          //Delete permission
          if(permissions.setting[i].name=="2"){
            if(permissions.setting[i].value=="true"){
              $("#deleteButton").prop("disabled", false);
              $("#cutbutton").prop("disabled", false);
            }
          else{
              $("#deleteButton").prop("disabled", true);
              $("#cutbutton").prop("disabled", true);
           }
          }
        else{
            $("#deleteButton").prop("disabled", true);
            $("#cutbutton").prop("disabled", true);
         }
          //Write Permission
          if(permissions.setting[i].name=="1"){
            if(permissions.setting[i].value=="true"){
              $("#renameButton").prop("disabled", false);
            }
            else{
              $("#renameButton").prop("disabled", true);
            }
          }
        }
      }
    },

    eventLogger: function(event){
      console.log(event.action + " : " + event.message);
    },

    updateCopyButtonPanel: function(canCopy){
        if (canCopy=="true") {
            $('#copyButton').show();
        }
        else{
            $('#copyButton').hide();
        }
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
      if(this.browserUtils.isFileExecutable(extension) && 
         this.browserUtils.isEditAllowed(extension) ){
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
		},

    renameHandler: function(path){
      this.renameDialog.init(path, "file");
    }
	};

	var FileButtons = function(i18n){
    this.i18n=i18n;
    this.init();
  }
  FileButtons.prototype = local;

  return FileButtons;

});