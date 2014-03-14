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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 */
public class ControlPanel extends JPanel implements ActionListener,
		PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Properties props = null;

	private GridBagLayout gbLayout = null;

	private GridBagConstraints constraints = null;

	private JPanel propertiesPane = null;

	private JPanel buttonsPane = null;

	private JPanel currentValuesPanel = null;

	private JLabel currentValuesTitle = null;

	private JLabel currentFrequency = null;

	private JLabel currentAlgo = null;

	private JLabel currentGraphType = null;

	private JLabel currentClosed = null;

	private TitledBorder enabledPanelBorder = null;

	private TitledBorder disabledPanelBorder = null;

	private JCheckBox closed = null;

	private JCheckBox sequenceOn = null;

	private PropertyChangeSupport cpListeners;

	private SliderPanel sliders = null;

	private MyComboBox algoPanel = null;

	private MyComboBox typePanel = null;

	private int algo = 0;

	private int graphType = 0;

	private int frequency = 0;

	private String[] algoNames = null;

	private String[] graphTypeNames = null;

	public ControlPanel(final ControlPanel master) {
		props = master.props;
		gbLayout = master.gbLayout;
		constraints = master.constraints;
		propertiesPane = master.propertiesPane;
		buttonsPane = master.buttonsPane;
		currentValuesPanel = master.currentValuesPanel;
		currentValuesTitle = master.currentValuesTitle;
		currentFrequency = master.currentFrequency;
		currentAlgo = master.currentAlgo;
		currentGraphType = master.currentGraphType;
		currentClosed = master.currentClosed;
		enabledPanelBorder = master.enabledPanelBorder;
		disabledPanelBorder = master.disabledPanelBorder;
		closed = master.closed;
		sequenceOn = master.sequenceOn;
		cpListeners = master.cpListeners;
		sliders = master.sliders;
		algoPanel = master.algoPanel;
		typePanel = master.typePanel;
		algo = master.algo;
		graphType = master.graphType;
		frequency = master.frequency;
		algoNames = master.algoNames;
		graphTypeNames = master.graphTypeNames;

		this.addPropertyChangeListener("number", sliders);
		this.addPropertyChangeListener("changed graph number", sliders);
		this.addPropertyChangeListener("disable", sliders);
		this.addPropertyChangeListener("enable", sliders);
		this.addPropertyChangeListener("remove sliders", sliders);
		this.addPropertyChangeListener("changed frequency number", sliders);
		this.addPropertyChangeListener("set language properties", sliders);

		this.addPropertyChangeListener("disable", algoPanel);
		this.addPropertyChangeListener("enable", algoPanel);
		this.addPropertyChangeListener("set language properties", algoPanel);

		this.addPropertyChangeListener("disable", typePanel);
		this.addPropertyChangeListener("enable", typePanel);
		this.addPropertyChangeListener("set language properties", typePanel);

		this.add(propertiesPane, BorderLayout.CENTER);
	}

	public ControlPanel(final Properties p) {
		super(new BorderLayout());
		props = p;
		gbLayout = new GridBagLayout();
		constraints = new GridBagConstraints();
		propertiesPane = new JPanel(new BorderLayout());
		buttonsPane = new JPanel(gbLayout);

		enabledPanelBorder = BorderFactory.createTitledBorder(
				new EtchedBorder(), props.getProperty("leftColumn"),
				TitledBorder.CENTER, TitledBorder.TOP, new Font("Dialog",
						Font.BOLD, 14));
		enabledPanelBorder.setTitleColor(VisualisationConstants.enabledColor);

		disabledPanelBorder = BorderFactory.createTitledBorder(
				new EtchedBorder(), props.getProperty("leftColumn"),
				TitledBorder.CENTER, TitledBorder.TOP, new Font("Dialog",
						Font.BOLD, 14));
		disabledPanelBorder.setTitleColor(VisualisationConstants.disabledColor);

		closed = new JCheckBox();
		closed.addActionListener(this);

		sequenceOn = new JCheckBox();
		sequenceOn.addActionListener(this);

		sliders = new SliderPanel(props);

		algoNames = new String[] { props.getProperty("gspan") };
//				props.getProperty("mofa"), props.getProperty("gaston"),
//				props.getProperty("ffsm") };
		graphTypeNames = new String[] { props.getProperty("all_graphs"),
				props.getProperty("trees_only"),
				props.getProperty("paths_only") };
		algoPanel = new MyComboBox(props, algoNames, 1);
		typePanel = new MyComboBox(props, graphTypeNames, 2);

		addCurrentValuesPanel();
		setLayout();
		initListener();
		resetLanguage();
	}

	public void actionPerformed(final ActionEvent event) {
		final String command = event.getActionCommand();
		if (command.equals(props.getProperty("checkBoxClosed"))) {
			cpListeners.firePropertyChange("closed", !closed.isSelected(),
					closed.isSelected());
		}

		if (command.equals(props.getProperty("sequenceOn"))) {
			cpListeners.firePropertyChange("set sequence", !sequenceOn
					.isSelected(), sequenceOn.isSelected());
		}
	}

	private void addCurrentValuesPanel() {
		// current values
		currentValuesPanel = new JPanel();
		currentValuesPanel.setLayout(new BoxLayout(currentValuesPanel,
				BoxLayout.Y_AXIS));
		currentValuesPanel.setPreferredSize(new Dimension(223, 130));
		currentValuesPanel.setBorder(new EtchedBorder());
		currentValuesPanel
				.setBackground(VisualisationConstants.selectedFragmentColor);
		currentValuesTitle = new JLabel();
		currentValuesTitle.setBorder(VisualisationConstants.standardBorder);

		currentFrequency = new JLabel();
		currentAlgo = new JLabel();
		currentGraphType = new JLabel();
		currentClosed = new JLabel();

		currentFrequency.setBorder(VisualisationConstants.currentValuesBorder);
		currentAlgo.setBorder(VisualisationConstants.currentValuesBorder);
		currentGraphType.setBorder(VisualisationConstants.currentValuesBorder);
		currentClosed.setBorder(VisualisationConstants.currentValuesBorder);

		currentValuesPanel.add(currentValuesTitle);
		currentValuesPanel.add(currentFrequency);
		currentValuesPanel.add(currentAlgo);
		currentValuesPanel.add(currentGraphType);
		currentValuesPanel.add(currentClosed);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Container#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	@Override
	public void addPropertyChangeListener(final PropertyChangeListener l) {
		cpListeners.addPropertyChangeListener(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Container#addPropertyChangeListener(java.lang.String,
	 *      java.beans.PropertyChangeListener)
	 */
	@Override
	public void addPropertyChangeListener(final String str,
			final PropertyChangeListener l) {
		cpListeners.addPropertyChangeListener(str, l);
	}

	public void addSlides() {
		sliders.setVisible(true);
	}

	private void disableAll() {
		propertiesPane.setBorder(disabledPanelBorder);
		cpListeners.firePropertyChange("disable", 0, 1);
		closed.setEnabled(false);
		sequenceOn.setEnabled(false);
	}

	private void enableAll() {
		propertiesPane.setBorder(enabledPanelBorder);
		cpListeners.firePropertyChange("enable", 0, 1);
		closed.setEnabled(true);
		sequenceOn.setEnabled(true);

	}

	private void initListener() {
		cpListeners = new PropertyChangeSupport(this);
		algoPanel.addPropertyChangeListener(props.getProperty("box" + 1), this);
		typePanel.addPropertyChangeListener(props.getProperty("box" + 2), this);
		sliders.addPropertyChangeListener("minimumFrequency", this);

		this.addPropertyChangeListener("number", sliders);
		this.addPropertyChangeListener("changed graph number", sliders);
		this.addPropertyChangeListener("disable", sliders);
		this.addPropertyChangeListener("enable", sliders);
		this.addPropertyChangeListener("remove sliders", sliders);
		this.addPropertyChangeListener("changed frequency number", sliders);
		this.addPropertyChangeListener("set language properties", sliders);

		this.addPropertyChangeListener("disable", algoPanel);
		this.addPropertyChangeListener("enable", algoPanel);
		this.addPropertyChangeListener("set language properties", algoPanel);

		this.addPropertyChangeListener("disable", typePanel);
		this.addPropertyChangeListener("enable", typePanel);
		this.addPropertyChangeListener("set language properties", typePanel);

	}

	public void propertyChange(final PropertyChangeEvent event) {
		final String propertyName = event.getPropertyName();
		int newValue;
		if (propertyName.equals("parsed")) {
			newValue = ((Integer) event.getNewValue()).intValue();
			cpListeners.firePropertyChange("number", 0, newValue);
			sliders.setVisible(true);
			return;
		}

		if (propertyName.equals("minimumFrequency")) {
			frequency = ((Integer) event.getNewValue()).intValue();
			cpListeners.firePropertyChange("minimumFrequency", 0, frequency);
			return;
		}

		if (propertyName.equals("disable")) {
			currentValuesPanel.setVisible(false);
			disableAll();
			return;
		}

		if (propertyName.equals("enable")) {
			resetLanguageCurrentValues();
			currentValuesPanel.setVisible(true);
			enableAll();
			return;
		}

		if (propertyName.equals(props.getProperty("box" + 1))) {
			algo = ((Integer) event.getNewValue()).intValue();
			cpListeners.firePropertyChange("algo", 0, algo);
			return;
		}

		if (propertyName.equals(props.getProperty("box" + 2))) {
			graphType = ((Integer) event.getNewValue()).intValue();
			cpListeners.firePropertyChange("graphType", 0, graphType);
			return;
		}

		if (propertyName.equals("set language properties")) {
			cpListeners.firePropertyChange(event);
			this.props = (Properties) event.getNewValue();
			this.resetLanguage();
		}

	}

	public void removeSlides() {
		sliders.setVisible(false);
	}

	private void resetLanguage() {
		enabledPanelBorder.setTitle(props.getProperty("leftColumn"));
		disabledPanelBorder.setTitle(props.getProperty("leftColumn"));
		this.repaint();
		closed.setText(props.getProperty("checkBoxClosed"));
		sequenceOn.setText(props.getProperty("sequenceOn"));
		resetLanguageCurrentValues();
	}

	private void resetLanguageCurrentValues() {
		currentValuesTitle.setText(props.getProperty("currentValuesTitle"));
		currentFrequency.setText(String.format(props
				.getProperty("currentFrequency"), frequency));
		currentAlgo.setText(String.format(props.getProperty("currentAlgo"),
				algoNames[algo]));
		currentGraphType.setText(String.format(props
				.getProperty("currentGraphType"), graphTypeNames[graphType]));
		if (closed.isSelected()) {
			currentClosed.setText(props.getProperty("checkBoxClosed"));
		} else {
			currentClosed.setText(props.getProperty("checkBoxNoClosed"));
		}
	}

	private void setLayout() {
		propertiesPane.setBorder(enabledPanelBorder);
		propertiesPane.setPreferredSize(new Dimension(240, 300));

		// += frequency slider
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets(0, 0, 20, 0);
		gbLayout.setConstraints(sliders, constraints);
		sliders.setVisible(false);
		buttonsPane.add(sliders);

		// += graphTypeBox
		constraints.insets = new Insets(2, 2, 2, 2);
		constraints.gridy = 1;
		constraints.anchor = GridBagConstraints.WEST;
		// constraints.fill = GridBagConstraints.HORIZONTAL;
		gbLayout.setConstraints(typePanel, constraints);
		buttonsPane.add(typePanel);
		// += algoBox
		constraints.gridy = 2;
		gbLayout.setConstraints(algoPanel, constraints);
		buttonsPane.add(algoPanel);

		// += checkbox for closed fragments
		closed.setSelected(true);
		constraints.gridy = 3;
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.WEST;
		gbLayout.setConstraints(closed, constraints);
		buttonsPane.add(closed);

		// += checkbox for sequence
		sequenceOn.setSelected(true);
		constraints.gridy = 4;
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.WEST;
		gbLayout.setConstraints(sequenceOn, constraints);
		buttonsPane.add(sequenceOn);

		currentValuesPanel.setVisible(false);
		constraints.gridy = 5;
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.WEST;
		gbLayout.setConstraints(currentValuesPanel, constraints);
		buttonsPane.add(currentValuesPanel);

		propertiesPane.add(buttonsPane, BorderLayout.NORTH);

		this.add(propertiesPane, BorderLayout.CENTER);
	}

}
