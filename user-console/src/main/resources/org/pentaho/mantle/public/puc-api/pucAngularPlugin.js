var deps = [
	'common-ui/AnimatedAngularPlugin',
	'./pucAngularApi',
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
			this.config.pluginHandler._onRegister(plugin);

			this.$super(plugin);
		},

		onUnregister : function(plugin) {
			this.config.pluginHandler._onUnregister(plugin);

			this.$super(plugin);
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
