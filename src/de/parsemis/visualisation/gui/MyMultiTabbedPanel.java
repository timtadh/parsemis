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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.parsemis.graph.HPGraph;
import de.parsemis.miner.chain.Extender;
import de.parsemis.miner.chain.SearchLatticeNode;
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
public class MyMultiTabbedPanel<NodeType, EdgeType> extends JPanel implements
		ChangeListener, PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static Object selectedGraphs[] = null;

	@SuppressWarnings("unchecked")
	public static ArrayList fragments;

	@SuppressWarnings("unchecked")
	public static ArrayList hpGraphs = null;

	// private MyTab tabs[] = null;

	@SuppressWarnings("unchecked")
	public static ArrayList embGraphs;

	private Properties props;

	private JTabbedPane tabbedPane = null;

	private JLabel errorMessage = null;

	private final LinkedList<MyMultiTab<NodeType, EdgeType>> tabs = new LinkedList<MyMultiTab<NodeType, EdgeType>>();

	@SuppressWarnings("unused")
	private MyListModel<NodeType, EdgeType> graphsListModel = null;

	@SuppressWarnings("unused")
	private MyListModel<NodeType, EdgeType> fragmentsListModel = null;

	@SuppressWarnings("unused")
	private MyListModel<NodeType, EdgeType> embListModel = null;

	@SuppressWarnings("unused")
	private int selectedGraphIndex[] = null;

	private final PropertyChangeSupport tpListeners;

	@SuppressWarnings("unused")
	private boolean setSequence = true;

	private Dimension panelDimension = null;

	private int selectedTabIndex = 0;

	@SuppressWarnings("unused")
	private final int tabsNumber = 4;

	@SuppressWarnings("unused")
	private int visualisationType = 0;

	public Extender<NodeType, EdgeType> ext = null;

	public MyMultiTabbedPanel(final Properties p) {
		super(new BorderLayout());
		props = p;

		tpListeners = new PropertyChangeSupport(this);
		tpListeners.addPropertyChangeListener("add graphs", this);
		initTabs();
		initErrorMessage();
		this.resetLanguage();
	}

	public void addLayer(
			final List<SearchLatticeNode<NodeType, EdgeType>> nodes,
			final int pos, final GraphPanel selected) {
		while (tabbedPane.getTabCount() > pos) {
			final MyMultiTab<NodeType, EdgeType> r = tabs.remove(pos);
			tpListeners.removePropertyChangeListener(r);
			tabbedPane.removeTabAt(pos);
		}
		MyMultiTab<NodeType, EdgeType> t = null;
		if (nodes.size() != 0) {
			t = new MyMultiTab<NodeType, EdgeType>(pos, props, nodes, selected,
					this);
			tabbedPane.insertTab(String.format(props.getProperty("multitab"),
					pos), null, t, String.format(props.getProperty("multitab"),
					pos), pos);
			this.add(tabbedPane, BorderLayout.CENTER);
			t.addPropertyChangeListener("selected", this);
			tabs.add(t);
		}
		repaint();
		// }
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
		tpListeners.addPropertyChangeListener(str, l);
	}

	private void clearAll() {
		tabbedPane.removeAll();
		this.removeAll();
		this.repaint();
		selectedGraphs = new Object[2];
		selectedGraphIndex = new int[2];
		graphsListModel = null;
		fragmentsListModel = null;
		embListModel = null;
		hpGraphs = null;

	}

	private void initErrorMessage() {
		errorMessage = new JLabel();
		errorMessage.setHorizontalAlignment(SwingConstants.CENTER);
	}

	private void initTabs() {
		tabbedPane = new JTabbedPane();
		tabbedPane.addChangeListener(this);
		panelDimension = new Dimension(100, 100);
	}

	@SuppressWarnings("unchecked")
	public void propertyChange(final PropertyChangeEvent event) {
		final String propertyName = event.getPropertyName();
		System.err.println(this.getClass().getName() + " property changed "
				+ propertyName);

		if (propertyName.equals("selected")) {
			final int index = ((Integer) event.getNewValue()).intValue();
			final int tabIndex = ((IndexedPropertyChangeEvent) event)
					.getIndex();
			final MyMultiTab tab = tabs.get(tabIndex);
			final SearchLatticeNode<NodeType, EdgeType> node = (SearchLatticeNode<NodeType, EdgeType>) tab.nodes
					.get(index);
			// System.err.println("selected is node " + node.toString()
			// + " on tab " + tabIndex + " with index " + index);
			final Collection<SearchLatticeNode<NodeType, EdgeType>> next = ext
					.getChildren(node);
			tab.selectedGraph = node.toHPFragment().toHPGraph();
			tab.selectedGraphChildren = next.size();
			final GraphPanel p = GraphPanelGenerator.createPanel(
					panelDimension, node.toFragment().toGraph(), true);
			this
					.addLayer(
							(next instanceof List) ? (List<SearchLatticeNode<NodeType, EdgeType>>) next
									: new ArrayList<SearchLatticeNode<NodeType, EdgeType>>(
											next), tabIndex + 1, p);
			tab.showGraph(node.toHPFragment().toHPGraph(), index);
			resetLanguage();
			this.repaint();
		}

		if (propertyName.equals("set empty tab")) {
			setEmptyTab();
			this.repaint();
			return;
		}

		if (propertyName.equals("remove all")) {
			this.tpListeners.firePropertyChange("remove all", null, null);
			this.clearAll();
			return;
		}

		if (propertyName.equals("visualisation type")) {
			final int newValue = ((Integer) event.getNewValue()).intValue();
			visualisationType = newValue;
			this.tpListeners.firePropertyChange("visualisation type", -1,
					newValue);
			return;
		}

		if (propertyName.equals("export")) {
			this.tpListeners.firePropertyChange("export" + selectedTabIndex,
					null, event.getNewValue());
			return;
		}

		if (propertyName.equals("set language properties")) {
			this.props = (Properties) event.getNewValue();
			this.tpListeners.firePropertyChange(event);
			this.resetLanguage();
			return;
		}

		if (propertyName.equals("set mining view")) {
			return;
		}

		if (propertyName.equals("set graph view")) {
			return;
		}

		if (propertyName.equals("set sequence")) {
			setSequence = (Boolean) event.getNewValue();
			this.tpListeners.firePropertyChange(event);
			return;
		}
		this.tpListeners.firePropertyChange(event);

	}

	private void resetLanguage() {
		final int counter = tabbedPane.getComponentCount();
		for (int i = 0; i < counter; i++) {
			tabbedPane.setTitleAt(i, String.format(props
					.getProperty("multitab"), i));
			tabbedPane.setToolTipTextAt(i, String.format(props
					.getProperty("multitab"), i));
		}
		errorMessage.setText(props.getProperty("message"));
	}

	private void setEmptyTab() {
		// this.add(errorMessage, BorderLayout.CENTER);
	}

	public void setFragments(final ArrayList<HPGraph<NodeType, EdgeType>> f) {
		// fragments = f;
	}

	public void stateChanged(final ChangeEvent changeEvent) {

		final JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent
				.getSource();
		selectedTabIndex = sourceTabbedPane.getSelectedIndex();
		System.err.println("stateChanged to index " + selectedTabIndex);
		this.repaint();
	}
}
