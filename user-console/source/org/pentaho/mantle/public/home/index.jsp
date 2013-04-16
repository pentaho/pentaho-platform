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
  </style>

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
                    onclick="window.parent.openURL('{{i18n.analyzer_report}}', '{{i18n.analyzer_tooltip}}', 'api/repos/xanalyzer/service/selectSchema');$('#btnCreateNew').popover('hide')">
              {{i18n.analysis_report}}
            </button>
            <button class="btn btn-large btn-block nobreak"
                    onclick="window.parent.openURL('{{i18n.interactive_report}}', '{{i18n.interactive_report}}', 'api/repos/pentaho-interactive-reporting/prpti.new');$('#btnCreateNew').popover('hide')">
              {{i18n.interactive_report}}
            </button>
            <button class="btn btn-large btn-block nobreak"
                    onclick="window.parent.openURL('{{i18n.dashboard}}', '{{i18n.dashboard}}', 'api/repos/dashboards/editor');$('#btnCreateNew').popover('hide')">
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
          <script type="text/x-handlebars-template">
            <div class="well widget-panel">
              <h3>{{i18n.favorites}}</h3>
            </div>
          </script>
        </div>

        <div class="span6">
          <script type="text/x-handlebars-template">
            <div class="well widget-panel">
              <h3>{{i18n.recents}}</h3>
            </div>
          </script>
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
            var source = $(node).html();
            var template = Handlebars.compile(source);
            var html = template(context);
            node.parentNode.appendChild($(html.trim())[0])
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

</script>

</body>
</html>
