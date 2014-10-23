<%--
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
--%>

<!DOCTYPE html>
<%@page import="org.pentaho.platform.api.engine.IAuthorizationPolicy" %>
<%@page import="org.pentaho.platform.api.engine.IPluginManager" %>
<%@page import="org.pentaho.platform.engine.core.system.PentahoSessionHolder" %>
<%@page import="org.pentaho.platform.engine.core.system.PentahoSystem" %>
<%@page import="org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction" %>
<%@page import="org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction" %>
<%@page import="java.util.List" %>
<%@page import="java.util.Locale"%>
<%@page import="javax.servlet.http.HttpServletRequest"%>
<%
  boolean canCreateContent = PentahoSystem.get( IAuthorizationPolicy.class, PentahoSessionHolder.getSession() )
      .isAllowed( RepositoryCreateAction.NAME );
  boolean canAdminister = PentahoSystem.get( IAuthorizationPolicy.class, PentahoSessionHolder.getSession() )
      .isAllowed( AdministerSecurityAction.NAME );
  List<String> pluginIds =
      PentahoSystem.get( IPluginManager.class, PentahoSessionHolder.getSession() ).getRegisteredPlugins();
  Locale locale = request.getLocale();
%>
<html lang="en" class="bootstrap">
<head>
  <meta charset="utf-8">
  <title>Home Page</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="locale" content="<%=locale.toString()%>">

  <!-- Le styles -->
  <link href="css/home.css" rel="stylesheet">

  <!-- We need web context for requirejs and css -->
  <script type="text/javascript" src="webcontext.js?context=mantle&cssOnly=true"></script>
  <script language='JavaScript' type='text/javascript' src='http://admin.brightcove.com/js/BrightcoveExperiences.js'></script>

  <!-- Avoid 'console' errors in browsers that lack a console. -->
  <script type="text/javascript">
    if (!(window.console && console.log)) {
      (function () {
        var noop = function () {
        };
        var methods = ['assert', 'debug', 'error', 'info', 'log', 'trace', 'warn'];
        var length = methods.length;
        var console = window.console = {};
        while (length--) {
          console[methods[length]] = noop;
        }
      }());
    }
  </script>

  <!-- Require Home -->
  <script type="text/javascript">
    var Home = null;
    require(["home/home", 
      "common-ui/util/ContextProvider"], function (pentahoHome, ContextProvider) {
      Home = pentahoHome;

      // Define properties for loading context
      var contextConfig = [
        {
          path: "properties/config",
          post: function (context, loadedMap) {
            context.config = loadedMap;
          }
        },
        {
          path: "properties/messages",
          post: function (context, loadedMap) {
            context.i18n = loadedMap;
          }
      }];

      // Define permissions
      ContextProvider.addProperty("canCreateContent", <%=canCreateContent%>);
      ContextProvider.addProperty("canAdminister", <%=canAdminister%>);
      ContextProvider.addProperty("hasAnalyzerPlugin", <%=pluginIds.contains("analyzer")%>);
      ContextProvider.addProperty("hasIRPlugin", <%=pluginIds.contains("pentaho-interactive-reporting")%>);
      ContextProvider.addProperty("hasDashBoardsPlugin", <%=pluginIds.contains("dashboards")%>);
      ContextProvider.addProperty("hasMarketplacePlugin", <%=pluginIds.contains("marketplace")%>);
      ContextProvider.addProperty("hasDataAccess", false); // default

      // BISERVER-8631 - Manage datasources only available to roles/users with appropriate permissions
      var serviceUrl = Home.getUrlBase() + "plugin/data-access/api/permissions/hasDataAccess";
      Home.getContent(serviceUrl, function (result) {
        ContextProvider.addProperty("hasDataAccess", result);
        ContextProvider.get(Home.init, contextConfig); // initialize
      }, function (error) {
        console.log(error);
        ContextProvider.get(Home.init, contextConfig); // log error and initialize anyway
      });

    });
  </script>
</head>

<body data-spy="scroll" data-target=".sidebar">
<div class="container-fluid main-container">
  <div class="row-fluid">
    <div class="span3" id="buttonWrapper">


      <div class='row-fluid'>
        <script type="text/x-handlebars-template">
          <div class="well sidebar">
            <button class="btn btn-large btn-block" onclick="window.top.mantle_setPerspective('browser.perspective')">
              {{i18n.browse}}
            </button>

            <!-- Only show create button if user is allowed -->

            {{#if canCreateContent}}
            <button id="btnCreateNew" class="btn btn-large btn-block popover-source" data-toggle="dropdown"
                data-toggle="popover" data-placement="right" data-html="true" data-id="my_hid" data-container="body" onclick="preCreatePopover();">
              {{i18n.create_new}}
            </button>
            {{/if}}

            {{#if hasDataAccess}}
            <button class="btn btn-large btn-block" onclick="window.parent.executeCommand('ManageDatasourcesCommand')">
              {{i18n.manage_datasources}}
            </button>
            {{/if}}

            <button class="btn btn-large btn-block" onclick="window.parent.executeCommand('OpenDocCommand')">
              {{i18n.documentation}}
            </button>
          </div>

          <div style="display:none" id="btnCreateNewContent"></div>
        </script>

      </div>


      <div class="row-fluid">
        <div class='span12'>
          <script id="recentsTemplate" type="text/x-handlebars-template" delayCompile="true">
            <div id="recents" class="well widget-panel">
              <h3>
                {{i18n.recents}}
              </h3>

              <div id="recentsSpinner"></div>
              {{#if isEmpty}}
              <div class="empty-panel content-panel">
                <div class="centered">
                  <div class="empty-message">{{i18n.empty_recents_panel_message}}</div>
                  <button class="pentaho-button" onclick="window.top.mantle_setPerspective('browser.perspective');">{{i18n.browse}}</button>
                </div>
              </div>
              {{else}}
              <div id="recents-content-panel" class="content-panel">
                <ul class="nav nav-tabs nav-stacked">
                  {{#eachRecent recent}}
                  <li>
                    <a href="javascript:Home.openRepositoryFile('{{escapeQuotes fullPath}}', 'run')" title='{{title}}'>
                      <div class="row-fluid">
                        <div class="span10 ellipsis">
                          {{#if xanalyzer}} <i class="pull-left content-icon file-xanalyzer"/> {{/if}}
                          {{#if xdash}} <i class="pull-left content-icon file-xdash"/> {{/if}}
                          {{#if xcdf}} <i class="pull-left content-icon file-xcdf"/> {{/if}}
                          {{#if prpti}} <i class="pull-left content-icon file-prpti"/> {{/if}}
                          {{#if ktr}} <i class="pull-left content-icon file-ktr"/> {{/if}}
                          {{#if prpt}} <i class="pull-left content-icon file-prpt"/> {{/if}}
                          {{#if xaction}} <i class="pull-left content-icon file-xaction"/> {{/if}}
                          {{#if url}} <i class="pull-left content-icon file-url"/> {{/if}}
                          {{#if html}} <i class="pull-left content-icon file-html"/> {{/if}}
                          {{#if cda}} <i class="pull-left content-icon file-cda"/> {{/if}}
                          {{#if wcdf}} <i class="pull-left content-icon file-wcdf"/> {{/if}}
                          {{#if unknownType}} <i class="pull-left content-icon file-unknown"/> {{/if}}
                          <span class="pad-left">{{title}}</span>
                        </div>
                        <div class="span2">
                          {{#unless isEmpty}}
                          {{#if isFavorite}}
                          <i title="{{../../../i18n.remove_favorite_tooltip}}" class="pull-right favorite-on" onclick="controller.unmarkRecentAsFavorite('{{escapeQuotes fullPath}}'); return false;"/>
                          {{else}}
                          <i title="{{../../../i18n.add_favorite_tooltip}}" class="pull-right favorite-off" onclick="controller.markRecentAsFavorite('{{escapeQuotes fullPath}}', '{{escapeQuotes title}}'); return false;"/>
                          {{/if}}
                          {{/unless}}
                        </div>
                      </div>
                    </a>
                  </li>
                  {{/eachRecent}}
                </ul>
              </div>
              {{/if}}
            </div>
          </script>

          <div id="recentsContianer"></div>
        </div>
      </div>


      <div class="row-fluid">


        <div class="span12">
          <script id="favoritesTemplate" type="text/x-handlebars-template" delayCompile="true">
            <div id="favorites" class="well widget-panel">
              <h3>
                {{i18n.favorites}}
              </h3>

              <div id="favoritesSpinner"></div>
              {{#if isEmpty}}
              <div class="empty-panel content-panel">
                <div class="centered">
                  <div class="empty-message">{{i18n.empty_favorites_panel_message}}</div>
                  <button class="pentaho-button" onclick="window.top.mantle_setPerspective('browser.perspective')">{{i18n.browse}}</button>
                </div>
              </div>
              {{else}}
              <div id="favorites-content-panel" class="content-panel">
                <ul class="nav nav-tabs nav-stacked">
                  {{#eachFavorite favorites}}
                  <li>
                    <a href="javascript:Home.openRepositoryFile('{{escapeQuotes fullPath}}', 'run')" title='{{title}}'>
                      <div class="row-fluid">
                        <div class="span10 ellipsis">
                          {{#if xanalyzer}} <i class="pull-left content-icon file-xanalyzer"/> {{/if}}
                          {{#if xdash}} <i class="pull-left content-icon file-xdash"/> {{/if}}
                          {{#if xcdf}} <i class="pull-left content-icon file-xcdf"/> {{/if}}
                          {{#if prpti}} <i class="pull-left content-icon file-prpti"/> {{/if}}
                          {{#if prpt}} <i class="pull-left content-icon file-prpt"/> {{/if}}
                          {{#if ktr}} <i class="pull-left content-icon file-ktr"/> {{/if}}
                          {{#if xaction}} <i class="pull-left content-icon file-xaction"/> {{/if}}
                          {{#if url}} <i class="pull-left content-icon file-url"/> {{/if}}
                          {{#if html}} <i class="pull-left content-icon file-html"/> {{/if}}
                          {{#if unknownType}} <i class="pull-left content-icon file-unknown"/> {{/if}}
                          <span class="pad-left">{{title}}</span>
                        </div>
                        <div class="span2">
                          {{#unless isEmpty}}
                          <i title="{{../../../i18n.remove_favorite_tooltip}}" class="pull-right favorite-on" onclick="controller.unmarkRecentAsFavorite('{{escapeQuotes fullPath}}'); return false;"/>
                          {{/unless}}
                        </div>
                      </div>
                    </a>
                  </li>
                  {{/eachFavorite}}
                </ul>
              </div>
              {{/if}}
            </div>
          </script>

          <div id="favoritesContianer"></div>

        </div>


      </div>


    </div>
    <div class="span9" style="overflow:visible">

      <div class="row-fluid welcome-container">

        <iframe src="content/welcome/index.html" class='welcome-frame' frameborder="0" scrolling="no"></iframe>

      </div>

    </div>
  </div>
</div>
  <script type="text/javascript">
		
		var popup_init = false;
		
		function preCreatePopover(){
			if(!popup_init){
				var tmp = $.fn.popover.Constructor.prototype.show; 
				$.fn.popover.Constructor.prototype.show = function () {
				  tmp.call(this);

				  //Keep the popover from running off the screen
				  var offset = 5;
				  var top = this.$element.offset().top;
				  var height =  this.$element.outerHeight();
				  var topOffset = top-offset;
				  $('.popover').css('top', topOffset+"px");
				  $('.arrow').css('top', offset + height / 2);

				  if (!$('.popover-title').html()) 
						$('.popover-title').hide();
				}; 
				popup_init = true;
			}
		}
  
  </script>
</body>
</html>
