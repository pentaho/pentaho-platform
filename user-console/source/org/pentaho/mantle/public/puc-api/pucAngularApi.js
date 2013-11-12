/*
 * PentahoPluginHandler
 * PUC API for Angular Plugins
 */

// Define Pentaho Puc Plugin Handler
var deps = [
	'common-ui/AngularPluginHandler', 
	'common-ui/jquery',
	'common-ui/angular', 
	'common-ui/angular-route',
	'common-ui/angular-animate'
];

pen.define(deps, function(AngularPluginHandler) {
	var moduleName = 'angular-app-wrapper';

	var PUCAngularPlugin = function(routes, controllers, services, onRegister, onUnregister) {
		$.extend(this, new AngularPluginHandler.AngularPlugin(moduleName, routes, controllers, services, 
			[_onRegister, onRegister], [_onUnregister, onUnregister]));
	};

	var _onRegister = function(plugin) {
		// TODO - Add code for PUC which is required after registration - possibly start of loading modal
	}

	var _onUnregister = function(plugin) {
		// TODO - Add code for clean up of PUC view
	}

	// Sets the animation to be performed on animation transitions
	var setAnimation = function(anim) {			
		animation = anim;
	}

	// Sets the current rootscope view
	var setView = function(view, apply) {
		module.$rootScope.viewContainer = view;

		if (apply) {
			module.$rootScope.$apply();
		}
	}

	// Sets the animation as slide left and goes to the url
	var goNext = function(url) {
		setAnimation("slide-left");
		AngularPluginHandler.goto(url, moduleName);
	}

	// Sets the animation as slide right and goes to the url
	var goPrevious = function(url) {
		setAnimation("slide-right");
		AngularPluginHandler.goto(url, moduleName);
	}

	// Sets the animatione to fade and goes back to the root application
	var close = function() {
		setAnimation("fade");
		AngularPluginHandler.goHome();
	}
	
	/*
	 * Make a module and Boostrap the application
	 */ 

	// Create module
	var module = angular.module(moduleName, ['ngRoute', 'ngAnimate']);

	// Set animation actions
	var animation = "fade";
	module.animation(".ng-app-view", function() {
		return {
		    enter: function(element, done) {
				return function(cancelled) {
					$(".ng-app-view").attr("animate", animation);
				}
		    }
	    }	
	})
	
	var canSetView = false;	
	module.run(["$rootScope", "$location", function($rootScope, $location) {
		module.$rootScope = $rootScope;

		$rootScope.viewContainer = "PUC";

		// Switches the view container variable based on the location of the url
		$rootScope.$on("$locationChangeSuccess", function(event, current, last) {
			if (!canSetView) {
				return;
			}

			var hash = window.location.hash;
			
			if(hash == "" || hash == "#" || hash == "#/") {
				setView("PUC");
			} else {
				setView("ngView");
			}
		});

		// Provides the navigation controlls to any template
		$rootScope.goNext = goNext;
		$rootScope.goPrevious = goPrevious;
		$rootScope.close = close;
	}]);

	// Make the base module pluggable
	AngularPluginHandler.makePluggable(module);

	// Bootstrap the document
	angular.bootstrap(document, [moduleName]);
	
	// Reset hash
	if (window.location.hash != "") {
		window.location.hash = "";
	}

	$(document).ready(function(){
		canSetView = true;	
	});

	return $.extend({
		PUCAngularPlugin : PUCAngularPlugin,
		goNext : goNext,
		goPrevious : goPrevious,
		close : close
	}, AngularPluginHandler);
})