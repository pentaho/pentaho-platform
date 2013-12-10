var deps = [
    'mantle/puc-api/pucAngularPlugin',
    'mantle/puc-api/pucAngularApi',
    'common-ui/AnimatedAngularPluginHandler'
];

pen.define(deps, function(PUCAngularPlugin, PUCAngularApi, AnimatedAngularPluginHandler) {

    describe("PUC Angular Plugin", function() {
        

        describe("Pre-creation of plugin", function() {
            var pluginHandlerInstance;
            beforeEach(function() {
                pluginHandlerInstance = PUCAngularApi.PUCAngularPluginHandlerInstance;
                PUCAngularApi.PUCAngularPluginHandlerInstance = new AnimatedAngularPluginHandler();
            })

            afterEach(function() {
                PUCAngularApi.PUCAngularPluginHandlerInstance = pluginHandlerInstance;
            })

            it("should change the plugin handler instance in the PUC Angular Api", function() {
                expect(function() {
                    new PUCAngularPlugin({});
                }).toThrow(PUCAngularPlugin.errMsgs.incorrectHandlerType);
            })    
        })

        describe("PUC Angular Plugin created", function() {
            var plugin;
            
            beforeEach(function() {
                plugin = new PUCAngularPlugin({});

                spyOn(plugin, "onRegister").andCallThrough();
                spyOn(plugin, "onUnregister").andCallThrough();
            })

            it("should extend the same functions as the Animated Angular Plugin", function() {
                expect(plugin.goto).toBeDefined();
                expect(plugin.goHome).toBeDefined();
                expect(plugin.goNext).toBeDefined();
                expect(plugin.goPrevious).toBeDefined();
                expect(plugin.open).toBeDefined();
                expect(plugin.close).toBeDefined();
            });

            it("should be able to register itself and call the onRegister function", function() {
                plugin.register();
                expect(plugin.onRegister).toHaveBeenCalled();
            })

            it("should register then unregister itself and call the onUnregister function", function() {
                plugin.register();
                plugin.unregister();
                expect(plugin.onUnregister).toHaveBeenCalled();
            })    
        })
    })
})