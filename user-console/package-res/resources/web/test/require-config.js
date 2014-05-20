// Find and inject tests using require
var tests = Object.keys(window.__karma__.files).filter(function (file) {
    return (/Spec\.js$/).test(file);
});

pen = {};
pen.require = function() {
  return require.apply(this, arguments);
} 
pen.define = function() {
  return define.apply(this, arguments);
}


var commonUi = 'build-res/module-scripts/common-ui/resources/web/';
var mantle = 'source/org/pentaho/mantle/public/';

requirejs.config({

  baseUrl: 'base/',
  paths: {
    'common-ui/angular': commonUi + 'angular/angular',
    'common-ui/angular-resource': commonUi + 'angular/angular-resource',
    'common-ui/angular-route': commonUi + 'angular/angular-route',
    'common-ui/angular-ui-bootstrap': commonUi + 'bootstrap/ui-bootstrap-tpls-0.6.0.min',
    'angular-mocks': commonUi + 'angular/angular-mocks',
    'angular-scenario': commonUi + 'angular/angular-scenario',
    'common-ui/underscore' : commonUi + 'underscore/underscore',
    'common-ui/ring':  commonUi + 'ring/ring',

    'common-ui/Plugin' : commonUi + 'plugin-handler/plugin',
    'common-ui/PluginHandler': commonUi + 'plugin-handler/pluginHandler',
    'common-ui/AngularPlugin': commonUi + 'plugin-handler/angularPlugin',
    'common-ui/AngularPluginHandler': commonUi + 'plugin-handler/angularPluginHandler',
    'common-ui/angular-animate': commonUi + 'angular/angular-animate',
    'common-ui/jquery': commonUi + 'jquery/jquery-1.9.1.min',
    'common-ui/AnimatedAngularPlugin': commonUi + 'plugin-handler/animatedAngularPlugin',
    'common-ui/AnimatedAngularPluginHandler': commonUi + 'plugin-handler/animatedAngularPluginHandler',

    'mantle/puc-api/pucAngularApi' : mantle + 'puc-api/pucAngularApi',
    'mantle/puc-api/pucAngularPlugin' : mantle + 'puc-api/pucAngularPlugin',
    'mantle/puc-api/gwtDialogHandlerApi' : mantle + 'puc-api/gwtDialogHandlerApi'
  },

  shim: {
    'common-ui/angular': { exports: 'angular' },
    'common-ui/angular-resource': { deps: ['common-ui/angular'], exports: 'Resource' },
    'common-ui/angular-route': { deps: ['common-ui/angular'], exports: 'Route' },
    'common-ui/angular-ui-bootstrap': { deps: ['common-ui/angular'] },
    'angular-mocks': { deps: ['common-ui/angular-resource'] },
    
    'common-ui/jquery': { exports: '$' },
    'common-ui/PluginHandler': { deps: ['common-ui/jquery'] },
    'common-ui/angular-animate': { deps: ['common-ui/angular'] },
    'common-ui/ring' : {exports: 'ring', deps : ['common-ui/underscore']},
    'common-ui/underscore': { exports: '_' }
  },

  // ask Require.js to load these files (all our tests)
  deps: tests,

  // start test run, once Require.js is done
  callback: function() {
    window.__karma__.start();
  }
});
