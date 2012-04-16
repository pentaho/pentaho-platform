/*
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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 *
 * Created Mar 03, 2012
 * @author Ezequiel Cuellar
 */

package org.pentaho.mantle.client.admin;

import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.ui.xul.MantleXul;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.gwt.tags.GwtConfirmBox;
import org.pentaho.ui.xul.gwt.tags.GwtMessageBox;
import org.pentaho.ui.xul.util.XulDialogCallback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.Widget;

public class EmailAdminPanelController extends EmailAdminPanel implements ISysAdminPanel, UpdatePasswordController {

  private boolean isDirty = false;

  private static EmailAdminPanelController instance = new EmailAdminPanelController();

  public static EmailAdminPanelController getInstance() {
    return instance;
  }

  private JsEmailConfiguration emailConfig;

  private EmailAdminPanelController() {
    super();

    editPasswordButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(final ClickEvent clickEvent) {
        ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog(EmailAdminPanelController.this);
        changePasswordDialog.show();
      }
    });

    testButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(final ClickEvent clickEvent) {
        testEmail();
      }
    });

    authenticationCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(final ValueChangeEvent<Boolean> booleanValueChangeEvent) {
        emailConfig.setAuthenticate(booleanValueChangeEvent.getValue());
        isDirty = true;
        validate();
      }
    });

    smtpHostTextBox.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(final KeyUpEvent keyUpEvent) {
        emailConfig.setSmtpHost(smtpHostTextBox.getText());
        isDirty = true;
        validate();
      }
    });

    portTextBox.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(final KeyUpEvent keyUpEvent) {
        emailConfig.setSmtpPort(Short.parseShort(portTextBox.getValue()));
        isDirty = true;
        validate();
      }
    });

    fromAddressTextBox.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(final KeyUpEvent keyUpEvent) {
        emailConfig.setDefaultFrom(fromAddressTextBox.getText());
        isDirty = true;
        validate();
      }
    });

    userNameTextBox.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(final KeyUpEvent keyUpEvent) {
        emailConfig.setUserId(userNameTextBox.getText());
        isDirty = true;
        validate();
      }
    });


    authenticationCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(final ValueChangeEvent<Boolean> booleanValueChangeEvent) {
        emailConfig.setAuthenticate(authenticationCheckBox.getValue());
        isDirty = true;
        validate();
      }
    });

    debuggingCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(final ValueChangeEvent<Boolean> booleanValueChangeEvent) {
        emailConfig.setDebug(debuggingCheckBox.getValue());
        isDirty = true;
        validate();
      }
    });

    useSSLCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(final ValueChangeEvent<Boolean> booleanValueChangeEvent) {
        emailConfig.setUseSsl(useSSLCheckBox.getValue());
        isDirty = true;
        validate();
      }
    });

    useStartTLSCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(final ValueChangeEvent<Boolean> booleanValueChangeEvent) {
        emailConfig.setUseStartTls(useStartTLSCheckBox.getValue());
        isDirty = true;
        validate();
      }
    });

    protocolsListBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(final ChangeEvent changeEvent) {
        emailConfig.setSmtpProtocol(protocolsListBox.getItemText(protocolsListBox.getSelectedIndex()));
        isDirty = true;
        validate();
      }
    });

    activate();


    ((Button) saveButton.getManagedObject()).addClickHandler(new ClickHandler() {
      @Override
      public void onClick(final ClickEvent clickEvent) {
        setEmailConfig();
      }
    });

    passwordTextBox.getManagedObject().addFocusListener(new FocusListener() {
      @Override
      public void onFocus(Widget sender) {
      }

      @Override
      public void onLostFocus(Widget sender) {
        if (!StringUtils.isEmpty(passwordTextBox.getValue())) {
          editPasswordButton.setEnabled(true);
          passwordTextBox.getManagedObject().setEnabled(false);
          isDirty = true;
          validate();
        }
      }

    });
  }

  public void updatePassword(String password) {
    passwordTextBox.setValue(password);
    if (!StringUtils.isEmpty(passwordTextBox.getValue())) {
      passwordTextBox.getManagedObject().setEnabled(false);
    }
    isDirty = true;
    validate();
  }

  private void validate() {
    boolean smtpValid = !StringUtils.isEmpty(emailConfig.getSmtpProtocol());
    boolean fromAddressValid = isValidEmail(emailConfig.getDefaultFrom());
    boolean portValid = emailConfig.getSmtpPort() != null && isPortValid(emailConfig.getSmtpPort().toString());
    boolean authenticationValid = true;
    if (emailConfig.isAuthenticate()) {
      boolean userNameValid = !StringUtils.isEmpty(emailConfig.getUserId());
      boolean passwordValid = !StringUtils.isEmpty(emailConfig.getPassword());
      authenticationValid = userNameValid && passwordValid;
    }

    if (portValid && smtpValid && fromAddressValid && authenticationValid) {
      actionBar.expand(1);
      testButton.setEnabled(true);
    } else {
      actionBar.collapse(1);
      testButton.setEnabled(false);
    }
  }

  // -- Remote Calls.

  private void setEmailConfig() {
    saveButton.inProgress(true);
    String serviceUrl = GWT.getHostPageBaseURL() + "api/emailconfig/setEmailConfig";
    RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.PUT, serviceUrl);
    try {
      executableTypesRequestBuilder.sendRequest(emailConfig.toString(), new RequestCallback() {
        public void onError(Request request, Throwable exception) {
          saveButton.inProgress(false);
        }

        public void onResponseReceived(Request request, Response response) {
          actionBar.collapse(500);
          saveButton.inProgress(false);
          isDirty = false;
        }
      });
    } catch (RequestException e) {
    }
  }

  private void testEmail() {
    String serviceUrl = GWT.getHostPageBaseURL() + "api/emailconfig/sendEmailTest";
    RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.GET, serviceUrl);
    try {
      executableTypesRequestBuilder.sendRequest(emailConfig.toString(), new RequestCallback() {
        public void onError(Request request, Throwable exception) {
        }

        public void onResponseReceived(Request request, Response response) {
          String message = null;
          if (response.getText().equals("EmailTester.SUCESS")) {
            message = Messages.getString("connectionTest.sucess");
          } else if (response.getText().equals("EmailTester.FAIL")) {
            message = Messages.getString("connectionTest.fail");
          }
          GwtMessageBox messageBox = new GwtMessageBox();
          messageBox.setTitle(Messages.getString("connectionTest"));
          messageBox.setMessage(message);
          messageBox.setButtons(new Object[GwtMessageBox.ACCEPT]);
          messageBox.setAcceptLabel(Messages.getString("close"));
          messageBox.show();
        }
      });
    } catch (RequestException e) {
    }
  }

  private void getEmailConfig() {
    String serviceUrl = GWT.getHostPageBaseURL() + "api/emailconfig/getEmailConfig";
    RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.GET, serviceUrl);
    executableTypesRequestBuilder.setHeader("accept", "application/json");
    try {
      executableTypesRequestBuilder.sendRequest(null, new RequestCallback() {
        public void onError(Request request, Throwable exception) {
        }

        public void onResponseReceived(Request request, Response response) {
          emailConfig = JsEmailConfiguration.parseJsonString(response.getText());

          authenticationCheckBox.setValue(emailConfig.isAuthenticate());
          authenticationPanel.setVisible(emailConfig.isAuthenticate());
          smtpHostTextBox.setValue(emailConfig.getSmtpHost());
          portTextBox.setValue(emailConfig.getSmtpPort().toString());
          useStartTLSCheckBox.setValue(emailConfig.isUseStartTls());
          useSSLCheckBox.setValue(emailConfig.isUseSsl());
          fromAddressTextBox.setValue(emailConfig.getDefaultFrom());
          userNameTextBox.setValue(emailConfig.getUserId());
          debuggingCheckBox.setValue(emailConfig.isDebug());

          // If password is non-empty.. disable the text-box
          final String password = emailConfig.getPassword();
          if (!StringUtils.isEmpty(password)) {
            passwordTextBox.getManagedObject().setEnabled(false);
            editPasswordButton.setEnabled(true);
          }
          passwordTextBox.setValue(password);


          final String protocol = emailConfig.getSmtpProtocol();
          protocolsListBox.setSelectedIndex(-1); // TODO ?
          if (!StringUtils.isEmpty(protocol)) {
            for (int i = 0; i < protocolsListBox.getItemCount(); ++i) {
              if (protocol.equals(protocolsListBox.getItemText(i))) {
                protocolsListBox.setSelectedIndex(i);
                break;
              }
            }
          }

          validate();
        }
      });
    } catch (RequestException e) {
    }
  }

  // -- ISysAdminPanel implementation.

  public void activate() {
    isDirty = false;
    getEmailConfig();
  }

  public String getId() {
    return "emailAdminPanel";
  }

  public void passivate(final AsyncCallback<Boolean> callback) {
    if (isDirty) {
      GwtConfirmBox messageBox = new GwtConfirmBox();
      messageBox.setTitle(Messages.getString("confirm"));
      messageBox.setMessage(Messages.getString("dirtyStateMessage"));
      messageBox.addDialogCallback(new XulDialogCallback<String>() {

        public void onClose(XulComponent component, XulDialogCallback.Status status, String value) {
          if (status == XulDialogCallback.Status.ACCEPT) {
            callback.onSuccess(true);
          }
          if (status == XulDialogCallback.Status.CANCEL) {
            MantleXul.getInstance().selectAdminCatTreeTreeItem(Messages.getString("emailSmtpServer"));
            callback.onSuccess(false);
          }
        }

        public void onError(XulComponent e, Throwable t) {
        }
      });
      messageBox.show();
    } else {
      callback.onSuccess(true);
    }
  }
}
