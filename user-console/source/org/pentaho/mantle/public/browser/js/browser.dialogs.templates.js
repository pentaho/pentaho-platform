/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

define(["common-ui/handlebars"],
    function () {
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
          "<div class='button-panel center'>" +
              "<button class='ok pentaho-button'>{{ok}}</button>" +
              "</div>");


      return templates;
    }
);
