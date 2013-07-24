pen.define(["common-ui/handlebars"], 
  function(){
  	var templates = {};

    templates.dialog = Handlebars.compile(
      "<div id='{{dialog.id}}' class='pentaho-dialog modal' tabindex='-1' role='dialog' aria-labelledby='myModalLabel' aria-hidden='true' data-keyboard='true'>" +
          "<div class='header Caption'>" +
            "<div class='header-content'>" +
              "{{{dialog.content.header}}}" +
            "</div>" +
            "<div class='dialog-close-button pentaho-closebutton-big'></div>" +
          "</div>" +          
          "<div class='body'>" +
            "<div class='dialog-content'>{{{dialog.content.body}}}</div>" +
          "</div>" +          
          "<div class='footer'>" +
            "{{{dialog.content.footer}}" +
          "</div>" +
      "</div>");

    templates.buttons = Handlebars.compile(
      "<div class='button-panel right'>" +
        "<button class='ok pentaho-button'>{{ok}}</button>" +
        "<button class='cancel pentaho-button' data-dismiss='modal' aria-hidden='true'>{{cancel}}</button>" +
      "</div>");

    templates.centered_button = Handlebars.compile(
      "<div class='button-panel center'>"+
        "<button class='ok pentaho-button'>{{ok}}</button>" + 
      "</div>");


  	return templates;
  }
);