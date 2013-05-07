/*
 * ******************************************************************************
 * Pentaho
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 */

 pen.define(["common-ui/util/PentahoSpinner"], function(spinConfigs) {

	var spinner;
	var initContext;

 	/**
 	 * Initializes the getting started widget
 	 */
 	function init(context) {
 		initContext = context;
 		
 		// Create spinner
 		spinner = new Spinner(spinConfigs.getLargeConfig());


 		// Generate samples array descriptions
 		var samplesArray = new Array();
 		for (var i = 1; i <= 9; i++) {
 			samplesArray.push({
 				title: context.i18n["getting_started_sample" + i],
 				description : context.i18n["getting_started_sample" + i + "_description"]
 			});
 		}
 		context.getting_started_samples = samplesArray;

 		// Generate turorials array descriptions
 		var tutorialsArray = new Array();
 		for (var i = 1; i <=5; i++) {
 			tutorialsArray.push({
 				title: context.i18n["getting_started_video" + i],
 				description: context.i18n["getting_started_video" + i + "_description"],
 				link: context.i18n["tutorial_link" + i]
 			});
 		}
 		context.getting_started_tutorials = tutorialsArray;
 		
 		// Getting started widget
        var gettingStartedWidget = $("#getting-started");

 		// Bind click events to the tabs in the tab group
 		$('#tab-group a').bind("click", function (e) {
			e.preventDefault();

			// Hide shown content
			var openTabSelector = $("#getting-started #tab-group li.active a").attr("href");
			$(openTabSelector).hide();

			var tabSelector = $(this).attr("href");
			$(this).tab('show');

			// Load content for tab if it has not been loaded yet
			if ($("#getting-started " + tabSelector).children().length == 0) {				
				loadTabContent("content/getting_started_" + tabSelector.replace("#", "") + "_content.html", tabSelector, context);
			} else {
				$(tabSelector).show();
			}
		});

 		// Load first Tab Content
  		loadTabContent("content/getting_started_tab1_content.html", "#getting-started #tab1", context, function(jHtml) {
  			var youtubeLink = jHtml.find("#youtube-link").attr("href");
  			injectYoutubeVideoDuration(youtubeLink, jHtml, "#video-length");  			
  		});
 	}

 	/**
 	 * Initializes the interactive content on Samples Tab
 	 */
 	function initSamplesTab() { 		
 		bindCardInteractions(".sample-card", "#sample-details-content");
 	}

 	/**
 	 * Initializes the Tutorials Tab
 	 */
 	function initTutorialsTab() {
 		$(".tutorial-card").each(function(index) {
 			var jThis = $(this);
 			var youtubeLink = initContext.i18n["tutorial_link" + (index+1)];

 			jThis.find("")
 			injectYoutubeVideoDuration(youtubeLink, jThis, ".tutorial-card-time", function(time) {
 				if (index == 0) {
 					$(".video-length").text(formatSeconds(time));	
 				} 				
 			});
 		});

 		bindCardInteractions(".tutorial-card", "#tutorial-details-content", function(card) {
 			$(".video-length").text(card.find(".tutorial-card-time").text());
 		}); 		
 	}

 	/**
 	 * Binds the interactions for the card selection and detail population
 	 */
 	function bindCardInteractions(cardSelector, detailsContentSelector, postClick) {
 		$(cardSelector).bind("click", function() {
 			$(cardSelector + ".selected").removeClass("selected");

 			var card = $(this); 			
 			card.addClass("selected");

 			var detailsContentContainer = $(detailsContentSelector);
 			detailsContentContainer.find(".detail-title").text(card.find(".card-title").text());
 			detailsContentContainer.find(".detail-description").text(card.find(".card-description").text());

 			// Execute specific on click functions
 			if (postClick) {
 				postClick(card);
 			} 			
 		}).first().click();
 	}

 	/*
 	 * Injects the duration of the youtube video into an element
 	 */
 	function injectYoutubeVideoDuration(youtubeLink, injectContent, injectSelector, post) { 		
  		var videoId = youtubeLink.replace("http://www.youtube.com/watch?v=", "");

 		// Retrieve duration of main video
		$.get("http://gdata.youtube.com/feeds/api/videos/" + videoId + "?v=2&alt=jsonc&prettyprint=true", function(data) {
			var videoLength = data.data.duration;
			injectContent.find(injectSelector).text(formatSeconds(videoLength));

			if (post) {
				post(videoLength);
			}
		});
 	}

 	/**
 	 * Loads the content for a tab and compiles the content
 	 */
 	function loadTabContent(url, selector, context, post) {

 		// Show loading spinner
 		injectSpinner(selector);

 		$.get(url, function (data) {
 			var compiledContent = compile(data, context);

 			// Delay content injection to give a moment for the loading spinner
 			setTimeout(function() {
 				spinner.stop();
 				
 				var html = $(compiledContent);
 				
 				if (post) {
 					post(html);
 				}

 				$(selector).html(html);
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
 			overflow: "hidden"
 		});

 		$(selector).html(jqSpinner); 		
		spinner.spin(jqSpinner[0]);
 	}

 	/**
 	 * Takes seconds and formats it into minutes and seconds
 	 */
 	function formatSeconds(seconds) {
 		var min = parseInt(seconds / 60);
 		var sec = seconds % 60;

 		return " " + (min < 10 ? "0" : "") + min + ":" + (sec < 10 ? "0" : "") + sec;
 	}

 	return {
 		init:init,
 		initSamplesTab:initSamplesTab,
 		initTutorialsTab:initTutorialsTab
 	};
 });