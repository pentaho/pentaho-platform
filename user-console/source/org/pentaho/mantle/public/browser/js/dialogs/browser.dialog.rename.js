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
			showInitialDialog: true
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
			"click p.checkbox"	: "setShowInitialDialog",
			"click .yes"		: "showRenameDialog",
			"click .ok"			: "doRename"
		},

        initialize: function(){
        	
        },

        render: function(){
        	var myself = this;

        	pen.require(["js/dialogs/browser.dialog.rename.templates"], function(templates){
				
        		if(myself.model.get("showInitialDialog")){
        			
					var i18n = myself.options.i18n;

        			var footer = DialogTemplates.buttons({ 
        				ok: i18n.prop("overrideYesButton"), 
        				cancel: i18n.prop("overrideNoButton") 
        			});

        			var cfg = myself.DialogBuilder.buildCfg(
        				"dialogOverride",							// id
        				i18n.prop("overrideTitle"),					// header
        				templates.dialogOverride({ i18n: i18n }),	// body
        				footer); 									// footer	        				        			

        			myself.DialogBuilder.show(cfg);
        		} else {
        			showRenameDialog();
        		}					
        	});
        },

        setShowInitialDialog: function(event){
        	var $target = $(event.target),
        		$inputChecked = $target.find("input:checked");

        	this.model.set("showInitialDialog", $inputChecked.length > 0 ? true : false);
        },

        showRenameDialog: function(){
        	var myself = this;

        	pen.require(["js/dialogs/browser.dialog.rename.templates"], function(templates){

				var i18n = myself.options.i18n;

        		var body = templates.dialogDoRename({
    				i18n: i18n,
    				name: myself.model.get("path")
    			});

    			var footer = DialogTemplates.buttons({
    				ok: i18n.prop("ok"),
    				cancel: i18n.prop("cancel")
    			});

				var cfg = myself.DialogBuilder.buildCfg(
					"dialogRename",
					i18n.prop("renameTitle"),
					body, 
					footer);
        		

				myself.DialogBuilder.show(cfg);
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