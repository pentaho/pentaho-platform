/*
 * ******************************************************************************
 * Pentaho
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 */


this.FileBrowser = {};


FileBrowser.$container = null;
FileBrowser.fileBrowserModel = null;
FileBrowser.fileBrowserView = null;
FileBrowser.openFileHandler = undefined;


FileBrowser.setContainer =  function($container){
	this.$container = $container;
}

FileBrowser.setOpenFileHandler = function(handler){
	this.openFileHandler = handler;
}




FileBrowser.init = function(){

};

FileBrowser.update = function(){

	this.redraw();
};

FileBrowser.redraw = function(){

	this.fileBrowserModel = new FileBrowserModel({
		openFileHandler: this.openFileHandler
	});
	this.FileBrowserView = new FileBrowserView({
		model: this.fileBrowserModel,
		el: this.$container
	});

	this.FileBrowserView.render();
};


var FileBrowserModel = Backbone.Model.extend({
	defaults: {
		getAuthenticatedURL: "/pentaho/api/mantle/isAuthenticated",
		showHiddenFilesURL: "/pentaho/api/user-settings/MANTLE_SHOW_HIDDEN_FILES",

		fileButtons : {
			buttons: [
				{id: "openButton",		text: "Open"},
				{id: "openNewButton",	text: "Open in a new window"},
				{id: "runButton",		text: "Run in background"},
				{id: "editButton",		text: "Edit"},
				{id: "separator"},
				{id: "deleteButton",	text: "Delete"},
				{id: "cutbutton",		text: "Cut"},
				{id: "copyButton",		text: "Copy"},
				{id: "separator"},
				{id: "downloadButton",	text: "Download..."},
				{id: "separator"},
				{id: "shareButton",		text: "Share..."},
				{id: "scheduleButton",	text: "Schedule..."},
				{id: "showButton",		text: "Show Generated Content..."},
				{id: "favoritesButton",	text: "Add to Favorites"},
				{id: "propertiesButton",text: "Properties"}
			]
		},
		folderButtons : {
			buttons: [
				{id: "newFolderButton",	text: "New Folder"},
				{id: "openButton",		text: "Delete"},
				{id: "separator"},
				{id: "pasteButton",		text: "Paste"},
				{id: "separator"},
				{id: "uploadButton",	text: "Upload..."},
				{id: "downloadButton",	text: "Download..."},
				{id: "separator"},
				{id: "propertiesButton",text: "Properties..."}
			]
		},
		structure: {
			files: "Files",
			folders: "Folders"
		},

		foldersTreeModel: undefined,
		fileListModel: undefined,

		clickedFolder : undefined,
		clickedFile: undefined,

		lastClick: "folder", 

		data: undefined,
		fileData: undefined,

		openFileHandler: undefined
	},

	initialize: function(){
		var foldersTreeModel = this.get("foldersTreeModel"),
			fileListModel = this.get("fileListModel");

		//handle data
		var foldersObj = {}

		var folders = this.fetchTree("/");

		foldersTreeModel = new FileBrowserFolderTreeModel({
			data: folders			
		});
		fileListModel = new FileBrowserFileListModel({
			openFileHandler: this.get("openFileHandler")
		});

		foldersTreeModel.on("change:clicked", this.updateClicked, this);
		foldersTreeModel.on("change:clickedFolder", this.updateFolderClicked, this);
		this.on("change:clickedFolder", this.updateFileList, this);

		fileListModel.on("change:clickedFile", this.updateFileClicked, this);
		
		//handlers for buttons header update
		foldersTreeModel.on("change:clicked", this.updateFolderLastClick, this);
		fileListModel.on("change:clicked", this.updateFileLastClick, this);

		this.set("foldersTreeModel", foldersTreeModel);
		this.set("fileListModel", fileListModel);

		
	},

	fetchTree: function(path){
		var tree = null;

		if(this.isAuthenticated()){
			var url = this.getFileTreeRequest(path == null ? "/" : path);

			$.ajax({
				async: false,
			 	type: "GET",
			 	url: url,
			 	success: function(response){
					tree = response;
				},
				error: function(){
				}
			});
		} 

		return JSON.parse(tree).solution.folders[0];
	},

	fetchFileList: function(path){
		var tree = null;

		if(this.isAuthenticated()){
			var url = this.getFileListRequest(path == null ? "/" : path);

			$.ajax({
				async: false,
			 	type: "GET",
			 	url: url,
			 	success: function(response){
					tree = response;
				},
				error: function(){
				}
			});
		} 

		return JSON.parse(tree);
	},

	getFileTreeRequest: function(path){
		return "/pentaho/plugin/pentaho-cdf/api/getJSONSolution?mode=solutionTree&path="+
			path+"&depth=-1&filter=*&showHiddenFiles="+
			(this.showHiddenFiles() ? "true" : "false");
	},

	getFileListRequest: function(path){
		return "/pentaho/plugin/pentaho-cdf/api/getJSONSolution?mode=contentList&path="+
			path+"&depth=1&filter=*&showHiddenFiles="+
			(this.showHiddenFiles() ? "true" : "false");
	},


	isAuthenticated: function(){
		var returnValue = false;

		$.ajax({
			async: false,
			type: "GET",
			url: this.defaults.getAuthenticatedURL,
			success: function(response){
				returnValue = (response == "true" ? true : false);
			},
			error: function(){
				returnValue = false;
			}
		});
		
		return returnValue;
	},

	showHiddenFiles: function(){
		var returnValue = false;

		$.ajax({
			async: false,
			type: "GET",
			url: this.defaults.showHiddenFilesURL,
			success: function(response){
				returnValue = (response == "true" ? true : false);
			},
			error: function(){
				returnValue = false;
			}
		});
		
		return returnValue;
	},

	clearClicked: function(){
		this.set("clickedFolder", undefined);
		this.set("fileClicked", undefined);
	},

	updateClicked: function(){
		this.set("clicked", true);
	},

	updateFolderClicked: function(){
		this.set("clickedFolder",this.get("foldersTreeModel").get("clickedFolder"));
	},

	updateFileClicked: function(){
		this.set("clickedFile",this.get("fileListModel").get("clickedFile"));
	},

	updateFolderLastClick: function(){
		this.set("lastClick", "folder");
	},

	updateFileLastClick: function(){
		this.set("lastClick", "file");
	},

	getLastClick: function(){
		//clear last clicks
		//this.get("foldersTreeModel").set("clicked", undefined);
		//this.get("fileListModel").set("clicked", undefined);

		return this.get("lastClick");
	},

	getFolderClicked: function(){
		return this.get("clickedFolder");
	}, 

	getFileClicked: function(){
		return this.get("clickedFile");
	},

	updateFileList: function(){
		//fetch file list

		var path = this.get("clickedFolder").attr("path");

		var fileList = this.fetchFileList(path);

		this.set("fileData",fileList);
		this.get("fileListModel").set("fileData", fileList);

	}
});


var FileBrowserFolderTreeModel = Backbone.Model.extend({
	defaults: {
		clicked: false,
		clickedFolder: undefined,
		data: undefined		
	},

	initialize: function(){
		
	}
});

var FileBrowserFileListModel = Backbone.Model.extend({
	defaults: {
		clicked: false,
		clickedFile: undefined,
		fileData: undefined,
		openFileHander: undefined
	},

	initialize: function(){
		
	}

});


var FileBrowserView = Backbone.View.extend({
	attributes: {
		buttonsEnabled: false
	},

   	initialize: function() {
    	this.initializeOptions();
    	this.configureListeners();
  	},

  	configureListeners: function() {
  		//update buttons when changed folder/file
    	this.model.on("change:lastClick", this.updateButtons, this);

    	//update buttons header on folder/file selection
    	this.model.on("change:clickedFolder", this.updateButtonsHeader, this);
    	this.model.on("change:clickedFile", this.updateButtonsHeader, this);

    	//update folder and file browser headers on folder selection change
    	this.model.on("change:clickedFolder", this.updateFolderBrowserHeader, this);
    	this.model.on("change:clickedFolder", this.updateFileBrowserHeader, this);

    	//check buttons enabled
    	this.model.on("change:clickedFolder", this.checkButtonsEnabled, this);
    	this.model.on("change:clickedFile", this.checkButtonsEnabled, this);
  	},

  	initializeOptions: function() {
		foldersTreeView = undefined;
		fileListView = undefined;

  		this.foldersTreeView = new FileBrowserFolderTreeView({
  			model: this.model.get("foldersTreeModel"),
  			data: this.model.get("foldersTreeModel").get("data")
  		});

  		this.fileListView = new FileBrowserFileListView({
  			model: this.model.get("fileListModel"),
  			data: this.model.get("fileListModel").get("data")
  		});
 	},

	render: function(){
		var $el = $(this.el);

		$el.empty();

		var htmlStructure = FileBrowser.templates.structure(this.model.defaults.structure);
		$el.append($(htmlStructure));

		this.updateButtons();
		this.updateButtonsHeader();

		this.foldersTreeView.setElement($el.find("#fileBrowserFolders .body"));
		this.fileListView.setElement($el.find("#fileBrowserFiles .body"));

		this.foldersTreeView.render();

		this.updateFolderBrowserHeader();
		this.updateFileBrowserHeader();

		//disable all buttons on start
		$("button.btn.btn-block").each(function(){
			$(this).attr("disabled", "disabled");
		});

		//handle empty folders
		$("* > .folders:empty").each(function(){
			$(this).parent().addClass("empty");
		});

		//remove padding of first folder
		$el.find("#fileBrowserFolders").children().each(function(){
			$(this).addClass("first");
		});

		//sanitize folder names
		$el.find(".element").each(function(){
			var $this = $(this);

			if($this.height() > 20){
				$this.find('.name').css("overflow", "hidden");
			}
		});
	},

	updateButtonsHeader: function(){
		var $el = $(this.el),
			$buttonsContainer = $el.find($("#fileBrowserButtons"));

		$buttonsContainer.find($(".header")).detach();

		var lastClick = this.model.getLastClick(),
			folderClicked = this.model.getFolderClicked(),
			fileClicked = this.model.getFileClicked();

		var obj = {};

		if(lastClick == "file" && fileClicked != undefined){
			obj["folderName"] = undefined;
			obj["fileName"] = $(fileClicked.find('.name')[0]).text();
		} else if(lastClick == "folder" && folderClicked != undefined){
			obj["folderName"] = $(folderClicked.find('.name')[0]).text();
			obj["fileName"] = undefined;
		}	
		$buttonsContainer.prepend($(FileBrowser.templates.buttonsHeader(obj)));
	},

	updateFolderBrowserHeader: function(){
		var $el = $(this.el),
			$folderBrowserContainer = $el.find($("#fileBrowserFolders"));

		$folderBrowserContainer.find($(".header")).detach();

		var folderClicked = this.model.getFolderClicked();

		var obj = {
			folderBreadcrumb: folderClicked != undefined ? folderClicked.attr("path").split("/").slice(1).join(" > ") : undefined
		};

		$folderBrowserContainer.prepend($(FileBrowser.templates.folderBrowserHeader(obj)));
	},

	updateFileBrowserHeader: function(){
		var $el = $(this.el),
			$folderBrowserContainer = $el.find($("#fileBrowserFiles"));

		$folderBrowserContainer.find($(".header")).detach();

		var folderClicked = this.model.getFolderClicked();

		var obj = {
			folderName : folderClicked != undefined? folderClicked.find("> .element .name").text() : undefined
		}

		$folderBrowserContainer.prepend($(FileBrowser.templates.fileBrowserHeader(obj)));
	},

	updateButtons: function(){
		var $el = $(this.el),
			$buttonsContainer = $el.find($("#fileBrowserButtons .body"));

		$buttonsContainer.empty();
		var lastClick = this.model.getLastClick(),
			folderClicked = this.model.getFolderClicked(),
			fileClicked = this.model.getFileClicked();

		if(lastClick == "file"){
			$buttonsContainer.append($(FileBrowser.templates.buttons(this.model.defaults.fileButtons)));
		} else if(lastClick == "folder"){
			$buttonsContainer.append($(FileBrowser.templates.buttons(this.model.defaults.folderButtons)));
		}	
	},

	checkButtonsEnabled: function(){
		//disable all buttons on start
		$("button.btn.btn-block[disabled=disabled]").each(function(){
			$(this).removeAttr("disabled");
		});
	}
});


var FileBrowserFolderTreeView = Backbone.View.extend({

	events: {
		"click .folder .icon" 	: "expandFolder",
		"click .folder .name" 	: "clickFolder"
	},

	render: function(){
		$(this.el).append(FileBrowser.templates.folders(this.options.data));
		$(this.el).find(".folders").hide();

		return this;
	},

	expandFolder: function(event){
		var $target = $(event.currentTarget).parent().parent();
		this.model.set("clicked", $target.attr("id"));
		this.model.set("clickedFolder",$target);

		if($target.hasClass("open")){
			$target.removeClass("open").find("> .folders").hide();
		} else {
			$target.addClass("open").find("> .folders").show();
		}

		$(".folder.selected").removeClass("selected");
		$target.addClass("selected");

		event.stopPropagation();
	},

	clickFolder: function(event){
		var $target = $(event.currentTarget).parent().parent();
		this.model.set("clicked", $target.attr("id"));
		this.model.set("clickedFolder",$target);

		$(".folder.selected").removeClass("selected");
		$target.addClass("selected");

		event.stopPropagation();
	}
});


var FileBrowserFileListView = Backbone.View.extend({
	events: {
		"click div.file" : "clickFile",
		"dblclick div.file" : "doubleClickFile"
	},

	initialize: function(){
		this.model.on("change:fileData", this.updateFileList, this);
	},	

	render: function(){
		var data = this.model.get("fileData");
		var html = FileBrowser.templates.files(data);

		$(this.el).empty().append(html);

	},

	clickFile: function(event){
		var $target = $(event.currentTarget);
		this.model.set("clicked", $target.attr("id"));
		this.model.set("clickedFile", $target);

		$(".file.selected").removeClass("selected");
		$target.addClass("selected");
	},

	doubleClickFile: function(event){
		var path = $(event.currentTarget).attr("path");
		this.model.get("openFileHandler")(path, "run");
	},

	updateFileList: function(){
		this.render();

	}
});





