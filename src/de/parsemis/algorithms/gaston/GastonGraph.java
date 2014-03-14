/**
 * Created Jan 01, 2008
 * 
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * Copyright 2008 Marc Woerlein
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.algorithms.gaston;

import java.util.BitSet;
import java.util.Collection;
import java.util.Map;

import de.parsemis.graph.Graph;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.HPMutableGraph;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.miner.general.Frequency;

/**
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class GastonGraph<NodeType, EdgeType> implements
		DataBaseGraph<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final HPGraph<NodeType, EdgeType> originalGraph;

	private final int idx;

	private final Frequency freq;

	public GastonGraph(final HPGraph<NodeType, EdgeType> originalGraph,
			final int idx, final Frequency freq) {
		this.originalGraph = originalGraph;
		this.idx = idx;
		this.freq = freq;
	}

	protected void createInitials(
			final Map<NodeType, GastonPath<NodeType, EdgeType>> map,
			final Collection<NodeType> nodes,
			final Collection<Leg<NodeType, EdgeType>> siblings,
			final GastonEnvironment<NodeType, EdgeType> tenv) {
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		final GastonEmbedding<NodeType, EdgeType> parent = tenv
				.createEmbedding(this);
		// check all nodes of the database graph
		for (int node = 0; node < originalGraph.getMaxNodeIndex(); node++) {
			if (originalGraph.isValidNode(node)) {
				final NodeType label = originalGraph.getNodeLabel(node);
				if (nodes.contains(label)) {
					// get the code for the frequent label
					GastonPath<NodeType, EdgeType> code = map.get(label);

					if (code == null) {
						// create new code for the frequent label
						final HPMutableGraph<NodeType, EdgeType> subGraph = env
								.newHPGraph();
						final int newNode = subGraph.addNodeIndex(label);
						final Leg<NodeType, EdgeType> leg = new Leg<NodeType, EdgeType>(
								new Refinement(env.getNodeLabelIndex(label)),
								subGraph, newNode);
						// Initial nodes are created for thread 0
						code = new GastonPath<NodeType, EdgeType>(leg,
								siblings, 0);
						siblings.add(leg);
						map.put(label, code);
					}
					code.toHPFragment().add(tenv.createEmbedding(parent, node));
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.Frequented#frequency()
	 */
	public Frequency frequency() {
		return freq;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.DataBaseGraph#getEdges()
	 */
	public BitSet getEdges() {
		// TODO: remove infrequent ones??
		return (BitSet) originalGraph.getEdges().clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.DataBaseGraph#getIndex()
	 */
	public int getIndex() {
		return idx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.DataBaseGraph#getNodes()
	 */
	public BitSet getNodes() {
		// TODO: remove infrequent ones??
		return (BitSet) originalGraph.getNodes().clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.DataBaseGraph#toGraph()
	 */
	public Graph<NodeType, EdgeType> toGraph() {
		return originalGraph.toGraph();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.DataBaseGraph#toHPGraph()
	 */
	public HPGraph<NodeType, EdgeType> toHPGraph() {
		return originalGraph;
	}

	@Override
	public String toString() {
		return originalGraph.toString();
	}

}
