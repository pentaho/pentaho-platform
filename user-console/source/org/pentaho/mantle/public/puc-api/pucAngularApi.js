/*
 * PentahoPluginHandler
 * PUC API for Angular Plugins
 */

// Define Pentaho Puc Plugin Handler
var deps = [
	'common-ui/AnimatedAngularPluginHandler', 
	'common-ui/angular-resource'
];

pen.define(deps, function(AnimatedAngularPluginHandler) {
	var moduleName = 'angular-app-wrapper';

	var Plugin = function(config, onRegister, onUnregister) {
		$.extend(this, new AnimatedAngularPluginHandler.Plugin(moduleName, config, [_onRegister, onRegister], [_onUnregister, onUnregister]));
	};

	var _onRegister = function(plugin) {
		// TODO - Add code for PUC which is required after registration - possibly start of loading modal
	}

	var _onUnregister = function(plugin) {
		// TODO - Add code for clean up of PUC view
	}

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
	
	/*
	 * Make a module and Boostrap the application
	 */ 

	// Create module
	var module = AnimatedAngularPluginHandler.module(moduleName, ['ngResource']);

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

	// Bootstrap the document
	angular.bootstrap(document, [moduleName]);
	
	$(document).ready(function(){
		canSetView = true;
	});

	var returnObj = $.extend({}, AnimatedAngularPluginHandler);

	// Override Plugin provided in AnimatedAngularPluginHandler
	returnObj.Plugin = Plugin;

	return returnObj;
})