<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Home Page</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">

  <!-- Le styles -->
  <link href="bootstrap/css/bootstrap.css" rel="stylesheet">

  <style>
    .widget-panel {
      height: 240px;
    }

    .main-container {
      padding-top: 20px;
      max-width: 1024px;
    }

    .widget-panel.well {
      padding: 0px 19px 19px;
    }

    button.btn-large.btn-block {
      padding-left: 10px;
      padding-right: 10px;
    }

    .nobreak {
      white-space: nowrap;
    }

    .widget-panel.well .content-panel {
      height: 186px;
      overflow-y: auto;
      background: #ffffff;
      min-width: 50px;
    }

    .pointer {
      cursor: pointer;
    }

    .content-icon {
      height: 32px;
      width: 32px;
    }

    .pad-left {
      padding-left: 4px;
    }

    /*spinner gets added dynamically later*/
    .content-panel .spinner {
        margin-left: auto;
        margin-right: auto;
        margin-top: 80px;;
    }

  </style>

  <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
  <!--[if lt IE 9]>
  <script src="bootstrap/js/html5shiv.js"></script>
  <![endif]-->

  <!-- We need web context for requirejs and css -->
  <script type="text/javascript" src="webcontext.js?context=mantle&cssOnly=true"></script>

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
          <button id="btnCreateNew" class="btn btn-large btn-block popover-source" data-toggle="dropdown"
                  data-toggle="popover" data-placement="right" data-html="true" data-container="body">
            {{i18n.create_new}}
          </button>
          <button class="btn btn-large btn-block" onclick="window.parent.executeCommand('ManageDatasourcesCommand')">
            {{i18n.manage_datasources}}
          </button>
          <button class="btn btn-large btn-block" onclick="window.parent.executeCommand('OpenDocCommand')">
            {{i18n.documentation}}
          </button>


          <div style="display:none" id="btnCreateNewContent">
            <button class="btn btn-large btn-block nobreak"
                    onclick="openFile('{{i18n.analyzer_report}}', '{{i18n.analyzer_tooltip}}', 'api/repos/xanalyzer/service/selectSchema');$('#btnCreateNew').popover('hide')">
              {{i18n.analysis_report}}
            </button>
            <button class="btn btn-large btn-block nobreak"
                    onclick="openFile('{{i18n.interactive_report}}', '{{i18n.interactive_report}}', 'api/repos/pentaho-interactive-reporting/prpti.new');$('#btnCreateNew').popover('hide')">
              {{i18n.interactive_report}}
            </button>
            <button class="btn btn-large btn-block nobreak"
                    onclick="openFile('{{i18n.dashboard}}', '{{i18n.dashboard}}', 'api/repos/dashboards/editor');$('#btnCreateNew').popover('hide')">
              {{i18n.dashboard}}
            </button>
          </div>

        </div>
      </script>
    </div>
    <div class="span9" style="overflow:auto">

      <div class="row-fluid">
        <script type="text/x-handlebars-template">
          <div class="well getting-started widget-panel">
            <h3>{{i18n.getting_started_heading}}</h3>
            <ul class="masthead-links">
              <li>
                <a href="#">Link</a>
              </li>
              <li>
                <a href="#">Link</a>
              </li>
              <li>
                <a href="#">Link</a>
              </li>
            </ul>
            <hr class="soften">
          </div>
        </script>
      </div>

      <div class="row-fluid">

        <div class="span6">
          <script id="favoritesTemplate" type="text/x-handlebars-template" delayCompile="true">
            <div class="well widget-panel">
              <h3>
                  {{i18n.favorites}}
                  <i class="icon-refresh pull-right pointer" onclick="loadFavorites();" title="{{i18n.refresh}}"></i>
              </h3>
              <div id="favoritesSpinner"></div>
              <div id="favorites-content-panel" class="content-panel">
                <ul class="nav nav-tabs nav-stacked">
                  {{#eachFavorite favorites}}
                  <li>
                      <a href="#" onclick="openRepositoryFile('{{fullPath}}', 'run')">
                      {{#if xanalyzer}} <img src="images/analyzer.png" class="pull-left content-icon">   {{/if}}
                      {{#if xdash}}     <img src="images/dashboard.png" class="pull-left content-icon">  {{/if}}
                      {{#if xcdf}}      <img src="images/cdf.png" class="pull-left content-icon">        {{/if}}
                      {{#if prpti}}     <img src="images/pir.png" class="pull-left content-icon">        {{/if}}
                      {{#if prpt}}      <img src="images/pir.png" class="pull-left content-icon">        {{/if}}
                      {{#if xaction}}   <img src="images/xaction.png" class="pull-left content-icon">    {{/if}}
                      {{#if url}}       <img src="images/url.png" class="pull-left content-icon">        {{/if}}
                      {{#if html}}      <img src="images/url.png" class="pull-left content-icon">        {{/if}}
                      <span class="pad-left">{{title}}</span>
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
            <div class="well widget-panel">
              <h3>
                  {{i18n.recents}}
                  <i class="icon-refresh pull-right pointer" onclick="loadRecents();" title="{{i18n.refresh}}"></i>
              </h3>
              <div id="recentsSpinner"></div>
              <div id="recents-content-panel" class="content-panel">
                <ul class="nav nav-tabs nav-stacked">
                  {{#eachRecent recent}}
                  <li>
                      <a href="#" onclick="openRepositoryFile('{{fullPath}}', 'run')">
                        {{#if xanalyzer}} <img src="images/analyzer.png" class="pull-left content-icon">   {{/if}}
                        {{#if xdash}}     <img src="images/dashboard.png" class="pull-left content-icon">  {{/if}}
                        {{#if xcdf}}      <img src="images/cdf.png" class="pull-left content-icon">        {{/if}}
                        {{#if prpti}}     <img src="images/pir.png" class="pull-left content-icon">        {{/if}}
                        {{#if prpt}}      <img src="images/pir.png" class="pull-left content-icon">        {{/if}}
                        {{#if xaction}}   <img src="images/xaction.png" class="pull-left content-icon">    {{/if}}
                        {{#if url}}       <img src="images/url.png" class="pull-left content-icon">        {{/if}}
                        {{#if html}}      <img src="images/url.png" class="pull-left content-icon">        {{/if}}
                        <span class="pad-left">{{title}}</span>
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

<script type="text/javascript" src="http://code.jquery.com/jquery.js"></script>
<script type="text/javascript" src="http://platform.twitter.com/widgets.js"></script>
<script type="text/javascript" src="bootstrap/js/bootstrap.js"></script>
<script type="text/javascript" src="handlebars.js"></script>
<script type="text/javascript" src="jquery.i18n.properties-min.js"></script>

<script type="text/javascript">

  // Retrieve Message bundle, then process templates
  jQuery.i18n.properties({
    name: 'messages',
    mode: 'map',
    callback: function () {
      var context = {};

      // one bundle for now, namespace later if needed
      context.i18n = jQuery.i18n.map;

      // Process and inject all handlebars templates, results are parented to the template's parent node.
      $("script[type='text/x-handlebars-template']").each(
          function (pos, node) {
            if(!node.attributes.delayCompile) {
              var source = $(node).html();
              var template = Handlebars.compile(source);
              var html = $.trim(template(context));
              node.parentNode.appendChild($(html)[0])
            }
          });

      // Handle the new popover menu. If we add another, make generic
      $("#btnCreateNew").popover({
        html: true,
        content: function () {
          return $('#btnCreateNewContent').html();
        }
      });
      // setup a listener to hide popovers when a click happens outside of them
      $('body').on('click', function (e) {
        $('.popover-source').each(function () {
          if ($(this).has(e.target).length == 0 && !$(this).is(e.target) && $('.popover').has(e.target).length == 0) {
            $(this).popover('hide');
          }
        });
      });
    }
  });

  function loadFavorites() {
    pen.require(["favorites"], function(Favorites){
      var favorites = new Favorites();
      favorites.load();
    });
  }


  function loadRecents() {
      pen.require(["favorites"], function(Favorites){

        var recents = new Favorites();
        $.extend(recents, {
          name: "recent",
          template: {
              id: "recentsTemplate",
              itemIterator: "eachRecent"
          },
          displayContainerId: "recentsContianer",
          contentPanelId: "recents-content-panel",
          serviceUrl: "api/user-settings/recent",
          spinContainer: "recentsSpinner"
        });

        recents.load();
      });
  }

  function openFile(title, tooltip, fullPath) {
    if(parent.mantle_setPerspective && window.parent.openURL) {
      // show the opened perspective
      parent.mantle_setPerspective('default.perspective');
      window.parent.openURL(title, tooltip, fullPath);
    }
  }

  function openRepositoryFile(path, mode) {
    if(!mode) {
      mode = "edit";
    }
    // show the opened perspective
    parent.mantle_setPerspective('default.perspective');
    window.parent.openRepositoryFile(path, mode);
  }

  /**
   * this gets triggered when the Home perspective becomes active
   */
  function perspectiveActivated() {
    loadFavorites();
    loadRecents();
  }

  loadFavorites();
  loadRecents();

</script>

</body>
</html>
