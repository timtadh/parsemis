/**
 * Created on Jun 08, 2007
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

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 */
public class StatusPanel extends JPanel implements PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final JLabel openLabel;

	private Properties props;

	private Object[] args = null;

	private int currentStatus = 0;

	// 0 - init status field NoDatabase
	// 1 - errorMessage
	// 2 - file name with graphs number

	public StatusPanel(final Properties p) {
		super();
		props = p;
		final GridBagLayout gb = new GridBagLayout();
		final GridBagConstraints constraints = new GridBagConstraints();
		this.setLayout(gb);
		// += openLabel
		openLabel = new JLabel(props.getProperty("NoDatabase"),
				SwingConstants.LEFT);
		// + fileName,
		openLabel.setFont(new Font("Dialog", Font.BOLD, 14));

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 0.5;
		constraints.insets = new Insets(0, 0, 0, 50);
		constraints.fill = GridBagConstraints.VERTICAL;
		constraints.anchor = GridBagConstraints.EAST;
		gb.setConstraints(openLabel, constraints);
		add(openLabel);
	}

	public void parsed() {
		System.err.println("StatusPanel::parsing state");
		openLabel.setText("Blaaaaaaaaaaaaaaa");
		this.repaint();
	}

	public void propertyChange(final PropertyChangeEvent event) {
		final String propertyName = event.getPropertyName();
		if (propertyName.equals("set text")) {
			currentStatus = (Integer) event.getNewValue();
			resetLanguage();
		}

		if (propertyName.equals("set language properties")) {
			this.props = (Properties) event.getNewValue();
			this.resetLanguage();
		}

		if (propertyName.equals("set status")) {
			currentStatus = 2;
			args = (Object[]) event.getNewValue();
			resetLanguage();
		}

	}

	private void resetLanguage() {
		switch (currentStatus) {
		case 1:
			openLabel.setText(props.getProperty("NoParser"));
			break;
		case 2:
			openLabel.setText(String.format(props.getProperty("textField"),
					args));
			break;
		default:
			openLabel.setText(props.getProperty("NoDatabase"));
		}
	}
}
