/*
 * ******************************************************************************
 * Pentaho
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 */

 /*
 	<!-- HTML CONTENT -->

	 <ul class="nav nav-tabs" id="myTab">
	  <li><a href="#tab1">Home</a></li>
	  <li><a href="#tab2">Profile</a></li>
	  <li><a href="#tab3">Messages</a></li>
	  <li><a href="#tab4">Settings</a></li>
	</ul>
	 
	<div class="tab-content">
	  <div class="tab-pane active" id="tab1">...</div>
	  <div class="tab-pane" id="tab2">...</div>
	  <div class="tab-pane" id="tab3">...</div>
	  <div class="tab-pane" id="tab4">...</div>
	</div>
 */

 /*
	var demoConfig = {
		parentSelector: "#some-id",
		tabContentPattern : "folder1/folder2/this_is_content{{contentNumber}}.html",
		defaultTabSelector : "#tabId",
		before: function() { },
		post: function(jHtml, tabSelector) { }, 
		contextConfig: ["path", { path:"path", post: function(context, loadedMap) { } } ] //SEE contextProvider.js in common-ui
	};
*/

 pen.define([
 	"common-ui/util/PentahoSpinner", 
 	"home/ContextProvider",
 	"home/HandlebarsCompiler"
 ], function(spinConfigs, ContextProvider, HandlebarsCompiler) {	

	var spinner;

 	/**
 	 * Initializes the getting started widget
 	 */
 	function init(config) {

 		// Create spinner
	 	spinner = new Spinner(spinConfigs.getLargeConfig());
	
 		// Bind click events to the tabs in the tab group	
 		$(config.parentSelector + ' #tab-group a').bind("click", function (e) {
			e.preventDefault();

			var tabSelector = $(this).attr("href");
			$(this).tab('show');

			var parentedSelector = config.parentSelector + " " + tabSelector;
			// Load content for tab if it has not been loaded yet
			if ($(parentedSelector).children().length == 0) {
				var url = HandlebarsCompiler.compile(config.tabContentPattern, { contentNumber: tabSelector.replace("#tab", "") });
				
				ContextProvider.get(function(context) {
					loadTabContent(url, parentedSelector, context, config.post);
				}, config.contextConfig);
			}
		});

 		if (config.before) {
 			config.before();
 		}

 		// Selects the default element and clicks it
		$(config.parentSelector + " a[href=" + config.defaultTabSelector + "]").click();
 	}

 	/**
 	 * Loads the content for a tab and compiles the content
 	 */
 	function loadTabContent(url, selector, context, post) {

 		// Show loading spinner
 		injectSpinner(selector);

 		$.get(url, function (data) { 			
 			HandlebarsCompiler.compile(data, context, function(compiledContent) {
				// Delay content injection to give a moment for the loading spinner
	 			setTimeout(function() {
	 				spinner.stop();
	 				
	 				var html = $(compiledContent);	 				 			
	 				$(selector).html(html);

	 				if (post) {
	 					post(html, selector);
	 				}
	 			}, 200);
 			}); 			
 		});
 	}

 	/**
 	 * Injects a spinner into content
 	 */
 	function injectSpinner(selector) {
 		var jqSpinner = $("<div></div>");
 		jqSpinner.css({
 			width: "100%",
 			overflow: "hidden"
 		});

 		$(selector).html(jqSpinner); 		
		spinner.spin(jqSpinner[0]);
 	}

 	return {
 		init:init
 	};
 });