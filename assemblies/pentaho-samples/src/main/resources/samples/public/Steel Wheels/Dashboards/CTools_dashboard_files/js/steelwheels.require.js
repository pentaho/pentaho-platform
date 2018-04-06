define([
  "cdf/lib/CCC/protovis-compat!",
  "cdf/lib/CCC/pvc",
  "cdf/lib/jquery"
], function(pv, pvc, $) {

  /**
   * @typedef {{
   *   "Classic Cars": string,
   *   "Vintage Cars": string,
   *   "Motorcycles": string,
   *   "Trucks and Buses": string,
   *   "Trains": string
   *  }} ProductLinesColorMap
   */
  var PRODUCT_LINES_COLOR_MAP = {
    "Classic Cars":     "#005CA7",
    "Vintage Cars":     "#3E83B7",
    "Motorcycles":      "#5C9FBC",
    "Trucks and Buses": "#66C2A5",
    "Trains":           "#22B573"
  };

  /**
   * The `steelwheels` namespace contains a collection of utility functions.
   *
   * @name pentaho.steelwheels
   * @namespace
   */
  var steelwheels = /** @lends pentaho.steelwheels */{
    // region Properties
    /**
     * Gets or sets the value of Product Lines' color map
     *
     * @type {ProductLinesColorMap}
     */
    _productLines_colorMap: PRODUCT_LINES_COLOR_MAP,

    get productLines_colorMap() {
      return this._productLines_colorMap;
    },

    set productLines_colorMap(colorMap) {
      this._productLines_colorMap = colorMap;
    },
    // endregion

    // region MISC functions && settings
    /**
     * Used to format the KPI numbers.
     *
     * @param {string} nStr
     *
     * @return {string}
     */
    addCommas: addCommas,

    /**
     * Show or hide elements (charts), depending which measure we choose.
     *
     * @param {HTMLElement} clickedButton - The click event target.
     */
    changePerspective: changePerspective,

    /**
     * Shows the active container.
     */
    startPerspective: startPerspective,

    /**
     * Used by Treemap chart, to simulate the "colorMap" property.
     * (this property exists in the other charts, but currently it is not working for Treemap)
     *
     * [See Related Jira]{@link http://jira.pentaho.com/browse/CDF-452}
     *
     * @param {ProductLinesColorMap} colorMap
     *
     * @return {object} - The mapped color scheme.
     */
    __createMappedColorScheme: createMappedColorScheme,
    // endregion

    // region Charts
    /**
     * Styling the baseAxis labels
     */
    yearLabelsOptions: yearLabelsOptions,

    /**
     * Styling of the barChart tooltips.
     *
     * @param {string} prefix - value prefix symbol.
     */
    kpiTooltipOptions: kpiTooltipOptions,

    /**
     * Styling of barChart territory tooltips.
     */
    kpiTerritoryTooltipOptions: kpiTerritoryTooltipOptions,

    /**
     * Bar Chart Properties
     * (this function is called on the preExecution of the barCharts)
     */
    barChartOptions: barChartOptions,

    /**
     * Highlight all the bar (category) when hovering a series
     * (this function is called on the preExecution of the barCharts)
     */
    barChartHoverableOptions: barChartHoverableOptions,

    /**
     * Sunburst Chart Properties
     * (this function is called on the preExecution of the sunburstChart)
     */
    sunburstChartOptions: sunburstChartOptions,

    /**
     * Styling of Sunburst Chart Tooltips.
     *
     * @param {string} prefix - value prefix symbol.
     */
    sunburstTooltipOptions: sunburstTooltipOptions,

    /**
     * Treemap Chart Properties.
     */
    treemapChartOptions: treemapChartOptions,

    /**
     * Styling of Treemap Tooltips.
     */
    treemapTooltipOptions: treemapTooltipOptions
    // endregion
  };

  $(document).ready(function() {
    steelwheels.startPerspective();
  });

  return steelwheels;

  // ----------------

  /** @inheritDoc */
  function createMappedColorScheme(colorMap) {

    return function mappedColorScheme() { // colorScheme / color scale factory
      var scale = function(key) {
        return pv.color(colorMap[key] || "transparent");
      };

      return pv.copyOwn(scale, pv.Scale.common);
    };
  }

  /** @inheritDoc */
  function addCommas(nStr) {
    nStr += "";

    var x = nStr.split(".");

    var x1 = x[0];
    var x2 = x.length > 1 ? "." + x[1] : "";

    var rgx = /(\d+)(\d{3})/;

    while(rgx.test(x1)) {
      x1 = x1.replace(rgx, "$1,$2");
    }

    x2 = x2.slice(0, 3);

    return x1 + x2;
  }

  /** @inheritDoc */
  function changePerspective(clickedButton) {
    var activeButton = $(".kpiColumn button.active");
    var clickedName = clickedButton.closest(".radioButtonObj")
      .attr("id")
      .split("ButtonObj")[0];

    var clickedContainer = $("#" + clickedName + "CenterRow");
    var activeContainer = $(".centerRow.active");

    var activeClass = "active";
    activeContainer.removeClass(activeClass);
    activeButton.removeClass(activeClass);

    clickedButton.addClass(activeClass);
    clickedContainer.addClass(activeClass);

    var fade = 300;
    activeContainer.fadeOut(fade, function() {
      clickedContainer.fadeIn(fade);
    });
  }

  /** @inheritDoc */
  function startPerspective() {
    var activeContainer = $(".centerRow.active");

    activeContainer.show();
  }

  /** @inheritDoc */
  function barChartOptions() {
    var cd = this.chartDefinition;

    // Main options
    cd.margins = 0;

    // Visual options
    cd.plotFrameVisible = false;
    cd.legend = false;
    cd.baseAxisBandSizeRatio = 1;
    cd.barStackedMargin = 0;

    cd.colorMap = steelwheels.productLines_colorMap;

    // Axis
    cd.axisLabel_font = "lighter 9px 'Open Sans'";

    // Ortho axis
    cd.orthoAxisVisible = false;
    cd.orthoAxisGrid = true;
    cd.orthoAxisGrid_strokeStyle = "#FFF";

    // Base axis
    cd.baseAxisGrid = false;
    cd.baseAxisRule_strokeStyle = "#CCC";
    cd.baseAxisLabel_textStyle = "#666";
    cd.baseAxisTooltipEnabled = false;

    cd.hoverable = true;
    cd.bar_strokeStyle = null;
  }

  /** @inheritDoc */
  function barChartHoverableOptions() {
    var cd = this.chartDefinition;

    cd.bar_fillStyle = function(scene) {
      var color = this.delegate();

      if(color) {
        var activeScene = scene.active();

        if(activeScene && scene.getCategory() === activeScene.getCategory()) {
          color = color.brighter(0.5);
        }
      }

      return this.finished(color);
    };
  }

  /** @inheritDoc */
  function sunburstChartOptions(colorMap) {
    var cd = this.chartDefinition;

    cd.colorMap = steelwheels.productLines_colorMap;
    cd.valuesVisible = false;
    cd.width = 598;
    cd.slice_strokeStyle = pvc.finished("white");
    cd.slice_lineWidth = pvc.finished(1);
    cd.colorAxisSliceBrightnessFactor = 0;
    cd.hoverable = true;

    // Also Highlight the parent when we are hovering a child
    cd.slice_fillStyle = function(scene) {
      var c = this.delegate();

      return c && scene.isActiveDescendantOrSelf()
        ? this.finished(c.brighter(0.5)) : c;
    };
  }

  /** @inheritDoc */
  function treemapChartOptions() {
    var cd = this.chartDefinition;

    cd.valuesVisible = false;
    cd.width = 598;
    cd.legend = false;
    cd.colorMode = "bySelf";
    cd.colors = steelwheels.__createMappedColorScheme(steelwheels.productLines_colorMap);
    cd.colorRole = "category2";
    cd.ascendant_lineWidth = pvc.finished(3);
    cd.ascendant_strokeStyle = pvc.finished("#FFF");
    cd.leaf_lineWidth = pvc.finished(0.5);
    cd.leaf_strokeStyle = pvc.finished("#FFF");
    cd.hoverable = true;

    // Mdify the on-hover highlighting effect (change the bright)
    cd.leaf_fillStyle = function(scene) {
      if(scene.isActive) {
        return this.finished(this.delegate().brighter(0.5));
      }

      return this.delegate();
    };
  }

  /** @inheritDoc */
  function yearLabelsOptions() {
    var cd = this.chartDefinition;

    cd.baseAxisTickFormatter = function(value/*, label*/) {
      return "Q" + value.substring(8, 9);
    };

    cd.baseAxisLabel_textAlign = "center";
    cd.baseAxisOverlappedLabelsMode = "leave";
  }

  /** @inheritDoc */
  function kpiTooltipOptions(prefix) {
    var cd = this.chartDefinition;

    cd.tooltipArrowVisible = false;
    cd.tooltipFollowMouse = true;
    cd.tooltipOpacity = 1;

    cd.tooltipFormat = function() {
      var series = this.getSeriesLabel();
      var lineClass = series.split(" ")[0];

      var category = this.getCategory();
      var year = category.substring(0, 4);
      var quarter = category.substring(8, 9);

      return __createTooltipContainer({
        title: series,
        subTitle: year + ", Q" + quarter,
        css: lineClass.toLowerCase(),
        prefix: prefix,
        value: steelwheels.addCommas(this.getValue().toFixed(0))
      });
    };
  }

  /** @inheritDoc */
  function kpiTerritoryTooltipOptions() {
    var cd = this.chartDefinition;

    cd.tooltipArrowVisible = false;
    cd.tooltipFollowMouse = true;
    cd.tooltipOpacity = 1;

    cd.tooltipFormat = function() {
      var category = this.getCategory();

      var series = this.getSeries();
      var lineClass = series.split(" ")[0];

      return __createTooltipContainer({
        title: series,
        subTitle: category,
        css: lineClass.toLowerCase(),
        prefix: "$",
        value: steelwheels.addCommas(this.getValue().toFixed(0))
      });
    };
  }

  /** @inheritDoc */
  function sunburstTooltipOptions(prefix) {
    var cd = this.chartDefinition;

    cd.tooltipArrowVisible = false;
    cd.tooltipFollowMouse = true;
    cd.tooltipOpacity = 1;

    cd.tooltipFormat = function() {
      var scene = this.scene;
      var atoms = scene.atoms;
      var group = scene.group;

      var category = atoms.category.label;
      var lineClass = category.split(" ")[0];

      return __createTooltipContainer({
        title: category,
        subTitle: group.depth === 2 ? atoms.category2.label : null,
        css: lineClass.toLowerCase(),
        prefix: prefix,
        value: steelwheels.addCommas(scene.getSize().toFixed(0))
      });
    };
  }

  /** @inheritDoc */
  function treemapTooltipOptions() {
    var cd = this.chartDefinition;

    cd.tooltipArrowVisible = false;
    cd.tooltipFollowMouse = true;
    cd.tooltipOpacity = 1;

    cd.tooltipFormat = function() {
      var region = this.scene.atoms.category.label;

      var line = this.getCategory();
      var lineClass = line.split(" ")[0];

      return __createTooltipContainer({
        title: line,
        subTitle: region,
        css: lineClass.toLowerCase(),
        prefix: "$",
        value: steelwheels.addCommas(this.getSize().toFixed(0))
      });
    };
  }

  function __createTooltipContainer(options) {
    var title = "<div class='tooltipTitle'>" + options.title + "</div>";

    var subTitle = "";
    if(options.subTitle != null) {
      subTitle = "<div class='tooltipSubtitle'>" + options.subTitle + "</div>";
    }

    var value = "<div class='tooltipValue " + options.css + "'>" + options.prefix + options.value + "</div>";

    return "<div class='tooltipContainer'>" + title + subTitle + value + "</div>";
  }

});
