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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 */
public class MyComboBox extends JPanel implements ActionListener,
		PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int id = 0;

	private Properties props = null;

	private JLabel label = null;

	private JComboBox box = null;

	private GridBagLayout gbLayout = null;

	private GridBagConstraints constraints = null;

	private final PropertyChangeSupport mcbListeners;

	MyComboBox(final Properties p, final String[] names, final int n) {
		super();
		id = n;
		props = p;
		box = new JComboBox(names);
		label = new JLabel();
		label.setHorizontalAlignment(SwingConstants.LEFT);

		final Dimension textsize = new Dimension(130, 25);
		label.setMaximumSize(textsize);
		label.setSize(textsize);
		label.setPreferredSize(textsize);

		mcbListeners = new PropertyChangeSupport(this);
		setLayout();
		resetLanguage();
		box.setSelectedIndex(0);
	}

	public void actionPerformed(final ActionEvent event) {
		final int index = ((JComboBox) event.getSource()).getSelectedIndex();
		mcbListeners.firePropertyChange(props.getProperty("box" + id), -1,
				index);
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
		mcbListeners.addPropertyChangeListener(str, l);
	}

	public void propertyChange(final PropertyChangeEvent event) {
		final String propertyName = event.getPropertyName();
		if (propertyName.equals("disable")) {
			label.setEnabled(false);
			box.setEnabled(false);
		}

		if (propertyName.equals("enable")) {
			label.setEnabled(true);
			box.setEnabled(true);
		}

		if (propertyName.equals("set language properties")) {
			this.props = (Properties) event.getNewValue();
			this.resetLanguage();
		}
	}

	private void resetLanguage() {
		label.setText(props.getProperty("box" + id));
	}

	private void setLayout() {
		gbLayout = new GridBagLayout();
		constraints = new GridBagConstraints();
		this.setLayout(gbLayout);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.fill = GridBagConstraints.NONE;

		gbLayout.setConstraints(label, constraints);
		this.add(label);

		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.anchor = GridBagConstraints.LINE_END;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weighty = 0.0;
		constraints.weightx = 1.0;
		gbLayout.setConstraints(box, constraints);
		this.add(box);

		box.addActionListener(this);
	}
}
