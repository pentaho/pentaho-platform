/*
 * PentahoPluginHandler
 * PUC API for Angular Plugins
 */

// Define Pentaho PUC Plugin Handler
var deps = [
	'common-ui/AnimatedAngularPluginHandler',
	'common-ui/ring',
	'common-ui/angular-resource'
];

define(deps, function(AnimatedAngularPluginHandler, ring) {

	/*
	 * Define future development extension of AnimatedAngularPluginHandler
	 */
	var PUCAngularPluginHandler = ring.create([AnimatedAngularPluginHandler], {

		_onRegister : function(plugin) {
			this.$super(plugin);

			// TODO - Add code for PUC which is required after registration - possibly start of loading modal
		},

		_onUnregister : function(plugin) {
			this.$super(plugin);

			// TODO - Add code for clean up of PUC view
		}
		
	})

	/*
	 * Make a module and Boostrap the application
	 */ 
	var moduleName = 'angular-app-wrapper';

	// Create module
	var pluginHandler = new PUCAngularPluginHandler();
	var module = pluginHandler.module(moduleName, ['ngResource']);

	// Sets the current rootscope view
	var canSetView = false;
	var setView = function(view) {
		if (!canSetView) {
			return;
		}

		module.$rootScope.viewContainer = view;

		if (!module.$rootScope.$$phase) {
			module.$rootScope.$apply();
		}
	}

	// Provides additional configuratione for the angular wrapper	
	module.run(["$rootScope", "$location", function($rootScope, $location) {
		$rootScope.viewContainer = "PUC";

		// Switches the view container variable based on the location of the url
		$rootScope.$on("$locationChangeSuccess", function(event, current, last) {		

			var hash = $location.path();
			
			if(hash.search(moduleName) > -1) {
				setView("ngView");
			} else {
				setView("PUC");
			}
		});

		// Reset hash
		if ($location.path() != "") {
			$location.path("/");
		}
	}]);

	$(document).ready(function(){
		// Bootstrap the document
		angular.bootstrap(document, [moduleName]);

		canSetView = true;
	});

	return {
		PUCAngularPluginHandler : PUCAngularPluginHandler,
		PUCAngularPluginHandlerInstance : pluginHandler,
		moduleName : moduleName
	}
})