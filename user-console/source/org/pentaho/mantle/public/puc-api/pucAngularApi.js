/*
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
	var moduleName = 'PUC-angular-app-wrapper';

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

	var setAnimation = function(anim) {			
		animation = anim;
	}

	var setView = function(view, apply) {
		module.$rootScope.viewContainer = view;

		if (apply) {
			module.$rootScope.$apply();
		}
	}

	var goNext = function(url) {
		setAnimation("slide-left");
		AngularPluginHandler.goto(url);
	}

	var goPrevious = function(url) {
		setAnimation("slide-right");
		AngularPluginHandler.goto(url);
	}

	var close = function() {
		setAnimation("fade");
		AngularPluginHandler.goto("/");
	}

	/*
	 * Make a module and Boostrap the application
	 */ 

	// Create module
	var module = angular.module(moduleName, ['ngRoute', 'ngAnimate']);

	// Set animation actions
	var animation = "slide-left";
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

		$rootScope.$on("$locationChangeSuccess", function(event, current, last) {
			$rootScope.actualLocation = $location.path();

			if (!canSetView) {
				return;
			}

			var hash = window.location.hash;
			
			if(hash == "" || hash == "#" || hash == "#/") {
				setView("PUC");
			} else {
				setView("");
			}
		})

		$rootScope.$watch(function () {
			return $location.path()
		}, function (newLocation, oldLocation) {
	        if($rootScope.actualLocation === newLocation) {
	            // alert('Why did you use history back?');
	        }
	    });

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

	return $.extend({
		PUCAngularPlugin : PUCAngularPlugin,
		goNext : goNext,
		goPrevious : goPrevious,
		close : close
	}, AngularPluginHandler);
})