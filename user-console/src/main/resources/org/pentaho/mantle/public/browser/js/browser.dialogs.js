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
 * Copyright (c) 2002-2023 Hitachi Vantara. All rights reserved.
 */

define([
  "./browser.dialogs.templates",
  "./browser.utils",
  "common-ui/bootstrap",
  "common-ui/jquery-pentaho-i18n",
  "common-ui/jquery"
], function (DialogTemplates, BrowserUtils) {

  var dialogs = new Array();

  var refWindow = window.parent,
      $body = $(refWindow.document).find("html body"),
      $container = $body.find(".bootstrap.dialogs");

  // Add container to body once
  if ($container.length == 0) {
    $container = $("<div class='bootstrap dialogs'></div>");
    $container.appendTo($body);
  }

  var local = {

    $dialog: null,

    postShow: null,

    postHide: null,

    isDragging: false,

    // 	config = {
    // 		dialog.id: 				"id",
    // 		dialog.content.header: 	"string" or undefined,
    // 		dialog.content.body: 	"string" or undefined,
    // 		dialog.content.footer: 	"string" or undefined,
    // 		dialog.close_btn: 		boolean (default true)
    // 	}

    init: function (config, postShow, postHide) {
      var me = this;

      this.postShow = postShow;
      this.postHide = postHide;

      // Create dialog from content
      this.$dialog = $(DialogTemplates.dialog(config));
      this.$dialog.find(".footer .cancel").bind("click", function () {
        me.hide.apply(me);
      });

      // Toggle close button
      var closeBtn = this.$dialog.find(".dialog-close-button").bind("click", this.hide);
      if (config.dialog.close_btn === false) {
        closeBtn.detach();
      }

      // Register this dialog and store in global dialogs
      dialogs.push(this);

      // Re-center dialog
      $(window).bind("resize", function () {
        me._center();
      })

      this.$dialog.find(".Caption")
          .bind("mousedown", function (event) {
            var mouseX = event.clientX;
            var mouseY = event.clientY;

            var dialogX = me.$dialog.position().left;
            var dialogY = me.$dialog.position().top;

            me.isDragging = true;
            me.$dialog.unbind("mousemove");

            me.$dialog.bind("mousemove", function (event) {
              var newMouseX = event.clientX;
              var newMouseY = event.clientY;

              if (me.isDragging) {
                me.$dialog.css({
                  left: dialogX + (newMouseX - mouseX),
                  top: dialogY + (newMouseY - mouseY)
                });
              }
            });
          })
          .mouseup(function () {
            this.isDragging = false;
            me.$dialog.unbind("mousemove");
          });

      return this.$dialog;
    },

    show: function () {

      // Hide all other dialogs before showing the next
      for (var index in dialogs) {
        dialogs[index].hide();
      }

      // Opening the JS dialog must be done here:
      // - after above dialogs[i].hide(), so as to capture restored focus from previous dialog.
      // - before below .modal('show'), because it "steals" the focus to the dialog,
      //   thus "hiding" the previous active element.
      //
      // However, autofocus will fail and needs to be repeated later,
      // as the dialog is still hidden at this time.
      var jsDialog = refWindow.pho.util._dialog.create(this.$dialog[0]).open();

      this.$dialog.bind("hidden.pen-browser-dialogs", $.proxy(this._onHidden, this));
      this.$dialog.modal('show');
      this.$dialog.appendTo($container);

      // Repeat, now that dialog is visible.
      jsDialog.autoFocus();

      $(".modal-backdrop").detach().appendTo($container);

      // Center modal within container
      this._center();

      if (this.postShow) {
        this.postShow();
      }

      return this.$dialog;
    },

    hide: function () {
      this.$dialog.modal('hide');
    },

    _onHidden: function() {
      this.$dialog.unbind("hidden.pen-browser-dialogs");

      this.isDragging = false;

      // Must be done after the above modal('hide').
      // When done before, modal('hide') was somehow changing the focus restored by pho.util._dialog.
      var openDialogContext = refWindow.pho.util._dialog.getOpen(this.$dialog[0]);
      if (openDialogContext != null) {
        openDialogContext.close();
      }

      if (this.postHide) {
        this.postHide();
      }
    },

    _center: function () {
      var backdrop = $container.find(".modal-backdrop");

      this.$dialog.css({
        "left": backdrop.width() / 2 - this.$dialog.outerWidth() / 2,
        "top": backdrop.height() / 2 - this.$dialog.outerHeight() / 2
      });
    }
  };

  var Dialog = function (cfg, postShow, postHide, i18n) {
    this.init(cfg, postShow, postHide);
    this.i18n = i18n;
  }

  Dialog.buildCfg = function (id, header, body, footer, close_btn, ariaConfig) {
    var cfg = {};

    cfg.dialog = {};
    cfg.dialog.id = id;
    cfg.dialog.close_btn = close_btn;

    cfg.dialog.content = {};
    cfg.dialog.content.header = header;
    cfg.dialog.content.body = body;
    cfg.dialog.content.footer = footer;

    if (ariaConfig == null) {
      ariaConfig = {};
    }

    cfg.dialog.aria = {
      role: ariaConfig.role || "dialog",
      describedBy: (ariaConfig.role === "alertdialog") ? (ariaConfig.describedBy || (id + "-body")) : undefined
    };

    return cfg;
  };

  Dialog.prototype = local;


  return Dialog;
});
