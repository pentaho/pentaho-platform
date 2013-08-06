/*
 * ******************************************************************************
 * Pentaho
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 */
 pen.define([
 	"common-ui/util/ContextProvider",
 	"common-ui/util/BootstrappedTabLoader",
 	"common-ui/util/HandlebarsCompiler"
 ], function(ContextProvider, BootstrappedTabLoader, HandlebarsCompiler) {
 	
 	var disableWelcomeVideo = function() {
		$("#welcome-video").remove();
		$(".welcome-img").show();
	}

 	function init() {

		if (window.perspectiveDeactivated) {
			var perspectiveDeactivated = window.perspectiveDeactivated;

			window.perspectiveDeactivated = function() {
				perspectiveDeactivated();
				disableWelcomeVideo();
			}
		} else {
			window.perspectiveDeactivated = disableWelcomeVideo;
		}

 		BootstrappedTabLoader.init({
			parentSelector: "#getting-started",
			tabContentPattern : "content/getting_started_tab{{contentNumber}}_content.html",
			defaultTabSelector : "#tab1",
			before: function() {
				ContextProvider.get(function(context) {
					
					injectMessagesArray(
						"getting_started_samples", 
						context.config.getting_started_sample_message_template, 
						context.config.getting_started_sample_link_template, 
						"sample-card" );
					
					injectMessagesArray(
						"getting_started_tutorials", 
						context.config.getting_started_video_message_template, 
						context.config.getting_started_video_link_template,
						"tutorial-card" );				
		 		});

		 		// Remove embedded youtube since it shows through the other tabs
		 		$("a[href=#tab2], a[href=#tab3]").bind("click", disableWelcomeVideo);
			},
			postLoad: function(jHtml, tabSelector) {
				var tabId = $(tabSelector).attr("id");				

				if (tabId == "tab1") {									
					ContextProvider.get(function(context) {

						checkInternet(jHtml, 
							function() {
		  						// Swap the welcome image for the embedded youtube link
		  						$(".welcome-img").bind("click", function(){

		  							var youtubeWelcomeVideo = HandlebarsCompiler.compile("<iframe id='welcome-video' "+
		  								"src='{{config.youtube_embed_base}}{{config.welcome_link_id}}?autoplay=1' "+
		  								"width='550px' height='372px' frameborder='0' allowfullscreen></iframe>", context);

		  							$(this).hide().after(youtubeWelcomeVideo);
		  						});
							});						
		  			});

				} else if (tabId == "tab2") {								
					bindCardInteractions(jHtml, ".sample-card", "#sample-details-content", "sample-img", true);

				} else if (tabId == "tab3") {						
			 		checkInternet( jHtml, 
			 			function() {
				 			bindCardInteractions(jHtml, ".tutorial-card", "#tutorial-details-content", "tutorial-img", true);
			 			}, 
			 			function() {
			 				bindCardInteractions(jHtml, ".tutorial-card", "#tutorial-details-content", "tutorial-img", false);
			 			});
				}
			}
		});
 	} 	

 	/**
 	 * Binds the interactions for the card selection and detail population
 	 */
 	function bindCardInteractions(jParent, cardSelector, detailsContentSelector, detailsImgBaseId, bindNavParams, postClick) {
 		var cards = jParent.find(cardSelector);
 		cards.bind("click", function() { 			
 			var card = $(this);
 			if (card.hasClass("selected")) {
 				return;
 			}

 			$(cardSelector + ".selected").removeClass("selected");
 			card.addClass("selected");

 			var index = cards.index(card);

 			var detailsContentContainer = $(detailsContentSelector);
 			detailsContentContainer.find(".detail-title").text(card.find(".card-title").text());
 			detailsContentContainer.find(".detail-description").text(card.find(".card-description").text());
 			
 			detailsContentContainer.find(".details-img")
 				.hide()
 				.attr("id", detailsImgBaseId + (index+1))
 				.fadeIn(500);
 			
 			if (bindNavParams) {
 				appendNavParams(jParent, jParent.parent().attr("id"), index);	
 			}

 			// Execute specific on click functions
 			if (postClick) {
 				postClick(card);
 			} 			
 		}).first().click();
 	}

 	/**
 	 * Locates launch links and creates the appropriate link to select the correct tab and content
 	 */
 	function appendNavParams(jTab, selectedTab, selectedContentIndex) {
 		var href = "content/getting_started_launch.html?";
 		href += "selectedTab=" + selectedTab;
 		href += "&selectedContentIndex=" + selectedContentIndex	
 		
 		if (typeof jTab=="string") {
 			jTab = $(jTab);
 		}

 		var launchLink = jTab.find(".launch-link");

 		launchLink.unbind("click");
 		launchLink.bind("click", function() {
 			window.open(href, "_blank");
 		});
 	}

 	/**
 	 * Injects titles and descriptions to be used by tab content later. Takes a context property to store the
 	 * final array and the templates for the templates and link.
 	 */
 	function injectMessagesArray(contextProperty, messagesTemplate, linkTemplate, idBase) {
 		ContextProvider.get(function(context) {
	 		var i = 1;
			var str;

			// Generate samples array descriptions
	 		var array = new Array();	 	
	 		while ( str = context.i18n[ HandlebarsCompiler.compile(messagesTemplate, {contentNumber: i++}) ] ) {

	 			// Log error if no supporting link is provided in configuration
	 			var link = HandlebarsCompiler.compile(linkTemplate, {contentNumber: i-1});
	 			if ( !context.config[link] ) {
	 				console.error(HandlebarsCompiler.compile(context.i18n.error_propterty_does_not_exist, {link: link}));
	 				continue;
	 			}			 			
	 			
	 			var infoArr = str.split("|");
	 			array.push({ title: infoArr[0], description : infoArr[1], id: idBase + (i-1) });			 			
	 		}
	 		ContextProvider.addProperty(contextProperty, array);
 		});
 	}

 	/**
 	 * Checks for an internet connection. If successful, the success function is run, otherwise an error message will be displayed
 	 */
 	function checkInternet( jParent, success, fail ) {
 		var errorMsg = jParent.find(".no-internet-error").hide();

 		if (navigator.onLine) {
 			if (success) {
 				success.call();
 			}

 		} else {
 			if (fail) {
				fail();
			}
		
			var launchLink = jParent.find(".launch-link");

			ContextProvider.get(function(context){
				if (launchLink.length > 0) {

					launchLink.bind("click", function(){							
							errorMsg.text(context.i18n.error_no_internet_access).show();

							setTimeout(function(){
								errorMsg.hide();
							}, 1500);											
						})					
					
				} else {
					errorMsg.text(context.i18n.error_no_internet_access).show();
				}
			});		
 		} 		
 	}

 	return {
 		init:init, 		
 		injectMessagesArray:injectMessagesArray,
 		checkInternet:checkInternet
 	};
 });