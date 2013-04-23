pen.define([], function() {
	
	function init(permissions) {
		var controller = undefined;

		// Retrieve configuration properites
		jQuery.i18n.properties({
			name: 'properties/config',
			mode: 'map'
		});

		// Retrieve Message bundle, then process templates
		jQuery.i18n.properties({
			name: 'properties/messages',
			mode: 'map',
			callback: function () {

				var context = {};
				
				for (permission in permissions) {
					context[permission] = permissions[permission];
				}

				// one bundle for now, namespace later if needed
				context.i18n = jQuery.i18n.map;

				var favoriteControllerConfig = {
						favoritesDisabled: false,
						recentsDisabled: false,
						i18nMap: jQuery.i18n.map
				};

				// Set disabled = true for
				var disabledWidgetIdsArr = jQuery.i18n.map.disabled_widgets.split(",");
				$.each(disabledWidgetIdsArr, function(index, value) {
 		
					if (value == "favorites") {
						favoriteControllerConfig.favoritesDisabled = true;
					} else if (value == "recents") {
						favoriteControllerConfig.recentsDisabled = true;
					}
				});

				initFavoritesAndRecents(favoriteControllerConfig);

				// Process and inject all handlebars templates, results are parented to
				// the template's parent node.
				$("script[type='text/x-handlebars-template']:not([delayCompile='true'])").each(
						function (pos, node) {	
							var source = $(node).html();
							var template = Handlebars.compile(source);
							var html = $($.trim(template(context)));

							var widgetId = html.attr("id");
							if (widgetId && $.inArray(widgetId, disabledWidgetIdsArr) != -1){
								return;
							}
	
							node.parentNode.appendChild(html[0])
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
	}
	
	function openFile(title, tooltip, fullPath) {
		if(parent.mantle_setPerspective && window.parent.openURL) {
			// show the opened perspective
			parent.mantle_setPerspective('default.perspective');
			window.parent.openURL(title, tooltip, fullPath);
		}
	}

	function openRepositoryFile(path, mode) {
		if(!path) {
			return;
		}
		if(!mode) {
			mode = "edit";
		}

		// show the opened perspective
		parent.mantle_setPerspective('default.perspective');
		window.parent.openRepositoryFile(path, mode);
	}

	function initFavoritesAndRecents(config) {
		// TODO Do not use path
		pen.require(["js/FavoritesController"], function(FavoritesController){
			controller = new FavoritesController(config);
		});
	}

	/**
	 * this gets triggered when the Home perspective becomes active
	 */
	function perspectiveActivated() {
		controller.refreshAll();
	}
	
	return {
		openFile:openFile,
		openRepositoryFile:openRepositoryFile,
		init:init
	}
});