/*
 * ******************************************************************************
 * Pentaho
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 */
 pen.define([
 	"common-ui/util/ContextProvider",
 	"common-ui/util/BootstrappedTabLoader"
 ], function(ContextProvider, BootstrappedTabLoader) {

 	function init() {
		$.support.cors = true;

 		BootstrappedTabLoader.init({
			parentSelector: "#getting-started",
			tabContentPattern : "content/getting_started_tab{{contentNumber}}_content.html",
			defaultTabSelector : "#tab1",
			before: function() {
				ContextProvider.get(function(context) {
					// Generate samples array descriptions
			 		var samplesArray = new Array();
			 		for (var i = 1; i <= 8; i++) {
			 			samplesArray.push({
			 				title: context.i18n["getting_started_sample" + i],
			 				description : context.i18n["getting_started_sample" + i + "_description"]
			 			});
			 		}
			 		ContextProvider.addProperty("getting_started_samples", samplesArray);

			 		// Generate turorials array descriptions
			 		var tutorialsArray = new Array();
			 		for (var i = 1; i <=5; i++) {
			 			tutorialsArray.push({
			 				title: context.i18n["getting_started_video" + i],
			 				description: context.i18n["getting_started_video" + i + "_description"]
			 			});
			 		}
			 		ContextProvider.addProperty("getting_started_tutorials", tutorialsArray);
		 		});
			},
			postLoad: function(jHtml, tabSelector) {
				var tabId = $(tabSelector).attr("id");
				if (tabId == "tab1") {
					ContextProvider.get(function(context) {
						injectYoutubeVideoDuration(context.config.welcome_link_id, jHtml, "#video-length");  
		  				appendNavParams(jHtml, "tab1");	
		  			});

				} else if (tabId == "tab2") {
					bindCardInteractions(jHtml, ".sample-card", "#sample-details-content", ".tab-pane:has(#sample-details)");

				} else if (tabId == "tab3") {
					// Get context to retrieve tutorial links from config
			 		ContextProvider.get(function(context) {
						
						jHtml.find(".tutorial-card").each(function(index) {
				 			var youtubeLinkId = context.config["tutorial_link" + (index+1) + "_id"];

				 			injectYoutubeVideoDuration(youtubeLinkId, $(this), ".tutorial-card-time", function(time) {
				 				if (index == 0) {
				 					$(".video-length").text(formatSeconds(time));	
				 				} 				
				 			});
				 		});
			 		}); 		

			 		bindCardInteractions(jHtml, ".tutorial-card", "#tutorial-details-content", ".tab-pane:has(#tutorial-details)", function(card) {
			 			$(".video-length").text(card.find(".tutorial-card-time").text());
			 		}); 		
				}
			}
		});
 	} 	

 	/**
 	 * Binds the interactions for the card selection and detail population
 	 */
 	function bindCardInteractions(jParent, cardSelector, detailsContentSelector, tabContentSelector, postClick) {
 		var cards = jParent.find(cardSelector);
 		cards.bind("click", function() {
 			$(cardSelector + ".selected").removeClass("selected");

 			var card = $(this); 			
 			card.addClass("selected");

 			var detailsContentContainer = $(detailsContentSelector);
 			detailsContentContainer.find(".detail-title").text(card.find(".card-title").text());
 			detailsContentContainer.find(".detail-description").text(card.find(".card-description").text());

 			var tabContent = $(tabContentSelector);
 			appendNavParams(jParent, tabContent.attr("id"), cards.index(card));

 			// Execute specific on click functions
 			if (postClick) {
 				postClick(card);
 			} 			
 		}).first().click();
 	}

 	/*
 	 * Injects the duration of the youtube video into an element
 	 */
 	function injectYoutubeVideoDuration(youtubeLinkId, injectContent, injectSelector, post) {
 		var url ="http://gdata.youtube.com/feeds/api/videos/" + youtubeLinkId + "?v=2&alt=jsonc&prettyprint=true";

 		function postSuccess(data) {
 			if (typeof data=="string"){
 				data = eval("(" + data + ")");
 			}
 			var videoLength = data.data.duration;
			injectContent.find(injectSelector).text(formatSeconds(videoLength));

			if (post) {
				post(videoLength);
			}
 		}

 		function postError(error) {
 			alert(error);

 			// TODOD
 		}

 		if ($(".IE").length > 0 && window.XDomainRequest) {
            // Use Microsoft XDR
            var xdr = new XDomainRequest();
            xdr.open("get", url);
            xdr.onload = function() {
                postSuccess(xdr.responseText);
            };
            xdr.onerror = function(event) {
            	postError(event);
            };

            xdr.ontimeout = function(event) {
            	alert(event);
            };

            xdr.onprogress = function(event) {};

            xdr.send();

        } else {            
			$.ajax(url, {
				type: "GET",
				success: postSuccess,
				error : function(data, status, error) {
					postError(error);					
				}
			});
        }
 	}

 	/**
 	 * Takes seconds and formats it into minutes and seconds
 	 */
 	function formatSeconds(seconds) {
 		var min = parseInt(seconds / 60);
 		var sec = seconds % 60;

 		return " " + (min < 10 ? "0" : "") + min + ":" + (sec < 10 ? "0" : "") + sec;
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

 		jTab.find("a.launch-link").attr("href", href);
 	}

 	return {
 		init:init,
 		injectYoutubeVideoDuration:injectYoutubeVideoDuration
 	};
 });