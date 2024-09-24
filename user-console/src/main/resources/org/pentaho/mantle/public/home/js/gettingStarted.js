/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

define([
  "common-ui/util/ContextProvider",
  "common-ui/util/BootstrappedTabLoader",
  "common-ui/util/HandlebarsCompiler"
], function (ContextProvider, BootstrappedTabLoader, HandlebarsCompiler) {

  var brightCoveVideoTemplate =
      '<iframe src="https://players.brightcove.net/4680021553001/default_default/index.html?videoId={{videoId}}&autoplay=true"' +
      ' width="{{width}}"' +
      ' height="{{height}}"' +
      ' allowfullscreen' +
      ' webkitallowfullscreen' +
      ' mozallowfullscreen>' +
      '</iframe>';

  var disableWelcomeVideo = function () {
    $("#welcome-video").empty();
    $(".welcome-img").show();
  };

  function init() {

    if (window.perspectiveDeactivated) {
      var perspectiveDeactivated = window.perspectiveDeactivated;

      window.perspectiveDeactivated = function () {
        perspectiveDeactivated();
        disableWelcomeVideo();
      }
    } else {
      window.perspectiveDeactivated = disableWelcomeVideo;
    }

    BootstrappedTabLoader.init({
      parentSelector: "#getting-started",
      tabContentPattern: "content/getting_started_tab{{contentNumber}}_content.html",
      defaultTabSelector: "tab1",
      before: function () {
        ContextProvider.get(function (context) {

          injectMessagesArray(
              "getting_started_samples",
              context.config.getting_started_sample_message_template,
              context.config.getting_started_sample_link_template,
              "sample-card");

          injectMessagesArray(
              "getting_started_tutorials",
              context.config.getting_started_video_message_template,
              context.config.getting_started_video_link_template,
              "tutorial-card");
        });

        // Remove embedded youtube since it shows through the other tabs
        $("a[href=\\#tab2], a[href=\\#tab3]").bind("click", disableWelcomeVideo);
        $(".tab-list").keydown(function (event) {
          let nextItem;
          var keyCode = event.which || event.keyCode;
          if (keyCode === 39) { // RIGHT Arrow
            nextItem = $(this).parent().next();
          } else if (keyCode === 37) { // LEFT Arrow
            nextItem = $(this).parent().prev();
          }

          if ( nextItem != null && nextItem.length !== 0) {
            nextItem.children().click();
            nextItem.children().focus();
          }
        });

        $(".tab-list").click(function (){
          $("#tab-group a[tabindex=0]").attr("tabindex",-1).attr("aria-selected",false);
          $(this).attr("tabindex", 0).attr("aria-selected",true);
        });
      },
      postLoad: function ($html, tabSelector) {
        var tabId = $(tabSelector).attr("id");

        if (tabId == "tab1") {
          ContextProvider.get(function (context) {

            checkInternet($html,
                function () {
                  // Swap the welcome image for the embedded youtube link
                  $(".welcome-img").bind("click keydown", function (event) {
                    if (event.type === "click" ||
                        (event.type === "keydown" && event.keyCode === 13)) { // ENTER
                      var video = HandlebarsCompiler.compile(brightCoveVideoTemplate, {
                        width: "551",
                        height: "310",
                        videoId: context.config.bc_welcome_link_id
                      });

                      $(this).hide();
                      $("#welcome-video").append(video);
                    }
                  });
                });
          });

        } else if (tabId == "tab2") {
          bindCardInteractions($html, ".sample-card", "#sample-details-content", "sample-img", true);

        } else if (tabId == "tab3") {
          checkInternet($html,
              function () {
                bindCardInteractions($html, ".tutorial-card", "#tutorial-details-content", "tutorial-img", true);
              },
              function () {
                bindCardInteractions($html, ".tutorial-card", "#tutorial-details-content", "tutorial-img", false);
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
    cards.bind("click",function (event) {
      var card = $(this);
      if (card.hasClass("selected")) {
        return;
      }

      $(cardSelector + ".selected").removeAttr("tabindex");
      $(cardSelector + ".selected").removeClass("selected");
      card.addClass("selected");
      card.attr("tabindex",0);

      var index = cards.index(card);

      var detailsContentContainer = $(detailsContentSelector);
      detailsContentContainer.find(".detail-title").text(card.find(".card-title").text());
      detailsContentContainer.find(".detail-description").text(card.find(".card-description").text());

      var detailsImg = detailsContentContainer.find(".details-img");
      var buttonContainer = detailsContentContainer.find(".button-container");

      detailsImg.hide();
      buttonContainer.hide();

      detailsImg.attr("id", detailsImgBaseId + (index + 1))

      buttonContainer.fadeIn(500);
      detailsImg.fadeIn(500);


      if (bindNavParams) {
        appendNavParams(jParent, jParent.parent().attr("id"), index);
      }

      // Execute specific on click functions
      if (postClick) {
        postClick(card);
      }
      event.preventDefault();
      event.stopPropagation();
    }).first().click();

    cards.keydown(function (event) {
      let nexItem;
      var keyCode = event.which || event.keyCode;
      if (keyCode === 38){ // UP Arrow
        nexItem = $(this).prev();
      }else if (keyCode === 40){ // DOWN Arrow
        nexItem = $(this).next();
      }else if(keyCode === 13){ // ENTER
        jParent.find(".launch-link").first().click();
      }

      if ( nexItem != null && nexItem.length !== 0 ){
        nexItem.click();
        nexItem.focus();
      }
    });
  }

  /**
   * Locates launch links and creates the appropriate link to select the correct tab and content
   */
  function appendNavParams(jTab, selectedTab, selectedContentIndex) {
    var href = "content/getting_started_launch.html?";
    href += "selectedTab=" + selectedTab;
    href += "&selectedContentIndex=" + selectedContentIndex

    if (typeof jTab == "string") {
      jTab = $(jTab);
    }

    var launchLink = jTab.find(".launch-link");

    launchLink.unbind("click");
    launchLink.bind("click", function () {
      window.open(href, "_blank");
    });
  }

  /**
   * Injects titles and descriptions to be used by tab content later. Takes a context property to store the
   * final array and the templates for the templates and link.
   */
  function injectMessagesArray(contextProperty, messagesTemplate, linkTemplate, idBase) {
    ContextProvider.get(function (context) {
      var i = 1;
      var str;

      // Generate samples array descriptions
      var array = new Array();
      while (str = context.i18n[ HandlebarsCompiler.compile(messagesTemplate, {contentNumber: i++}) ]) {

        // Log error if no supporting link is provided in configuration
        var link = HandlebarsCompiler.compile(linkTemplate, {contentNumber: i - 1});
        if (!context.config[link]) {
          console.error(HandlebarsCompiler.compile(context.i18n.error_propterty_does_not_exist, {link: link}));
          continue;
        }

        var infoArr = str.split("|");
        array.push({
          title: infoArr[0],
          description: infoArr[1],
          id: idBase + (i - 1),
          tooltip: $("<div>" + infoArr[0] + "</div>").text()
        });
      }
      ContextProvider.addProperty(contextProperty, array);
    });
  }

  /**
   * Checks for an internet connection. If successful, the success function is run, otherwise an error message will be displayed
   */
  function checkInternet(jParent, success, fail) {
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

      ContextProvider.get(function (context) {
        if (launchLink.length > 0) {

          launchLink.bind("click", function () {
            errorMsg.text(context.i18n.error_no_internet_access).show();

            setTimeout(function () {
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
    init: init,
    injectMessagesArray: injectMessagesArray,
    checkInternet: checkInternet,
    brightCoveVideoTemplate: brightCoveVideoTemplate
  };
});
