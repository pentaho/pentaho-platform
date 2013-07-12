pen.define(["common-ui/handlebars"], 
  function(){
  	var templates = {};

    templates.dialogRename = Handlebars.compile(
      "<div id='dialogOverride' class='pentaho-dialog modal hide' tabindex='-1' role='dialog' aria-labelledby='myModalLabel' aria-hidden='true'>"+
          "<div class='Caption'>{{i18n 'overrideTitle'}}</div>"+
          "<div class='dialogContent'>" +
            "<div> {{i18n 'overrideDescription'}}</div>" +
            "<br/>" +
            "<label class='checkbox'> <input type='checkbox'>{{i18n 'overrideCheckbox'}}</input></label>" +
          "</div>" +
          "<div class='modal-footer'>" +
            "<button class='yes pentaho-button'>{{i18n 'overrideYesButton'}}</button>" +
            "<button class='no pentaho-button' data-dismiss='modal' aria-hidden='true'>{{i18n 'overrideNoButton'}}</button>" +
          "</div>" +
      "</div>");

  	//rename dialog
  	templates.dialogDoRename = Handlebars.compile(
  		"<div id='dialogRename' class='pentaho-dialog modal hide' tabindex='-1' role='dialog' aria-labelledby='myModalLabel' aria-hidden='true'>"+
    			"<div class='Caption'>{{i18n 'renameTitle'}}</div>"+
    			"<div class='dialogContent'>" +
            "<p>{{i18n 'renameName'}}</p>" +
      			"<input type='text' value='{{name}}'>" + 
    			"</div>" +
    			"<div class='modal-footer'>" +
      			"<button class='pentaho-button' data-dismiss='modal' aria-hidden='true'>Cancel</button>" +
      			"<button class='ok pentaho-button' data-dismiss='modal' aria-hidden='true'>Ok</button>" +
      		"</div>" +
  		"</div>");

  	return templates;
  }
);