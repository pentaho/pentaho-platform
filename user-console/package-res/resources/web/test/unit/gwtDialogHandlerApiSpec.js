var deps = [
    'mantle/puc-api/gwtDialogHandlerApi',
    'common-ui/angular',
    'common-ui/jquery'
];

pen.define(deps, function(GwtDialogHandlerApi, angular, $) {

    describe("GWT Dialog Handler Api", function() {
                
        it("should register gwt dialog plugin and place it in its route map", function() {
            var id = "test";
            var plugin = GwtDialogHandlerApi.register(id);
            var routeMapJson = GwtDialogHandlerApi.getRouteMapJson(id);

            expect(routeMapJson).toBeDefined();
            expect(routeMapJson.plugin).toEqual(plugin);
        });

        it("should unregister a plugin by its id and remove it from the route map", function() {
            var id = "test1";
            var plugin = GwtDialogHandlerApi.register(id);
            GwtDialogHandlerApi.unregister(id);

            expect(plugin.isRegistered).toBe(false);
            expect(GwtDialogHandlerApi.getRouteMapJson(id)).not.toBeDefined();
        })

        describe("registered plugin", function() {
            var plugin, module, $ele;
            var id = "gwt-dialog-handler-id";

            beforeEach(function() {
                plugin = GwtDialogHandlerApi.register(id);
                module = angular.module(plugin.moduleName);

                angular.bootstrap(null, [plugin.moduleName]);
                
                $ele = $("<div id='test' class='dialog'></div>");
                $("body").append($ele);
            })

            afterEach(function() {
                GwtDialogHandlerApi.unregister(id);
                $ele.remove();
            })

            it("should fail when trying to register a plugin with the same id twice", function() {
                expect(function() {
                    GwtDialogHandlerApi.register(id);
                }).toThrow(id + GwtDialogHandlerApi.errMsgs.dialogIdAlreadyRegistered)
            })

            it("should 'show' the dialog plugin and create a jquery element for the dialog. Angular should also redirect the browser", function() {
                var $dialog = GwtDialogHandlerApi.show(id, $ele[0]);
                var routeMapJson = GwtDialogHandlerApi.getRouteMapJson(id);

                expect($dialog).toBeDefined();
                expect($dialog.hasClass(GwtDialogHandlerApi.fullScreenCssName)).toBe(true);
                expect(module.$location.path()).toMatch(routeMapJson.routeUrl);
                expect(routeMapJson.$parent).toBeDefined();
                expect(routeMapJson.$parent.attr("id")).toEqual($dialog.parent().attr("id"));
            })

            it("should 'hide' the dialog plugin and angular should change the url path back to '/'", function() {
                GwtDialogHandlerApi.show(id, $ele[0]);

                var $dialog = GwtDialogHandlerApi.hide(id, $ele[0]);
                var routeMapJson = GwtDialogHandlerApi.getRouteMapJson(id);

                expect(routeMapJson.$parent).toEqual($dialog.parent());
                expect(module.$location.path()).toMatch("/");
            })
        })
    })
})