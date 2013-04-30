this.NewFileBrowser.templates = {};

Handlebars.registerHelper('button', function() {
	return new Handlebars.SafeString(NewFileBrowser.templates.button({
		id: this.id,
		text:this.text,
		predicate: (this.id=="separator")
	}));
});

Handlebars.registerHelper('file', function(){
	return new Handlebars.SafeString(NewFileBrowser.templates.file({
		path: this.path,
		name: this.name,
		id: this.id
	}));
});


NewFileBrowser.templates.structure = Handlebars.compile(
	"<div id='fileBrowserFolders' class='span4'></div>" +
	"<div id='fileBrowserFiles' class='span4'></div>" +
	"<div id='fileBrowserButtons' class='span4'></div>" 
);

NewFileBrowser.templates.button = Handlebars.compile(
	"{{#if predicate}}" +
    	"<div class='separator'></div>" +
	"{{else}}" +
		"<button id='{{id}}' class='btn btn-block'>{{text}}</button>" +
    "{{/if}}"
);

NewFileBrowser.templates.buttons = Handlebars.compile(
	"{{#each buttons}}" +
		"{{button}}" +
	"{{/each}}"
);

NewFileBrowser.templates.folderText = "<div id='{{id}}' class='folder' path='{{path}}'>" + 
		"<div class='icon'> </div>" +
		"<div class='name'>{{name}}</div>" +
		"<div class='options'> </div>" +
		"<div class='folders'>" +
		"{{#each folders}} {{> folder}} {{/each}}" + 
		"</div>" +
	"</div>";

NewFileBrowser.templates.foldersText = "{{#each folders}} {{> folder}} {{/each}}";

NewFileBrowser.templates.folders = Handlebars.compile(NewFileBrowser.templates.foldersText);
NewFileBrowser.templates.folder = Handlebars.compile(NewFileBrowser.templates.folderText);
Handlebars.registerPartial('folder', NewFileBrowser.templates.folderText);

NewFileBrowser.templates.file = Handlebars.compile(
	"<div id='{{id}}' class='file' path='{{path}}'>" + 
		"<div class='icon'> </div>" +
		"<div class='name'>{{name}}</div>" +
		"<div class='options'> </div>" +
	"</div>"
);

NewFileBrowser.templates.files = Handlebars.compile(
	"{{#each content}}" +
		"{{file}}" +
	"{{/each}}"
); 





