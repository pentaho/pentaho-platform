/*
 * ******************************************************************************
 * Pentaho
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 */

pen.define([
	"js/browser.fileButtons",
	"js/browser.folderButtons",
    "js/browser.trashButtons",
    "js/browser.trashItemButtons",
  "common-ui/bootstrap",
  "common-ui/handlebars",
  "common-ui/jquery-i18n",
  "common-ui/jquery",
  "js/browser.templates"
], function(FileButtons, FolderButtons, TrashButtons, TrashItemButtons) {

	var fileButtons = new FileButtons();
	var folderButtons = new FolderButtons();
    var trashButtons = new TrashButtons();
    var trashItemButtons = new TrashItemButtons();

	this.FileBrowser = {};


	FileBrowser.$container = null;
	FileBrowser.fileBrowserModel = null;
	FileBrowser.fileBrowserView = null;
	FileBrowser.openFileHandler = undefined;
	FileBrowser.showHiddenFiles = false;
	FileBrowser.showDescriptions = false;

	FileBrowser.setShowHiddenFiles = function(value){
		this.showHiddenFiles = value;
	};

	FileBrowser.setShowDescriptions = function(value){
		this.showDescriptions = value;
	};

	FileBrowser.updateShowDescriptions = function(value){
		this.fileBrowserModel.set("showDescriptions", value);
	};

	FileBrowser.setContainer =  function($container){
		this.$container = $container;
	};

	FileBrowser.setOpenFileHandler = function(handler){
		this.openFileHandler = handler;
	};

	FileBrowser.init = function(){

	};

	FileBrowser.update = function(initialPath){

		this.redraw(initialPath);
	};

	FileBrowser.updateData = function(){
		if(this.fileBrowserModel != null && this.fileBrowserModel.get('fileListModel') != null){
			this.fileBrowserModel.get('fileListModel').updateData();
		}
	};

	FileBrowser.redraw = function(initialPath){
		var myself = this;

		pen.require(["common-ui/util/PentahoSpinner"],function(spin){
			myself.fileBrowserModel = new FileBrowserModel({
				spinConfig: spin,
				openFileHandler: myself.openFileHandler,
				showHiddenFiles: myself.showHiddenFiles,
				showDescriptions: myself.showDescriptions,
				startFolder: initialPath
			});
			myself.FileBrowserView = new FileBrowserView({
				model: myself.fileBrowserModel,
				el: myself.$container

			});
		});

		//this.FileBrowserView.render();
	};

	FileBrowser.openFolder = function(path){
		var myself = this;

		myself.fileBrowserModel.set("startFolder", path);
	};

	


	var FileBrowserModel = Backbone.Model.extend({
		defaults: {
			showHiddenFilesURL: "/pentaho/api/user-settings/MANTLE_SHOW_HIDDEN_FILES",

			fileButtons : fileButtons,
			folderButtons : folderButtons,
      trashButtons : trashButtons,
      trashItemButtons : trashItemButtons,

			foldersTreeModel: undefined,
			fileListModel: undefined,

			clickedFolder : undefined,
			clickedFile: undefined,

			lastClick: "folder", 

			data: undefined,
			fileData: undefined,

			openFileHandler: undefined,

			showHiddenFiles: false,
			showDescriptions: false,

			spinConfig: undefined,

			startFolder: "/"
		},

		initialize: function(){
			var myself = this,
				foldersTreeModel = myself.get("foldersTreeModel"),
				fileListModel = myself.get("fileListModel");

			//handle data
			var foldersObj = {}
			
			//get spinner and give a new to each browser
	      	var config = myself.get("spinConfig").getLargeConfig();

	      	var spinner1 = new Spinner(config),
	      		spinner2 = new Spinner(config);

	      	//create two models
			foldersTreeModel = new FileBrowserFolderTreeModel({	
				spinner: spinner1,
				showHiddenFiles: myself.get("showHiddenFiles"),
				showDescriptions: myself.get("showDescriptions"),
				startFolder: myself.get("startFolder")
			});
			fileListModel = new FileBrowserFileListModel({
				spinner: spinner2,
				openFileHandler: myself.get("openFileHandler"),
				showHiddenFiles: myself.get("showHiddenFiles"),
				showDescriptions: myself.get("showDescriptions")
			});

			//assign backbone events
			foldersTreeModel.on("change:clicked", myself.updateClicked, myself);
			foldersTreeModel.on("change:clickedFolder", myself.updateFolderClicked, myself);
			myself.on("change:clickedFolder", myself.updateFileList, myself);

			fileListModel.on("change:clickedFile", myself.updateFileClicked, myself);
			
			//handlers for buttons header update
			foldersTreeModel.on("change:clicked", myself.updateFolderLastClick, myself);
			fileListModel.on("change:clicked", myself.updateFileLastClick, myself);

			myself.set("foldersTreeModel", foldersTreeModel);
			myself.set("fileListModel", fileListModel);

			myself.on("change:showDescriptions", myself.updateDescriptions, myself);

			window.top.mantle_addHandler("FavoritesChangedEvent", $.proxy(myself.onFavoritesChanged, myself));

			myself.on("change:startFolder" , myself.updateStartFolder, myself);
		},

		updateStartFolder: function(){
			var myself = this;

			myself.get("foldersTreeModel").set("startFolder", myself.get("startFolder"));
		},

		onFavoritesChanged: function(){
			// BISERVER-9127	- Reselect current file
			var that = this;
			setTimeout(function(){
				that.get('fileListModel').trigger("change:clickedFile");
			}, 100);
		},

		updateClicked: function(){
			this.set("clicked", true);
		},

		updateFolderClicked: function(){
            var clickedFolder=this.get("foldersTreeModel").get("clickedFolder");
            if(clickedFolder.obj.attr("path")===".trash"){
                this.updateTrashLastClick();
            }
            this.set("clickedFolder",clickedFolder);
		},

		updateFileClicked: function(){

            var clickedFile=this.get("fileListModel").get("clickedFile");
            if(this.get("clickedFolder").obj.attr("path")===".trash"){
                this.updateTrashItemLastClick();
            }
            else{
      // BISERVER-9127 - Provide the selected path to the FileButtons object
			fileButtons.onFileSelect(clickedFile.obj.attr("path"));
            }

            this.set("clickedFile",clickedFile);

		},

		updateFolderLastClick: function(){
			this.set("lastClick", "folder");
		},

		updateFileLastClick: function(){
			this.set("lastClick", "file");
		},

        updateTrashLastClick: function(){
            this.set("lastClick", "trash");
        },

        updateTrashItemLastClick: function(){
            this.set("lastClick", "trashItem");
        },

		getLastClick: function(){
			return this.get("lastClick");
		},

		getFolderClicked: function(){
			return this.get("clickedFolder") == null || this.get("clickedFolder") == undefined ? null : this.get("clickedFolder").obj;
		}, 

		getFileClicked: function(){
			return this.get("clickedFile") == null || this.get("clickedFile") == undefined ? null : this.get("clickedFile").obj ; // [BISERVER-9128] - wrap in jquery object
		},

		updateFileList: function(){
			var myself = this;
			//trigger file list update
			myself.get("fileListModel").set("path", myself.get("clickedFolder").obj.attr("path"));

		},

		updateDescriptions: function(){
			var myself = this;

			myself.get("fileListModel").set("showDescriptions", myself.get("showDescriptions"));
			myself.get("foldersTreeModel").set("showDescriptions", myself.get("showDescriptions"));
		}
	});


	var FileBrowserFolderTreeModel = Backbone.Model.extend({
		defaults: {
			clicked: false,
			clickedFolder: undefined,
			
			data: undefined,
			updateData: false,
			
			runSpinner: false,
			spinner: undefined,

			showHiddenFiles: false,
			showDescriptions: false,

			startFolder: "/"
		},

		initialize: function(){
			var myself = this;

	    	myself.on("change:updateData", myself.updateData, myself);
		},

		updateData: function(){
			var myself = this;

			myself.set("runSpinner",true);

			myself.fetchData("/", function(response){
                    var trash={
                        "file": {
                            "trash": "trash",
                            "createdDate": "1365427106132",
                            "fileSize": "0",
                            "folder": "true",
                            "hidden": "false",
                            "id:": "Trash",
                            "locale": "en",
                            "locked": "false",
                            "name": "Trash",
                            "ownerType": "-1",
                            "path": ".trash",
                            "title": "Trash",
                            "versioned": "false"
                        }
                    };
                    //Add trash to data model
                    response.children.push(trash);

				myself.set("data", response);
			});
		},

		fetchData: function(path, callback){
			var myself = this,
				tree = null;

			var url = this.getFileTreeRequest(path == null ? ":" : path.replace(/\//g, ":"));

			$.ajax({
				async: true,
				cache: false, // prevent IE from caching the request
				dataType: "json",
			 	url: url,
			 	success: function(response){
					if(callback != undefined){
						callback(response);
					}
				},
				error: function(){
				},
				beforeSend: function() {
          			myself.set("runSpinner",true);
        		}
			});
		},

		getFileTreeRequest: function(path){
			return "/pentaho/api/repo/files/"+path+"/children?depth=-1&showHidden="+this.get("showHiddenFiles")+"&filter=*|FOLDERS";
		}

	});

	var FileBrowserFileListModel = Backbone.Model.extend({
		defaults: {
			clicked: false,
			clickedFile: undefined,
			
			data: undefined,
			path: "/",
			
			runSpinner: false,
			spinner: undefined,

			openFileHander: undefined,

			showHiddenFiles: false,
      showDescriptions: false,
      deletedFiles: ""
		},

		initialize: function(){
			var myself = this;

	    	myself.on("change:path", myself.updateData, myself);
		},

		updateData: function(){
			var myself = this;

			myself.set("runSpinner",true);

			myself.fetchData(myself.get("path"), function(response){

                //If we have trash data we reformat it to match the handlebar templates
                if(myself.get("path")===".trash"){
                  var newResp = {
                    children: []
                  }
                    if(response && response.repositoryFileDto){
                        myself.deletedFiles="";
                        for (var i=0;i<response.repositoryFileDto.length;i++){
                            var file = {
                                file: Object
                            }

                            file.file=response.repositoryFileDto[i];
                            file.file.trash="true";
                            if(file.file.id){
                                if(!myself.deletedFiles){
                                    myself.deletedFiles=file.file.id+",";
                                }
                                else{
                                    myself.deletedFiles=myself.deletedFiles+file.file.id+",";
                                }
                            }
                            newResp.children.push(file);
                        }
                    }
                    myself.set("data", newResp);
                }
                else{
				myself.set("data", response);
                }
			});
		},

		fetchData: function(path, callback){
			var myself = this,
				url = this.getFileListRequest(path == null ? ":" : path.replace(/\//g, ":"));

			$.ajax({
				async: true,
				cache: false, // prevent IE from caching the request
				dataType: "json",
			 	url: url,
			 	success: function(response){
			 		if(callback != undefined){
			 			callback(response);
			 		}
				},
				error: function(){
				},
				beforeSend: function() {
          			myself.set("runSpinner",true);
        		}
			});
		},

		getFileListRequest: function(path){
            var request;
            if(path === ".trash"){
              request="/pentaho/api/repo/files/deleted";
		}
            else {
              request="/pentaho/api/repo/files/"+path+"/children?depth=1&showHidden="+this.get("showHiddenFiles")+"&filter=*|FILES";
            }
            return request;
        }
	});

	var FileBrowserView = Backbone.View.extend({
		attributes: {
			buttonsEnabled: false
		},

	   	initialize: function() {
	   		this.initializeLayout();
	    	this.initializeOptions();
	    	this.configureListeners();
	    	this.render();
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

	  	initializeLayout: function(){
	  		var myself = this;

			myself.$el.empty();

			//require structure template
			pen.require(["js/browser.templates"],function(templates){
				myself.$el.append($(templates.structure({})));
			});
	  	},

	  	initializeOptions: function() {
	  		var myself = this;

			foldersTreeView = undefined;
			fileListView = undefined;

	  		this.foldersTreeView = new FileBrowserFolderTreeView({
	  			model: myself.model.get("foldersTreeModel"),
	  			data: myself.model.get("foldersTreeModel").get("data"),
	  			el: myself.$el.find("#fileBrowserFolders .body")
	  		});

	  		this.fileListView = new FileBrowserFileListView({
	  			model: myself.model.get("fileListModel"),
	  			data: myself.model.get("fileListModel").get("data"),
	  			el: myself.$el.find("#fileBrowserFiles .body")
	  		});
	 	},

		render: function(){
			var myself = this;

			myself.updateButtons();
			myself.updateButtonsHeader();

			//this.foldersTreeView.render();

			myself.updateFolderBrowserHeader();
			myself.updateFileBrowserHeader();

			//disable all buttons on start
			$("button.btn.btn-block").each(function(){
				$(this).attr("disabled", "disabled");
			});
		},

		updateButtonsHeader: function(){
			var myself = this,
				$buttonsContainer = myself.$el.find($("#fileBrowserButtons"));

			$buttonsContainer.find($(".header")).detach();

			var lastClick = myself.model.getLastClick(),
				folderClicked = myself.model.getFolderClicked(),
				fileClicked = myself.model.getFileClicked();

			var obj = {};

			if(lastClick == "file" && fileClicked != undefined){
				obj.folderName = undefined;
				obj.fileName = $(fileClicked.find('.title')[0]).text();
			} else if(lastClick == "folder" && folderClicked != undefined){
				obj.folderName = $(folderClicked.find('.title')[0]).text();
				obj.fileName = undefined;
			}	

			//require buttons header template
			pen.require(["js/browser.templates"],function(templates){
				$buttonsContainer.prepend($(templates.buttonsHeader(obj)));
			});
			
		},

		updateFolderBrowserHeader: function(){
			var $el = $(this.el),
				$folderBrowserContainer = $el.find($("#fileBrowserFolders"));

			$folderBrowserContainer.find($(".header")).detach();

			var folderClicked = this.model.getFolderClicked();

			var obj = {
				folderBreadcrumb: folderClicked != undefined ? folderClicked.attr("path").split("/").slice(1).join(" > ") : undefined
			};

			//require folders header template
			pen.require(["js/browser.templates"],function(templates){
				$folderBrowserContainer.prepend($(templates.folderBrowserHeader(obj)));
			});
			
		},

		updateFileBrowserHeader: function(){
			var $el = $(this.el),
				$folderBrowserContainer = $el.find($("#fileBrowserFiles"));

			$folderBrowserContainer.find($(".header")).detach();

			var folderClicked = this.model.getFolderClicked();

			var obj = {
				folderName : folderClicked != undefined? folderClicked.find("> .element .title").text() : undefined
			}

			//require files header template
			pen.require(["js/browser.templates"],function(templates){
				$folderBrowserContainer.prepend($(templates.fileBrowserHeader(obj)));
			});
		},

		updateButtons: function(){
			var $el = $(this.el),
				$buttonsContainer = $el.find($("#fileBrowserButtons .body"));

			$buttonsContainer.empty();
			var lastClick = this.model.getLastClick(),
				folderClicked = this.model.getFolderClicked(),
				fileClicked = this.model.getFileClicked();

			var buttonsType;	

			if(lastClick == "file"){
				buttonsType = this.model.defaults.fileButtons;
			} else if(lastClick == "folder"){
				buttonsType = this.model.defaults.folderButtons;
			}	

      else if (lastClick == "trash"){
        buttonsType = this.model.defaults.trashButtons;
      }


      else if (lastClick == "trashItem"){
         buttonsType = this.model.defaults.trashItemButtons;
      }

			var model = this.model; // trap model

			//require buttons template
			pen.require(["js/browser.templates"],function(templates){
				$buttonsContainer.append($(templates.buttons(buttonsType)));

				// add onClick handler to each button
				$(buttonsType.buttons).each(function(idx, fb){
					$('#'+fb.id).on("click", { model:model, handler:fb.handler }, function(event){
						var path = null;
						var title = null;
            var fileList = null;

						if(model.getLastClick() == "file"){
							path = $(model.getFileClicked()[0]).attr("path");
							title = $(model.getFileClicked()[0]).children('.title').text();
						} else if(model.getLastClick() == "folder"){
							path = $(model.getFolderClicked()[0]).attr("path");
							title = $(model.getFolderClicked()[0]).children('.title').text();
						}
            else if(model.getLastClick() == "trash"){
                fileList = model.attributes.fileListModel.deletedFiles;
            }
            else if(model.getLastClick() == "trashItem"){
                fileList = $(model.getFileClicked()[0]).attr("id")+",";
            }

						if((path != null) && event.data.handler){
							event.data.handler(path, title);
							event.stopPropagation();
						}
            else{
                event.data.handler(fileList);
                event.stopPropagation();
            }
					});
				});

			});
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
			"click .folder .expandCollapse" : "expandFolder",
			
			"click .folder .icon" 			: "clickFolder",
			"dblclick .folder .icon"		: "expandFolder",
			
			"click .folder .title" 			: "clickFolder",
			"dblclick .folder .title"		: "expandFolder",
		},

		initialize: function(){
			var myself = this,
				data = myself.model.get("data"),
				spinner = myself.model.get("spinner");

			myself.model.on("change:runSpinner", myself.manageSpinner, myself);
			myself.model.on("change:data", myself.render, myself);

			myself.model.on("change:showDescriptions", this.updateDescriptions, this);

			myself.model.on("change:startFolder", this.setFolder, this);

			if(data == undefined){ //update data
				//start spinner
				myself.$el.html(spinner.spin());
				myself.model.set("updateData", true);
			}
		},

		render: function(){
			var myself = this,
				data = myself.model.get("data");


			//require folders template
			pen.require(["js/browser.templates"],function(templates){
				//stop spinner
		        myself.model.set("runSpinner", false);

		        //append content
				myself.$el.append(templates.folders(data));


				//fix folder widths
				$(".folder").each(function(){
					$(this).addClass("selected");
				});

				$(".element").each(function(){
					var $this = $(this);
					while($this.height() > 20){
						$this.width($this.width() + 20);
					}
				});

				$(".folder").each(function(){
					$(this).removeClass("selected");
				});

				//close all children folders
				myself.$el.find(".folders").hide();


				//handle empty folders
				$(".folders").each(function(){
					if($(this).children().length == 0){
						$(this).parent().addClass("empty");
					}
				});

				//remove padding of first folder
				myself.$el.children().each(function(){
					$(this).addClass("first");
				});

				//set initial folder start
				myself.setFolder();
			});


		},

		expandFolder: function(event){
			var $target = $(event.currentTarget).parent().parent();
			if($target.hasClass("open")){
				$target.removeClass("open").find("> .folders").hide();
			} else {
				$target.addClass("open").find("> .folders").show();
			}
			event.stopPropagation();
		},

		clickFolder: function(event){
			var $target = $(event.currentTarget).parent().parent();
			//BISERVER-9259 - added time parameter to force change event
			this.model.set("clicked", {
				obj: $target.attr("id"),
				time: (new Date()).getTime()
			});
			this.model.set("clickedFolder", {
				obj: $target,
				time: (new Date()).getTime()
			});

			$(".folder.selected").removeClass("selected");
			$target.addClass("selected");

			event.stopPropagation();
		},

		manageSpinner: function() {
			var myself = this,
				runSpinner = this.model.get("runSpinner"),
				spinner = this.model.get("spinner");

			if(runSpinner){
				if(spinner != undefined){
					myself.$el.html(spinner.spin().el);
				} else {
			    	
				}
			} else {
				myself.model.get("spinner").stop();
			}
	    },

	    updateDescriptions: function(){
	    	var $folders = $(".folder"),
	    		showDescriptions = this.model.get("showDescriptions");


	    	if(showDescriptions){
	    		$folders.each(function(){
	    			var $this = $(this),
	    				title = $this.attr("title"),
	    				title2 = $this.attr("title2");

	    			$this.attr("title", title2).attr("title2", title);
	    		});
	    	} else {
	    		$folders.each(function(){
	    			var $this = $(this),
	    				title = $this.attr("title"),
	    				title2 = $this.attr("title2");

	    			$this.attr("title",title2).attr("title2", title);
	    		});
	    	}
	    },

	    setFolder: function(){
			var $folder = $("[path='"+this.model.get("startFolder")+"']"),
				$parentFolder = $folder.parent(".folders");

			while(!$parentFolder.hasClass("body") && $parentFolder.length > 0){
				$parentFolder.show();
				$parentFolder.parent().addClass("open");
				$parentFolder = $parentFolder.parent().parent();
			}

			$folder.find("> .element .title").click();
			$folder.addClass("open");
			$folder.find("> .folders").show();
	    }

	});


	var FileBrowserFileListView = Backbone.View.extend({
		events: {
			"click div.file" : "clickFile",
			"dblclick div.file" : "doubleClickFile"
		},

		initialize: function(){
			var myself = this,
				data = myself.model.get("data");
			this.model.on("change:data", this.updateFileList, this);
			myself.model.on("change:runSpinner", myself.manageSpinner, myself);

			myself.model.on("change:showDescriptions", myself.updateDescriptions, this);
		},	

		render: function(){
			var myself = this,
				data = myself.model.get("data");

			//require file list template
			pen.require(["js/browser.templates"],function(templates){
				myself.$el.empty().append(templates.files(data));

				$(".file").each(function(){
					var $this = $(this);
					while($this.height() > 20){
						$this.width($this.width() + 20);
					}
				});
			});

	      	setTimeout(function() {
		        myself.model.set("runSpinner", false);
	      	}, 100);
		},

		clickFile: function(event){
			var $target = $(event.currentTarget).eq(0);
			//BISERVER-9259 - added time parameter to force change event
			this.model.set("clicked", {
				obj: $target,
				time: (new Date()).getTime()
			});

			this.model.set("clickedFile", {
				obj: $target,
				time: (new Date()).getTime()
			}); 
 
			$(".file.selected").removeClass("selected");
			$target.addClass("selected");
		},

		doubleClickFile: function(event){
			var path = $(event.currentTarget).attr("path");
			this.model.get("openFileHandler")(path, "run");
		},

		updateFileList: function(){
			var myself = this;
			this.render();

	      	setTimeout(function() {
	      		myself.model.set("runSpinner", false);
	      	}, 100);
	    },

		manageSpinner: function() {
			var myself = this,
				runSpinner = this.model.get("runSpinner"),
				spinner = this.model.get("spinner");

			if(runSpinner){
				if(spinner != undefined){
					myself.$el.html(spinner.spin().el);
				} else {
			    	
				}
			} else {
				myself.model.get("spinner").stop();
			}
	    },

	    updateDescriptions: function(){
	    	var $files = $(".file"),
	    		showDescriptions = this.model.get("showDescriptions");

	    	if(showDescriptions){
	    		$files.each(function(){
	    			var $this = $(this),
	    				title = $this.attr("title"),
	    				title2 = $this.attr("title2");

	    			$this.attr("title", title2).attr("title2", title);
	    		});
	    	} else {
	    		$files.each(function(){
	    			var $this = $(this),
	    				title = $this.attr("title"),
	    				title2 = $this.attr("title2");

	    			$this.attr("title",title2).attr("title2", title);
	    		});
	    	}
	    }

	});


	return {
		setContainer: FileBrowser.setContainer,
		setOpenFileHandler: FileBrowser.setOpenFileHandler,
		setShowHiddenFiles: FileBrowser.setShowHiddenFiles,
		setShowDescriptions: FileBrowser.setShowDescriptions,
		updateShowDescriptions: FileBrowser.updateShowDescriptions,
		update: FileBrowser.update,
		updateData: FileBrowser.updateData,
		redraw: FileBrowser.redraw,
		templates: FileBrowser.templates,
		openFolder: FileBrowser.openFolder
	}
});



