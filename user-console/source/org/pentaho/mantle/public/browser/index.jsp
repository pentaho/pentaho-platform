<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Browse Files</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">

  <!-- Le styles -->
  <link href="lib/bootstrap/css/bootstrap.css" rel="stylesheet">
  <link href="resources/filebrowser.css" rel="stylesheet">

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

    div.myClass {
    overflow-x: auto;
    white-space: nowrap;
}
div.myClass [class*="span"] {
    display: inline-block;
    float: none; /* Very important */
}
  </style>

  <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
  <!--[if lt IE 9]>
  <script src="bootstrap/js/html5shiv.js"></script>
  <![endif]-->

</head>

<body data-spy="scroll" data-target=".sidebar">


<div class="container-fluid main-container">
  <div id="fileBrowser" class="row-fluid" style="margin-bottom: 30px">
</div>


<!-- libs -->
<script type="text/javascript" src="lib/jquery/jquery-1.9.1.min.js"></script>
<script type="text/javascript" src="lib/jquery/jquery.i18n.properties-min-1.0.9.js"></script>
<script type="text/javascript" src="lib/bootstrap/widgets.js"></script>
<script type="text/javascript" src="lib/bootstrap/js/bootstrap.min.js"></script>
<script type="text/javascript" src="lib/handlebars/handlebars.js"></script>
<script type="text/javascript" src="lib/underscore/underscore-min.js"></script>
<script type="text/javascript" src="lib/backbone/backbone.js"></script>

<!-- resources -->
<script type="text/javascript" src="resources/filebrowser.js"></script>
<script type="text/javascript" src="resources/templates.handlebars.js"></script>




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

      //FileBrowser.init($("#fileBrowser"));
      FileBrowser.setOpenFileHandler(openRepositoryFile);
      FileBrowser.setContainer($("#fileBrowser"));
      FileBrowser.update();
    }
  });

    function openRepositoryFile(path, mode) {
    if(!path) {
      return;
    }
    if(!mode) {
      mode = "edit";
    }

    // show the opened perspective
    parent.mantle_setPerspective('default.perspective');
    window.parent.mantle_openRepositoryFile(path, mode);
  }

</script>

</body>
</html>
