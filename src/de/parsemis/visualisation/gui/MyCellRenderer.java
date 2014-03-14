/**
 * Created on Jan 25, 2007
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import de.parsemis.graph.HPGraph;

/**
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class MyCellRenderer<NodeType, EdgeType> extends JPanel implements
		ListCellRenderer, PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private GridBagLayout gb = null;

	private GridBagConstraints constraints = null;

	private JPanel background = null;

	private final JLabel name;

	private HashSet<Integer> visitedElements = null;

	private Hashtable<Integer, JComponent> renderedElements = null;

	// default value is set to prefuse visualization
	// private int visualisationType = 1;

	@SuppressWarnings("unused")
	private boolean setColor = true;

	@SuppressWarnings("unused")
	private boolean setCarbonLabels = false;

	@SuppressWarnings("unused")
	private boolean setHighQuality = true;

	PropertyChangeSupport crListeners = null;

	public MyCellRenderer() {
		// visualisationType = visType;
		gb = new GridBagLayout();
		constraints = new GridBagConstraints();
		name = new JLabel();
		name.setAlignmentX(Component.CENTER_ALIGNMENT);
		constraints.gridx = 0;
		constraints.gridy = 0;
		gb.setConstraints(name, constraints);
		this.add(name);

		background = new JPanel();
		background.setBorder(VisualisationConstants.emptyBorder);
		background.setPreferredSize(new Dimension(110, 110));
		constraints.gridx = 0;
		constraints.gridy = 1;
		gb.setConstraints(background, constraints);
		this.add(background);
		this.setBackground(Color.white);
		this.setBorder(VisualisationConstants.standardBorder);
		this.setLayout(gb);
		visitedElements = new HashSet<Integer>();
		renderedElements = new Hashtable<Integer, JComponent>();
		crListeners = new PropertyChangeSupport(this);

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
		crListeners.addPropertyChangeListener(str, l);
	}

	public Component getListCellRendererComponent(final JList list,
			final Object value, final int index, final boolean isSelected,
			final boolean cellHasFocus) {
		@SuppressWarnings("unchecked")
		final MyListModel<NodeType, EdgeType> myListModel = (MyListModel<NodeType, EdgeType>) list
				.getModel();
		background.removeAll();
		final String text = (String) value;
		name.setText(text);
		if (visitedElements.contains(index)) {
			JComponent demo = null;
			// Die Graphen nacheinanderer renden und die gerendeten Graphen
			// zwischenspeichern
			if (renderedElements.containsKey(index)) {
				demo = renderedElements.get(index);
			} else {
				final HPGraph<NodeType, EdgeType> current = myListModel
						.getListElementAt(index);
				// switch (visualisationType) {
				// case 0:
				// demo = new Demo(new DataAnalyser(current), setCarbonLabels,
				// false, setColor, true,
				// VisualisationConstants.cellElementDimension);
				// break;
				// default:
				// // Die Liste in prefuse-Fall nochmal renden, weil sonst
				// // werden die ersten Listenelemente nur bei dem naechsten
				// // paintComponent Aufruf sichtbar, warum auch immer
				// }
				demo = GraphPanelGenerator.createPanel(
						VisualisationConstants.cellElementDimension,
						current.toGraph(), true).getComponent();
				this.crListeners.firePropertyChange("repaint", null, null);
				renderedElements.put(index, demo);
			}
			background.add(demo);

		} else {
			// Bei der Aufbau der ScrollPanel muss zuerst die Liste mit
			// Fragmenten erzeugt werden
			// In diesem Durchlauf dann keine Graphen erzeugen,leere JPanel
			// zurueckgeben, um die Zeit zu sparen
			visitedElements.add(index);
		}
		// Wenn ein Fragment ausgewaehlt wird, dann wird ein Rahmen um die
		// Komponente sichtbar
		if (isSelected) {
			background.setBackground(VisualisationConstants.cellBorderColor);
		} else {
			background.setBackground(list.getBackground());
		}
		return this;
	}

	public void propertyChange(final PropertyChangeEvent event) {
		final String propertyName = event.getPropertyName();
		// if (propertyName.equals("visualisation type")) {
		// visualisationType = ((Integer) event.getNewValue()).intValue();
		// }

		if (propertyName.equals("remove old data")) {
			visitedElements.clear();
			renderedElements.clear();
		}

		if (propertyName.equals("remove all")) {
			visitedElements.clear();
			renderedElements.clear();
		}
		if (propertyName.equals("carbon labels")) {
			setCarbonLabels = ((Boolean) event.getNewValue()).booleanValue();
			renderedElements.clear();
			this.crListeners.firePropertyChange("repaint", null, null);
		}

		if (propertyName.equals("colored")) {
			setColor = ((Boolean) event.getNewValue()).booleanValue();
			renderedElements.clear();
			this.crListeners.firePropertyChange("repaint", null, null);
		}
		if (propertyName.equals("high quality")) {
			setHighQuality = ((Boolean) event.getNewValue()).booleanValue();
			renderedElements.clear();
			this.crListeners.firePropertyChange("repaint", null, null);
		}
	}
}