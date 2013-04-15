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

    .sidebar button {
      padding-left: 10px;
      padding-right: 10px;
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
    <div class="span3">
      <div class="well sidebar" data-spy="affix">

        <button class="btn btn-large btn-block" onclick="window.parent.executeCommand('ShowBrowserCommand')">
          Browse Files
        </button>

        <button class="btn btn-large btn-block" data-toggle="dropdown" data-toggle="popover" data-placement="right"
                data-content="Vivamus sagittis lacus vel augue laoreet rutrum faucibus." onclick="$(this).popover()">
          Create New
        </button>


        <button class="btn btn-large btn-block" onclick="window.parent.executeCommand('ManageDatasourcesCommand')">
          Manage Data Sources
        </button>

        <button class="btn btn-large btn-block" onclick="window.parent.executeCommand('OpenDocCommand')">
          Documentation
        </button>

      </div>

    </div>
    <div class="span9" style="overflow:auto">

      <div class="row-fluid">
        <div class="well getting-started widget-panel">
          <h3>Getting Started with the Pentaho User Console</h3>
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
      </div>

      <div class="row-fluid">

        <div class="span6">
          <div class="well widget-panel">
            <h3>Favorites</h3>
          </div>
        </div>

        <div class="span6">
          <div class="well widget-panel">
            <h3>Recents</h3>
          </div>
        </div>

      </div>
    </div>
  </div>
</div>

<script src="http://code.jquery.com/jquery.js"></script>
<script type="text/javascript" src="http://platform.twitter.com/widgets.js"></script>
<script src="bootstrap/js/bootstrap.js"></script>

</body>
</html>
