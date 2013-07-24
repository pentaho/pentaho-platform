/*
 * ******************************************************************************
 * Pentaho
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 */

 pen.define([
 	"js/browser.dialogs.js",
 	"js/browser.dialogs.templates.js",
 	"js/dialogs/browser.dialog.rename.templates",
	"js/browser.utils.js",
	"common-ui/bootstrap",
	"common-ui/jquery-i18n",
  	"common-ui/jquery"
], function(Dialog, DialogTemplates, RenameTemplates, Utils) {

	var BrowserUtils = new Utils();
	
	var DialogModel =  Backbone.Model.extend({

		buildsessionVariableUrl: function(key, value) {
			return BrowserUtils.getUrlBase() + "api/mantle/session-variable?key=" + key + (value == undefined ? "" : "&value=" + value);
		},

		defaults: {
			name: "",
			path: "",
			showOverrideDialog: true
		},

		initialize: function() {
			var me = this;

			this.on("change:name", this.renamePath);		
			
			// Set default for showOverrideDialog			
			BrowserUtils._makeAjaxCall("GET", "text", this.buildsessionVariableUrl("showOverrideDialog"), function(result) {				
				me.set("showOverrideDialog", result.length == 0 || result === "true");
			});
		},

		renamePath: function(){
			console.log("Renaming path " + this.get("name"));
			//add here the call to perform the rename on the jcr
		}
	});

	var DialogView =  Backbone.View.extend({
		
		FileOverrideDialog : null,

		FolderOverrideDialog : null,

		RenameDialog : null,

		CannotRenameDialog: null,

		overrideType: "file",

		events: {
		},

        initialize: function(){
        	var me = this;

        	var onOverrideOk = function() {
        		var showOverrideDialog = !$(this).parents(".pentaho-dialog").find("#do-not-show").prop("checked");
        		
        		BrowserUtils._makeAjaxCall("POST", "text", me.model.buildsessionVariableUrl("showOverrideDialog", showOverrideDialog));
				me.model.set("showOverrideDialog", showOverrideDialog);
				
				me.showRenameDialog.apply(me);
			}

        	this.initFolderOverrideDialog(this.makeOverrideDialogCfg("dialogOverrideFolder", RenameTemplates.dialogFolderOverride), onOverrideOk);
        	this.initFileOverrideDialog(this.makeOverrideDialogCfg("dialogeOverrideFile", RenameTemplates.dialogFileOverride), onOverrideOk);
        	this.initRenameDialog();
        	this.initCannotRenameDialog();
        },

        initCannotRenameDialog: function() {
        	var i18n = this.options.i18n;
        	var me = this;

        	var header = i18n.prop("cannotRenameDialogTitle");

        	var body = i18n.prop("cannotRenameDialogDescription");

        	var footer = DialogTemplates.centered_button({
        		ok: i18n.prop("close")
        	});

        	var cfg = Dialog.buildCfg("cannot-rename-dialog", header, body, footer, false);

        	this.CannotRenameDialog = new Dialog(cfg);

        	this.CannotRenameDialog.$dialog.find(".ok").bind("click", function(){
        		me.CannotRenameDialog.hide();
        	});
        },

        makeOverrideDialogCfg: function(id, bodyTemplate) {
			var i18n = this.options.i18n;
        	var me = this;

        	var header = i18n.prop("overrideTitle");

			var body = bodyTemplate({ 
				i18n: i18n 
			});

			var footer = DialogTemplates.buttons({ 
				ok: i18n.prop("overrideYesButton"), 
				cancel: i18n.prop("overrideNoButton") 
			});

			return Dialog.buildCfg(id, header, body, footer, false);
        },

		initFolderOverrideDialog: function(cfg, onOk) {
			var me = this;

			var onShow = function() {
        		this.$dialog.find("#do-not-show").prop("checked", false);
        	};

        	this.FolderOverrideDialog = new Dialog(cfg, onShow);
        	this.FolderOverrideDialog.$dialog.find(".ok").bind("click", onOk);
		},        

        initFileOverrideDialog: function(cfg, onOk) {        	
        	var me = this;

			var onShow = function() {
        		this.$dialog.find("#do-not-show").prop("checked", false);
        	};

        	this.FileOverrideDialog = new Dialog(cfg, onShow);
        	this.FileOverrideDialog.$dialog.find(".ok").bind("click", onOk);
        },

        initRenameDialog: function() {
        	var i18n = this.options.i18n;
        	var me = this;

    		var body = RenameTemplates.dialogDoRename({
				i18n: i18n
			});

			var footer = DialogTemplates.buttons({
				ok: i18n.prop("ok"),
				cancel: i18n.prop("cancel")
			});

			var cfg = Dialog.buildCfg(
				"dialogRename",				// id
				i18n.prop("renameTitle"),	// header
				body, 						// body
				footer, 					// footer
				false);						// close_btn

			var onShow = function() {
				var okButton = this.$dialog.find(".ok").prop("disabled", true);

				var renameField = this.$dialog.find("#rename-field")
					.val(me.model.get("name"))
					.bind("keyup", function() {
						var val = renameField.val();
						okButton.prop("disabled", val == me.model.get("name") || val.length == 0);
					});
			};

			this.RenameDialog = new Dialog(cfg, onShow);

			this.RenameDialog.$dialog.find(".ok").bind("click", function(){
				me.RenameDialog.hide();
				me.doRename.apply(me);				
			});
        },

        render: function(){        	
    		if(!this.model.get("showOverrideDialog")) {
    			this.showRenameDialog();
    			return;
    		}

    		if (this.overrideType === "file") {
    			this.setElement(this.FileOverrideDialog.show());
    			return;
    		}

    		 this.setElement(this.FolderOverrideDialog.show());
			
        },

        showRenameDialog: function(){        	
        	this.setElement(this.RenameDialog.show());
        },

        doRename: function(){
        	var me = this;
        	
        	// api/repo/files/:home:joe:test.xaction/rename?newName=newFileOrFolderName
        	BrowserUtils._makeAjaxCall("POST", "text", BrowserUtils.getUrlBase() + "api/repo/files/" + 
        		this.model.get("path") + "/rename?newName=" + this.model.get("name"), function(){
        			me.model.set("name", this.$el.find("input").val());
        		}, function(){
        			me.setElement(me.CannotRenameDialog.show());
        		});
        }
	});

	var local = {
		
		model: null,

		view: null,
				
		init: function(path, overrideType){
			var repoPath = path;
			while (repoPath.search("/") > -1) {
				repoPath = repoPath.replace("/", ":");
			}

			var name = path.split("/")[path.split("/").length-1];
			var dotIndex = name.search("\\.");
			if (dotIndex > -1) {
				name = name.substr(0, dotIndex);
			}

			this.model.set("path", repoPath);
			this.model.set("name", name);			
			this.view.overrideType = overrideType;
			this.view.render();
		}
	}

	var DialogRename = function(i18n){
	    this.i18n=i18n;
	    this.model = new DialogModel();
	    this.view = new DialogView({
			model: this.model,
			i18n: this.i18n
		});
	}

	DialogRename.prototype = local;

	return DialogRename;
});