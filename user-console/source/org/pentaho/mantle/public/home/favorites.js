/*
 * ******************************************************************************
 * Pentaho
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 */

pen.define(["common-ui/util/PentahoSpinner"], function(spin) {

  var local = {
    name: "favorites",
    template: {
      id: "favoritesTemplate",
      itemIterator: "eachFavorite"
    },
    displayContainerId: "favoritesContianer",
    contentPanelId: "favorites-content-panel",
    serviceUrl: "api/user-settings/favorites",
    spinContainer: "favoritesSpinner",

    load: function() {
      var that = this;
      Handlebars.registerHelper(this.template.itemIterator, function(context, options) {
        var ret = "";

        for(var i=0, j=context.length; i<j; i++) {
          var repositoryPath = context[i].fullPath;
          var extension = repositoryPath.substr( (repositoryPath.lastIndexOf('.') +1) );
          context[i][extension] = true;
          ret = ret + options.fn(context[i]);
        }

        return ret;
      });

      $.i18n.properties({
        name: 'messages',
        mode: 'map',
        callback: function () {
          var context = {};

          context.i18n = $.i18n.map;

          that.getContent(function(items) {
            if(!that.template.html) {
              that.template.html = $("#" + that.template.id).html();
            }
            var template = Handlebars.compile(that.template.html);
            context[that.name] = JSON.parse(items);
            var html = template(context);
            // make sure the spinner is visible long enough for the user to see it
            setTimeout(function() {
              that.spinner.stop();
              $("#" + that.displayContainerId).html(html);
            }, 500);
          });

        }
      });

    },

    getUrlBase: function() {
      if(!this.urlBase) {
        this.urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf("/mantle/home")) + "/";
      }
      return this.urlBase;
    },

    getContent: function(/*Function*/ callback) {
      var settings = [];
      var now = new Date();
      var that = this;
      $.ajax({
        url: that.getUrlBase() + that.serviceUrl + "?ts=" + now.getTime(),

        success: function(result) {
          callback(result)
        },

        error: function(err) {
          console.log("Error getting user settings - " + err);
        },

        beforeSend: function() {
          var config = spin.getLargeConfig();
          config.color = "#BBB";
          that.spinner = new Spinner(config);
          var s = that.spinner.spin();
          $("#"+ that.contentPanelId).html(s.el);
        }

      });

    }

  };

  var favorite = function(){}
  favorite.prototype = local;

  return favorite;

});
