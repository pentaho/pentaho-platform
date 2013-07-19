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
	"js/browser.utils.js",
	"common-ui/bootstrap",
	"common-ui/jquery-i18n",
  	"common-ui/jquery"
], function(Dialogs, DialogTemplates) {
	
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
		DialogBuilder : new Dialogs(),

		events: {
			"click p.checkbox" : "setShowInitialDialog"
		},

        initialize: function(){
        },

        render: function(){
        	var me = this;

        	pen.require(["js/dialogs/browser.dialog.rename.templates"], function(templates){
				
        		if(me.model.get("showInitialDialog")) {
        			
					var i18n = me.options.i18n;

					var body = templates.dialogOverride({ 
						i18n: i18n 
					});

        			var footer = DialogTemplates.buttons({ 
        				ok: i18n.prop("overrideYesButton"), 
        				cancel: i18n.prop("overrideNoButton") 
        			});

        			var cfg = me.DialogBuilder.buildCfg(
        				"dialogOverride",			// id
        				i18n.prop("overrideTitle"),	// header
        				body,						// body
        				footer, 					// footer	        				        			
        				false);						// close_btn

        			me.setElement(me.DialogBuilder.show(cfg));

        			me.$el.find(".ok").bind("click", function() {
        				me.model.set("showInitialDialog", !me.$el.find("#do-not-show").prop("checked"));
        				me.showRenameDialog.apply(me);
        			});
        		} else {
        			me.showRenameDialog();
        		}					
        	});
        },

        showRenameDialog: function(){
        	var me = this;

        	pen.require(["js/dialogs/browser.dialog.rename.templates"], function(templates){

				var i18n = me.options.i18n;

        		var body = templates.dialogDoRename({
    				i18n: i18n,
    				name: me.model.get("path")
    			});

    			var footer = DialogTemplates.buttons({
    				ok: i18n.prop("ok"),
    				cancel: i18n.prop("cancel")
    			});

				var cfg = me.DialogBuilder.buildCfg(
					"dialogRename",
					i18n.prop("renameTitle"),
					body, 
					footer);

				me.setElement(me.DialogBuilder.show(cfg));
				me.$el.find(".ok").bind("click", function(){
					me.doRename.apply(me);
				});
        	});
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