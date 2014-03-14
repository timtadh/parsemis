/**
 * Created on Apr 24, 2007
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.parsemis.MainFrame;
import de.parsemis.graph.Graph;
import de.parsemis.graph.HPGraph;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.miner.general.Fragment;
import de.parsemis.visualisation.GraphPanel;
import de.parsemis.visualisation.chemicalVisualisation.ChemicalVisualisationSettings;
import de.parsemis.visualisation.prefuseVisualisation.PrefuseVisualisationSettings;

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
public class MyTabbedPanel<NodeType, EdgeType> extends JPanel implements
		ChangeListener, PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Object selectedGraphs[] = null;

	@SuppressWarnings("unchecked")
	public static ArrayList fragments;

	@SuppressWarnings("unchecked")
	public static ArrayList hpGraphs = null;

	@SuppressWarnings("unchecked")
	public static ArrayList embGraphs;

	private Properties props;

	private JTabbedPane tabbedPane = null;

	private JLabel errorMessage = null;

	private MyTab<NodeType, EdgeType> tabs[] = null;

	private MyListModel<NodeType, EdgeType> graphsListModel = null;

	private MyListModel<NodeType, EdgeType> fragmentsListModel = null;

	private MyListModel<NodeType, EdgeType> embListModel = null;

	private int selectedGraphIndex[] = null;

	private final PropertyChangeSupport tpListeners;

	private boolean setHighQuality = true;

	@SuppressWarnings("unused")
	private boolean setColor = false;

	@SuppressWarnings("unused")
	private boolean setCarbonLabels = false;

	@SuppressWarnings("unused")
	private boolean setSequence = true;

	private Dimension panelDimension = null;

	private int selectedTabIndex = 0;

	private final int tabsNumber = 4;

	// private int visualisationType = 0;

	public MyTabbedPanel(final Properties p) {
		super(new BorderLayout());
		props = p;
		setHighQuality = PrefuseVisualisationSettings.setHighQuality;
		setColor = ChemicalVisualisationSettings.doColoredLabels;
		setCarbonLabels = ChemicalVisualisationSettings.setCarbonLabels;

		tpListeners = new PropertyChangeSupport(this);
		tpListeners.addPropertyChangeListener("add graphs", this);
		initTabs();
		initErrorMessage();
		this.resetLanguage();
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

	@SuppressWarnings("unchecked")
	private void initTabs() {
		tabbedPane = new JTabbedPane();
		tabbedPane.addChangeListener(this);
		if (tabs == null) {
			tabs = new MyTab[tabsNumber];
			for (int i = 0; i < tabsNumber; i++) {
				tabs[i] = new MyTab<NodeType, EdgeType>(i, props);
				this.addPropertyChangeListener("remove all", tabs[i]);
				// this.addPropertyChangeListener("visualization type",
				// tabs[i]);
				this.addPropertyChangeListener("high quality", tabs[i]);
				this.addPropertyChangeListener("colored", tabs[i]);
				this.addPropertyChangeListener("carbon labels", tabs[i]);
				this.addPropertyChangeListener("export" + i, tabs[i]);
				this.addPropertyChangeListener("set language properties",
						tabs[i]);
				this.addPropertyChangeListener("set sequence", tabs[i]);
			}

			tabs[0].addPropertyChangeListener("fragment selected", this);
			tabs[1].addPropertyChangeListener("graph selected", this);
			tabs[2].addPropertyChangeListener("embedding selected", this);
			this.addPropertyChangeListener("remove old data", tabs[1]);
			this.tpListeners.firePropertyChange("high quality",
					!setHighQuality, setHighQuality);
		}
		selectedGraphs = new Object[2];
		selectedGraphIndex = new int[2];
		panelDimension = new Dimension(100, 100);
	}

	@SuppressWarnings("unchecked")
	public void propertyChange(final PropertyChangeEvent event) {
		final String propertyName = event.getPropertyName();
		System.err.println(this.getClass().getName() + " property changed "
				+ propertyName);

		if (propertyName.equals("colored")) {
			this.setColor = (Boolean) event.getNewValue();
			this.tpListeners.firePropertyChange(event);
			return;
		}

		if (propertyName.equals("carbon labels")) {
			this.setCarbonLabels = (Boolean) event.getNewValue();
			this.tpListeners.firePropertyChange(event);
			return;
		}

		if (propertyName.equals("high quality")) {
			this.setHighQuality = (Boolean) event.getNewValue();
			this.tpListeners.firePropertyChange(event);
			return;
		}

		if (propertyName.equals("add fragments")) {

			fragmentsListModel = new MyListModel<NodeType, EdgeType>(fragments,
					true);
			tabs[0].addLeftColumn(fragmentsListModel, null, 0);
			tabbedPane.addTab(null, tabs[0]);
			resetLanguage();
			this.add(tabbedPane, BorderLayout.CENTER);
			this.repaint();
			return;
		}

		if (propertyName.equals("add graphs")) {
			if (tabbedPane.getComponentCount() == 1) {
				tabbedPane.addTab(null, tabs[1]);
				resetLanguage();
			} else {
				if (tabs[1] != null) {
					this.tpListeners.firePropertyChange("remove old data",
							null, null);
				}
				hpGraphs = null;
			}
			return;
		}

		if (propertyName.equals("add embeddings")) {
			return;
		}

		if (propertyName.equals("set empty tab")) {
			setEmptyTab();
			this.repaint();
			return;
		}

		if (propertyName.equals("fragment selected")) {
			final int newValue = ((Integer) event.getNewValue()).intValue();
			final Fragment frag = MainFrame.ff[newValue];
			final HPGraph<NodeType, EdgeType> graph = frag.toGraph()
					.toHPGraph();
			selectedGraphs[0] = frag;
			tabs[0].showGraph(graph, "" + (newValue + 1));
			selectedGraphIndex[0] = newValue;
			this.tpListeners.firePropertyChange("add graphs", null, null);
			this.tpListeners.firePropertyChange("add save button", null, null);
		}

		if (propertyName.equals("graph selected")) {
			final int newValue = ((Integer) event.getNewValue()).intValue();
			final HPGraph<NodeType, EdgeType> graph = (HPGraph<NodeType, EdgeType>) hpGraphs
					.get(newValue);
			selectedGraphs[1] = graph;
			selectedGraphIndex[1] = newValue;
			tabs[1].showGraph(graph, graph.getName());
			selectedGraphIndex[1] = newValue;
			this.tpListeners.firePropertyChange("add save button", null, null);
		}

		if (propertyName.equals("remove all")) {
			this.tpListeners.firePropertyChange("remove all", null, null);
			this.clearAll();
		}

		// if (propertyName.equals("visualisation type")) {
		// int newValue = ((Integer) event.getNewValue()).intValue();
		// visualisationType = newValue;
		// this.tpListeners.firePropertyChange("visualisation type", -1,
		// newValue);
		// return;
		// }

		if (propertyName.equals("export")) {
			this.tpListeners.firePropertyChange("export" + selectedTabIndex,
					null, event.getNewValue());
		}

		if (propertyName.equals("set language properties")) {
			this.props = (Properties) event.getNewValue();
			this.tpListeners.firePropertyChange(event);
			this.resetLanguage();
		}

		if (propertyName.equals("set mining view")) {

		}

		if (propertyName.equals("set graph view")) {

		}

		if (propertyName.equals("set sequence")) {
			setSequence = (Boolean) event.getNewValue();
			this.tpListeners.firePropertyChange(event);
		}

	}

	private void resetLanguage() {
		final int counter = tabbedPane.getComponentCount();
		for (int i = 0; i < counter; i++) {
			tabbedPane.setTitleAt(i, props.getProperty("tab" + i));
			tabbedPane.setToolTipTextAt(i, props.getProperty("tab" + i));
		}
		errorMessage.setText(props.getProperty("message"));
	}

	private void setEmptyTab() {
		this.add(errorMessage, BorderLayout.CENTER);
	}

	public void setFragments(final ArrayList<HPGraph<NodeType, EdgeType>> f) {
		fragments = f;
	}

	@SuppressWarnings("unchecked")
	public void stateChanged(final ChangeEvent changeEvent) {

		final JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent
				.getSource();
		selectedTabIndex = sourceTabbedPane.getSelectedIndex();
		GraphPanel test = null;
		switch (selectedTabIndex) {
		case 0:

			// tabs[0].scrollPane.renderer.resetRenderer();
			// this.repaint();
			break;
		case 1:

			if ((hpGraphs == null) && (selectedGraphs[0] != null)) {
				hpGraphs = new ArrayList<HPGraph>();
				final Iterator<DataBaseGraph> git = ((Fragment) selectedGraphs[0])
						.graphIterator();

				while (git.hasNext()) {
					final Graph g = git.next().toGraph();
					hpGraphs.add(g.toHPGraph());
				}
				graphsListModel = new MyListModel(hpGraphs, false);
				test = GraphPanelGenerator.createPanel(panelDimension,
						((Fragment) selectedGraphs[0]).toGraph(), true);
				tabs[1].addLeftColumn(graphsListModel, test,
						selectedGraphIndex[0]);

				selectedGraphs[0] = null;
			}

			break;
		case 2:

			if (selectedGraphs[1] != null) {
				embGraphs = new ArrayList<HPGraph>();
				embListModel = new MyListModel(embGraphs, false);

				test = GraphPanelGenerator.createPanel(panelDimension,
						((HPGraph) selectedGraphs[0]).toGraph(), true);
				tabs[2]
						.addLeftColumn(embListModel, test,
								selectedGraphIndex[1]);
			} else {
				sourceTabbedPane.setSelectedIndex(1);
			}

			break;
		}
	}
}
