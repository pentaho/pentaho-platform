pen.define(["common-ui/handlebars"], 
  function(){
  	var templates = {};

    templates.dialogOverride = Handlebars.compile(      
      "<div> {{i18n 'overrideDescription'}}</div>" +
      "<p class='checkbox'> <input type='checkbox'>{{i18n 'overrideCheckbox'}}</input></p>");

  	//rename dialog
  	templates.dialogDoRename = Handlebars.compile(  		
      "<p>{{i18n 'renameName'}}</p>" +
			"<input type='text' value='{{name}}'>");

  	return templates;
  }
);