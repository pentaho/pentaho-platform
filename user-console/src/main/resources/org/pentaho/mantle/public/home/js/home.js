/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


define([
  "common-ui/util/HandlebarsCompiler",
  "common-ui/bootstrap",
  "common-ui/jquery", "./gettingStarted", "./createNew"
], function (HandlebarsCompiler, _bootstrap, _jquery, gettingStarted, createNew) {


  function init(context) {
    var favoriteControllerConfig = {
      favoritesDisabled: false,
      recentsDisabled: false,
      i18nMap: context.i18n,

    };

    var createNewConfig = {
      canAdminister: context.canAdminister,
      hasMarketplacePlugin: context.hasMarketplacePlugin,
      i18nMap: context.i18n
    };


    // Set disabled = true for
    var disabledWidgetIdsArr = context.config.disabled_widgets.split(",");
    $.each(disabledWidgetIdsArr, function (index, value) {

      if (value == "favorites") {
        favoriteControllerConfig.favoritesDisabled = true;
      }
      if (value == "recents") {
        favoriteControllerConfig.recentsDisabled = true;
      }
    });

    initFavoritesAndRecents(favoriteControllerConfig);

    // Process and inject all handlebars templates, results are parented to
    // the template's parent node.
    HandlebarsCompiler.compileScripts(context, function (compiledContent, jScriptEle) {
      var html = $(compiledContent);
      var widgetId = html.attr("id");
      if (widgetId && $.inArray(widgetId, disabledWidgetIdsArr) != -1) {
        return;
      }

      jScriptEle.parent().append(html);
    });

    // Require getting-started widget if it has not been disabled
    if ($("#getting-started").length > 0) {
        gettingStarted.init();
    }

    // Handle the new popover menu. If we add another, make generic

      createNew.buildContents(createNewConfig, function ($contents) {
        var result = "";
        for (var i = 0; i < $contents.length; i++) {
          result += $contents[i][0].outerHTML;
        }
        $("#btnCreateNewContent").append($(result));
      });

    $("#btnCreateNew").popover({
      'html': true,
      content: function () {
        return $('#btnCreateNewContent').html();
      }
    });

    $('#btnCreateNew').on('shown.bs.modal', function () {
      $('.popover').find("button").first().focus();
      $('.popover .btn').keydown(function (event) {
        var keyCode = event.which || event.keyCode;
        if (event.shiftKey && keyCode === 9) { // SHIFT + TAB
          var firstButtonId = $('.popover .btn').first().attr("id");
          if (firstButtonId === this.id) {
            $('#btnCreateNew').popover('hide');
            $('#btnCreateNew').focus();
            event.preventDefault();
            event.stopPropagation();
          }
        } else if (keyCode === 27) { //ESC
          $('#btnCreateNew').popover('hide');
          $('#btnCreateNew').focus();
        } else if (keyCode === 9) { // TAB
          var lastButtonId = $('.popover .btn').last().attr("id");
          if (lastButtonId === this.id) {
            $('#btnCreateNew').popover('hide');
            $('#btnCreateNew').next().focus();
            event.preventDefault();
            event.stopPropagation();
          }
        }
      });
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

  $(window.parent.document.body).on('click', function () {
    if ($(".popover-content").length > 0){
      $('#btnCreateNew').popover('hide');
    }
  });

  function openFile(title, tooltip, fullPath) {
    if (parent.mantle_setPerspective && window.parent.openURL) {
      // show the opened perspective
      parent.mantle_setPerspective('opened.perspective');
      window.parent.openURL(title, tooltip, fullPath);
    }
  }

  function openRepositoryFile(path, mode) {
    if (!path) {
      return;
    }
    if (!mode) {
      mode = "edit";
    }

    // show the opened perspective
    var extension = path.split(".").pop();
    var hasPlugin = window.parent.PluginOptionHelper_hasPlugin(path);
    if (window.parent.mantle_isSupportedExtension(extension) && !hasPlugin) {
        var filename = path.split('\\').pop().split('/').pop();
        window.parent.mantle_showPluginError(filename);
        return;
    }

    // force to open pdf files in another window due to issues with pdf readers in IE browsers
    // via class added on themeResources for IE browsers
    if (!($("body").hasClass("pdfReaderEmbeded") && extension == "pdf")) {
      parent.mantle_setPerspective('opened.perspective');
    }
    window.parent.mantle_openRepositoryFile(path, mode);
  }

  function initFavoritesAndRecents(config) {
    require(["home/FavoritesController"], function (FavoritesController) {
      controller = new FavoritesController(config);
    });
  }

  function getUrlBase() {
    return window.location.pathname.substring(0, window.location.pathname.indexOf("/mantle/home")) + "/";
  }

  function getContent(serviceUrl, successCallback, errorCallback, beforeSendCallback) {
    var now = new Date();
    $.ajax({
      url: serviceUrl + "?ts=" + now.getTime(),
      success: function (result) {
        if (successCallback) {
          successCallback(result);
        }
      },
      error: function (err) {
        console.log(err);
        if (errorCallback) {
          errorCallback(err);
        }
      },
      beforeSend: function () {
        if (beforeSendCallback) {
          beforeSendCallback();
        }
      }
    });
  }

  return {
    getUrlBase: getUrlBase,
    getContent: getContent,
    openFile: openFile,
    openRepositoryFile: openRepositoryFile,
    init: init
  }
});

var controller = undefined;

/**
 * this gets triggered when the Home perspective becomes active, must be globally available to get called
 */
function perspectiveActivated() {
  if (controller) {
    controller.refreshAll();
  }
}

