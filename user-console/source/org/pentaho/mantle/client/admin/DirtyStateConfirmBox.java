package org.pentaho.mantle.client.admin;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.gwt.tags.GwtMessageBox;
import org.pentaho.ui.xul.util.XulDialogCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;

@SuppressWarnings("all")
public class DirtyStateConfirmBox extends GwtMessageBox implements XulConfirmBox {

	private List<XulDialogCallback<String>> callbacks = new ArrayList<XulDialogCallback<String>>();

	private String saveLabel = "Save";
	private String cancelLabel = "Cancel";
	private String dontSaveLabel = "Don't Save";

	private Button saveBtn = new Button();
	private Button cancelBtn = new Button();
	private Button dontSaveBtn = new Button();
	private boolean showDefaultButtons = true;

	public DirtyStateConfirmBox() {
		this(true);
	}

	public DirtyStateConfirmBox(boolean showDefaultButtons) {
		this.showDefaultButtons = showDefaultButtons;
		setHeight(150);
		setWidth(275);

		saveBtn.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent evt) {
				hide();
				for (XulDialogCallback<String> callback : callbacks) {
					callback.onClose(DirtyStateConfirmBox.this, XulDialogCallback.Status.ACCEPT, null);
				}
			}
		});
		saveBtn.setStylePrimaryName("pentaho-button");

		cancelBtn.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent evt) {
				hide();
				for (XulDialogCallback<String> callback : callbacks) {
					callback.onClose(DirtyStateConfirmBox.this, XulDialogCallback.Status.CANCEL, null);
				}
			}
		});
		cancelBtn.setStylePrimaryName("pentaho-button");

		dontSaveBtn.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent evt) {
				hide();
				for (XulDialogCallback<String> callback : callbacks) {
					callback.onClose(DirtyStateConfirmBox.this, XulDialogCallback.Status.ONEXTRA1, null);
				}
			}
		});
		dontSaveBtn.setStylePrimaryName("pentaho-button");
	}

	public Panel getButtonPanel() {
		saveBtn.setText(saveLabel);
		cancelBtn.setText(cancelLabel);
		dontSaveBtn.setText(dontSaveLabel);

		HorizontalPanel hp = new HorizontalPanel();
		if (showDefaultButtons) {
			hp.add(saveBtn);
			hp.setCellWidth(saveBtn, "100%");
			hp.setCellHorizontalAlignment(saveBtn, HorizontalPanel.ALIGN_RIGHT);
			hp.add(dontSaveBtn);
			hp.add(cancelBtn);
		} else {
			hp.add(cancelBtn);
			hp.setCellWidth(cancelBtn, "100%");
			hp.setCellHorizontalAlignment(cancelBtn, HorizontalPanel.ALIGN_RIGHT);
		}
		return hp;
	}

	public Panel getDialogContents() {
		VerticalPanel vp = new VerticalPanel();
		Label lbl = new Label(getMessage());
		vp.add(lbl);
		vp.setCellHorizontalAlignment(lbl, VerticalPanel.ALIGN_CENTER);
		vp.setCellVerticalAlignment(lbl, VerticalPanel.ALIGN_MIDDLE);
		return vp;
	}

	public Object[] getButtons() {
		return null;
	}

	public Object getIcon() {
		return null;
	}

	public void setSaveLabel(String lbl) {
		this.saveLabel = lbl;
	}

	public void setCancelLabel(String lbl) {
		this.cancelLabel = lbl;
	}

	public void addDialogCallback(XulDialogCallback callback) {
		this.callbacks.add(callback);
	}

	public void removeDialogCallback(XulDialogCallback callback) {
		this.callbacks.remove(callback);
	}
}
