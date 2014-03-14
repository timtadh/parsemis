/**
 * Created on Apr 20, 2007
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

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Dictionary;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Diese Klasse ist fuer das Aussehen und Funktionen eines Sliders
 * verantwortlich
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 */
public class MySlider extends JPanel implements PropertyChangeListener,
		ChangeListener, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JSlider slider = null;

	private GridBagLayout gbLayout = null;

	private GridBagConstraints constraints = null;

	private JLabel label = null;

	private JTextField text = null;

	private JLabel from;

	private JLabel to;

	private Properties props;

	private int maxNumber = 0;

	private int sliderType = 0;

	private int currentValue = 0;

	private int graphsNumber = 0;

	private final PropertyChangeSupport msListeners;

	private JPanel labelPanel = null;

	private int textFieldSize = 3;

	@SuppressWarnings("unchecked")
	MySlider(final Properties p, final int c, final int number) {
		super();
		sliderType = c;
		props = p;
		graphsNumber = number;
		// set layout properties for the label object
		gbLayout = new GridBagLayout();
		this.setLayout(gbLayout);
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.insets = new Insets(10, 50, 0, 0);

		int step = 10;
		Dictionary<Integer, JLabel> labelTable;
		switch (sliderType) {
		case 1:
			// i am a graph slider
			maxNumber = graphsNumber;
			currentValue = (int) Math.ceil(10.0 * graphsNumber / 100.0);
			slider = new JSlider(SwingConstants.HORIZONTAL, 0, graphsNumber,
					currentValue);
			// set slider labels
			slider.setMajorTickSpacing(maxNumber);
			slider.setPaintLabels(true);
			labelTable = slider.getLabelTable();
			from = new JLabel(Integer.toString(0));
			to = new JLabel(Integer.toString(graphsNumber));
			labelTable.put(slider.getMinimum(), from);
			labelTable.put(slider.getMaximum(), to);
			slider.setLabelTable(labelTable);
			step = getLevel(graphsNumber) / 10;

			break;
		case 2:
			// i am a frequency slider
			maxNumber = 100;
			currentValue = 10;
			slider = new JSlider(SwingConstants.HORIZONTAL, 0, 100,
					currentValue);
			// set slider labels
			slider.setMajorTickSpacing(maxNumber);
			slider.setPaintLabels(true);
			labelTable = slider.getLabelTable();
			from = new JLabel("0%");
			to = new JLabel("100%");
			labelTable.put(slider.getMinimum(), from);
			labelTable.put(slider.getMaximum(), to);
			slider.setLabelTable(labelTable);
			break;
		}

		// set misc slider properties
		slider.setMinorTickSpacing(step);
		slider.addChangeListener(this);
		slider.setPaintTicks(true);
		slider.setSnapToTicks(false);
		slider.setFont(new Font("Dialog", Font.ITALIC, 12));

		labelPanel = new JPanel(new FlowLayout());
		label = new JLabel();
		label.setHorizontalAlignment(SwingConstants.LEFT);

		textFieldSize = (Integer.toString(maxNumber)).length();
		text = new JTextField(new LimitedDoc(textFieldSize), Integer
				.toString(currentValue), textFieldSize);
		text.addActionListener(this);

		labelPanel.add(label);
		constraints.gridx = 0;
		constraints.anchor = GridBagConstraints.LINE_END;
		labelPanel.add(text);

		gbLayout.setConstraints(label, constraints);
		this.add(labelPanel);
		// set layout properties for the slider object
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 2;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(5, 0, 0, 0);
		gbLayout.setConstraints(slider, constraints);
		this.add(slider);

		msListeners = new PropertyChangeSupport(this);

		resetLanguage();
	}

	public void actionPerformed(final ActionEvent event) {
		final String newInput = text.getText();
		final String oldInput = Integer.toString(currentValue);

		int result;
		if (newInput.length() > textFieldSize) {
			text.setText(oldInput);
			setSliderValue(currentValue);
			return;
		}

		for (int i = 0; i < newInput.length(); i++) {
			if (!Character.isDigit(newInput.charAt(i))) {
				text.setText(oldInput);
				setSliderValue(currentValue);
				return;
			}
		}
		if ((Integer.parseInt(newInput) > maxNumber)
				|| (Integer.parseInt(newInput) < 0)) {
			result = currentValue;
			text.setText(oldInput);
		} else {
			result = Integer.parseInt(newInput);
		}
		setSliderValue(result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Container#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	@Override
	public void addPropertyChangeListener(final PropertyChangeListener l) {
		msListeners.addPropertyChangeListener(l);
	}

	private int getLevel(final int number) {
		int result = 1;
		int mod = 10;
		int rest = number % mod;
		while (rest != number) {
			result = mod;
			mod *= 10;
			rest = number % mod;
		}
		return result;
	}

	public void propertyChange(final PropertyChangeEvent event) {
		final String propertyName = event.getPropertyName();
		if (propertyName == "new graph value") {
			final int newValue = ((Integer) event.getNewValue()).intValue();
			if (newValue == currentValue) {
				return;
			}
			try {
				text.setText(Integer.toString(newValue));
				slider.setValue(newValue);
			} catch (final StackOverflowError e) {
			}
			return;
		}

		if (propertyName == "new frequency value") {

			final int newValue = ((Integer) event.getNewValue()).intValue();
			if (newValue == currentValue) {
				return;
			}
			try {
				text.setText(Integer.toString(newValue));
				currentValue = newValue;
				slider.setValue(newValue);

			} catch (final StackOverflowError e) {
			}
			return;
		}

		if (propertyName.equals("disable")) {
			from.setEnabled(false);
			to.setEnabled(false);
			slider.setEnabled(false);
			label.setEnabled(false);
			text.setEditable(false);
			text.setEnabled(false);
			return;
		}

		if (propertyName.equals("enable")) {
			from.setEnabled(true);
			to.setEnabled(true);
			slider.setEnabled(true);
			label.setEnabled(true);
			text.setEditable(true);
			text.setEnabled(true);
		}

		if (propertyName.equals("set language properties")) {
			this.props = (Properties) event.getNewValue();
			this.resetLanguage();
		}
	}

	private void resetLanguage() {
		label.setText(props.getProperty("frequencySlider" + sliderType));
	}

	private void setSliderValue(final int value) {
		text.setText(Integer.toString(value));
		switch (sliderType) {
		// I am a graphs Slider
		case 1:
			msListeners.firePropertyChange("changed graph number", -1, value);
			break;
		// I am a frequency Slider
		case 2:
			msListeners.firePropertyChange("changed frequency number", -1,
					value);
			break;
		default:
		}
	}

	public void stateChanged(final ChangeEvent e) {
		JSlider s;
		s = (JSlider) e.getSource();
		final int value = s.getValue();
		if (currentValue == value) {
			return;
		}
		currentValue = value;
		setSliderValue(currentValue);

	}
}
