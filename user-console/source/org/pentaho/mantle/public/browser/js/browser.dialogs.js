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
		
		postShow: null,

		postHide: null,

		isDragging: false,

		// 	config = { 					
		// 		dialog.id: 				"id",				
		// 		dialog.content.header: 	"string" or undefined,
		// 		dialog.content.body: 	"string" or undefined,
		// 		dialog.content.footer: 	"string" or undefined,
		// 		dialog.close_btn: 		boolean (default true)
		// 	}

		init: function ( config, postShow, postHide ) {
			var me = this;
			
			this.postShow = postShow;
			this.postHide = postHide;

			// Create dialog from content
			this.$dialog = $(DialogTemplates.dialog(config));
			this.$dialog.find(".footer .cancel").bind("click", function(){
				me.hide.apply(me);
			});

			// Toggle close button
			var closeBtn = this.$dialog.find(".dialog-close-button").bind("click", this.hide);
			if (config.dialog.close_btn === false) {
				closeBtn.detach();
			}	

			// Register this dialog and store in global dialogs			
			dialogs.push(this);

			// Re-center dialog
			$(window).bind("resize", function(){
				me._center();
			})

			this.$dialog.find(".Caption")
				.bind("mousedown", function(event) {
					var mouseX = event.clientX;
					var mouseY = event.clientY;

					var dialogX = me.$dialog.position().left;
					var dialogY = me.$dialog.position().top;

					me.isDragging = true;
					me.$dialog.unbind("mousemove");

				    me.$dialog.bind("mousemove", function(event) {
				        var newMouseX = event.clientX;
						var newMouseY = event.clientY;

				        if (me.isDragging) {
				        	me.$dialog.css({
				        		left: dialogX + (newMouseX - mouseX),
				        		top: dialogY + (newMouseY - mouseY)
				        	});
				        }
				    });
				})
				.mouseup(function() {
				    this.isDragging = false;
				    me.$dialog.unbind("mousemove");				    
				});

			return this.$dialog;
		},			

		show: function() {			
			
			// Hide all other dialogs before showing the next
			for (index in dialogs) {
				dialogs[index].hide();
			}

			this.$dialog.modal('show');				
			this.$dialog.appendTo($container);    		
    		this.$dialog.focus();
    		$(".modal-backdrop").detach().appendTo($container);

    		// Center modal within container
    		this._center();

    		if (this.postShow) {
    			this.postShow();
    		}

    		return this.$dialog;
		},

		hide: function() {
			this.$dialog.modal('hide');							

			this.isDragging = false;

			if (this.postHide) {
				this.postHide();
			}
		},

		_center: function() {
			var backdrop = $container.find(".modal-backdrop");

			this.$dialog.css({
				"left": backdrop.width() / 2 - this.$dialog.width() / 2,
				"top": backdrop.height() / 2 - this.$dialog.height() / 2
			});
		}
	};

	var Dialog = function(cfg, postShow, postHide, i18n) {
		this.init(cfg, postShow, postHide);
	    this.i18n=i18n;
	}

	Dialog.buildCfg = function(id, header, body, footer, close_btn) {
		var cfg = {};
		
		cfg.dialog = {};
		cfg.dialog.id = id;
		cfg.dialog.close_btn = close_btn;

		cfg.dialog.content={};
		cfg.dialog.content.header = header;
		cfg.dialog.content.body = body;
		cfg.dialog.content.footer = footer;

		return cfg;
	};

	Dialog.prototype = local;


	return Dialog;
});