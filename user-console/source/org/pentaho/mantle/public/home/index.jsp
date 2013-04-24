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

<html lang="en">
<head>
<meta charset="utf-8">
  <title>Home Page</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">

  <!-- Le styles -->
  <link href="twitter/css/bootstrap.css" rel="stylesheet">
  <link href="css/home.css" rel="stylesheet">

  <!-- We need web context for requirejs and css -->
  <script type="text/javascript" src="webcontext.js?context=mantle&cssOnly=true"></script>

  <!-- jQuery -->
  <script type="text/javascript" src="jquery/js/jquery-1.9.1.min.js"></script>
  <script type="text/javascript" src="jquery/js/jquery.i18n.properties-min.js"></script>
  
  <!-- Twitter -->
  <script type="text/javascript" src="twitter/js/widgets.js"></script>
  <script type="text/javascript" src="twitter/js/bootstrap.js"></script>
  
  <!-- Handlebars -->
  <script type="text/javascript" src="handlebars/js/handlebars.js"></script>

  <!-- Require Home -->
  <script type="text/javascript">
  	var Home = null;
  	pen.require(["home/home"], function(pentahoHome) {
  		Home = pentahoHome;
  		
  		// Define permissions
  		var permissionsMap = {};
  	
  		permissionsMap.canCreateContent 	= <%=canCreateContent%>;
  		permissionsMap.hasAnalyzerPlugin 	= <%=pluginIds.contains("analyzer")%>;
  		permissionsMap.hasIRPlugin 			= <%=pluginIds.contains("pentaho-interactive-reporting")%>;
  		permissionsMap.hasDashBoardsPlugin 	= <%=pluginIds.contains("dashboards")%>;

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
          <button class="btn btn-large btn-block" onclick="window.parent.executeCommand('ShowBrowserCommand')">
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
          <script id="favoritesTemplate" type="text/x-handlebars-template" delayCompile="true">
            <div id="favorites" class="well widget-panel">
              <h3>
                  {{i18n.favorites}}
                  <span class="pull-right">
                    {{#unless isEmpty}}
                    <i class="icon-remove-circle pointer" onclick="controller.clearFavorites();" title="{{i18n.clearAllFavorites}}"></i>
                    {{/unless}}
                    <!--<i class="icon-refresh pointer" onclick="controller.loadFavorites();" title="{{i18n.refresh}}"></i>-->
                  </span>
              </h3>
              <div id="favoritesSpinner"></div>
              <div id="favorites-content-panel" class="content-panel">
                <ul class="nav nav-tabs nav-stacked">
                  {{#eachFavorite favorites}}
                  <li>
                    <a href="javascript:Home.openRepositoryFile('{{fullPath}}', 'run')">
                      {{#if xanalyzer}} <img src="images/analyzer.png" class="content-icon">   {{/if}}
                      {{#if xdash}}     <img src="images/dashboard.png" class="content-icon">  {{/if}}
                      {{#if xcdf}}      <img src="images/cdf.png" class="content-icon">        {{/if}}
                      {{#if prpti}}     <img src="images/pir.png" class="content-icon">        {{/if}}
                      {{#if prpt}}      <img src="images/pir.png" class="content-icon">        {{/if}}
                      {{#if xaction}}   <img src="images/xaction.png" class="content-icon">    {{/if}}
                      {{#if url}}       <img src="images/url.png" class="content-icon">        {{/if}}
                      {{#if html}}      <img src="images/url.png" class="content-icon">        {{/if}}
                      <span class="pad-left">{{title}}</span>
                      {{#unless isEmpty}}
                        <img src="images/favorite1.png" class="pull-right content-icon" onclick="controller.unmarkFavorite('{{fullPath}}'); return false;">
                      {{/unless}}
                    </a>
                  </li>
                  {{/eachFavorite}}
                </ul>
              </div>
            </div>
          </script>

          <div id="favoritesContianer"></div>

        </div>

        <div class="span6">
          <script id="recentsTemplate" type="text/x-handlebars-template" delayCompile="true">
            <div id="recents" class="well widget-panel">
              <h3>
                  {{i18n.recents}}
                  <span class="pull-right">
                    {{#unless isEmpty}}
                    <i class="icon-remove-circle pointer" onclick="controller.clearRecents();" title="{{i18n.clearAllRecents}}"></i>
                    {{/unless}}
                    <!--<i class="icon-refresh pointer" onclick="controller.loadRecents();" title="{{i18n.refresh}}"></i>-->
                  </span>
              </h3>
              <div id="recentsSpinner"></div>
              <div id="recents-content-panel" class="content-panel">
                <ul class="nav nav-tabs nav-stacked">
                  {{#eachRecent recent}}
                  <li>
                    <a href="javascript:Home.openRepositoryFile('{{fullPath}}', 'run')">
                      {{#if xanalyzer}} <img src="images/analyzer.png" class="content-icon">   {{/if}}
                      {{#if xdash}}     <img src="images/dashboard.png" class="content-icon">  {{/if}}
                      {{#if xcdf}}      <img src="images/cdf.png" class="content-icon">        {{/if}}
                      {{#if prpti}}     <img src="images/pir.png" class="content-icon">        {{/if}}
                      {{#if prpt}}      <img src="images/pir.png" class="content-icon">        {{/if}}
                      {{#if xaction}}   <img src="images/xaction.png" class="content-icon">    {{/if}}
                      {{#if url}}       <img src="images/url.png" class="content-icon">        {{/if}}
                      {{#if html}}      <img src="images/url.png" class="content-icon">        {{/if}}
                      <span class="pad-left">{{title}}</span>
                      {{#unless isEmpty}}
                        {{#if isFavorite}}
                          <img src="images/favorite1.png" class="pull-right content-icon" onclick="controller.unmarkRecentAsFavorite('{{fullPath}}'); return false;">
                        {{else}}
                          <img src="images/favorite0.png" class="pull-right content-icon" onclick="controller.markRecentAsFavorite('{{fullPath}}'); return false;">
                        {{/if}}
                      {{/unless}}
                    </a>
                  </li>
                  {{/eachRecent}}
                </ul>
              </div>
            </div>
          </script>

          <div id="recentsContianer"></div>
        </div>

      </div>
    </div>
  </div>
</div>


<div id="confirmClearAll" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="confirmLabel" aria-hidden="true">
</div>
<script id="confirmationDialogTemplate" type="text/x-handlebars-template" delayCompile="true">
    <div class="modal-header">
        <a href="#" class="close" data-dismiss="modal">&times;</a>
        <h3 id="confirmLabel">{{i18n.confirm}}</h3>
    </div>
    <div class="modal-body">
        <p>{{clearMessage}}</p>
    </div>
    <div class="modal-footer">
        <button class="pentaho-button" data-dismiss="modal" aria-hidden="true">{{i18n.cancel}}</button>
        <button class="pentaho-button" id="{{confirmBtnId}}">{{i18n.clear}}</button>
    </div>
</script>

</body>
</html>
