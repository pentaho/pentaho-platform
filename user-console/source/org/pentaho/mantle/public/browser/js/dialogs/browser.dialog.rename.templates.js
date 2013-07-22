pen.define(["common-ui/handlebars"], 
  function(){
  	var templates = {};

    templates.dialogOverride = Handlebars.compile(      
      "<div id='override-description'> {{i18n 'overrideDescription'}}</div>" +
      "<p class='checkbox'> <input id='do-not-show' type='checkbox'>{{i18n 'overrideCheckbox'}}</input></p>");

  	//rename dialog
  	templates.dialogDoRename = Handlebars.compile(  		
      "<p>{{i18n 'renameName'}}</p>" +
			"<input id='rename-field' type='text'>");

  	return templates;
  }
);