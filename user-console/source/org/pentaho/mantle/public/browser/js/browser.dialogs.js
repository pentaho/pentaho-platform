/*
 * ******************************************************************************
 * Pentaho
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 */

 pen.define([
 	"js/browser.dialogs.templates.js",
 	"js/browser.utils.js",	
	"common-ui/bootstrap",
	"common-ui/jquery-i18n",
  	"common-ui/jquery"
], function(DialogTemplates, BrowserUtils) {

	var dialogs = new Array();

	var $body = $(window.top.document).find("html body"),
		$container = $body.find(".bootstrap.dialogs");

	// Add container to body once
	if ($container.length == 0) {		
		$container = $("<div class='bootstrap dialogs'></div>");
		$container.appendTo($body);	
	}

	var local = {

		$dialog: null,

		init: function() {	
		},

		
		// 	config = { 					
		// 		dialog.id: 				"id",				
		// 		dialog.content.header: 	"string" or undefined,
		// 		dialog.content.body: 	"string" or undefined,
		// 		dialog.content.footer: 	"string" or undefined,
		// 		dialog.close_btn: 		boolean (default true)
		// 	}
		

		show: function(config) {
			var that = this;

			// Enable reshowing of dialog as to avoid re-processing
			if (!config && this.$dialog) {
				showDialog();
				return this.$dialog;
			}
			
			// Create dialog from content
			this.$dialog = $(DialogTemplates.dialog(config));
			this.$dialog.find(".footer .cancel").bind("click", function(){
				that.hide.apply(that);
			});

			// Toggle close button
			var closeBtn = this.$dialog.find(".dialog-close-button").bind("click", this.hide);
			if (config["dialog.close_btn"] === false) {
				closeBtn.hide();
			}

			showDialog();

			this.$dialog.bind("keydown", function(event) {
				if (event.keyCode == 27) {
					that.hide.apply(that);	
				}				
			});

			return this.$dialog;

			/*
			 * Anonymous inner function to make it private to show(config) function
			 */
			function showDialog() {				
				that.hide();	
				that.$dialog.modal();				
        		that.$dialog.appendTo($container);
        		that.$dialog.focus();
        		$(".modal-backdrop").appendTo($container);
			};			
		},

		hide: function() {
			$container.empty();
			this.$dialog.detach();
	        $(".modal-backdrop").detach();
		},

		buildCfg: function(id, header, body, footer, close_btn) {
			var cfg = {};
			
			cfg.dialog = {};
			cfg.dialog.id = id;
			cfg.dialog.close_btn = close_btn;

			cfg.dialog.content={};
			cfg.dialog.content.header = header;
			cfg.dialog.content.body = body;
			cfg.dialog.content.footer = footer;

			return cfg;
		}
	}

	var Dialogs = function(i18n){
	    this.i18n=i18n;
	    this.init();
	}

	Dialogs.prototype = local;

	return Dialogs;
});