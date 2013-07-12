/*
 * ******************************************************************************
 * Pentaho
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 */

 pen.define([
 	"js/dialogs/browser.dialog.rename",
	"js/browser.utils.js",
	"common-ui/bootstrap",
	"common-ui/jquery-i18n",
  	"common-ui/jquery"
], function(DialogRename, BrowserUtils) {

	var local = {
		dialogRename: new DialogRename(),

		init: function(){
			
		},

		showDialogRename: function(path){
			this.dialogRename.init(path);
		}
	}

	var Dialogs = function(i18n){
	    this.i18n=i18n;
	    this.init();
	}

	Dialogs.prototype = local;

	return Dialogs;
});