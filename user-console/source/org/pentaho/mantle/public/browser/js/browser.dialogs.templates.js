pen.define(["common-ui/handlebars"], 
  function(){
  	var templates = {};

    templates.dialog = Handlebars.compile(
      "<div id='{{dialog.id}}' class='pentaho-dialog modal hide' tabindex='-1' role='dialog' aria-labelledby='myModalLabel' aria-hidden='true' data-keyboard='true'>" +
          "<div class='header Caption'>" +
            "<div class='header-content'>" +
              "{{{dialog.content.header}}}" +
            "</div>" +
            "<div class='dialog-close-button pentaho-closebutton-big'></div>" +
          "</div>" +          
          "<div class='body dialogContent'>" +
            "<div class='body-content'>{{{dialog.content.body}}}</div>" +
          "</div>" +          
          "<div class='footer modal-footer'>" +
            "{{{dialog.content.footer}}" +
          "</div>" +
      "</div>");

    templates.buttons = Handlebars.compile(
      "<button class='ok pentaho-button'>{{ok}}</button>" +
      "<button class='cancel pentaho-button' data-dismiss='modal' aria-hidden='true'>{{cancel}}</button>"); 	

  	return templates;
  }
);