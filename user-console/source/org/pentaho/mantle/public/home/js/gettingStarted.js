/*
 * ******************************************************************************
 * Pentaho
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 */

 pen.define(["common-ui/util/PentahoSpinner"], function(spinConfigs) {

	var spinner;

 	/**
 	 * Initializes the getting started widget
 	 */
 	function init(context) {
 		
 		// Pre-load tabs with
 		var config = spinConfigs.getLargeConfig();
 		config.color = "#555";
 		spinner = new Spinner(config);


 		// Generate samples array descriptions
 		var samplesArray = new Array();
 		for (var i = 1; i <= 9; i++) {
 			samplesArray.push({
 				title: context.i18n["getting_started_sample" + i],
 				description : context.i18n["getting_started_sample" + i + "_description"]
 			});
 		}
 		context.getting_started_samples = samplesArray;


 		// Getting started widget
        var gettingStartedWidget = $("#getting-started");

 		// Bind click events to the tabs in the tab group
 		$('#tab-group a').bind("click", function (e) {
			e.preventDefault();

			var tabSelector = $(this).attr("href");

			$(this).tab('show');

			// Load content for tab if it has not been loaded yet
			var tabContent = $("#getting-started " + tabSelector);
			if (tabContent.children().length == 0) {


				// Show loading
				injectSpinner(tabSelector);				
				loadTabContent("content/getting_started_" + tabSelector.replace("#", "") + "_content.html", tabSelector, context);
			}
		});

 		// Load first Tab Content
 		var firstTabSelector = "#getting-started #tab1";
 		injectSpinner(firstTabSelector);
 		loadTabContent("content/getting_started_tab1_content.html", firstTabSelector, context);
 	}


 	/**
 	 * Initializes the interactive content on Samples Tab
 	 */
 	function initSamplesTab() { 		
 		$(".sample-card").bind("click", function() {
 			$(".selected").removeClass("selected");

 			var sampleCard = $(this); 			
 			sampleCard.addClass("selected");

 			$("#sample-title").text(sampleCard.find(".sample-card-title").text());
 			$("#sample-description").text(sampleCard.find(".sample-card-description").text());
 		}).first().click();
 	}

 	/**
 	 * Loads the content for a tab and compiles the content
 	 */
 	function loadTabContent(url, selector, context, post) {
 		$.get(url, function (data) {
 			var compiledContent = compile(data, context);

 			setTimeout(function(){
 				$(selector).html($(compiledContent));

 				if (post) {
 					post();
 				}
 			}, 200);        	
 		});
 	}

 	/**
 	 * Compiles string content using Handlebars
 	 */
 	function compile(data, context) {
 		if (!data || data == "") {
 			return;
 		}

		var template = Handlebars.compile(data);
        return $.trim(template(context));
 	}

 	/**
 	 * Injects a spinner into content
 	 */
 	function injectSpinner(selector) {
 		var jqSpinner = $("<div></div>");
 		jqSpinner.css({
 			width: "100%",
 			height: "48px" 			
 		});

 		$(selector).html(jqSpinner); 		
		spinner.spin(jqSpinner[0]);
 	}

 	return {
 		init:init,
 		initSamplesTab:initSamplesTab
 	};
 });