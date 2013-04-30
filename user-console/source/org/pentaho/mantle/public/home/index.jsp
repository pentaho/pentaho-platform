<!DOCTYPE html>
<%@page import="org.pentaho.platform.api.engine.IPluginManager"%>
<%@page import="org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction"%>
<%@page import="org.pentaho.platform.engine.core.system.PentahoSessionHolder"%>
<%@page import="org.pentaho.platform.api.engine.IAuthorizationPolicy"%>
<%@page import="org.pentaho.platform.engine.core.system.PentahoSystem"%>
<%@page import="java.util.List"%>
<%
  boolean canCreateContent = PentahoSystem.get(IAuthorizationPolicy.class, PentahoSessionHolder.getSession()).isAllowed(RepositoryCreateAction.NAME);
  List<String> pluginIds = PentahoSystem.get(IPluginManager.class, PentahoSessionHolder.getSession()).getRegisteredPlugins();
%>
<html lang="en" class="bootstrap">
<head>
  <meta charset="utf-8">
  <title>Home Page</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">

  <!-- Le styles -->
  <link href="css/home.css" rel="stylesheet">

  <!-- We need web context for requirejs and css -->
  <script type="text/javascript" src="webcontext.js?context=mantle&cssOnly=true"></script>


  <!-- Require Home -->
  <script type="text/javascript">
    var Home = null;
    pen.require(["home/home"], function(pentahoHome) {
      Home = pentahoHome;

      // Define permissions
      var permissionsMap = {};

      permissionsMap.canCreateContent   = <%=canCreateContent%>;
      permissionsMap.hasAnalyzerPlugin  = <%=pluginIds.contains("analyzer")%>;
      permissionsMap.hasIRPlugin      = <%=pluginIds.contains("pentaho-interactive-reporting")%>;
      permissionsMap.hasDashBoardsPlugin  = <%=pluginIds.contains("dashboards")%>;

      // BISERVER-8631 - Manage datasources only available to roles/users with appropriate permissions
      var serviceUrl = Home.getUrlBase() + "plugin/data-access/api/permissions/hasDataAccess";
      permissionsMap.hasDataAccess = false; // default
      Home.getContent(serviceUrl, function(result){
        permissionsMap.hasDataAccess = result;
        Home.init(permissionsMap); // initialize
      }, function(error){
        console.log(error);
        Home.init(permissionsMap); // log error and initialize anyway
      });

    });
  </script>

  <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
  <!--[if lt IE 9]>
  <script src="bootstrap/js/html5shiv.js"></script>
  <![endif]-->
</head>

<body data-spy="scroll" data-target=".sidebar">
<div class="container-fluid main-container">
  <div class="row-fluid">
    <div class="span3" id="buttonWrapper">
      <script type="text/x-handlebars-template">
        <div class="well sidebar" data-spy="affix">
          <button class="btn btn-large btn-block" onclick="window.top.mantle_setPerspective('browser.perspective')">
            {{i18n.browse}}
          </button>

          <!-- Only show create button if user is allowed -->

          {{#if canCreateContent}}
          <button id="btnCreateNew" class="btn btn-large btn-block popover-source" data-toggle="dropdown"
                  data-toggle="popover" data-placement="right" data-html="true" data-container="body">
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


          <div style="display:none" id="btnCreateNewContent">

            {{#if hasAnalyzerPlugin}}
            <button class="btn btn-large btn-block nobreak"
                    onclick="Home.openFile('{{i18n.analyzer_report}}', '{{i18n.analyzer_tooltip}}', 'api/repos/xanalyzer/service/selectSchema');$('#btnCreateNew').popover('hide')">
              {{i18n.analysis_report}}
            </button>
            {{/if}}

            {{#if hasIRPlugin}}
            <button class="btn btn-large btn-block nobreak"
                    onclick="Home.openFile('{{i18n.interactive_report}}', '{{i18n.interactive_report}}', 'api/repos/pentaho-interactive-reporting/prpti.new');$('#btnCreateNew').popover('hide')">
              {{i18n.interactive_report}}
            </button>
            {{/if}}

            {{#if hasDashBoardsPlugin}}
            <button class="btn btn-large btn-block nobreak"
                    onclick="Home.openFile('{{i18n.dashboard}}', '{{i18n.dashboard}}', 'api/repos/dashboards/editor');$('#btnCreateNew').popover('hide')">
              {{i18n.dashboard}}
            </button>
            {{/if}}
          </div>

        </div>
      </script>
    </div>
    <div class="span9" style="overflow:auto">

      <div class="row-fluid">
        <script type="text/x-handlebars-template">
          <div id="getting-started" class="well getting-started widget-panel">
            <h3>{{i18n.getting_started_heading}}</h3>
            <hr class="soften">
            {{content.getting_started}}
          </div>
        </script>
      </div>

      <div class="row-fluid">

        <div class="span6">
          <script id="recentsTemplate" type="text/x-handlebars-template" delayCompile="true">
            <div id="recents" class="well widget-panel">
              <h3>
                {{i18n.recents}}
              </h3>
              <div id="recentsSpinner"></div>
              {{#if isEmpty}}
              <div class="empty-panel content-panel">
                <div class="centered">
                  <div class="">{{i18n.empty_recents_panel_message}}</div>
                  <button class="pentaho-button" onclick="window.top.mantle_setPerspective('browser.perspective')">{{i18n.browse}}</button>
                </div>
              </div>
              {{else}}
              <div id="recents-content-panel" class="content-panel">
                <ul class="nav nav-tabs nav-stacked">
                  {{#eachRecent recent}}
                  <li>
                    <a href="javascript:Home.openRepositoryFile('{{fullPath}}', 'run')" title='{{title}}'>
                      <div class="row-fluid">
                        <div class="span10 ellipsis">
                          {{#if xanalyzer}}   <img src="images/analyzer.png" class="content-icon">    {{/if}}
                          {{#if xdash}}       <img src="images/dashboard.png" class="content-icon">   {{/if}}
                          {{#if xcdf}}        <img src="images/cdf.png" class="content-icon">         {{/if}}
                          {{#if prpti}}       <img src="images/pir.png" class="content-icon">         {{/if}}
                          {{#if prpt}}        <img src="images/prpt.png" class="content-icon">        {{/if}}
                          {{#if xaction}}     <img src="images/xaction.png" class="content-icon">     {{/if}}
                          {{#if url}}         <img src="images/url.png" class="content-icon">         {{/if}}
                          {{#if html}}        <img src="images/html.png" class="content-icon">        {{/if}}
                          {{#if unknownType}} <img src="images/generic.png" class="content-icon">     {{/if}}
                          <span class="pad-left">{{title}}</span>
                        </div>
                        <div class="span2">
                          {{#unless isEmpty}}
                          {{#if isFavorite}}
                          <img src="images/favorite1.png" title="{{../../../i18n.remove_favorite_tooltip}}" class="pull-right content-icon" onclick="controller.unmarkRecentAsFavorite('{{fullPath}}'); return false;">
                          {{else}}
                          <img src="images/favorite0.png" title="{{../../../i18n.add_favorite_tooltip}}" class="pull-right content-icon" onclick="controller.markRecentAsFavorite('{{fullPath}}', '{{title}}'); return false;">
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

        <div class="span6">
          <script id="favoritesTemplate" type="text/x-handlebars-template" delayCompile="true">
            <div id="favorites" class="well widget-panel">
              <h3>
                {{i18n.favorites}}
              </h3>
              <div id="favoritesSpinner"></div>
              {{#if isEmpty}}
              <div class="empty-panel content-panel">
                <div class="centered">
                  <div class="">{{i18n.empty_favorites_panel_message}}</div>
                  <button class="pentaho-button" onclick="window.top.mantle_setPerspective('browser.perspective')">{{i18n.browse}}</button>
                </div>
              </div>
              {{else}}
              <div id="favorites-content-panel" class="content-panel">
                <ul class="nav nav-tabs nav-stacked">
                  {{#eachFavorite favorites}}
                  <li>
                    <a href="javascript:Home.openRepositoryFile('{{fullPath}}', 'run')" title='{{title}}'>
                      <div class="row-fluid">
                        <div class="span10 ellipsis">
                          {{#if xanalyzer}}   <img src="images/analyzer.png" class="content-icon">   {{/if}}
                          {{#if xdash}}       <img src="images/dashboard.png" class="content-icon">  {{/if}}
                          {{#if xcdf}}        <img src="images/cdf.png" class="content-icon">        {{/if}}
                          {{#if prpti}}       <img src="images/pir.png" class="content-icon">        {{/if}}
                          {{#if prpt}}        <img src="images/prpt.png" class="content-icon">       {{/if}}
                          {{#if xaction}}     <img src="images/xaction.png" class="content-icon">    {{/if}}
                          {{#if url}}         <img src="images/url.png" class="content-icon">        {{/if}}
                          {{#if html}}        <img src="images/html.png" class="content-icon">       {{/if}}
                          {{#if unknownType}} <img src="images/generic.png" class="content-icon">    {{/if}}
                          <span class="pad-left">{{title}}</span>
                        </div>
                        <div class="span2">
                          {{#unless isEmpty}}
                          <img src="images/favorite1.png" title="{{../../i18n.remove_favorite_tooltip}}" class="pull-right content-icon" onclick="controller.unmarkFavorite('{{fullPath}}'); return false;">
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
  </div>
</div>

</body>
</html>
