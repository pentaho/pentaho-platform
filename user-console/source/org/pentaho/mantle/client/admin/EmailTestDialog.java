package org.pentaho.mantle.client.admin;

import org.pentaho.gwt.widgets.client.dialogs.DialogBox;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.IDialogValidatorCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

@SuppressWarnings("deprecation")
public class EmailTestDialog extends DialogBox {

	IDialogCallback callback;
	IDialogValidatorCallback validatorCallback;
	Widget content;
	final FlexTable dialogContent = new FlexTable();
	protected Button closeButton = null;

	private final Image waitImage = new Image("mantle/large-loading.gif");
	private final String serviceUrl = GWT.getHostPageBaseURL()
			+ "api/emailconfig/sendEmailTest";
	private final RequestBuilder executableTypesRequestBuilder = new RequestBuilder(
			RequestBuilder.PUT, serviceUrl);
	private JsEmailConfiguration emailConfig;
	protected Label statusLabel;

	public EmailTestDialog(JsEmailConfiguration emailConfig) {

		/* autohide= false; modal= true */
		super(false, true);
		setText(Messages.getString("connectionTest.label"));

		closeButton = new Button(Messages.getString("close"));
		closeButton.setStylePrimaryName("pentaho-button");
		closeButton.getElement().setAttribute("id", "okButton"); //$NON-NLS-1$ //$NON-NLS-2$
		closeButton.addClickListener(new ClickListener() {
			public void onClick(Widget sender) {
				onOk();
			}
		});

		final HorizontalPanel dialogButtonPanel = new HorizontalPanel();
		dialogButtonPanel.setSpacing(0);
		dialogButtonPanel.add(closeButton);

		HorizontalPanel dialogButtonPanelWrapper = new HorizontalPanel();
		dialogButtonPanelWrapper
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		dialogButtonPanelWrapper.setStyleName("button-panel"); //$NON-NLS-1$
		dialogButtonPanelWrapper.setWidth("100%"); //$NON-NLS-1$
		dialogButtonPanelWrapper.add(dialogButtonPanel);

		if (content instanceof FocusWidget) {
			setFocusWidget((FocusWidget) content);
		}

		dialogContent.setCellPadding(0);
		dialogContent.setCellSpacing(0);
		dialogContent.getFlexCellFormatter().setHeight(0, 0, "100%");
		// add button panel
		dialogContent.setWidget(1, 0, dialogButtonPanelWrapper);
		dialogContent.getCellFormatter().setVerticalAlignment(1, 0,
				HasVerticalAlignment.ALIGN_BOTTOM);

		dialogContent.setWidth("100%"); //$NON-NLS-1$
		setWidget(dialogContent);

		this.emailConfig = emailConfig;
		this.setWidth("360px");
		this.setHeight("100px");

		HorizontalPanel hp = new HorizontalPanel();
		statusLabel = new Label(Messages.getString("connectionTest.inprog"));
		// statusLabel.set
		waitImage.setStyleName("progress-image");

		hp.add(waitImage);
		hp.add(statusLabel);
		hp.setHeight("100%");
		hp.setWidth("100%");

		this.setContent(hp);

		closeButton.setEnabled(false);
		closeButton.setVisible(false);

		this.center();

		executableTypesRequestBuilder.setHeader("Content-Type",
				"application/json");
		try {
			executableTypesRequestBuilder.sendRequest(
					this.emailConfig.getJSONString(),
					new RequestCallbackHandler());
		} catch (RequestException e) {
		}

	}

	public IDialogCallback getCallback() {
		return callback;
	}

	public void setContent(Widget content) {
		this.content = content;
		if (content != null) {
			dialogContent.getFlexCellFormatter().setVerticalAlignment(0, 0,
					HasVerticalAlignment.ALIGN_MIDDLE);
			dialogContent.getFlexCellFormatter().setHorizontalAlignment(0, 0,
					HasHorizontalAlignment.ALIGN_CENTER);
			dialogContent.setWidget(0, 0, content);
			dialogContent.getCellFormatter().setStyleName(0, 0,
					"dialog-content");

			DOM.setStyleAttribute(
					dialogContent.getCellFormatter().getElement(0, 0),
					"padding", "10px 20px 20px 20px"); //$NON-NLS-1$ //$NON-NLS-2$
			content.setHeight("100%"); //$NON-NLS-1$
			content.setWidth("100%"); //$NON-NLS-1$
		}
	}

	public Widget getContent() {
		return content;
	}

	public void setCallback(IDialogCallback callback) {
		this.callback = callback;
	}

	public IDialogValidatorCallback getValidatorCallback() {
		return validatorCallback;
	}

	public void setValidatorCallback(IDialogValidatorCallback validatorCallback) {
		this.validatorCallback = validatorCallback;
	}

	protected void onOk() {
		if (validatorCallback == null
				|| (validatorCallback != null && validatorCallback.validate())) {
			try {
				if (callback != null) {
					callback.okPressed();
				}
			} catch (Throwable dontCare) {
			}
			hide();
		}
	}

	class RequestCallbackHandler implements RequestCallback {

		private final String EMAIL_TEST_SUCCESS = Messages.getString(
				"connectionTest.sucess", emailConfig.getDefaultFrom());
		private final String EMAIL_TEST_FAIL = Messages
				.getString("connectionTest.fail");

		@Override
		public void onError(Request arg0, Throwable arg1) {

			prep_close();
			statusLabel.setText(EMAIL_TEST_FAIL);
		}

		@Override
		public void onResponseReceived(Request request, Response response) {

			prep_close();

			if (response.getText().equals("EmailTester.SUCESS")) {
				statusLabel.setText(EMAIL_TEST_SUCCESS);

			} else if (response.getText().equals("EmailTester.FAIL")) {
				statusLabel.setText(EMAIL_TEST_FAIL);
			}

		}

		private void prep_close() {

			waitImage.setVisible(false);
			closeButton.setVisible(true);
			closeButton.setEnabled(true);
		}

	}
}
