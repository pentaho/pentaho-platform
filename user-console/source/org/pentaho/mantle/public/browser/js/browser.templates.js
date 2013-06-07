pen.define([
	  "common-ui/handlebars"
	], function(){
		var templates = {};

		//main component structure
		templates.structure = Handlebars.compile(
			"<div id='fileBrowserFolders' class='span4 well fileBrowserColumn'>" +
				"<div class='body'></div>" + 
			"</div>" +
			"<div id='fileBrowserFiles' class='span4 well fileBrowserColumn'>" + 
				"<div class='body'></div>" + 
			"</div>" +
			"<div id='fileBrowserButtons' class='span4 well fileBrowserColumn'>" +
				"<div class='body'></div>" + 
			"</div>");

		//header for folder browser
		templates.folderBrowserHeader = Handlebars.compile(
			"{{#if folderBreadcrumb}}" + 
				"<div id='foldersHeader' class='header'>Browsing: {{folderBreadcrumb}}</div>" +
			"{{else}}" +
				"<div id='foldersHeader' class='header'>Browsing: Root</div>" +
			"{{/if}}");

		//header for file browser
		templates.fileBrowserHeader = Handlebars.compile(
			"{{#if folderName}}" +
				"<div id='filesHeader' class='header'>{{folderName}} Files</div>" +
			"{{else}}" + 
				"<div id='filesHeader' class='header'>Root Files</div>" +
			"{{/if}}");

		//header for buttons
		templates.buttonsHeader = Handlebars.compile(
			"{{#if folderName}}" +
				"<div id='buttonsHeader' class='header'>Folder Actions for {{folderName}}</div>" +
			"{{else}}" + 
				"{{#if fileName}}" +
					"<div id='buttonsHeader' class='header'>File Actions for {{fileName}}</div>" +
				"{{else}}" +
					"<div id='buttonsHeader' class='header'>Folder Actions for Root</div>" +
				"{{/if}}" +
			"{{/if}}");

		//button template
		templates.button = Handlebars.compile(
			"{{#if predicate}}" +
		    	"<div class='separator'></div>" +
			"{{else}}" +
				"<button id='{{id}}' class='btn btn-block' onclick={{handler}}''>{{text}}</button>" +
		    "{{/if}}");

		//buttons template to create list of buttons based on one object
		templates.buttons = Handlebars.compile("{{#each buttons}}{{button}}{{/each}}");

		//folder template with recursive behavior
		templates.folderText =
			"{{#ifCond file.folder 'true'}}" +
				"{{#if file.description}}" +
				"<div id='{{file.id}}' class='folder' path='{{file.path}}' title2='{{file.description}}' title='{{file.name}}'>" + 
				"{{else}}" +
				"<div id='{{file.id}}' class='folder' path='{{file.path}}' title2='{{file.name}}' title='{{file.name}}'>" + 
				"{{/if}}" +
					"<div class='element'>" +
						"<div class='expandCollapse'> </div>" +
						"<div class='icon'> </div>" +
						"{{#if file.title}}" + 
							"<div class='name'>{{file.title}}</div>" +
						"{{else}}" + 
							"<div class='name'>{{file.name}}</div>" +
						"{{/if}}" +
					"</div>" +
					"<div class='folders'>" +
					"{{#each children}} {{> folder}} {{/each}}" + 
					"</div>" +
				"</div>" +
			"{{/ifCond}}";

		//folders recursion
		templates.foldersText = "{{#each children}} {{> folder}} {{/each}}";

		//file template
		templates.file = Handlebars.compile(
			"{{#ifCond folder 'false'}}" + 
				"{{#if file.description}}" +
				"<div id='{{id}}' class='file' path='{{path}}' title2='{{file.description}}' title='{{fileWithExtension}}'>" + 
				"{{else}}" +
				"<div id='{{id}}' class='file' path='{{path}}' title2='{{fileWithExtension}}' title='{{fileWithExtension}}'>" +
				"{{/if}}" +
					"<div class='icon {{classes}}'> </div>" +
					"<div class='name'>{{title}}</div>" +
				"</div>" +
			"{{/ifCond}}");

		//files template to create list of files based on one object
		templates.files = Handlebars.compile("{{#each children}} {{file}} {{/each}}");

		//templates for folders creation
		templates.folders = Handlebars.compile(templates.foldersText);
		templates.folder = Handlebars.compile(templates.folderText);

		//helper registration for button template
		Handlebars.registerHelper('button', function() {
			return new Handlebars.SafeString(templates.button({
				id: this.id,
				text:this.text,
				predicate: (this.id=="separator")
			}));
		});

		//helper registration for file template
		Handlebars.registerHelper('file', function(){
			//handle file name
			var name 	= this.file.name,
			    title   = this.file.title,
				path 	= this.file.path;
			

			var correctName = (name == "" ? path : name);

			var lastIndex = correctName.lastIndexOf('.'),
				nameNoExtension = correctName.substr(0,lastIndex),
				extension = correctName.substr(lastIndex+1, correctName.length);

			return new Handlebars.SafeString(templates.file({
				path: path,
				name: nameNoExtension,
				title: title,
				id: this.file.id,
				classes: extension,
				folder: this.file.folder,
				fileWithExtension: this.file.name
			}));
		});

		//partial registration (essential for folder recursive method) 
		Handlebars.registerPartial('folder', templates.folderText);


		//extra helpers
		Handlebars.registerHelper('ifCond', function(v1, v2, options) {
			if(v1 == v2) {
				return options.fn(this);
			}
			return options.inverse(this);
		});

		//return object
		return {
			structure: templates.structure,
			folderBrowserHeader: templates.folderBrowserHeader,
			fileBrowserHeader: templates.fileBrowserHeader,
			buttonsHeader: templates.buttonsHeader,
			buttons: templates.buttons,
			folders: templates.folders,
			files: templates.files
		}
	});