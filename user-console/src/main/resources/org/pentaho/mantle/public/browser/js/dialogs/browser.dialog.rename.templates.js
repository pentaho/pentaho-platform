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

      templates.dialogFileOverride = Handlebars.compile(
          "<div id='override-description'> {{i18n 'overrideDescription_file'}}</div>" +
              "<p class='checkbox'> <input id='do-not-show' type='checkbox'>{{i18n 'overrideCheckbox'}}</input></p>");

      templates.dialogFolderOverride = Handlebars.compile(
          "<div id='override-description'> {{i18n 'overrideDescription_folder'}}</div>" +
              "<p class='checkbox'> <input id='do-not-show' type='checkbox'>{{i18n 'overrideCheckbox'}}</input></p>");

      //rename dialog
      templates.dialogDoRename = Handlebars.compile(
          "<p>{{i18n 'renameName'}}</p>" +
              "<input id='rename-field' type='text'>" +
              "<p style='margin-top: 15px;'>{{i18n 'renameTitle'}}</p>" +
              "<input id='title-field' type='text' readonly='readonly' style='background-color: #f5f5f5; cursor: not-allowed;'>");

      return templates;
    }
);
