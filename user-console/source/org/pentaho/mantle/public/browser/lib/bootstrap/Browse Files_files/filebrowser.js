/*
 * ******************************************************************************
 * Pentaho
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 */

this.FileBrowser = {}

FileBrowser.getAuthenticatedURL = "/pentaho/api/mantle/isAuthenticated";

FileBrowser.container = null;
FileBrowser.init = function($htmlObject){
	this.container = $htmlObject;
	this.drawTree("/");
};

FileBrowser.drawTree = function(path){
	this.container.empty().addClass("fileBrowser");
	FileBrowser.open(path, this.container);
};

FileBrowser.drawChildren = function(children, $container){
	var $level = $("<div/>").addClass("span2 fileBrowser level").appendTo(this.container);
	var level = this.getLevelCounter($level)
	$level.attr("level",level);

	if(level == 1){
		$level.addClass("first");
	}

	if(level == $container.children().length){
		$(".last").each(function(){$(this).removeClass("last")});
		$level.addClass("last");
	}

	children.each(function(){
		var $item, $image, $text;

		var $this = $(this),
			name = $this.find("name"),
			path = $this.find("path"),
			folder = $this.find("folder"),
			nameText = (name != undefined ? name.text() : ""),
			pathText = (path != undefined ? path.text() : ""),
			isFolder = (folder != undefined ? folder.text() == "true" : false);

		var correctName = (nameText == "" ? pathText : nameText);
		//extract name and extension

		var lastIndex = correctName.lastIndexOf('.'),
			nameNoExtension = correctName.substr(0,lastIndex),
			extension = correctName.substr(lastIndex+1, correctName.length);

		$item = $("<div/>")
				.addClass("item")
				.attr("path",pathText)
				.attr("folder",isFolder)
				.appendTo($level);

		$image = $("<div/>").addClass("image").appendTo($item);
		$text = $("<div/>");
		if(isFolder){
			$item.addClass("folder")
				.click(function(){
					var $clicked = $(this);

					$item.parent().find(".selected").each(function(){
						var $this = $(this);
						FileBrowser.close($this.attr("path"),$this.parent());
						if($this.attr("path") == $clicked.attr("path")){
							FileBrowser.open($this.attr("path"), $this.parent().parent());
							return false;
						}
						$this.removeClass("selected");
					});
					if(!$clicked.hasClass("selected")){
						$clicked.addClass("selected");
						FileBrowser.open($clicked.attr("path"),$container);
					}
				});
			$text.text(correctName);
		} else {
			$text.text(nameNoExtension);
			$item.addClass("file")
				.click(function(){
					openRepositoryFile(pathText, 'run');
				});
			$image.addClass(extension);

		}	
		$text.appendTo($item); 

		$image.height($text.height() + "px");

	});
};



FileBrowser.open = function(path, $container){
	console.log("Opening " + path);

	var doc = this.getFileTree(path);
	var children = doc.find("children file");

	if(children.length > 0){
		$container.width($container.width()+175 + "px");
		$container.parent().scrollLeft(9999999999);
		this.drawChildren(children,$container);
	}
};

FileBrowser.close = function (path, $obj){
	var level = $obj.attr("level");

	$obj.parent().children().each(function(){
		var $this = $(this);

		if($this.attr("level") > level){
			$this.parent().width($this.parent().width()-175 + "px");
			$this.detach();
		}
	});

};

FileBrowser.getLevelCounter = function($obj){
	var $parent = $obj.parent();

	return $parent.children().length;
};

FileBrowser.isAuthenticated = function(){
	var returnValue = false;

	$.ajax({
		async: false,
		type: "GET",
		url: FileBrowser.getAuthenticatedURL,
		success: function(response){
			console.log("Permissions obtained");
			returnValue = true;
		},
		error: function(){
			console.log("Error getting administrator permissions");
			returnValue = false;
		}
	});
	
	return returnValue;
};

FileBrowser.getFileTree = function(path){
	var tree = null;

	if(this.isAuthenticated()){
		console.log("There are permissions!");

		var url = this.getFileTreeRequest(path == null ? "/" : path);

		$.ajax({
			async: false,
		 	type: "GET",
		 	url: url,
		 	success: function(response){
				console.log("Tree obtained");
				tree = response;
			},
			error: function(){
				console.log("Error getting tree");
			}
		});
	} else {
		console.log("No tree view permissions avaialble!");
	}

	return $(tree.getElementsByTagName("repositoryFileTreeDto")[0]);
};

FileBrowser.getFileTreeRequest = function(path){

	var p = path.replace(/\//g, ':');

	return "/pentaho/api/repo/files/"+p+"/children?depth=1&filter=*&showHidden=false";
};