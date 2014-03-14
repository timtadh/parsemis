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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Properties;

import javax.swing.JPanel;

/**
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 */
public class GraphViewPanel extends JPanel implements ActionListener,
		PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private Properties props = null;

	public GraphViewPanel(final Properties p) {
		props = p;
	}

	public void actionPerformed(final ActionEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void propertyChange(final PropertyChangeEvent arg0) {
		// TODO Auto-generated method stub

	}

}
