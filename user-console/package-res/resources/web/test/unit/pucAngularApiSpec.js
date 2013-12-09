var deps = [
    'mantle/puc-api/pucAngularApi',
    'mantle/puc-api/pucAngularPlugin',
    'common-ui/angular'
];

pen.define(deps, function(PUCAngularApi, PUCAngularPlugin, angular) {

    describe("PUC Angular API", function() {

        describe("Plugin Handler Instance", function() {

            it("should verify the plugin handler instance has been created", function() {
                expect(PUCAngularApi.PUCAngularPluginHandlerInstance).toBeDefined();
            })

            it("should verify the plugin handler extends the animated angular plugin handler", function() {
                var pluginHandler = PUCAngularApi.PUCAngularPluginHandlerInstance;
                expect(pluginHandler.goNext).toBeDefined();
                expect(pluginHandler.goPrevious).toBeDefined();
                expect(pluginHandler.close).toBeDefined();
                expect(pluginHandler.open).toBeDefined();
            })
        })

        describe("Functionality", function() {
            var module, pluginHandler, plugin;

            beforeEach(function() {
                module = angular.module(PUCAngularApi.moduleName);
                pluginHandler = PUCAngularApi.PUCAngularPluginHandlerInstance;
                plugin = new PUCAngularPlugin({})

                spyOn(pluginHandler, '_onRegister').andCallThrough();
                spyOn(pluginHandler, '_onUnregister').andCallThrough();
            })

            it("should have defined an angular module", function() {
                expect(module).toBeDefined();
            })

            it("should start with the view container set to 'PUC'", function() {
                expect(module.$rootScope.viewContainer).toBe("PUC");
            })

            it("should change the location and change the view container to 'ngView'", function() {
                pluginHandler.goto("test", PUCAngularApi.moduleName);
                expect(module.$rootScope.viewContainer).toBe("ngView");
            })

            it("should register a plugin and call the private onRegister function", function() {
                pluginHandler.register(plugin);
                expect(pluginHandler._onRegister).toHaveBeenCalled();
            })

            it("should register then unregister a plugin and call the private onUnregister function", function() {
                pluginHandler.register(plugin);
                pluginHandler.unregister(plugin);
                expect(pluginHandler._onUnregister).toHaveBeenCalled();
            })

            it("should change the path of the window back to '/' when bootstapping the document", function() {
                module.$location.path("test");

                expect(module.$location.path()).toBe("/test");

                angular.bootstrap(null, [PUCAngularApi.moduleName]);

                expect(module.$location.path()).toBe("/");
            })
        })
    })
});