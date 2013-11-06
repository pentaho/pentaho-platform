/*
 * PUC API for Angular Plugins
 */

// Define Pentaho Puc Plugin Handler
pen.define(['common-ui/AngularPluginHandler', 'common-ui/jquery'], function(AngularPluginHandler) {
	var PUCAngularPlugin = function(routes, controllers, services, onRegister, onUnregister) {
		$.extend(this, new AngularPluginHandler.AngularPlugin("PUC-angular-app-wrapper", routes, controllers, services, 
			[_onRegister, onRegister], [_onUnregister, onUnregister]));
	};

	var _onRegister = function(plugin) {
		// TODO - Add code for PUC which is required after registration - possibly start of loading modal
	}

	var _onUnregister = function(plugin) {
		// TODO - Add code for clean up of PUC view
	}

	return {
		PUCAngularPlugin : PUCAngularPlugin
	};
})