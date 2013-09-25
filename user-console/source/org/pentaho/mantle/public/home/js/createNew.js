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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

pen.define(["common-ui/jquery-i18n"], function() {

  var local = {
    name: "createNew",
    overlayId: "launch",
    serviceUrlOverlays: "api/plugin-manager/overlays",
    serviceUrlPlugins: "api/plugin-manager/ids",
    buttonTemplate: "<button></button>",
    bootstrapButtonClasses: "btn btn-large btn-block nobreak",
    marketplaceOnClick: "parent.mantle_setPerspective('marketplace.perspective');$('#btnCreateNew').popover('hide')",
    marketplaceButtonText: "More on Marketplace...",
    marketplaceAvaliable: false,


    init: function() {
    },

    getUrlBase: function() {
      if(!this.urlBase) {
        this.urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf("/mantle/home")) + "/";
      }
      return this.urlBase;
    },

    checkPluginMarketplace: function(callback){
      var myself = this;

      $.ajax({
        url: myself.getUrlBase() + myself.serviceUrlPlugins,
        success: function(result){
          if(result["strings"].lastIndexOf("marketplace") > 0){
            myself.marketplaceAvaliable = true;
          }
          myself.getOverlays(callback);
        }, 
        error: function(){
          console.log("Error getting plugin ids");
          myself.getOverlays(callback);
        }
      });
    },

    getOverlays: function(callback){
      var myself = this;
      
      $.ajax({
        url: myself.getUrlBase() + myself.serviceUrlOverlays + "?id=" + myself.overlayId,
        success: function(result){
          callback(result);
        },
        error: function(err){
          console.log("error: " + err);
        }
      });
    },

    buildContents: function(popupCallback /*(contentToAppend*/){
      var myself = this,
          $content = $();

      myself.checkPluginMarketplace(function(result){

        if(result.overlay == undefined){

        } else {
          for(var i = 0; i < result.overlay.length; i++){
            var overlay = result.overlay[i],
                $button = $(myself.buttonTemplate)
                  .attr("id", "plugin"+i)
                  .addClass(myself.bootstrapButtonClasses);

            var overlayButton = $(overlay.source).find("button"),
                buttonId = myself.createName(overlayButton.attr("id"));

            $button.attr("id", buttonId);
            $content.push($button);

            myself.processOverlay(overlay, $button);
          }
        }

        //check logic of only jpivot is installed and add the marketplace link to it
        if($content.length == 1 && myself.marketplaceAvaliable){
          var id = $($content[0]).attr("id").toLowerCase();
          if(id.search("jpivot")>0){
            var $newButton = $(myself.buttonTemplate).
              addClass(myself.bootstrapButtonClasses).
              attr("onclick", myself.marketplaceOnClick).
              text(myself.marketplaceButtonText);

            $content.push($newButton);
          } 
        }

        //call the popup callback!
        popupCallback($content);
      });
    },

    processOverlay: function(overlay, $button){
      var myself = this,
          path = myself.getUrlBase() + overlay.resourceBundleUri;

      jQuery.i18n.properties({
        name: path,
        mode: 'map',
        callback: function () {

          var copiedMap = {};

          // Copy elements and remove elements for next file load
          for (configProp in jQuery.i18n.map) {
            copiedMap[configProp] = jQuery.i18n.map[configProp];
            delete jQuery.i18n.map[configProp];
          }

          var source = overlay.source;
          var matches = myself.getMatches(/\$\{(.*?)\}/g,source);

          //localize button
          for(var i = 0; i < matches.length; i++){
            if(copiedMap[matches[i][1]] != undefined){
              source = source.replace(matches[i][0],copiedMap[matches[i][1]]);
            }
          }

          var $sourceButton = $(source).find("button");

          if($sourceButton.length == 0){
            console.log("Overlay without button to add, please check");
            return;
          }

          $button.text($sourceButton.attr("label"));
          $button.attr("onclick", $sourceButton.attr("command"));
        }

      });
    },

    //first entry with match string and second with the string [${param}, param]
    getMatches: function(myRegexp, string){
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

    createName: function(pluginName){
      return "createNew"+pluginName+"Button";
    }

    
  };

  return local;
});
