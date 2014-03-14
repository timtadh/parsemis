/**
 * Created on Jan 18, 2007
 *
 * @by Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 * Copyright 2007 Olga Urzova
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.visualisation.gui;

import java.awt.AWTKeyStroke;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 */
public class ImageAccessory extends JPanel implements FocusListener,
		PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static int currentWidth;

	private static int currentHeight;

	public static Dimension getCurrentDimension() {
		return new Dimension(currentWidth, currentHeight);
	}

	private GridBagLayout gbLayout = null;

	private GridBagConstraints constraints = null;

	private JTextField widthText = null;

	private JTextField heightText = null;

	private final int minValue = 20;

	private final int defaultWidth = 500;

	private final int defaultHeight = 500;

	private final int textFieldSize = 3;

	private JLabel widthLabel = null;

	private JLabel heightLabel = null;

	private JLabel errorMessage = null;

	private JLabel width_px = null;

	private JLabel height_px = null;

	private Properties props;

	public ImageAccessory(final Properties p) {
		props = p;
		gbLayout = new GridBagLayout();
		this.setLayout(gbLayout);

		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;

		final JPanel widthPanel = new JPanel(new FlowLayout());
		width_px = new JLabel("px", SwingConstants.RIGHT);
		height_px = new JLabel("px", SwingConstants.RIGHT);

		widthLabel = new JLabel();
		widthLabel.setHorizontalAlignment(SwingConstants.LEFT);
		widthPanel.add(widthLabel);

		widthText = new JTextField(new LimitedDoc(textFieldSize), Integer
				.toString(defaultWidth), textFieldSize);
		widthText.addFocusListener(this);
		widthPanel.add(widthText);
		widthPanel.add(width_px);

		gbLayout.setConstraints(widthPanel, constraints);
		this.add(widthPanel);

		final JPanel heightPanel = new JPanel(new FlowLayout());
		heightLabel = new JLabel();
		heightLabel.setHorizontalAlignment(SwingConstants.LEFT);
		heightPanel.add(heightLabel);
		heightText = new JTextField(new LimitedDoc(textFieldSize), Integer
				.toString(defaultHeight), textFieldSize);
		heightText.addFocusListener(this);

		heightPanel.add(heightText);
		heightPanel.add(height_px);

		constraints.gridy = 1;
		gbLayout.setConstraints(heightPanel, constraints);
		this.add(heightPanel);

		errorMessage = new JLabel();
		errorMessage.setBorder(new EmptyBorder(0, 5, 0, 5));
		constraints.gridy = 2;
		gbLayout.setConstraints(errorMessage, constraints);
		this.add(errorMessage);

		currentWidth = defaultWidth;
		currentHeight = defaultHeight;

		final Set<AWTKeyStroke> forwardKeys = getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
		final Set<AWTKeyStroke> newForwardKeys = new HashSet<AWTKeyStroke>(
				forwardKeys);
		newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
				newForwardKeys);
		this.resetLanguage();

	}

	public void focusGained(final FocusEvent event) {
		return;
	}

	public void focusLost(final FocusEvent event) {
		String newInput = null;
		String oldInput = null;
		if (event.getSource() == widthText) {
			newInput = widthText.getText();
			oldInput = Integer.toString(currentWidth);
			if ((!isNumber(newInput)) || (isTooSmall(newInput))) {
				widthText.setText(oldInput);
				widthText.requestFocus();
			} else {
				currentWidth = Integer.parseInt(newInput);
			}
			return;
		}
		if (event.getSource() == heightText) {
			newInput = heightText.getText();
			oldInput = Integer.toString(currentHeight);
			if ((!isNumber(newInput)) || (isTooSmall(newInput))) {
				heightText.setText(oldInput);
				heightText.requestFocus();
			} else {
				currentHeight = Integer.parseInt(newInput);
			}
		}
	}

	private boolean isNumber(final String text) {
		if (text.length() == 0) {
			return false;
		}
		for (int i = 0; i < text.length(); i++) {
			if (!Character.isDigit(text.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	private boolean isTooSmall(final String input) {
		if (Integer.parseInt(input) < minValue) {
			return true;
		}
		return false;
	}

	public void propertyChange(final PropertyChangeEvent event) {
		final String propertyName = event.getPropertyName();
		if (propertyName.equals("set language properties")) {
			this.props = (Properties) event.getNewValue();
			this.resetLanguage();
		}

	}

	private void resetLanguage() {
		heightLabel.setText(props.getProperty("save_dialog_height"));
		widthLabel.setText(props.getProperty("save_dialog_width"));
		errorMessage.setText("<html>"
				+ props.getProperty("save_dialog_error_format")
				+ "<br>"
				+ String.format(props.getProperty("save_dialog_error_size"),
						minValue) + "<html>");
	}

}
