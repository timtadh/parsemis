/**
 * Created Jul 06, 2007
 * 
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * Copyright 2007 Marc Woerlein
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.visualisation;

import java.awt.Dimension;
import java.awt.Graphics;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

/**
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public interface GraphPanel extends PropertyChangeListener {

	public void addToPropertyChangeListener(JComponent propertyChanger);

	public GraphPanel clone() throws CloneNotSupportedException;

	public JComponent getComponent();

	public void paintOffscreen(Graphics graphic, Dimension d);
}
