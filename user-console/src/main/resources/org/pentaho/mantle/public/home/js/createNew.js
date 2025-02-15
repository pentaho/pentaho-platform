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


define(["common-ui/jquery-pentaho-i18n"], function (context) {

  var local = {
    name: "createNew",
    overlayId: "launch",
    serviceUrlOverlays: "api/plugin-manager/overlays",
    buttonTemplate: "<button></button>",
    hrTemplate: "<hr style='color: #a7a7a7' />",
    bootstrapButtonClasses: "btn btn-large btn-block nobreak",
    marketplaceOnClick: "parent.mantle_setPerspective('marketplace.perspective');$('#btnCreateNew').popover('hide')",
    marketplaceButtonText: "Add options via Marketplace",
    marketplaceAvaliable: false,


    init: function () {
    },

    getUrlBase: function () {
      if (!this.urlBase) {
        this.urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf("/mantle/home")) + "/";
      }
      return this.urlBase;
    },

    getOverlays: function (callback) {
      var myself = this;

      $.ajax({
        url: myself.getUrlBase() + myself.serviceUrlOverlays + "?id=" + myself.overlayId,
        success: function (result) {
          callback(result);
        },
        error: function (err) {
          console.log("error: " + err);
        }
      });
    },

    buildContents: function (config, popupCallback /*(contentToAppend*/) {
      var myself = this,
          $content = $();

      // retrieve hasDataAccess permission
      var hasDataAccess = false;
      $.ajax({
        url: myself.getUrlBase() + "plugin/data-access/api/permissions/hasDataAccess",
        async: false,
        success: function (result) {
          hasDataAccess = result;
        },
        error: function (err) {
          console.log("error: " + err);
        }
      });

      myself.getOverlays(function (result) {

        if (result.overlay == undefined) {

        } else {

          result.overlay.sort(function(left, right) {
            return (left.priority ? left.priority : 9999) - (right.priority ? right.priority : 9999);
          });

          for (var i = 0; i < result.overlay.length; i++) {
            var overlay = result.overlay[i],
                $button = $(myself.buttonTemplate)
                    .attr("id", "plugin" + i)
                    .addClass(myself.bootstrapButtonClasses);

            myself.processOverlay(overlay, $button);
            var buttonId = $($button).attr("id");
            if (buttonId === 'createNewdatasourceButton') {
              // check permission for createNewdatasourceButton only
              if (hasDataAccess) {
                // $content.push($(myself.hrTemplate)); don't add separator yet
                $content.push($button);
              }
            } else {
              $content.push($button);
            }
          }
        }

        //check logic of only jpivot and create new datasource buttons and add the marketplace button
        if ($content.length == 2 && config.hasMarketplacePlugin && config.canAdminister) {
          var firstId = $($content[0]).attr("id").toLowerCase(),
              secondId = $($content[1]).attr("id").toLowerCase();
          if ( (firstId.search("jpivot") > 0 && secondId.search("datasource") > 0) ||
            (secondId.search("jpivot") > 0 && firstId.search("datasource") > 0)) {
            if (config.i18nMap['marketplace'] != undefined) {
              myself.marketplaceButtonText = config.i18nMap['marketplace'];
            }

            var $newButton = $(myself.buttonTemplate).
                addClass(myself.bootstrapButtonClasses).addClass("marketplace").
                attr("onclick", myself.marketplaceOnClick).
                text(myself.marketplaceButtonText);

            $content.push($newButton);
          }
        }

        //call the popup callback!
        popupCallback($content);
      });
    },

    getUrlVars: function() {
      var vars = {};
      var parts = window.parent.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
          vars[key] = value;
      });
      return vars;
    },

    processOverlay: function (overlay, $button) {
      var myself = this,
          path = myself.getUrlBase() + overlay.resourceBundleUri,
          locale = myself.getUrlVars()["locale"];

      if( !locale ) {
        // look to see if locale is set on the page in a meta tag
        if($("meta[name='locale']")) {
          locale = $("meta[name='locale']").attr("content");
        }
      }

      jQuery.i18n.properties({
        name: path,
        mode: 'map',
        language: locale,
        callback: function () {

          var copiedMap = {};

          // Copy elements and remove elements for next file load
          for (configProp in jQuery.i18n.map) {
            copiedMap[configProp] = jQuery.i18n.map[configProp];
            delete jQuery.i18n.map[configProp];
          }

          var source = overlay.source;
          var matches = myself.getMatches(/\$\{(.*?)\}/g, source);

          //localize button
          for (var i = 0; i < matches.length; i++) {
            if (copiedMap[matches[i][1]] != undefined) {
              source = source.replace(matches[i][0], copiedMap[matches[i][1]]);
            }
          }

          var obj = $.parseXML(source),
              button = obj.getElementsByTagName("button")[0];

          if (button != undefined) {
            var label = button.getAttribute("label"),
                id = button.getAttribute("id"),
                command = button.getAttribute("command");

            if (label == null) console.log("Button created without label");
            if (id == null) console.log("Button created without id");
            if (command == null) console.log("Button created without command");

            $button.text(label)
                .attr("id", myself.createName(id))
                .attr("onclick", command);

          } else {
            console.log("Overlay without button to add, please check");
          }
        }
      });
    },

    //first entry with match string and second with the string [${param}, param]
    getMatches: function (myRegexp, string) {
      var content = [];
      var match = myRegexp.exec(string);
      while (match != null) {
        content.push(match);
        // matched text: match[0]
        // match start: match.index
        // capturing group n: match[n]
        match = myRegexp.exec(string);
      }

      return content;
    },

    createName: function (pluginName) {
      return "createNew" + pluginName + "Button";
    }


  };

  return local;
});
