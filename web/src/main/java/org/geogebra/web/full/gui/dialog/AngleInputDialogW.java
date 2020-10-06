/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

package org.geogebra.web.full.gui.dialog;

import org.geogebra.common.gui.InputHandler;
import org.geogebra.common.gui.view.algebra.DialogType;
import org.geogebra.common.util.AsyncOperation;
import org.geogebra.common.util.StringUtil;
import org.geogebra.web.html5.gui.inputfield.AutoCompleteTextFieldW;
import org.geogebra.web.html5.main.AppW;

import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.himamis.retex.editor.share.util.Unicode;

public class AngleInputDialogW extends InputDialogW {

	protected RadioButton rbCounterClockWise;
	protected RadioButton rbClockWise;

	/**
	 * Input Dialog for a GeoAngle object.
	 */
	public AngleInputDialogW(AppW app, String message, String title,
			String initString, boolean autoComplete, InputHandler handler,
			boolean modal) {
		super(modal, app, true);
		setInputHandler(handler);
		setInitString(initString);

		// create radio buttons for "clockwise" and "counter clockwise"
		String id = DOM.createUniqueId();
		rbCounterClockWise = new RadioButton(id,
				loc.getMenu("counterClockwise"));
		rbClockWise = new RadioButton(id, loc.getMenu("clockwise"));
		rbCounterClockWise.setValue(true);

		HorizontalPanel rbPanel = new HorizontalPanel();
		rbPanel.setStyleName("DialogRbPanel");
		rbPanel.add(rbCounterClockWise);
		rbPanel.add(rbClockWise);

		createGUI(title, message, autoComplete, DEFAULT_COLUMNS, 1, true, false,
				false, false, DialogType.GeoGebraEditor);

		VerticalPanel centerPanel = new VerticalPanel();
		centerPanel.add(messagePanel);
		centerPanel.add(inputPanel);
		centerPanel.add(errorPanel);
		centerPanel.add(rbPanel);
		((VerticalPanel) wrappedPopup.getWidget()).insert(centerPanel, 0);

		wrappedPopup.center();
		inputPanel.getTextComponent().setFocus(true);

		inputPanel.addTextComponentInsertHandler(t -> insertDegreeSymbolIfNeeded());
		inputPanel.addTextComponentKeyUpHandler(e -> {
			// return unless digit typed (instead of !Character.isDigit)
			if (e.getNativeKeyCode() < 48
					|| (e.getNativeKeyCode() > 57 && e.getNativeKeyCode() < 96)
					|| e.getNativeKeyCode() > 105) {
				return;
			}
			insertDegreeSymbolIfNeeded();
		});
	}

	public boolean isCounterClockWise() {
		return rbCounterClockWise.getValue();
	}

	@Override
	protected void actionPerformed(DomEvent<?> e) {
		Object source = e.getSource();

		try {

			if (source == btOK || sourceShouldHandleOK(source)) {
				String inputTextWithSign = inputPanel.getText();
				inputPanel.getTextComponent().hideTablePopup();

				// negative orientation ?
				if (rbClockWise.getValue()) {
					inputTextWithSign = "-(" + inputTextWithSign + ")";
				}

				getInputHandler().processInput(inputTextWithSign, this,
						new AsyncOperation<Boolean>() {

							@Override
							public void callback(Boolean ok) {
								afterActionPerformed(ok);

							}
						});
			} else if (source == btCancel) {
				afterActionPerformed(true);
				inputPanel.getTextComponent().hideTablePopup();
			}
		} catch (Exception ex) {
			afterActionPerformed(false);
			// do nothing on uninitializedValue
		}
	}

	void afterActionPerformed(boolean finished) {
		if (finished) {
			wrappedPopup.hide();
			app.getActiveEuclidianView().requestFocusInWindow();
		} else {
			wrappedPopup.show();
		}
	}

	/*
	 * auto-insert degree symbol when appropriate
	 */
	private void insertDegreeSymbolIfNeeded() {
		AutoCompleteTextFieldW tc = getTextComponent();
		String text = tc.getText();

		// if text already contains degree symbol or variable
		for (int i = 0; i < text.length(); i++) {
			if (!StringUtil.isDigit(text.charAt(i))) {
				return;
			}
		}

		int caretPos = tc.getCaretPosition();
		tc.setText(tc.getText() + Unicode.DEGREE_STRING);
		tc.setCaretPosition(caretPos);
	}
}
