pen.define(["favorites"], function(Favorites) {

  var local = {
    recents: undefined,
    favorites: undefined,
    favoritesUrl: "api/user-settings/favorites",

    favoritesConfig: {
      name: "favorites",
      template: {
        id: "favoritesTemplate",
        itemIterator: "eachFavorite"
      },
      displayContainerId: "favoritesContianer",
      contentPanelId: "favorites-content-panel",
      serviceUrl: this.favoritesUrl,
      favoritesUrl: this.favoritesUrl,
      spinContainer: "favoritesSpinner",
      confirmTemplate: {
        id: "confirmationDialogTemplate",
        container: "confirmClearAll",
        message: ""
      },
      disabled: false
    },

    recentsConfig: {
      name: "recent",
      template: {
        id: "recentsTemplate",
        itemIterator: "eachRecent"
      },
      displayContainerId: "recentsContianer",
      contentPanelId: "recents-content-panel",
      serviceUrl: "api/user-settings/recent",
      favoritesUrl: this.favoritesUrl,
      spinContainer: "recentsSpinner",
      confirmTemplate: {
        id: "confirmationDialogTemplate",
        container: "confirmClearAll",
        message: ""
      },
      disabled: false,

      _beforeLoad: function() {
        this.currentItems = undefined;
        this.favoritesList = undefined;
      },

      isItemAFavorite: function(fullPath) {
        if(!this.favoritesList) {
          var now = new Date();
          var that = this;
          $.ajax({
            url: that.getUrlBase() + that.favoritesUrl + "?ts=" + now.getTime(),
            async: false,
            success: function(result) {
              try {
                that.favoritesList = JSON.parse(result);
              } catch(err) {
                that.favoritesList = [];
              }
            },
            error: function(err) {
              console.log("Error getting favorites - " + err);
            }
          });
        }
        var isFave = false;
        $.each(this.favoritesList, function(idx, fave) {
          if(fullPath == fave.fullPath) {
            isFave = true;
            return false; // break the $.each loop
          }
        });
        return isFave;
      },
      getFavorites: function() {
        return this.favoritesList ? this.favoritesList : [];
      }
    },

    init: function(config) {
      this.favoritesConfig.confirmTemplate.message = config.i18nMap.confirmClearFavorites;
      this.favoritesConfig.disabled = config.favoritesDisabled;
      this.recentsConfig.confirmTemplate.message = config.i18nMap.confirmClearRecents;
      this.recentsConfig.disabled = config.recentsDisabled;
      this.refreshAll();
    },

    refreshAll: function() {
      this.loadFavorites();
      this.loadRecents();
    },

    loadFavorites: function() {
      this.favorites = new Favorites();
      $.extend(this.favorites, this.favoritesConfig);
      this.favorites.load();
    },

    loadRecents: function() {
      this.recents = new Favorites();
      $.extend(this.recents, this.recentsConfig);
      this.recents.load();
    },

    clearRecents: function() {
      var recents = new Favorites();
      $.extend(recents, this.recentsConfig);
      recents.clear();
    },
    clearFavorites: function() {
      var favorites = new Favorites();
      $.extend(favorites, this.favoritesConfig);
      favorites.clear($.proxy(this.loadRecents, this));
    },
    markRecentAsFavorite: function(fullPath) {
      this.recents.markFavorite(fullPath, $.proxy(this.loadFavorites, this));
    },
    unmarkRecentAsFavorite: function(fullPath) {
      this.recents.unmarkFavorite(fullPath, $.proxy(this.loadFavorites, this));
    },
    unmarkFavorite: function(fullPath) {
      if(this.recents.indexOf(fullPath) >= 0) {
        // if the favorite is also in the recents list, make sure it get updated too
        this.favorites.unmarkFavorite(fullPath, $.proxy(this.loadRecents, this));
      } else {
        this.favorites.unmarkFavorite(fullPath);
      }
    }

};


  var controller = function(config) {
    this.init(config);
  };

  controller.prototype = local;
  return controller;
});