/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


define(["common-ui/handlebars"],
    function () {
      var templates = {};

      templates.dialog = Handlebars.compile(
          "<div id='{{dialog.id}}' class='pentaho-dialog modal dw-text dmh-content responsive' tabindex='-1' role='{{dialog.aria.role}}' " +
              "aria-labelledby='{{dialog.id}}-header' aria-describedby='{{dialog.aria.describedBy}}' aria-hidden='true' " +
              "aria-modal='true' data-keyboard='true'>" +
              "<div>" +
              "<div class='header Caption'>" +
              "<div id='{{dialog.id}}-header' class='header-content'>" +
              "{{html dialog.content.header}}" +
              "</div>" +
              "<div class='dialog-close-button pentaho-closebutton-big'></div>" +
              "</div>" +
              "<div class='body'>" +
              "<div id='{{dialog.id}}-body' class='dialog-content'>{{html dialog.content.body}}</div>" +
              "</div>" +
              "<div class='footer'>" +
              "{{html dialog.content.footer}}" +
              "</div>" +
              "</div>" +
              "</div>");

      templates.buttons = Handlebars.compile(
          "<div class='button-panel right'>" +
              "<button class='ok pentaho-button'>{{ok}}</button>" +
              "<button class='cancel pentaho-button' data-dismiss='modal' aria-hidden='true'>{{cancel}}</button>" +
              "</div>");

      templates.centered_button = Handlebars.compile(
          "<div class='button-panel center'>" +
              "<button class='ok pentaho-button'>{{ok}}</button>" +
              "</div>");


      return templates;
    }
);
