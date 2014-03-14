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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Properties;

import javax.swing.JPanel;

/**
 * Diese Klasse verwaltet den Frequenz-Slider und den Graphen-Slider
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 */
public class SliderPanel extends JPanel implements PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private MySlider frequencyBox = null;

	private MySlider graphsBox = null;

	private Properties props = null;

	private final PropertyChangeSupport spListeners;

	private GridBagLayout gbLayout = null;

	private GridBagConstraints constraints = null;

	private int graphsNumber = 0;

	SliderPanel(final Properties p) {
		super();
		gbLayout = new GridBagLayout();
		constraints = new GridBagConstraints();
		this.setLayout(gbLayout);
		props = p;
		spListeners = new PropertyChangeSupport(this);

		frequencyBox = new MySlider(props, 2, 100);
		frequencyBox.addPropertyChangeListener(this);
		// frequencyBox.addFocusListener(this);
		this.addPropertyChangeListener("new frequency value", frequencyBox);
		this.addPropertyChangeListener("disable", frequencyBox);
		this.addPropertyChangeListener("enable", frequencyBox);
		this.addPropertyChangeListener("set language properties", frequencyBox);

		// frequency slider
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.gridwidth = 2;
		constraints.gridx = 0;
		constraints.gridy = 1;
		gbLayout.setConstraints(frequencyBox, constraints);
		add(frequencyBox);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Container#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	@Override
	public void addPropertyChangeListener(final PropertyChangeListener l) {
		spListeners.addPropertyChangeListener(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Container#addPropertyChangeListener(java.lang.String,
	 *      java.beans.PropertyChangeListener)
	 */
	@Override
	public void addPropertyChangeListener(final String pn,
			final PropertyChangeListener l) {
		spListeners.addPropertyChangeListener(pn, l);
	}

	private void initSliders(final int number) {
		if (graphsBox != null) {
			this.remove(graphsBox);
			this.removePropertyChangeListener(graphsBox);
			spListeners.firePropertyChange("new frequency value", -1, 10);
		}

		graphsBox = new MySlider(props, 1, number);
		graphsBox.addPropertyChangeListener(this);
		this.addPropertyChangeListener("new graph value", graphsBox);
		this.addPropertyChangeListener("disable", graphsBox);
		this.addPropertyChangeListener("enable", graphsBox);
		this.addPropertyChangeListener("set language properties", graphsBox);
		setLayout();
	}

	public void propertyChange(final PropertyChangeEvent event) {
		final String propertyName = event.getPropertyName();
		if (propertyName == "closed") {
			return;
		}
		int newValue;
		if (propertyName.equals("number")) {
			newValue = ((Integer) event.getNewValue()).intValue();
			if (graphsNumber == newValue) {
				return;
			}
			graphsNumber = newValue;
			initSliders(graphsNumber);
			return;
		}

		if (propertyName.equals("changed graph number")) {
			newValue = ((Integer) event.getNewValue()).intValue();
			spListeners.firePropertyChange("minimumFrequency", 0, newValue);
			newValue = (int) (newValue * 100.0 / graphsNumber);
			spListeners.firePropertyChange("new frequency value", -1, newValue);
			return;
		}

		if (propertyName.equals("changed frequency number")) {
			newValue = ((Integer) event.getNewValue()).intValue();
			newValue = (int) Math.ceil(newValue * graphsNumber / 100.0);
			spListeners.firePropertyChange("minimumFrequency", 0, newValue);
			spListeners.firePropertyChange("new graph value", -1, newValue);
			return;
		}

		if (propertyName.equals("disable")) {
			spListeners.firePropertyChange("disable", 0, 1);
			return;
		}

		if (propertyName.equals("enable")) {
			spListeners.firePropertyChange("enable", 0, 1);
		}

		if (propertyName.equals("remove sliders")) {
			if (graphsBox != null) {
				this.remove(graphsBox);
				this.removePropertyChangeListener(graphsBox);
			}
		}

		if (propertyName.equals("set language properties")) {
			this.props = (Properties) event.getNewValue();
			spListeners.firePropertyChange(event);
			this.resetLanguage();
		}

	}

	private void resetLanguage() {

	}

	private void setLayout() {
		// graphs slider
		constraints.anchor = GridBagConstraints.FIRST_LINE_START;
		constraints.gridx = 0;
		constraints.gridy = 0;
		gbLayout.setConstraints(graphsBox, constraints);
		add(graphsBox);
	}

	public void setSliderValues(final int number) {
		spListeners.firePropertyChange("new graph value", -1, number);
		spListeners.firePropertyChange("new frequency value", -1,
				(int) (number * 100.0 / graphsNumber));
	}
}
