/*
 * ******************************************************************************
 * Pentaho
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 */

 pen.define([
 	"js/dialogs/browser.dialog.rename.templates.js",
	"js/browser.utils.js",
	"common-ui/bootstrap",
	"common-ui/jquery-i18n",
  	"common-ui/jquery"
], function() {

	var local = {
		DialogModel: Backbone.Model.extend({
			defaults: {
				path: "",
				showInitialDialog: true
			},

			initialize: function(){
				this.on("change:path", this.renamePath);
			},

			renamePath: function(){
				console.log("Renaming path "+this.get("path"));
				//add here the call to perform the rename on the jrc
			}


		}),

		DialogView: Backbone.View.extend({
			events: {
				"click label.checkbox" : "setShowInitialDialog",
				"click .yes"           : "showRenameDialog",
				"click .ok"            : "doRename"
			},

	        initialize: function(){
	        	this.render();
	        },

	        render: function(){
	        	var myself = this;

	        	pen.require(["js/dialogs/browser.dialog.rename.templates"],function(templates){
	        		var $dialog;
	        		if(myself.model.get("showInitialDialog")){
	        			$dialog = $(templates.dialogRename({i18n:jQuery.i18n}));
	        		} else {
	        			$dialog = $(templates.dialogDoRename({
	        				i18n:jQuery.i18n,
	        				name: myself.model.get("path")
	        			}));
	        		}

	        		$dialog.modal();
	        		$("#"+$dialog.attr("id")).detach().appendTo(myself.$el);
	        		$(".modal-backdrop").detach().appendTo(myself.$el);
	        	});
	        },

	        setShowInitialDialog: function(event){
	        	var $target = $(event.target),
	        		$inputChecked = $target.find("input:checked");

	        	this.model.set("showInitialDialog", $inputChecked.length > 0? true : false);
	        },

	        showRenameDialog: function(){
	        	var myself = this;

	        	myself.$el.empty();

	        	pen.require(["js/dialogs/browser.dialog.rename.templates"],function(templates){
	        		var $dialog = $(templates.dialogDoRename({
	        				i18n:jQuery.i18n,
	        				name: myself.model.get("path")
	        			}));

	        		$dialog.modal();
	        		$("#"+$dialog.attr("id")).detach().appendTo(myself.$el);
	        		$(".modal-backdrop").detach().appendTo(myself.$el);
	        	});
	        },

	        doRename: function(){
	        	this.model.set("path", this.$el.find("input").val());
	        }
		}),

		init: function(path){
			var $body = $(window.top.document).find("html body"),
				$container = $("<div class='bootstrap dialogs'></div>");
			$container.appendTo($body);

			var name = path.split("/")[path.split("/").length-1];

			var model = new this.DialogModel({
				path: name
			});
			new this.DialogView({
				model: model,
				el: $container
			})
		},

	}

	var DialogRename = function(i18n){
	    this.i18n=i18n;
	}

	DialogRename.prototype = local;

	return DialogRename;
});