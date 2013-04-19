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
    i18nMap: undefined,
    helperRegistered: false,
    confirmTemplate: {
      id: "confirmationDialogTemplate",
      container: "confirmClearAll",
      message: ""
    },

    init: function() {
      var that = this;

      $.i18n.properties({
        name: 'messages',
        mode: 'map',
        callback: function () {
          that.i18nMap = $.i18n.map;
        }
      });
    },

    /**
     * register the Handlebars helper. can't do this on init since we allow post creation extension via $.extend().
     */
    registerHelper: function() {
      if(!this.helperRegistered) {
        var that = this;
        Handlebars.registerHelper(this.template.itemIterator, function(context, options) {
          var ret = "";

          for(var i=0, j=context.length; i<j; i++) {
            var repositoryPath = context[i].fullPath;
            if(repositoryPath) {
              var extension = repositoryPath.substr( (repositoryPath.lastIndexOf('.') +1) );
              context[i][extension] = true;
            }
            ret = ret + options.fn(context[i]);
          }

          return ret;
        });

        Handlebars.registerHelper("hasItems", function(context, options) {
          if(context.length == 0 || (context.length == 1 && context[0].title == that.i18nMap.emptyList)) {
            return options.inverse(this);
          }
          return options.fn(this);
        });

        this.helperRegistered = true;
      }
    },

    load: function() {
      this.registerHelper();
      var context = {};
      context.i18n = this.i18nMap;

      var that = this;
      this.getContent(function(items) {
        that.showList(items, context);
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
          console.log("Error getting " + that.name + " - " + err);
        },

        beforeSend: function() {
          that.showWaiting();
        }

      });
    },

    doClear: function() {
      var that = this;
      var context = {};
      context.i18n = that.i18nMap;

      $.ajax({
        url: that.getUrlBase() + that.serviceUrl,
        type: 'POST',
        data: [],

        success: function(result) {
          that.showList(result, context);
        },

        error: function(err) {
          console.log("Error clearing " + that.name + " - " + err);
        },

        beforeSend: function() {
          that.showWaiting()
        }

      });
    },

    showWaiting: function() {
      var config = spin.getLargeConfig();
      config.color = "#BBB";
      this.spinner = new Spinner(config);
      var s = this.spinner.spin();
      $("#"+ this.contentPanelId).html(s.el);
    },

    showList: function(items, context) {
      if(!this.template.html) {
        this.template.html = $("#" + this.template.id).html();
      }
      var template = Handlebars.compile(this.template.html);
      if(items.length > 0) {
        context[this.name] = JSON.parse(items);
      } else {
        context[this.name] = [{title: this.i18nMap.emptyList}];
      }
      var html = template(context);
      var that = this;
      // make sure the spinner is visible long enough for the user to see it
      setTimeout(function() {
        that.spinner.stop();
        $("#" + that.displayContainerId).html(html);
      }, 500);
    },

    clear: function() {
      if(!this.confirmTemplate.html) {
        this.confirmTemplate.html = $("#" + this.confirmTemplate.id).html();
      }
      var template = Handlebars.compile(this.confirmTemplate.html);
      var context = {}
      context.i18n = this.i18nMap;
      context.clearMessage = this.confirmTemplate.message;
      context.confirmBtnId = "clear-all-" + this.name;
      var html = template(context);
      $("#" + this.confirmTemplate.container).html(html);

      var that = this;
      $("#" + this.confirmTemplate.container).modal('show');
      $("#clear-all-"+this.name).click(function() {
        // clear the items
        that.doClear();
        // dismiss the dialog
        $("#" + that.confirmTemplate.container).modal('hide');
      });

    }

  };

  var favorite = function(){
    this.init();
  }
  favorite.prototype = local;
  return favorite;

});
