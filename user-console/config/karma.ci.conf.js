module.exports = function (config) {
  config.set({
    basePath: '../',

    frameworks: ['jasmine', 'requirejs'],

    files: [    
      {pattern: 'source/org/pentaho/mantle/public/**/*.js', included: false},
      {pattern: 'build-res/module-scripts/common-ui/resources/web/angular/**/*.js', included: false},
      {pattern: 'build-res/module-scripts/common-ui/resources/web/bootstrap/**/*.js', included: false},
      {pattern: 'build-res/module-scripts/common-ui/resources/web/plugin-handler/**/*.js', included: false},
      {pattern: 'build-res/module-scripts/common-ui/resources/web/jquery/**/*.js', included: false},
      {pattern: 'build-res/module-scripts/common-ui/resources/web/ring/**/*.js', included: false},
      {pattern: 'build-res/module-scripts/common-ui/resources/web/underscore/**/*.js', included: false},
      {pattern: 'package-res/resources/web/test/unit/**/*.js', included: false},
      'package-res/resources/web/test/require-config.js'
    ],

    // auto run tests when files change
    autoWatch: true,

    // CI mode
    singleRun: true,

    browsers: ['PhantomJS'],
    reporters: ['progress', 'junit', 'coverage'],

    junitReporter: {
      outputFile: 'bin/reports/test/js/test-results.xml',
      suite: 'unit'
    },

    preprocessors: {
      // source files, that you wanna generate coverage for
      // do not include tests or libraries
      // (these files will be instrumented by Istanbul)
      'source/org/pentaho/mantle/public/**/*.js': ['coverage']
    },

    // optionally, configure the reporter
    coverageReporter: {
      type : 'cobertura',
      dir : 'bin/reports/cobertura/xml/'
    },

    // level of logging
    // possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
    logLevel: config.LOG_INFO

  });
};
