/*
 * PentahoPluginHandler
 * PUC API for Angular Plugins
 */

// Define Pentaho Puc Plugin Handler
var deps = [
	'common-ui/AnimatedAngularPluginHandler', 
	'common-ui/jquery',
	'common-ui/angular', 
	'common-ui/angular-route',
	'common-ui/angular-animate'
];

pen.define(deps, function(AnimatedAngularPluginHandler) {
	var moduleName = 'angular-app-wrapper';

	var PUCAngularPlugin = function(routes, controllers, services, onRegister, onUnregister) {
		$.extend(this, new AnimatedAngularPluginHandler.AngularPlugin(moduleName, routes, controllers, services, 
			[_onRegister, onRegister], [_onUnregister, onUnregister]));

		this.goNext = function(url) {
			AnimatedAngularPluginHandler.goNext(url, moduleName);
		}

		this.goPrevious = function(url) {
			AnimatedAngularPluginHandler.goPrevious(url, moduleName);		
		}

		this.goHome = function() {
			AnimatedAngularPluginHandler.goHome(moduleName);
		}
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
	var module = angular.module(moduleName, ['ngRoute', 'ngAnimate']);

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

	// Make the base module pluggable
	AnimatedAngularPluginHandler.makePluggable(module);

	// Bootstrap the document
	angular.bootstrap(document, [moduleName]);
	
	$(document).ready(function(){
		canSetView = true;
	});

	return $.extend({
		PUCAngularPlugin : PUCAngularPlugin		
	}, AnimatedAngularPluginHandler);
})