/**
 * Created on Jun 27, 2007
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 */
public class ControlTabbedPanel extends JPanel implements ActionListener,
		PropertyChangeListener, ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTabbedPane tabbedPane = null;

	private Properties props;

	private final PropertyChangeSupport ctpListeners;

	private ControlPanel tab1 = null;

	private GraphViewPanel tab2 = null;

	private ControlPanel tab3 = null;

	private int selectedTabIndex = 0;

	private JButton start = null;

	public ControlTabbedPanel(final Properties p) {
		super(new BorderLayout());
		props = p;
		ctpListeners = new PropertyChangeSupport(this);
		initTabs();
		initStartButton();
		this.resetLanguage();
	}

	public void actionPerformed(final ActionEvent event) {
		final String command = event.getActionCommand();
		System.err.println(command);

		if (command.equals(props.getProperty("startButton"))) {
			ctpListeners.firePropertyChange("start", 0, 1);
		}
		if (command.equals(props.getProperty("stepStartButton"))) {
			ctpListeners.firePropertyChange("step start", 0, 1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Container#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	@Override
	public void addPropertyChangeListener(final PropertyChangeListener l) {
		ctpListeners.addPropertyChangeListener(l);
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
		ctpListeners.addPropertyChangeListener(str, l);
	}

	private void disableAll() {
		final int counter = tabbedPane.getComponentCount();
		for (int i = 0; i < counter; i++) {
			tabbedPane.setForegroundAt(i, VisualisationConstants.disabledColor);
			tabbedPane.setEnabledAt(i, false);
		}
		start.setEnabled(false);
	}

	private void enableAll() {
		final int counter = tabbedPane.getComponentCount();
		for (int i = 0; i < counter; i++) {
			tabbedPane.setForegroundAt(i, VisualisationConstants.enabledColor);
			tabbedPane.setEnabledAt(i, true);
		}
		start.setEnabled(true);
	}

	private void initStartButton() {
		start = new JButton();
		start.setSize(25, 27);
		start.addActionListener(this);
		this.add(start, BorderLayout.SOUTH);
	}

	private void initTabs() {
		tabbedPane = new JTabbedPane();
		tabbedPane.addChangeListener(this);
		tab1 = new ControlPanel(props);
		tab2 = new GraphViewPanel(props);
		tab3 = new ControlPanel(props);

		tab1.addPropertyChangeListener("minimumFrequency", this);
		tab1.addPropertyChangeListener("set sequence", this);
		this.addPropertyChangeListener("set language properties", tab1);
		this.addPropertyChangeListener("parsed", tab1);
		this.addPropertyChangeListener(props.getProperty("running"), tab1);
		this.addPropertyChangeListener("start", tab1);
		this.addPropertyChangeListener("step start", tab1);
		this.addPropertyChangeListener("disable", tab1);
		this.addPropertyChangeListener("enable", tab1);

		tab3.addPropertyChangeListener("minimumFrequency", this);
		tab3.addPropertyChangeListener("set sequence", this);
		this.addPropertyChangeListener("set language properties", tab3);
		this.addPropertyChangeListener("parsed", tab3);
		this.addPropertyChangeListener(props.getProperty("running"), tab3);
		this.addPropertyChangeListener("start", tab3);
		this.addPropertyChangeListener("step start", tab3);
		this.addPropertyChangeListener("disable", tab3);
		this.addPropertyChangeListener("enable", tab3);

		tabbedPane.addTab(null, tab1);
		tabbedPane.addTab(null, tab2);
		tabbedPane.addTab(null, tab3);
		this.add(tabbedPane, BorderLayout.CENTER);

	}

	public void propertyChange(final PropertyChangeEvent event) {
		final String propertyName = event.getPropertyName();
		if (propertyName.equals("set language properties")) {
			this.props = (Properties) event.getNewValue();
			this.ctpListeners.firePropertyChange(event);
			this.resetLanguage();
		}

		if (propertyName.equals("parsed")) {
			ctpListeners.firePropertyChange(event);
			return;
		}
		if (propertyName.equals("minimumFrequency")) {
			ctpListeners.firePropertyChange(event);
			return;
		}

		if (propertyName.equals("disable")) {
			ctpListeners.firePropertyChange(event);
			this.disableAll();
			return;
		}

		if (propertyName.equals("enable")) {
			ctpListeners.firePropertyChange(event);
			this.enableAll();
			return;
		}

		if (propertyName.equals("algoType")) {
			ctpListeners.firePropertyChange(event);
			return;
		}

		if (propertyName.equals("graphType")) {
			ctpListeners.firePropertyChange(event);
			return;
		}

		if (propertyName.equals(props.getProperty("running"))) {
			this.start.setText(props.getProperty("running"));
			return;
		}

		if (propertyName.equals(props.getProperty("search_done"))) {
			this.start.setText(props.getProperty("startButton"));
			ctpListeners.firePropertyChange(event);
			return;
		}

		if (propertyName.equals("set language properties")) {
			ctpListeners.firePropertyChange(event);
			this.props = (Properties) event.getNewValue();
			this.resetLanguage();
		}

		if (propertyName == "set sequence") {
			ctpListeners.firePropertyChange(event);
		}

	}

	private void resetButtonText() {
		switch (selectedTabIndex) {
		case 0:
			start.setText(props.getProperty("startButton"));
			break;
		case 1:
			start.setText(props.getProperty("showButton"));
			break;
		case 2:
			start.setText(props.getProperty("stepStartButton"));
			break;
		}
	}

	private void resetLanguage() {
		final int counter = tabbedPane.getComponentCount();
		for (int i = 0; i < counter; i++) {
			tabbedPane.setTitleAt(i, props.getProperty("controlTab" + i));
			tabbedPane.setToolTipTextAt(i, props.getProperty("controlTab" + i));
		}
		resetButtonText();
	}

	public void stateChanged(final ChangeEvent event) {
		final JTabbedPane sourceTabbedPane = (JTabbedPane) event.getSource();
		selectedTabIndex = sourceTabbedPane.getSelectedIndex();
		if (start != null) {
			resetButtonText();
		}
		switch (selectedTabIndex) {
		case 0:
			this.ctpListeners.firePropertyChange("set mining view", null, null);
			break;
		case 1:
			this.ctpListeners.firePropertyChange("set graph view", null, null);
			break;
		case 2:
			this.ctpListeners
					.firePropertyChange("set stewise view", null, null);
			break;
		}

	}

}
