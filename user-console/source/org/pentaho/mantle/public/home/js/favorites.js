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
    favoritesUrl: "api/user-settings/favorites",
    spinContainer: "favoritesSpinner",

    // assume this will be supplied by the controller via configuration
    i18nMap: undefined,

    helperRegistered: false,

    knownExtensions: {
      xanalyzer: "xanalyzer",
      prpt: "prpt",
      prpti: "prpti",
      xaction: "xaction",
      url: "url",
      xdash: "xdash",
      xcdf: "xcdf",
      html: "html"
    },

    init: function() {
    },

    /**
     * register the Handlebars helper. can't do this on init since we allow post creation extension via $.extend().
     */
    registerHelper: function() {
      if(!this.helperRegistered) {
        var that = this;
        Handlebars.registerHelper(this.template.itemIterator, function(context, options) {
          var ret = "";
          var isEmpty = that.isEmptyList(context);
          for(var i=0, j=context.length; i<j; i++) {
            var repositoryPath = context[i].fullPath;
            if(repositoryPath) {
              var extension = repositoryPath.substr( (repositoryPath.lastIndexOf('.') +1) );
              if(extension && that.knownExtensions[extension]) {
                context[i][extension] = true;
              } else {
                context[i].unknownType = true;
              }
            }
            context[i].isEmpty = isEmpty;
            context[i].isFavorite = that.isItemAFavorite(context[i].fullPath);
            ret = ret + options.fn(context[i]);
          }

          return ret;
        });

        Handlebars.registerHelper("hasItems", function(context, options) {
          if(that.isEmptyList(context)) {
            return options.inverse(this);
          }
          return options.fn(this);
        });

        this.helperRegistered = true;
      }
    },

    isEmptyList: function(context) {
      return (context.length == 0 || (context.length == 1 && context[0].title == this.i18nMap.emptyList));
    },

    load: function(/*Optional|Function*/callback) {
      this._contentRefreshed = callback;
      this._beforeLoad();
      this.registerHelper();
      var context = {};
      context.i18n = this.i18nMap;

      if (this.disabled) {
    	  return;
      }
      
      var that = this;
      this.getContent(function(items) {
        that.showList(items, context);
      });
    },

    _beforeLoad: function() {
      this.currentItems = undefined;
    },

    getUrlBase: function() {
      if(!this.urlBase) {
        this.urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf("/mantle/home")) + "/";
      }
      return this.urlBase;
    },

    getContent: function(/*Function*/ callback) {
      var now = new Date();
      var that = this;
      $.ajax({
        url: that.getUrlBase() + that.serviceUrl + "?ts=" + now.getTime(),

        success: function(result) {
          callback(result)
          if(that._contentRefreshed) {
            that._contentRefreshed();
          }
        },

        error: function(err) {
          console.log(that.i18nMap["error_could_not_get" + that.name] + " - " + err);
        },

        beforeSend: function() {
          that.showWaiting();
        }

      });
    },

    _contentRefreshed: function() {

    },

    doClear: function(callback) {
      var that = this;
      var context = {};
      context.i18n = that.i18nMap;

      $.ajax({
        url: that.getUrlBase() + that.serviceUrl,
        type: 'POST',
        data: [],

        success: function(result) {
          that.showList(result, context);
          if(callback) {
            callback();
          }
        },

        error: function(err) {
          console.log(that.i18nMap["error_could_not_clear_" + that.name] + " - " + err);
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
        try {
          context[this.name] = JSON.parse(items);
          context.isEmpty = context[this.name].length == 0;
          this.currentItems = context[this.name];
          if(context.isEmpty) {
            context[this.name] = [{title: this.i18nMap.emptyList}];
            this.currentItems = [];
          }
        } catch(err) {
          context[this.name] = [{title: this.i18nMap.emptyList}];
          context.isEmpty = true;
          this.currentItems = [];
        }
      } else {
        context[this.name] = [{title: this.i18nMap.emptyList}];
        context.isEmpty = true;
        this.currentItems = [];
      }
      var html = template(context);
      var that = this;
      // make sure the spinner is visible long enough for the user to see it
      setTimeout(function() {
        that.spinner.stop();
        $("#" + that.displayContainerId).html(html);
      }, 100);
    },

    clear: function(callback) {
      this.doClear(callback);
    },

    /**
     * override this for recents to have logic.
     * @param fullPath
     * @returns {boolean}
     */
    isItemAFavorite: function(fullPath) {
      return true;
    },

    getFavorites: function() {
      return this.getCurrentItems();
    },

    getCurrentItems: function() {
      return this.currentItems;
    },

    unmarkFavorite: function(fullPath) {
      //let mantle add the favorite
      if(window.parent.mantle_removeFavorite) {
        window.parent.mantle_removeFavorite(fullPath);
      } else {
        console.log(this.i18nMap.error_could_not_unmark_favorite);
      }
    },

    markFavorite: function(fullPath, title) {
      //let mantle add the favorite
      if(window.parent.mantle_addFavorite) {
        window.parent.mantle_addFavorite(fullPath, title);
      } else {
        console.log(this.i18nMap.error_could_not_mark_favorite);
      }
    },

    indexOf: function(fullPath) {
      var items = this.getCurrentItems();
      var index = -1;
      if(items) {
        $.each(items, function(idx, item) {
          if(item.fullPath == fullPath) {
            index = idx;
            return false;
          }
        });
      }
      return index;
    },
    indexOfFavorite: function(fullPath) {
      var items = this.getFavorites();
      var index = -1;
      if(items) {
        $.each(items, function(idx, item) {
          if(item.fullPath == fullPath) {
            index = idx;
            return false;
          }
        });
      }
      return index;
    }
  };

  var favorite = function(){
    this.init();
  }
  favorite.prototype = local;
  return favorite;

});
