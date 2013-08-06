/*
 * ******************************************************************************
 * Pentaho
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 */

pen.define([
	"common-ui/util/BootstrappedTabLoader",
	"common-ui/util/ContextProvider",
	"common-ui/util/HandlebarsCompiler",
	"home/gettingStarted"
], function(BootstrappedTabLoader, ContextProvider, HandlebarsCompiler, GettingStartedWidget) {

	function init() {
		var urlVars = getUrlVars();
		//selectedContentIndex

		var prevTab;

		BootstrappedTabLoader.init({
			parentSelector: "#launch-widget",
			tabContentPattern : "launch_tab{{contentNumber}}_content.html",
			defaultTabSelector : "#"+urlVars.selectedTab,
			before: function() { 
				ContextProvider.get(function(context) {
					GettingStartedWidget.injectMessagesArray(
						"getting_started_samples", 
						context.config.getting_started_sample_message_template, 
						context.config.getting_started_sample_link_template,
						"sample-card" );
					
					GettingStartedWidget.injectMessagesArray(
						"getting_started_tutorials", 
						context.config.getting_started_video_message_template, 
						context.config.getting_started_video_link_template,
						"tutorial-card" );	
		 		});
			}, postLoad: function(jHtml, tabSelector) {
				var tabId = $(tabSelector).attr("id");

				if (tabId == "tab1") {
					GettingStartedWidget.checkInternet(jHtml, function(){}, function() {
						$("#welcome-video").hide();
					});
				}
				else if (tabId == "tab2") {
					bindCardInteractions(jHtml, ".sample-card", (urlVars.selectedTab == "tab2" ? urlVars.selectedContentIndex : 0), function(card) {
						var cardIndex = jHtml.find(".sample-card").index(card);
						
						jHtml.find("#sample-frame").attr("src", "");
						jHtml.find("#sample .alert").hide();

						ContextProvider.get(function(context) {
							var date = new Date();
							var host = window.location.host;
							var sampleId = context.config["sample" + (cardIndex+1) + "_id"].split(",");
							
							var url = "http://" + host + context.config.sample_url_base + context.config.sample_repo_dir_base + sampleId[0] + sampleId[1];
							url += "?ts=" + date.getTime();
							
							var filePropsUrl ="http://" + host + context.config.sample_properties_url_base + context.config.sample_repo_dir_base + sampleId[0] + context.config.sample_properties_url_suffix;							
							filePropsUrl += "?ts=" + date.getTime();

							function error() {
								ContextProvider.get(function(context) {
									var errMsg = HandlebarsCompiler.compile(context.i18n.error_no_sample_content, { sample_title: $.trim(card.find(".card-title").text())});
									jHtml.find("#sample .alert").text(errMsg).show();
									jHtml.find("#sample-frame").hide();
									jHtml.find("#sample").css("height", "auto");
								});
							}

							$.ajax(filePropsUrl, {
								dataType: "text",
								success: function(data) {
									if (data == undefined) {
										error();
										return;
									}
									jHtml.find("#sample-frame").attr("src", url);

								}, 
								error: function(err) {
									error();
								}
							});
						});				
					});

				} else if (tabId == "tab3") {

					// Fixes a scrolling issue when it is not necessary to scroll
					jHtml.parent().css("overflow", "hidden");

					function bindInteractions(internet) {
						// Bind click interactions
						bindCardInteractions(jHtml, ".tutorial-card", (urlVars.selectedTab == "tab3" ? urlVars.selectedContentIndex : 0), function(card) {
							ContextProvider.get(function(context) {
								// Update video
								var cardIndex = jHtml.find(".tutorial-card").index(card);
								
								if (internet){
									$("#tutorial-video").attr("src", context.config.youtube_embed_base + context.config["tutorial_link" + (cardIndex+1) + "_id"] + "?autoplay=1");	
								}								
								
								// Copy title and description
								jHtml.find(".detail-title").text(card.find(".card-title").text());
								jHtml.find(".detail-description").text(card.find(".card-description").text());
							})						
						});	
					}

					GettingStartedWidget.checkInternet(jHtml, function() {						
						bindInteractions(true);
						
					}, function() {
						$("#tutorial-video").hide();
						bindInteractions(false);
					});

					
				}
				
			}, postClick: function(tabSelector) {
				var tabId = $(tabSelector).attr("id");

				// Re-populate welcome video src link
				if (tabId == "tab1") {
					ContextProvider.get(function(context) {
						$("#welcome-video").attr("src", context.config.youtube_embed_base + context.config.welcome_link_id + "?autoplay=1");
					});
				}

				// Re-populate tutorial video src link
				if (tabId == "tab3") {
					ContextProvider.get(function(context) {
						var selectedCard = $(".tutorial-card.selected");
						var cardIndex = $(".tutorial-card").index(selectedCard);
						$("#tutorial-video").attr("src", context.config.youtube_embed_base + context.config["tutorial_link" + (cardIndex+1) + "_id"] + "?autoplay=1")
					})
				}
				
				// Clear source of welcome video to comply with tab switching
				if (prevTab == "tab1" && tabId != "tab1") {
					try{
						$("#welcome-video").attr("src", "");
					} catch (err) {
						alert(err);
					}
										
				}

				// Clear source of tutorial video to comply with tab switching
				if (prevTab == "tab3" && tabId != "tab3") {					
					$("#tutorial-video").attr("src", "");					
				}

				prevTab = tabId;
			}
		});		
	}

	/**
	 * Provides the click interactions for "cards" on page
	 */
	function bindCardInteractions(jParent, cardSelector, defaultSelectedIndex, post) {

		var cards = jParent.find(cardSelector);

		cards.bind("click", function() {
			var card = $(this);

			// Clear selected cards
			jParent.find(".selected").removeClass("selected");
			card.addClass("selected");

			if (post) {
				post(card);
			}
		});

		cards.eq(defaultSelectedIndex).click();
	}

	/**
	 * Retrieves the url variables and places them into a JSON
	 */
	function getUrlVars()
	{
	    var vars = {}, hash;
	    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
	    for(var i = 0; i < hashes.length; i++)
	    {
	        hash = hashes[i].split('=');
	        vars[hash[0]] = hash[1];
	    }
	    return vars;
	}

	return {
		init:init
	};
});