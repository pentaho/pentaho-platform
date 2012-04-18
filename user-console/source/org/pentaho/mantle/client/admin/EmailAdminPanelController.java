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
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
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

public class EmailAdminPanelController extends EmailAdminPanel implements ISysAdminPanel, UpdatePasswordController {

	private boolean isDirty = false;
	private JsEmailConfiguration emailConfig;
	private static EmailAdminPanelController emailAdminPanelController;

	public static EmailAdminPanelController getInstance() {
		if (emailAdminPanelController == null) {
			emailAdminPanelController = new EmailAdminPanelController();
		}
		return emailAdminPanelController;
	}

	private EmailAdminPanelController() {
		activate();

		editPasswordButton.addClickHandler(new ClickHandler() {
			public void onClick(final ClickEvent clickEvent) {
				ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog(EmailAdminPanelController.this);
				changePasswordDialog.show();
			}
		});

		testButton.addClickHandler(new ClickHandler() {
			public void onClick(final ClickEvent clickEvent) {
				testEmail();
			}
		});

		authenticationCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			public void onValueChange(final ValueChangeEvent<Boolean> booleanValueChangeEvent) {
				emailConfig.setAuthenticate(booleanValueChangeEvent.getValue());
				authenticationPanel.setVisible(booleanValueChangeEvent.getValue());
				isDirty = true;
				validate();
			}
		});

		smtpHostTextBox.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(final KeyUpEvent keyUpEvent) {
				emailConfig.setSmtpHost(smtpHostTextBox.getValue());
				isDirty = true;
				validate();
			}
		});

		portTextBox.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(final KeyUpEvent keyUpEvent) {
				Short port = isPortValid(portTextBox.getValue()) ? Short.parseShort(portTextBox.getValue()) : -1;
				emailConfig.setSmtpPort(port);
				isDirty = true;
				validate();
			}
		});

		fromAddressTextBox.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(final KeyUpEvent keyUpEvent) {
				emailConfig.setDefaultFrom(fromAddressTextBox.getValue());
				isDirty = true;
				validate();
			}
		});

		userNameTextBox.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(final KeyUpEvent keyUpEvent) {
				emailConfig.setUserId(userNameTextBox.getValue());
				isDirty = true;
				validate();
			}
		});

		debuggingCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			public void onValueChange(final ValueChangeEvent<Boolean> booleanValueChangeEvent) {
				emailConfig.setDebug(debuggingCheckBox.getValue());
				isDirty = true;
				validate();
			}
		});

		useSSLCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			public void onValueChange(final ValueChangeEvent<Boolean> booleanValueChangeEvent) {
				emailConfig.setUseSsl(useSSLCheckBox.getValue());
				isDirty = true;
				validate();
			}
		});

		useStartTLSCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			public void onValueChange(final ValueChangeEvent<Boolean> booleanValueChangeEvent) {
				emailConfig.setUseStartTls(useStartTLSCheckBox.getValue());
				isDirty = true;
				validate();
			}
		});

		protocolsListBox.addChangeHandler(new ChangeHandler() {
			public void onChange(final ChangeEvent changeEvent) {
				emailConfig.setSmtpProtocol(protocolsListBox.getItemText(protocolsListBox.getSelectedIndex()));
				isDirty = true;
				validate();
			}
		});

		saveButton.addClickHandler(new ClickHandler() {
			public void onClick(final ClickEvent clickEvent) {
				setEmailConfig();
			}
		});

		passwordTextBox.getManagedObject().addBlurHandler(new BlurHandler() {
			public void onBlur(BlurEvent event) {
				if (!StringUtils.isEmpty(passwordTextBox.getValue())) {
					editPasswordButton.setEnabled(true);
					passwordTextBox.getManagedObject().setEnabled(false);
				}
			}
		});

		passwordTextBox.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(final KeyUpEvent keyUpEvent) {
				emailConfig.setPassword(passwordTextBox.getValue());
				isDirty = true;
				validate();
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
		boolean smtpHostValid = !StringUtils.isEmpty(emailConfig.getSmtpHost());
		boolean smtpValid = !StringUtils.isEmpty(emailConfig.getSmtpProtocol());
		boolean fromAddressValid = isValidEmail(emailConfig.getDefaultFrom());
		boolean portValid = isPortValid(emailConfig.getSmtpPort() + "");
		boolean authenticationValid = true;
		if (Boolean.parseBoolean(emailConfig.isAuthenticate() + "")) {
			boolean userNameValid = !StringUtils.isEmpty(emailConfig.getUserId());
			boolean passwordValid = !StringUtils.isEmpty(emailConfig.getPassword());
			authenticationValid = userNameValid && passwordValid;
		}
		if (smtpHostValid && portValid && smtpValid && fromAddressValid && authenticationValid) {
			if(isDirty) {
				actionBar.expand(1);
			}
			testButton.setEnabled(true);
		} else {
			actionBar.collapse(1);
			testButton.setEnabled(false);
		}
	}

	// -- Remote Calls.

	private void setEmailConfig() {
		progressIndicator.inProgress(true);
		String serviceUrl = GWT.getHostPageBaseURL() + "api/emailconfig/setEmailConfig";
		RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.PUT, serviceUrl);
		try {
			executableTypesRequestBuilder.setHeader("Content-Type", "application/json");
			executableTypesRequestBuilder.sendRequest(emailConfig.getJSONString(), new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					progressIndicator.inProgress(false);
				}

				public void onResponseReceived(Request request, Response response) {
					actionBar.collapse(500);
					progressIndicator.inProgress(false);
					isDirty = false;
				}
			});
		} catch (RequestException e) {
		}
	}

	private void testEmail() {
		String serviceUrl = GWT.getHostPageBaseURL() + "api/emailconfig/sendEmailTest";
		RequestBuilder executableTypesRequestBuilder = new RequestBuilder(RequestBuilder.PUT, serviceUrl);
		try {
			executableTypesRequestBuilder.setHeader("Content-Type", "application/json");
			executableTypesRequestBuilder.sendRequest(emailConfig.getJSONString(), new RequestCallback() {
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
					authenticationCheckBox.setValue(Boolean.parseBoolean(emailConfig.isAuthenticate() + ""));
					authenticationPanel.setVisible(Boolean.parseBoolean(emailConfig.isAuthenticate() + ""));
					smtpHostTextBox.setValue(emailConfig.getSmtpHost());
					portTextBox.setValue(emailConfig.getSmtpPort() + "");
					useStartTLSCheckBox.setValue(Boolean.parseBoolean(emailConfig.isUseStartTls() + ""));
					useSSLCheckBox.setValue(Boolean.parseBoolean(emailConfig.isUseSsl() + ""));
					fromAddressTextBox.setValue(emailConfig.getDefaultFrom());
					userNameTextBox.setValue(emailConfig.getUserId());
					debuggingCheckBox.setValue(Boolean.parseBoolean(emailConfig.isDebug() + ""));

					// If password is non-empty.. disable the text-box
					String password = emailConfig.getPassword();
					passwordTextBox.getManagedObject().setEnabled(StringUtils.isEmpty(password));
					editPasswordButton.setEnabled(!StringUtils.isEmpty(password));
					passwordTextBox.setValue(password);

					String protocol = emailConfig.getSmtpProtocol();
					protocolsListBox.setSelectedIndex(-1);
					if (!StringUtils.isEmpty(protocol)) {
						for (int i = 0; i < protocolsListBox.getItemCount(); ++i) {
							if (protocol.equalsIgnoreCase(protocolsListBox.getItemText(i))) {
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
						actionBar.collapse(1);
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
