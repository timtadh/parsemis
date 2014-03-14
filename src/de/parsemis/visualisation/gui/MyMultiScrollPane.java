/**
 * Created on Jul 06, 2007
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
package de.parsemis.visualisation.gui;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.parsemis.visualisation.GraphPanel;

/**
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class MyMultiScrollPane<NodeType, EdgeType> extends JScrollPane
		implements ListSelectionListener, PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final JViewport viewPort;

	public JList list;

	private final JPanel currentSelection;

	private final int tabIndex;

	// private int visualisationType = 1;

	public int listSize = 0;

	private int old = -1;

	boolean isFragmentSelected = false;

	boolean isGraphSelected = false;

	private final PropertyChangeSupport spListeners;

	public MyCellRenderer<NodeType, EdgeType> renderer = null;

	public MyMultiScrollPane(final int index) {
		super();
		this
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tabIndex = index;
		// visualisationType = visType;

		setPreferredSize(new Dimension(170, 300));
		viewPort = new JViewport();
		setViewportBorder(new EtchedBorder());

		currentSelection = new JPanel();
		viewPort.setView(currentSelection);
		setColumnHeader(viewPort);

		list = new JList();
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setSelectedIndex(0);
		list.addListSelectionListener(this);

		renderer = new MyCellRenderer<NodeType, EdgeType>();
		list.setCellRenderer(renderer);

		setViewportView(list);
		this.setBorder(VisualisationConstants.emptyBorder);
		spListeners = new PropertyChangeSupport(this);
		// this.addPropertyChangeListener("visualisation type", renderer);
		this.addPropertyChangeListener("clear all", renderer);
		this.addPropertyChangeListener("remove old data", renderer);
		this.addPropertyChangeListener("remove all", renderer);
		this.addPropertyChangeListener("colored", renderer);
		this.addPropertyChangeListener("carbon labels", renderer);
		this.addPropertyChangeListener("high quality", renderer);
		renderer.addPropertyChangeListener("repaint", this);
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
		spListeners.addPropertyChangeListener(str, l);
	}

	public void propertyChange(final PropertyChangeEvent event) {

		final String propertyName = event.getPropertyName();
		System.err.println(this.getClass().getName() + " property changed "
				+ propertyName);
		// if (propertyName.equals("visualisation type")) {
		// visualisationType = ((Integer) event.getNewValue()).intValue();
		// this.spListeners.firePropertyChange("visualisation type", -1,
		// visualisationType);
		// }

		if (propertyName.equals("remove old data")) {
			this.spListeners.firePropertyChange(event);
		}

		if (propertyName.equals("remove all")) {
			this.spListeners.firePropertyChange(event);
		}

		if (propertyName.equals("carbon labels")) {
			this.spListeners.firePropertyChange(event);
		}

		if (propertyName.equals("colored")) {
			this.spListeners.firePropertyChange(event);
		}

		if (propertyName.equals("high quality")) {
			this.spListeners.firePropertyChange(event);
		}
		if (propertyName.equals("repaint")) {
			this.repaint();
		}
	}

	public void setNewFragmentData(
			final MyListModel<NodeType, EdgeType> listModel,
			final GraphPanel fragment) {
		list.setModel(listModel);
		listSize = listModel.getSize();
		if (fragment != null) {
			fragment.addToPropertyChangeListener(this);
			// switch (visualisationType) {
			// case 0:
			// this.addPropertyChangeListener("colored", (Demo) fragment);
			// this
			// .addPropertyChangeListener("carbon labels",
			// (Demo) fragment);
			// break;
			// case 1:
			// this.addPropertyChangeListener("high quality",
			// (PrefuseDemo) fragment);
			// break;
			// }
			this.repaint();
			currentSelection.removeAll();
			fragment.getComponent().setBackground(
					VisualisationConstants.selectedFragmentColor);
			fragment.getComponent().setPreferredSize(
					VisualisationConstants.cellElementDimension);
			currentSelection
					.setBorder(VisualisationConstants.selectedFragmentBorder);
			currentSelection.add(fragment.getComponent());
			isFragmentSelected = true;
			isGraphSelected = false;
		}
	}

	public void valueChanged(final ListSelectionEvent e) {
		final int index = list.getSelectedIndex();
		System.err.println(this.getClass().getName() + " value changed " + old
				+ "-" + index + "/" + tabIndex);
		this.spListeners.fireIndexedPropertyChange("selected", tabIndex, old,
				index);
		old = index;
		// if ((e.getValueIsAdjusting() == false) && (index >= 0)) {
		// switch (tabIndex) {
		// case 0:
		// this.spListeners.firePropertyChange("fragment selected", -1,
		// index);
		//
		// break;
		// case 1:
		// this.spListeners
		// .firePropertyChange("graph selected", -1, index);
		// break;
		// }
		// }
	}

}
