var deps = [
	'common-ui/AnimatedAngularPlugin',
	'mantle/puc-api/pucAngularApi',
	'common-ui/ring'
];

define(deps, function(AnimatedAngularPlugin, PUCAngularApi, ring) {

	var PUCAngularPlugin = ring.create([AnimatedAngularPlugin], {
		init : function(config) {
			config.moduleName = PUCAngularApi.moduleName;
			config.pluginHandler = PUCAngularApi.PUCAngularPluginHandlerInstance;

			this.$super(config);

			if (!ring.instance(this.config.pluginHandler, PUCAngularApi.PUCAngularPluginHandler)) {
				throw PUCAngularPlugin.errMsgs.incorrectHandlerType;
			}
		},

		onRegister : function(plugin) {
			this.config.pluginHandler._onRegister.call(plugin, plugin);

			this.$super.call(plugin, plugin);
		},

		onUnregister : function(plugin) {
			this.config.pluginHandler._onUnregister.call(plugin, plugin);

			this.$super.call(plugin, plugin);
		},

		// Have to call because toString exists already in Object
		toString : function() {
			return this.$super();
		}
	});

	PUCAngularPlugin.errMsgs = {};
	PUCAngularPlugin.errMsgs.incorrectHandlerType = "There attached plugin handler is not a PUC Angular Plugin Handler";

	return PUCAngularPlugin;
});