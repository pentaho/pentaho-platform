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

pen.define(["common-ui/jquery-i18n"], function(context) {

  var local = {
    name: "createNew",
    overlayId: "launch",
    serviceUrlOverlays: "api/plugin-manager/overlays",
    buttonTemplate: "<button></button>",
    bootstrapButtonClasses: "btn btn-large btn-block nobreak",
    marketplaceOnClick: "parent.mantle_setPerspective('marketplace.perspective');$('#btnCreateNew').popover('hide')",
    marketplaceButtonText: "Add options via Marketplace",
    marketplaceAvaliable: false,


    init: function() {
    },

    getUrlBase: function() {
      if(!this.urlBase) {
        this.urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf("/mantle/home")) + "/";
      }
      return this.urlBase;
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

    buildContents: function(config, popupCallback /*(contentToAppend*/){
      var myself = this,
          $content = $();

      myself.getOverlays(function(result){

        if(result.overlay == undefined){

        } else {
          for(var i = 0; i < result.overlay.length; i++){
            var overlay = result.overlay[i],
                $button = $(myself.buttonTemplate)
                  .attr("id", "plugin"+i)
                  .addClass(myself.bootstrapButtonClasses);

            myself.processOverlay(overlay, $button);
            $content.push($button);
          }
        }

        //check logic of only jpivot is installed and add the marketplace link to it
        if($content.length == 1 && config.hasMarketplacePlugin && config.canAdminister){
          var id = $($content[0]).attr("id").toLowerCase();
          if(id.search("jpivot")>0){
            if(config.i18nMap['marketplace'] != undefined){
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

          var obj = $.parseXML(source),
              button = obj.getElementsByTagName("button")[0];

          if(button != undefined){
            var label = button.getAttribute("label"),
                id = button.getAttribute("id"),
                command = button.getAttribute("command");

            if(label == null) console.log("Button created without label");
            if(id == null) console.log("Button created without id");
            if(command == null) console.log("Button created without command");

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
