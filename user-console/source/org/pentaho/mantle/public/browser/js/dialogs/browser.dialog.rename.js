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
], function(Dialog, DialogTemplates, RenameTemplates) {
	
	var DialogModel =  Backbone.Model.extend({
		defaults: {
			path: "",
			showInitialDialog: true,
			showInitialDialogChecked: false
		},

		initialize: function(){
			this.on("change:path", this.renamePath);
		},

		renamePath: function(){
			console.log("Renaming path " + this.get("path"));
			//add here the call to perform the rename on the jcr
		}
	});

	var DialogView =  Backbone.View.extend({
		OverrideDialog : null,
		RenameDialog : null,

		events: {
			"click p.checkbox" : "setShowInitialDialog"
		},

        initialize: function(){        	
        	var i18n = this.options.i18n;
        	var me = this;

        	/*
        	 * Override Dialog
        	 */
			var body = RenameTemplates.dialogOverride({ 
				i18n: i18n 
			});

			var footer = DialogTemplates.buttons({ 
				ok: i18n.prop("overrideYesButton"), 
				cancel: i18n.prop("overrideNoButton") 
			});

			var cfg = Dialog.buildCfg(
				"dialogOverride",			// id
				i18n.prop("overrideTitle"),	// header
				body,						// body
				footer, 					// footer	        				        			
				false);						// close_btn

			var onShow = function() {
        		this.$dialog.find("#do-not-show").prop("checked", false);
        	};

        	this.OverrideDialog = new Dialog(cfg, onShow);

        	this.OverrideDialog.$dialog.find(".ok").bind("click", function() {
				me.model.set("showInitialDialog", !me.OverrideDialog.$dialog.find("#do-not-show").prop("checked"));
				me.showRenameDialog.apply(me);
			});

        	/*
        	 * Rename Dialog
        	 */
    		body = RenameTemplates.dialogDoRename({
				i18n: i18n
			});

			footer = DialogTemplates.buttons({
				ok: i18n.prop("ok"),
				cancel: i18n.prop("cancel")
			});

			cfg = Dialog.buildCfg(
				"dialogRename",				// id
				i18n.prop("renameTitle"),	// header
				body, 						// body
				footer, 					// footer
				false);						// close_btn

			onShow = function() {
				this.$dialog.find("#rename-field").attr("value", me.model.get("path"));
			};

			this.RenameDialog = new Dialog(cfg, onShow);

			this.RenameDialog.$dialog.find(".ok").bind("click", function(){
				me.RenameDialog.hide();
				me.doRename.apply(me);				
			});
        },

        render: function(){        	
    		if(!this.model.get("showInitialDialog")) {
    			this.showRenameDialog();
    			return;
    		}

			this.setElement(this.OverrideDialog.show());
        },

        showRenameDialog: function(){        	
        	this.setElement(this.RenameDialog.show());
        },

        doRename: function(){
        	this.model.set("path", this.$el.find("input").val());
        }
	});

	var local = {
		
		model: null,

		view: null,
				
		init: function(path){
			var name = path.split("/")[path.split("/").length-1];
			this.model.set("path", name);			
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