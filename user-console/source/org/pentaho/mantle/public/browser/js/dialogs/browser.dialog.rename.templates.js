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

      templates.dialogFileOverride = Handlebars.compile(
          "<div id='override-description'> {{i18n 'overrideDescription_file'}}</div>" +
              "<p class='checkbox'> <input id='do-not-show' type='checkbox'>{{i18n 'overrideCheckbox'}}</input></p>");

      templates.dialogFolderOverride = Handlebars.compile(
          "<div id='override-description'> {{i18n 'overrideDescription_folder'}}</div>" +
              "<p class='checkbox'> <input id='do-not-show' type='checkbox'>{{i18n 'overrideCheckbox'}}</input></p>");

      //rename dialog
      templates.dialogDoRename = Handlebars.compile(
          "<p>{{i18n 'renameName'}}</p>" +
              "<input id='rename-field' type='text'>");

      return templates;
    }
);
