this.NewFileBrowser = {};


NewFileBrowser.$container = null;
NewFileBrowser.fileBrowserModel = null;
NewFileBrowser.fileBrowserView = null;


NewFileBrowser.setContainer =  function($container){
	this.$container = $container;
}




NewFileBrowser.init = function(){

};

NewFileBrowser.update = function(){
	this.redraw();
};

NewFileBrowser.redraw = function(){

	this.fileBrowserModel = new FileBrowserModel({
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
				{id: "separator"},
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
				{id: "separator"},
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
		fileClicked: undefined,

		data: undefined,
		fileData: undefined
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

		});

		foldersTreeModel.on("change:clicked", this.updateClicked, this);
		foldersTreeModel.on("change:clickedFolder", this.updateFolderClicked, this);
		this.on("change:clickedFolder", this.updateFileList, this);
		

		fileListModel.on("change:clicked", this.updateFileClicked);

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

	updateFileClicked: function(file){
		this.set("fileClicked", folder);
	},

	getFolderClicked: function(folder){
		return this.get("folder");
	},

	getFileClicked: function(file){
		return this.get("file");
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
		fileData: undefined	
	},

	initialize: function(){
		
	}

});


var FileBrowserView = Backbone.View.extend({


   	initialize: function() {
    	this.initializeOptions();
    	this.configureListeners();
  	},

  	configureListeners: function() {
    	this.model.on("change", this.updateButtons, this);
    	
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

		var htmlStructure = NewFileBrowser.templates.structure(this.model.defaults.structure);
		$el.append($(htmlStructure));

		this.updateButtons();

		this.foldersTreeView.setElement($el.find("#fileBrowserFolders"));
		this.fileListView.setElement($el.find("#fileBrowserFiles"));

		this.foldersTreeView.render();

		//handle empty folders
		$("* > .folders:empty").each(function(){
			$(this).parent().addClass("empty");
		});

		//remove padding of first folder
		$el.find("#fileBrowserFolders").children().each(function(){
			$(this).addClass("first");
		});
	},

	updateButtons: function(){
		var $el = $(this.el),
			$buttonsContainer = $el.find($("#fileBrowserButtons"));

		$buttonsContainer.empty();

		var fileClicked = this.model.getFileClicked(),
			folderClicked = this.model.getFolderClicked();

		if(fileClicked != undefined){
			var buttons = NewFileBrowser.templates.buttons(this.model.defaults.fileButtons);
			$buttonsContainer.append($(buttons));
		} else {
			var buttons = NewFileBrowser.templates.buttons(this.model.defaults.folderButtons);
			$buttonsContainer.append($(buttons));
		}	
	},

	updateFileList: function(){

	}
});


var FileBrowserFolderTreeView = Backbone.View.extend({

	events: {
		"click .folder .icon" : "clickFolder",
		"click .folder .name" : "clickFolder"
	},

	render: function(){
		$(this.el).append(NewFileBrowser.templates.folders(this.options.data));
		$(this.el).find(".folders").hide();

		return this;
	},

	clickFolder: function(event){
		var $target = $(event.currentTarget).parent();
		this.model.set("clicked", true);
		this.model.set("clickedFolder",$target);

		if($target.hasClass("open")){
			$target.removeClass("open").find("> .folders").hide();
		} else {
			$target.addClass("open").find("> .folders").show();
		}

		$(".folder.selected").removeClass("selected");
		$target.addClass("selected");

		event.stopPropagation();
	}
});


var FileBrowserFileListView = Backbone.View.extend({
	events: {
		"click div.file" : "clickFile"
	},

	initialize: function(){
		this.model.on("change:fileData", this.updateFileList, this);
	},	

	render: function(){
		var data = this.model.get("fileData");
		var html = NewFileBrowser.templates.files(data);

		$(this.el).empty().append(html);

	},

	clickFile: function(){
		this.model.set("clicked", true);
	},

	updateFileList: function(){
		this.model.set("clicked", false);
		this.render();

	}
});





