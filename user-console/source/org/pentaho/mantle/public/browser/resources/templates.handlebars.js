/*
 * ******************************************************************************
 * Pentaho
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 */

this.FileBrowser.templates = {
	structure: Handlebars.compile(
		"<div id='fileBrowserFolders' class='span4 well fileBrowserColumn'>" +
			"<div class='body'></div>" + 
		"</div>" +
		"<div id='fileBrowserFiles' class='span4 well fileBrowserColumn'>" + 
			"<div class='body'></div>" + 
		"</div>" +
		"<div id='fileBrowserButtons' class='span4 well fileBrowserColumn'>" +
			"<div class='body'></div>" + 
		"</div>"),
	folderBrowserHeader: Handlebars.compile(
		"{{#if folderBreadcrumb}}" + 
			"<div id='foldersHeader' class='header'>Browsing: {{folderBreadcrumb}}</div>" +
		"{{else}}" +
			"<div id='foldersHeader' class='header'>Browsing: Root</div>" +
		"{{/if}}"),
	fileBrowserHeader: Handlebars.compile(
		"{{#if folderName}}" +
			"<div id='filesHeader' class='header'>{{folderName}} Files</div>" +
		"{{else}}" + 
			"<div id='filesHeader' class='header'>Root Files</div>" +
		"{{/if}}"),
	buttonsHeader: Handlebars.compile(
		"{{#if folderName}}" +
			"<div id='buttonsHeader' class='header'>Folder Actions for {{folderName}}</div>" +
		"{{else}}" + 
			"{{#if fileName}}" +
				"<div id='buttonsHeader' class='header'>File Actions for {{fileName}}</div>" +
			"{{else}}" +
				"<div id='buttonsHeader' class='header'>Folder Actions for Root</div>" +
			"{{/if}}" +

		"{{/if}}"),
	folderButtonsHeader: Handlebars.compile(
		"<div id='buttonsHeader' class=''>Folder Actions for {{folderName}}</div>"),
	fileButtonsHeader: Handlebars.compile(
		"<div id='buttonsHeader' class=''>File Actions for {{fileName}}</div>"),
	button: Handlebars.compile(
		"{{#if predicate}}" +
	    	"<div class='separator'></div>" +
		"{{else}}" +
			"<button id='{{id}}' class='btn btn-block'>{{text}}</button>" +
	    "{{/if}}"),
	buttons: Handlebars.compile("{{#each buttons}}{{button}}{{/each}}"),
	folderText: "<div id='{{id}}' class='folder' path='{{path}}'>" + 
			"<div class='element'>" +
				"<div class='icon'> </div>" +
				"{{#if title}}" + 
					"<div class='name'>{{title}}</div>" +
				"{{else}}" + 
					"<div class='name'>{{name}}</div>" +
				"{{/if}}" +
				"<div class='options'> </div>" +
			"</div>" +
			"<div class='folders'>" +
			"{{#each folders}} {{> folder}} {{/each}}" + 
			"</div>" +
		"</div>",
	foldersText: "{{#each folders}} {{> folder}} {{/each}}",
	file: Handlebars.compile(
		"<div id='{{id}}' class='file' path='{{path}}'>" + 
			"<div class='icon {{classes}}'> </div>" +
			"<div class='name'>{{name}}</div>" +
			"<div class='options'> </div>" +
		"</div>"
	),
	files: Handlebars.compile(
	"{{#each content}}" +
		"{{file}}" +
	"{{/each}}")

};

FileBrowser.templates.folders = Handlebars.compile(FileBrowser.templates.foldersText);
FileBrowser.templates.folder = Handlebars.compile(FileBrowser.templates.folderText);

Handlebars.registerHelper('button', function() {
	return new Handlebars.SafeString(FileBrowser.templates.button({
		id: this.id,
		text:this.text,
		predicate: (this.id=="separator")
	}));
});

Handlebars.registerHelper('file', function(){
	//handle file name
	var name 	= this.name,
		path 	= this.path;

	var correctName = (name == "" ? path : name);

	var lastIndex = correctName.lastIndexOf('.'),
		nameNoExtension = correctName.substr(0,lastIndex),
		extension = correctName.substr(lastIndex+1, correctName.length);

	return new Handlebars.SafeString(FileBrowser.templates.file({
		path: path,
		name: nameNoExtension,
		id: this.id,
		classes: extension
	}));
});

Handlebars.registerPartial('folder', FileBrowser.templates.folderText);





