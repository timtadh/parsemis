/**
 * created 01.06.2006
 *
 * @by Tobias Werth (sitowert@i2.informatik.uni-erlangen.de)
 *
 * Copyright 2006 Tobias Werth
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.algorithms.dagminer;

import static de.parsemis.miner.environment.Debug.INFO;
import static de.parsemis.miner.environment.Debug.VVERBOSE;
import static de.parsemis.miner.environment.Debug.err;
import static de.parsemis.miner.environment.Debug.out;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;

import de.parsemis.graph.Graph;
import de.parsemis.graph.GraphFactory;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.HPMutableGraph;
import de.parsemis.miner.chain.CanonicalPruningStep;
import de.parsemis.miner.chain.EdgeCountStep;
import de.parsemis.miner.chain.Extender;
import de.parsemis.miner.chain.MiningStep;
import de.parsemis.miner.chain.NodeCountStep;
import de.parsemis.miner.chain.SearchLatticeNode;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.environment.Settings;
import de.parsemis.miner.general.DataBase;
import de.parsemis.miner.general.Fragment;
import de.parsemis.miner.general.HPFragment;
import de.parsemis.utils.Generic;
import de.parsemis.utils.IntIterator;

/**
 * @author Tobias Werth (sitowert@i2.informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class Algorithm<NodeType, EdgeType> implements
		de.parsemis.algorithms.Algorithm<NodeType, EdgeType>,
		Generic<NodeType, EdgeType> {

	private class DAGIterator implements
			Iterator<SearchLatticeNode<NodeType, EdgeType>> {
		final Iterator<DAGmSearchLatticeNode<NodeType, EdgeType>> entryIterator;

		DAGmSearchLatticeNode<NodeType, EdgeType> lastNode = null;

		DAGIterator(
				final Collection<DAGmSearchLatticeNode<NodeType, EdgeType>> initials) {
			entryIterator = initials.iterator();
		}

		public boolean hasNext() {
			return entryIterator.hasNext();
		}

		public SearchLatticeNode<NodeType, EdgeType> next() {
			lastNode = entryIterator.next();
			return lastNode;
		}

		public void remove() {
			entryIterator.remove();
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Collection<DAGmSearchLatticeNode<NodeType, EdgeType>> initialFragments;

	private Settings<NodeType, EdgeType> settings;

	/**
	 * generates a new dagminer algorithm
	 */
	public Algorithm() {
		// TODO
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Algorithm#getExtender(int)
	 */
	public Extender<NodeType, EdgeType> getExtender(final int threadIdx) {
		final DAGmExtender<NodeType, EdgeType> extender = new DAGmExtender<NodeType, EdgeType>();
		MiningStep<NodeType, EdgeType> curFirst = extender;
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);

		if (!settings.singleRooted) {
			curFirst = new DAGmNewRootExtension<NodeType, EdgeType>(curFirst);
			if (settings.connectedFragments) {
				out.println("doppeltes UnconnectedPruning");
				curFirst = new DAGmUnconnectedPruning<NodeType, EdgeType>(
						curFirst);
			}
		}

		curFirst = new DAGmNewLevelExtension<NodeType, EdgeType>(curFirst);
		curFirst = new DAGmNewNodeExtension<NodeType, EdgeType>(curFirst);
		curFirst = new DAGmNewEdgeExtension<NodeType, EdgeType>(curFirst);
		curFirst = new CanonicalPruningStep<NodeType, EdgeType>(curFirst);

		if (settings.miningFactory != null) {
			try {
				final MiningStep<NodeType, EdgeType> tmp = settings.miningFactory
						.createMiningStep(curFirst);
				curFirst = tmp;
			} catch (final UnsupportedOperationException uo) {
				if (INFO) {
					err.println("couldn't create miningFactory: " + uo);
				}
			}
		}

		if (!settings.singleRooted && settings.connectedFragments) {
			curFirst = new DAGmUnconnectedPruning<NodeType, EdgeType>(curFirst);
		}

		if (env.minNodeCount > 0 || env.maxNodeCount < Integer.MAX_VALUE) {
			curFirst = new NodeCountStep<NodeType, EdgeType>(curFirst,
					env.minNodeCount, env.maxNodeCount);
		}
		if (env.minEdgeCount > 0 || env.maxEdgeCount < Integer.MAX_VALUE) {
			curFirst = new EdgeCountStep<NodeType, EdgeType>(curFirst,
					env.minEdgeCount, env.maxEdgeCount);
		}

		extender.setFirst(curFirst);
		return extender;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Algorithm#initialize(java.util.Collection,
	 *      de.parsemis.graph.GraphFactory, de.parsemis.Settings)
	 */
	public Collection<Fragment<NodeType, EdgeType>> initialize(
			final Collection<Graph<NodeType, EdgeType>> graphs,
			final GraphFactory<NodeType, EdgeType> factory,
			final Settings<NodeType, EdgeType> settings) {

		this.settings = settings;
		final DataBase<NodeType, EdgeType> db = new DataBase<NodeType, EdgeType>(
				graphs, settings);

		final SortedSet<NodeType> frequentNodes = db.frequentNodeLabels();
		final SortedSet<EdgeType> frequentEdges = db.frequentEdgeLabels();

		assert frequentEdges.size() == 1 : "DAGminer can not handle more than one edge label yet, found: "
				+ frequentEdges.size();

		LocalEnvironment.create(settings, graphs.size(),
				new ArrayList<NodeType>(frequentNodes),
				new ArrayList<EdgeType>(frequentEdges));

		// initialize DAGminer
		final HashMap<NodeType, HPFragment<NodeType, EdgeType>> usedFragments = new HashMap<NodeType, HPFragment<NodeType, EdgeType>>();
		initialFragments = new HashSet<DAGmSearchLatticeNode<NodeType, EdgeType>>();

		// create initial fragments
		if (INFO) {
			out.println("FREQUENT NODES");
			for (final NodeType freqNode : frequentNodes) {
				out.println("Node >"
						+ freqNode
						+ "<:  "
						+ db.nodeFreq(freqNode)
						+ " \t-> mapped to: "
						+ LocalEnvironment.env(this)
								.getNodeLabelIndex(freqNode));
			}
		}

		int i = 0;
		for (final Graph<NodeType, EdgeType> graph : graphs) {
			final HPGraph<NodeType, EdgeType> actHPGraph = graph.toHPGraph();
			final DAGmGraph<NodeType, EdgeType> actGraph = new DAGmGraph<NodeType, EdgeType>(
					actHPGraph, graph.getID(), settings.getFrequency(graph));
			final IntIterator nodeIt = actHPGraph.nodeIndexIterator();

			// add graph to local environment
			LocalEnvironment.env(this).setDataBaseGraph(i, actGraph);
			i++;

			while (nodeIt.hasNext()) {
				final int actNode = nodeIt.next();
				final NodeType actLabel = actHPGraph.getNodeLabel(actNode);

				if (db.nodeFreq(actLabel).compareTo(
						LocalEnvironment.env(this).minFreq) >= 0) {

					HPFragment<NodeType, EdgeType> actFragment = null;
					if (!usedFragments.containsKey(actLabel)) {
						// first occurrence -> construct fragment first
						final HPMutableGraph<NodeType, EdgeType> subGraph = LocalEnvironment
								.env(this).newHPGraph();
						subGraph.addNodeIndex(actLabel);

						// single node always has topological level 1
						final int nodeLevel[] = new int[] { 1 };
						actFragment = new DAGmFragment<NodeType, EdgeType>(
								subGraph, nodeLevel);
						usedFragments.put(actLabel, actFragment);
						initialFragments
								.add(new DAGmSearchLatticeNode<NodeType, EdgeType>(
										actFragment));
					} else {
						actFragment = usedFragments.get(actLabel);
					}
					final int superNodes[] = new int[] { actNode };

					final DAGmHPEmbedding<NodeType, EdgeType> actEmbedding = new DAGmHPEmbedding<NodeType, EdgeType>();
					actEmbedding.set(actGraph, actFragment.toHPGraph(),
							superNodes);
					usedFragments.get(actLabel).add(actEmbedding);
				} else {
					if (VVERBOSE) {
						out.println("infrequent node " + actLabel);
					}
				}
			}
		}

		if (!settings.embeddingBased) {
			// filter graphBased infrequent initial nodes
			for (final Iterator<DAGmSearchLatticeNode<NodeType, EdgeType>> eit = initialFragments
					.iterator(); eit.hasNext();) {
				final DAGmSearchLatticeNode<NodeType, EdgeType> code = eit
						.next();
				if (settings.minFreq.compareTo(code.frequency()) > 0) {
					eit.remove();
				}

			}
		}

		if (INFO) {
			out.println("initial fragments:" + initialFragments.size());
		}

		final Collection<Fragment<NodeType, EdgeType>> expectedFragments = new HashSet<Fragment<NodeType, EdgeType>>();
		return expectedFragments;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Algorithm#initialNodes()
	 */
	public Iterator<SearchLatticeNode<NodeType, EdgeType>> initialNodes() {
		return new DAGIterator(initialFragments);
	}

}