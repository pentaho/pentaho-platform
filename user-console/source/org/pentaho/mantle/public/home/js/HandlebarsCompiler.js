/*
 * ******************************************************************************
 * Pentaho
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 */
 pen.define([
 	"common-ui/handlebars", 
 	"common-ui/jquery"
 ], function() {

 	/*
 	 * Loops through all of the scripts of type 'text/x-handlebars-template'. A property of delayCompile=true
 	 * can be appended to these elements as to have them ignored from this compilation process. A post compile 
 	 * function is required as looping over elements and compiling them is quite useless. The compiled content
 	 * and the current script element is passed into the post compile function
 	 */
 	function compileScripts(context, postCompile) {
 		$("script[type='text/x-handlebars-template']:not([delayCompile='true'])").each(function () {
 			var jThis = $(this);
 			postCompile(compile(jThis.html(), context), jThis);
 		});
 	}

 	/*
 	 * Uses handlebars compile string html content. This can be used to compile a single content element,
 	 * provided that a context map is passed in. A post compile function can be provied and the compiled 
 	 * content will be passed in
 	 */
 	function compile(content, context, postCompile) {
      	var template = Handlebars.compile(content);
      	var compiledContent = $.trim(template(context));
      	
      	if (postCompile) {
      		postCompile(compiledContent);
      	}

      	return compiledContent;
 	}

 	return {
 		compileScripts:compileScripts,
 		compile:compile
 	};
});