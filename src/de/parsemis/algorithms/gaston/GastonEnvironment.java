/**
 * Created Jan 05, 2008
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

package de.parsemis.algorithms.gaston;

import java.util.ArrayList;
import java.util.Collection;

import de.parsemis.graph.Edge;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.HPMutableGraph;
import de.parsemis.miner.chain.ExtensionSet;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.environment.ThreadEnvironment;
import de.parsemis.miner.general.HPEmbedding;
import de.parsemis.strategy.SMPThread;
import de.parsemis.utils.SynchronizedCounter;

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
public class GastonEnvironment<NodeType, EdgeType> extends
		ThreadEnvironment<NodeType, EdgeType> {

	/** the index of the thread this environment is created for */
	public final int threadIdx;

	public final boolean doCycles;

	public final boolean hierarchical;

	private final Leg<NodeType, EdgeType> node_depth[][];

	private final Leg<NodeType, EdgeType> cycle[][];
	private final SynchronizedCounter counter;

	private final Collection<Leg<NodeType, EdgeType>> node_depth_set;
	private final Collection<Leg<NodeType, EdgeType>> cycle_set;

	/**
	 * creates a LegSet dimensioned for the given sizes
	 * 
	 * @param threadIdx
	 * @param maxEdgeLabels
	 * @param maxNodeLabels
	 * @param maxNodes
	 * @param doCycles
	 * @param hierarchical
	 * @param counter
	 */
	@SuppressWarnings("unchecked")
	GastonEnvironment(final int threadIdx, final int maxEdgeLabels,
			final int maxNodeLabels, final int maxNodes,
			final boolean doCycles, final boolean hierarchical,
			final SynchronizedCounter counter) {
		this.threadIdx = threadIdx;
		this.doCycles = doCycles;
		this.hierarchical = hierarchical;
		System.out.println("hierachical: " + hierarchical);
		node_depth = new Leg[maxEdgeLabels][maxNodeLabels];
		node_depth_set = new ArrayList<Leg<NodeType, EdgeType>>();
		cycle = doCycles ? new Leg[maxEdgeLabels][maxNodes] : null;
		cycle_set = new ArrayList<Leg<NodeType, EdgeType>>();
		this.counter = counter;
	}

	public final ExtensionSet<NodeType, EdgeType, Leg<NodeType, EdgeType>> clearAndAddExtensions(
			final ExtensionSet<NodeType, EdgeType, Leg<NodeType, EdgeType>> ret) {
		for (final Leg<NodeType, EdgeType> ack : node_depth_set) {
			ret.add(ack);
			node_depth[ack.ref.getEdgeLabel()][ack.ref.getToLabel()] = null;
		}
		node_depth_set.clear();
		for (final Leg<NodeType, EdgeType> ack : cycle_set) {
			ret.add(ack);
			cycle[ack.ref.getEdgeLabel()][ack.ref.getNodeB()] = null;
		}
		cycle_set.clear();
		return ret;
	}

	public GastonEmbedding<NodeType, EdgeType> createEmbedding(
			final GastonGraph<NodeType, EdgeType> superGraph) {
		if (hierarchical) {
			return new GastonEmbedding_hierarchical<NodeType, EdgeType>(
					superGraph);
		} else {
			return new GastonEmbedding_flat<NodeType, EdgeType>(superGraph,
					counter.next());
		}
	}

	public GastonEmbedding<NodeType, EdgeType> createEmbedding(
			final HPEmbedding<NodeType, EdgeType> o, final int newSuperNode) {
		final GastonEmbedding<NodeType, EdgeType> old = (GastonEmbedding<NodeType, EdgeType>) o;
		if (hierarchical) {
			return new GastonEmbedding_hierarchical<NodeType, EdgeType>(
					(GastonEmbedding_hierarchical<NodeType, EdgeType>) old
							.get(), newSuperNode);
		} else {
			return new LazyExtendedEmbedding_flat<NodeType, EdgeType>(
					(GastonEmbedding_flat<NodeType, EdgeType>) old.get(),
					newSuperNode, counter.next());
		}
	}

	/**
	 * searches or creates a Leg in the LegSeg for a cycle refinement
	 * 
	 * @param fromNode
	 * @param edgeLabel
	 * @param toNode
	 * @param parent
	 * @return the Leg for the corresponding refinement
	 */
	public final Leg<NodeType, EdgeType> getCycle(final int fromNode,
			final int edgeLabel, final int toNode,
			final HPGraph<NodeType, EdgeType> parent) {
		Leg<NodeType, EdgeType> l = cycle[edgeLabel][toNode];
		if (l == null) {
			final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
					.env(this);

			// extend graph
			final HPMutableGraph<NodeType, EdgeType> next = (HPMutableGraph<NodeType, EdgeType>) parent
					.clone();
			next.addEdgeIndex(fromNode, toNode, env.getEdgeLabel(edgeLabel),
					Edge.UNDIRECTED);

			l = new Leg<NodeType, EdgeType>(new CycleRefinement(fromNode,
					edgeLabel, toNode), next, HPGraph.NO_NODE);
			cycle[edgeLabel][toNode] = l;
			cycle_set.add(l);
		}
		return l;
	}

	/**
	 * searches or creates a Leg in the LegSeg for a death refinement
	 * 
	 * @param fromNode
	 * @param depth
	 * @param edgeLabel
	 * @param nodeLabel
	 * @param parent
	 * @param rmpNodes
	 * @return the Leg for the corresponding refinement
	 */
	public final Leg<NodeType, EdgeType> getDepth(final int fromNode,
			final int edgeLabel, final int nodeLabel, final int depth,
			final HPGraph<NodeType, EdgeType> parent, final int[] rmpNodes) {
		Leg<NodeType, EdgeType> l = node_depth[edgeLabel][nodeLabel];
		if (l == null) {
			final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
					.env(this);

			// extend graph
			final HPMutableGraph<NodeType, EdgeType> next = (HPMutableGraph<NodeType, EdgeType>) parent
					.clone();
			final int nextNode = next.addNodeAndEdgeIndex(fromNode, env
					.getNodeLabel(nodeLabel), env.getEdgeLabel(edgeLabel),
					Edge.UNDIRECTED);

			// adjust rightmost path
			final int[] rmp = rmpNodes.clone();
			rmp[depth] = nextNode;

			l = new Leg<NodeType, EdgeType>(new DepthRefinement(depth,
					edgeLabel, nodeLabel, rmp), next, nextNode);
			node_depth[edgeLabel][nodeLabel] = l;
			node_depth_set.add(l);
		}
		return l;
	}

	/**
	 * searches or creates a Leg in the LegSeg for a node refinement
	 * 
	 * @param fromNode
	 * @param edgeLabel
	 * @param nodeLabel
	 * @param parent
	 * @return the Leg for the corresponding refinement
	 */
	public final Leg<NodeType, EdgeType> getNode(final int fromNode,
			final int edgeLabel, final int nodeLabel,
			final HPGraph<NodeType, EdgeType> parent) {
		Leg<NodeType, EdgeType> l = node_depth[edgeLabel][nodeLabel];
		if (l == null) {
			final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
					.env(this);

			// extend graph
			final HPMutableGraph<NodeType, EdgeType> next = (HPMutableGraph<NodeType, EdgeType>) parent
					.clone();
			final int nextNode = next.addNodeAndEdgeIndex(fromNode, env
					.getNodeLabel(nodeLabel), env.getEdgeLabel(edgeLabel),
					Edge.UNDIRECTED);

			l = new Leg<NodeType, EdgeType>(new Refinement(fromNode, edgeLabel,
					nodeLabel), next, nextNode);
			node_depth[edgeLabel][nodeLabel] = l;
			node_depth_set.add(l);
		}
		return l;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GastonEnvironment of thread " + threadIdx
		// + "\nEmbeddingCount: " + GastonEmbedding_flat.getCount()
		;
	}

	public boolean check(final int idx) {
		if (idx != threadIdx) {
			return false;
		}
		final Thread c = Thread.currentThread();
		if (c instanceof SMPThread) {
			@SuppressWarnings("unchecked")
			final
			SMPThread<NodeType, EdgeType> cur = (SMPThread<NodeType, EdgeType>) c;
			return threadIdx == cur.getIdx();
		} else {
			return threadIdx == 0;
		}
	}
}
