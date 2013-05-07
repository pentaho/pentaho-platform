pen.define([
  "common-ui/bootstrap",
  "common-ui/handlebars",
  "common-ui/jquery-i18n",
  "common-ui/jquery"
], function() {


  function init(permissions) {
    // Retrieve configuration properites
    jQuery.i18n.properties({
      name: 'properties/config',
      mode: 'map'
    });

    // Retrieve Message bundle, then process templates
    jQuery.i18n.properties({
      name: 'properties/messages',
      mode: 'map',
      callback: function () {

        var context = {};
        context.content = {};

        for (permission in permissions) {
          context[permission] = permissions[permission];
        }

        // one bundle for now, namespace later if needed
        context.i18n = jQuery.i18n.map;

        var favoriteControllerConfig = {
          favoritesDisabled: false,
          recentsDisabled: false,
          i18nMap: jQuery.i18n.map
        };

        // Set disabled = true for
        var disabledWidgetIdsArr = jQuery.i18n.map.disabled_widgets.split(",");
        $.each(disabledWidgetIdsArr, function(index, value) {

          if (value == "favorites") {
            favoriteControllerConfig.favoritesDisabled = true;
          } else if (value == "recents") {
            favoriteControllerConfig.recentsDisabled = true;
          }
        });

        initFavoritesAndRecents(favoriteControllerConfig);

        // Process and inject all handlebars templates, results are parented to
        // the template's parent node.
        $("script[type='text/x-handlebars-template']:not([delayCompile='true'])").each(
            function (pos, node) {
              var source = $(node).html();
              var template = Handlebars.compile(source);
              var html = $($.trim(template(context)));

              var widgetId = html.attr("id");
              if (widgetId && $.inArray(widgetId, disabledWidgetIdsArr) != -1){
                return;
              }

              node.parentNode.appendChild(html[0])
        });

        // Require getting-started widget if it has not been disabled
        if ($("#getting-started").length > 0) {
          pen.require(["home/gettingStarted"], function(gettingStarted) {
            gettingStartedWidget = gettingStarted;
            gettingStartedWidget.init(context);
          });
        }

        // Handle the new popover menu. If we add another, make generic
        $("#btnCreateNew").popover({
          html: true,
          content: function () {
            return $('#btnCreateNewContent').html();
          }
        });

        // setup a listener to hide popovers when a click happens outside of them
        $('body').on('click', function (e) {
          $('.popover-source').each(function () {
            if ($(this).has(e.target).length == 0 && !$(this).is(e.target) && $('.popover').has(e.target).length == 0) {
              $(this).popover('hide');
            }
          });
        });
      }
    });
  }

  function openFile(title, tooltip, fullPath) {
    if(parent.mantle_setPerspective && window.parent.openURL) {
      // show the opened perspective
      parent.mantle_setPerspective('opened.perspective');
      window.parent.openURL(title, tooltip, fullPath);
    }
  }

  function openRepositoryFile(path, mode) {
    if(!path) {
      return;
    }
    if(!mode) {
      mode = "edit";
    }

    // show the opened perspective
    parent.mantle_setPerspective('opened.perspective');
    window.parent.mantle_openRepositoryFile(path, mode);
  }

  function initFavoritesAndRecents(config) {
    pen.require(["home/FavoritesController"], function(FavoritesController){
      controller = new FavoritesController(config);
    });
  }

  function getUrlBase(){
    return window.location.pathname.substring(0, window.location.pathname.indexOf("/mantle/home")) + "/";
  }

  function getContent(serviceUrl, successCallback, errorCallback, beforeSendCallback) {
    var now = new Date();
    $.ajax({
      url: serviceUrl + "?ts=" + now.getTime(),
      success: function(result) {
        if(successCallback){
          successCallback(result);
        }
      },
      error: function(err) {
        console.log(err);
        if(errorCallback){
          errorCallback(err);
        }
      },
      beforeSend: function() {
        if(beforeSendCallback){
          beforeSendCallback();
        }
      }
    });
  }

  var gettingStartedWidget = null;
  function getGettingStartedWidget() {
    return gettingStartedWidget;
  }

  return {
    getGettingStartedWidget:getGettingStartedWidget,
    getUrlBase:getUrlBase,
    getContent:getContent,
    openFile:openFile,
    openRepositoryFile:openRepositoryFile,
    init:init
  }
});

var controller = undefined;

/**
 * this gets triggered when the Home perspective becomes active, must be globally available to get called
 */
function perspectiveActivated() {
  if(controller) {
    controller.refreshAll();
  }
}

