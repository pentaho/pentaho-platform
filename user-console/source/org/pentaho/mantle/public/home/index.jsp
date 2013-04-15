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
      <div class="well sidebar">

        <button class="btn btn-large btn-block">
          Browse Files
        </button>

        <button class="btn btn-large btn-block">
          Create New
        </button>

        <button class="btn btn-large btn-block">
          Manage Data Sources
        </button>

        <button class="btn btn-large btn-block">
          Documentation
        </button>

      </div>

    </div>
    <div class="span9">

      <div class="row-fluid">
        <div class="well getting-started widget-panel">
          <h2>Getting Started with the Pentaho User Console</h2>
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
            <h2>Favorites</h2>
          </div>
        </div>

        <div class="span6">
          <div class="well widget-panel">
            <h2>Recents</h2>
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
