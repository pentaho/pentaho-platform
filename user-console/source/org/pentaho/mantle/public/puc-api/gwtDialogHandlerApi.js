/*
 * GwtDialogAngularPluginHandler
 * API for handling gwt dialogs that want to become fullscreen
 */

var deps = ['mantle/puc-api/pucAngularPlugin', 'common-ui/jquery'];

define(deps, function(PUCAngularPlugin, $) {

	var routeMap = {};
	var fullScreenCssName = "full-screen";

	var register = function(dialogId) {

		if (routeMap[dialogId]) {
			throw dialogId + errMsgs.dialogIdAlreadyRegistered;
		}

		var routeUrl = "/pentaho/gwt-dialog/" + dialogId;

		var plugin = new PUCAngularPlugin({
			routerCallback : function($routeProvider) {
				$routeProvider
					.when(routeUrl, {
						template : "<div id='" + getDialogContainerId(dialogId) + "' style='width:100%; height: 100%;'></div>"
					})
			}
		}).register();
		
		routeMap[dialogId] = {
			routeUrl : routeUrl,
			$parent : undefined,
			plugin : plugin
		}

		return plugin;
	}

	var unregister = function(dialogId) {
		// Unregister Plugin
		routeMap[dialogId].plugin.unregister();

		// Unregister
		delete routeMap[dialogId];
	}

	// Needs to be called once the dialog has been attached to the dom
	var show = function(dialogId, dialogDomElement) {
		
		var routeMapJson = getRouteMapJson(dialogId);
		
		// Navigate to the appropriate route to inject the content for the angular view
		routeMapJson.plugin.goNext(routeMapJson.routeUrl);
		
		var $dialog = $(dialogDomElement);

		// Find original parent for re-attachment on hide
		routeMap[dialogId]["$parent"] = $dialog.parent();

		
		// Append dialog to the angular container		
  		$("#" + getDialogContainerId(dialogId)).append($dialog.detach());  		

  		// Add full-screen class to dialog
  		$dialog.addClass(fullScreenCssName);

  		return $dialog;
	}

	// Reparents the dialog back into it's previous parent
	var hide = function(dialogId, dialogDomElement) {
		
		var routeMapJson = getRouteMapJson(dialogId);
		
		routeMapJson.plugin.close();

		// Remove style for fullscreen from dom element
		var $dialog = $(dialogDomElement).removeClass(fullScreenCssName);

		// Re-attach dialog to original parent
		routeMapJson.$parent.append($dialog);

		return $dialog;
	}

	// Creates a unique id based off of the provided dialog id
	var getDialogContainerId = function(dialogId) {
		return dialogId + "-angular-dialog-container";
	}

	var getRouteMapJson = function(dialogId) {
		return routeMap[dialogId];
	}

	var errMsgs = {};
	errMsgs.dialogIdAlreadyRegistered = " has already been registered";

	return {
		register : register,
		unregister : unregister,
		show : show,
		hide : hide,
		getRouteMapJson : getRouteMapJson,
		fullScreenCssName : fullScreenCssName,
		errMsgs : errMsgs
	};
});