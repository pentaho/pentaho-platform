pen.define(["home/favorites"], function(Favorites) {

  var local = {
    recents: undefined,
    favorites: undefined,
    i18nMap: undefined,
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
              console.log(that.i18nMap.error_could_not_get_favorites + " -- " + err);
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
      this._registerCallbacks();
      this.i18nMap = config.i18nMap;
      this.favoritesConfig.disabled = config.favoritesDisabled;
      this.favoritesConfig.i18nMap = config.i18nMap;
      this.recentsConfig.disabled = config.recentsDisabled;
      this.recentsConfig.i18nMap = config.i18nMap;
      this.refreshAll();
    },

    refreshAll: function() {
      this.loadFavorites();
      this.loadRecents();
    },

    loadFavorites: function(/*Optional|Function*/callback) {
      this.favorites = new Favorites();
      $.extend(this.favorites, this.favoritesConfig);
      this.favorites.load(callback);
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
    markRecentAsFavorite: function(fullPath, title) {
      this.recents.markFavorite(fullPath, title);
      this.favoritesActionSource = 'recents';
    },
    unmarkRecentAsFavorite: function(fullPath) {
      this.recents.unmarkFavorite(fullPath);
      this.favoritesActionSource = 'recents';
    },
    unmarkFavorite: function(fullPath) {
      this.favorites.unmarkFavorite(fullPath);
      this.favoritesActionSource = 'favorites';
    },

    onRecentsChanged: function() {
      this.loadRecents();
    },
    onFavoritesChanged: function() {
      this.loadFavorites($.proxy(this._possiblyReloadRecents, this));
    },

    onFavoritesListRequestEvent: function(event) {
      if(event.eventSubType == 'favoritesListRequest'){
        if(window.parent.mantle_fireEvent){
          var response = {
            eventSubType: 'favoritesListResponse',
            stringParam: JSON.stringify(this.favorites.currentItems)
          };
          window.parent.mantle_fireEvent('GenericEvent', response);
        }
      }
    },

    _possiblyReloadRecents: function() {
      if(this.favoritesActionSource && this.favoritesActionSource == 'favorites') {
        var that = this;
        // check our recents, if they any are marked as favorite and not in the favorites list... refresh the recents
        var recentItems = this.recents.getCurrentItems();
        $.each(recentItems, function(idx, recent) {
          if(that.recents.isItemAFavorite(recent.fullPath)) {
            if(that.favorites.indexOf(recent.fullPath) < 0) {
              that.loadRecents();
              return false;
            }
          }
        });
      } else {
        // recents widget was the originator of the favorites action, refresh it
        this.loadRecents();
      }

      this.favoritesActionSource = undefined;
    },

    _registerCallbacks: function() {
      if(window.parent.mantle_addHandler) {
        window.parent.mantle_addHandler("FavoritesChangedEvent", this.onFavoritesChanged.bind(this));
        window.parent.mantle_addHandler("RecentsChangedEvent", this.onRecentsChanged.bind(this));

        // expose current favorites list as an event-bus "service"
        window.parent.mantle_addHandler("GenericEvent", this.onFavoritesListRequestEvent.bind(this));
      }

    }

};


  var controller = function(config) {
    this.init(config);
  };

  controller.prototype = local;
  return controller;
});