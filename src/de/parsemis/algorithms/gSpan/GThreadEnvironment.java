/**
 * created May 23, 2006
 * 
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * Copyright 2006 Marc Woerlein
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.algorithms.gSpan;

import static de.parsemis.miner.environment.Debug.VVERBOSE;
import static de.parsemis.miner.environment.Debug.out;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

import de.parsemis.graph.Graph;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.HPListGraph;
import de.parsemis.graph.HPMutableGraph;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.environment.ThreadEnvironment;
import de.parsemis.miner.general.Fragment;
import de.parsemis.miner.general.GraphBasedFragment;
import de.parsemis.miner.general.HPFragment;

/**
 * Represents the thread local object pool for the gSpan algorithm.
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
public final class GThreadEnvironment<NodeType, EdgeType> extends
		ThreadEnvironment<NodeType, EdgeType> {

	// counter for generated embedding objects
	static int embeddings = 0;

	private final boolean embeddingBased;

	private final boolean storeHierachicalEmbeddings;

	private final boolean usePooling;

	/** the index of the thread this environment is created for */
	public final int threadIdx;

	/** the one and only ExtensionSet for each thread */
	private final MinExtensionSet<NodeType, EdgeType> mes = new MinExtensionSet<NodeType, EdgeType>(
			this);

	private EmbeddingBasedHPFragment<NodeType, EdgeType> firstEHPFrag;;

	private GraphBasedHPFragment<NodeType, EdgeType> firstGHPFrag;

	private GSpanHPEmbedding_flat<NodeType, EdgeType> firstHPEmbedding = null;

	private GSpanHPEmbedding_hierarchical<NodeType, EdgeType> firstHPEmbedding_hierarchical = null;

	HashMap<NodeType, HPMutableGraph<NodeType, EdgeType>> singleNodedGraphs = new HashMap<NodeType, HPMutableGraph<NodeType, EdgeType>>();

	// Initial hierarchical embeddings are stored sorted by graphs and nodes
	GSpanHPEmbedding_hierarchical<NodeType, EdgeType>[][] initialEmbeddings = null;

	private GSpanEdge<NodeType, EdgeType> firstGSpanEdge = null;

	private GSpanExtension<NodeType, EdgeType> firstGSpanExtension = null;

	private MinExtension<NodeType, EdgeType> firstMinExtension = null;

	/**
	 * creates a new environment
	 * 
	 * @param threadIdx
	 */
	public GThreadEnvironment(final int threadIdx) {
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		embeddingBased = env.embeddingBased;
		usePooling = env.usePooling;
		storeHierachicalEmbeddings = env.storeHierarchicalEmbeddings;
		this.threadIdx = threadIdx;
		if (VVERBOSE) {
			out.println("gThreadEnvironment " + threadIdx + " created");
		}
	}

	/**
	 * @param me
	 * @param first
	 * @param last
	 * @param parents
	 * @return a newly initialized DFSCode
	 */
	public final DFSCode<NodeType, EdgeType> getCode(
			final HPFragment<NodeType, EdgeType> me,
			final GSpanEdge<NodeType, EdgeType> first,
			final GSpanEdge<NodeType, EdgeType> last,
			final ArrayList<GSpanEdge<NodeType, EdgeType>> parents) {
		return new DFSCode<NodeType, EdgeType>(this).set(me, first, last,
				parents);
	}

	/**
	 * 
	 * @param nodeA
	 * @param nodeB
	 * @param labelA
	 * @param edgeLabel
	 * @param labelB
	 * @param direction
	 * @return a newly initialized GSpanEdge according to given parameters
	 */
	public final GSpanEdge<NodeType, EdgeType> getEdge(final int nodeA,
			final int nodeB, final int labelA, final int edgeLabel,
			final int labelB, final int direction) {
		return nextGSpanEdge().set(nodeA, nodeB, labelA, edgeLabel, labelB,
				direction);
	}

	/**
	 * @param code
	 * @param dbGraph
	 * @param superNodes
	 * @return a newly initialized embedding according to given parameters
	 */
	public final GSpanEmbedding<NodeType, EdgeType> getEmbedding(
			final DFSCode<NodeType, EdgeType> code,
			final GSpanGraph<NodeType, EdgeType> dbGraph, final int[] superNodes) {
		return new GSpanEmbedding<NodeType, EdgeType>(this).set(code, dbGraph,
				superNodes);
	}

	/**
	 * @param gEdge
	 * @param frag
	 * @return a newly initialized GSpanExtension
	 */
	public final GSpanExtension<NodeType, EdgeType> getExtension(
			final GSpanEdge<NodeType, EdgeType> gEdge,
			final HPFragment<NodeType, EdgeType> frag) {
		final GSpanExtension<NodeType, EdgeType> ret = nextGSpanExtension();
		ret.edge = gEdge;
		ret.frag = frag;
		return ret;
	}

	/** @return the one and only ExtensionSet for each thread */
	public final MinExtensionSet<NodeType, EdgeType> getExtensionSet() {
		return mes;
	}

	/**
	 * @param graph
	 * @return a newly initialized fragment according to the local settings
	 */
	public final Fragment<NodeType, EdgeType> getFragment(
			final Graph<NodeType, EdgeType> graph) {
		if (embeddingBased) {
			return nextEHPFragment().set(graph.toHPGraph()).toFragment();
		} else {
			return new GraphBasedFragment<NodeType, EdgeType>(graph);
		}
	}

	/**
	 * @param code
	 * @param dbGraph
	 * @param superNode
	 * @param parent
	 * @param freeEdges
	 * @return a newly initialized embedding according to given parameters
	 */
	public final GSpanHPEmbedding<NodeType, EdgeType> getHPEmbedding(
			final DFSCode<NodeType, EdgeType> code,
			final GSpanGraph<NodeType, EdgeType> dbGraph, final int superNode,
			final GSpanHPEmbedding_hierarchical<NodeType, EdgeType> parent,
			final BitSet freeEdges) {
		return nextHPEmbedding_hierarchical().set(
				code.toHPFragment().toHPGraph(), superNode, parent, freeEdges);
	}

	/**
	 * @param code
	 * @param dbGraph
	 * @param superNodes
	 * @param freeEdges
	 * @return a newly initialized embedding according to given parameters
	 */
	public final GSpanHPEmbedding<NodeType, EdgeType> getHPEmbedding(
			final DFSCode<NodeType, EdgeType> code,
			final GSpanGraph<NodeType, EdgeType> dbGraph,
			final int[] superNodes, final BitSet freeEdges) {
		if (storeHierachicalEmbeddings && superNodes.length == 2) {
			// Initial embedding required for initial edges, afterwards
			// hierarchical embeddings calls other get method
			return getHPEmbedding(code, dbGraph, superNodes[1],
					getInitialHPEmbedding_hierarchical(dbGraph, superNodes[0]),
					freeEdges);
		}
		return nextHPEmbedding().set(code.toHPFragment().toHPGraph(), dbGraph,
				superNodes, freeEdges);
	}

	/**
	 * @param graph
	 * @return a newly initialized fragment according to the local settings
	 */
	public final HPFragment<NodeType, EdgeType> getHPFragment(
			final HPGraph<NodeType, EdgeType> graph) {
		if (embeddingBased) {
			return nextEHPFragment().set(graph);
		} else {
			return nextGHPFragment().set(graph);
		}
	}

	@SuppressWarnings("unchecked")
	private final GSpanHPEmbedding_hierarchical<NodeType, EdgeType> getInitialHPEmbedding_hierarchical(
			final GSpanGraph<NodeType, EdgeType> dbGraph, final int node) {
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		final int idx = dbGraph.getIndex();
		if (initialEmbeddings == null) {
			initialEmbeddings = new GSpanHPEmbedding_hierarchical[env
					.graphCount()][];
		}
		if (initialEmbeddings[idx] == null) {
			initialEmbeddings[idx] = new GSpanHPEmbedding_hierarchical[dbGraph
					.toHPGraph().getNodeCount() + 1];
			initialEmbeddings[idx][0] = nextHPEmbedding_hierarchical().set(
					dbGraph);
		}
		if (initialEmbeddings[dbGraph.getIndex()][node + 1] == null) {
			final NodeType label = dbGraph.toHPGraph().getNodeLabel(node);
			HPMutableGraph<NodeType, EdgeType> g = singleNodedGraphs.get(label);
			if (g == null) {
				singleNodedGraphs.put(label,
						g = new HPListGraph<NodeType, EdgeType>());
				g.addNodeIndex(label);
			}
			initialEmbeddings[idx][node + 1] = nextHPEmbedding_hierarchical()
					.set(g, node, initialEmbeddings[idx][0], new BitSet());
		}
		return initialEmbeddings[dbGraph.getIndex()][node + 1];
	}

	/**
	 * 
	 * @param nodeA
	 * @param nodeB
	 * @param labelA
	 * @param edgeLabel
	 * @param labelB
	 * @param dir
	 * @param graph
	 * @param gNodeA
	 * @param gEdge
	 * @param gNodeB
	 * @return a newly initialized MinExtension
	 */
	public final MinExtension<NodeType, EdgeType> getMinExtension(
			final int nodeA, final int nodeB, final int labelA,
			final int edgeLabel, final int labelB, final int dir,
			final HPGraph<NodeType, EdgeType> graph, final int gNodeA,
			final int gEdge, final int gNodeB) {
		return nextMinExtension().set(nodeA, nodeB, labelA, edgeLabel, labelB,
				dir, gEdge, gNodeB);
	}

	private final EmbeddingBasedHPFragment<NodeType, EdgeType> nextEHPFragment() {
		if (firstEHPFrag == null) {
			return new EmbeddingBasedHPFragment<NodeType, EdgeType>(this);
		}
		final EmbeddingBasedHPFragment<NodeType, EdgeType> ret = firstEHPFrag;
		firstEHPFrag = ret.next;
		return ret;
	}

	private final GraphBasedHPFragment<NodeType, EdgeType> nextGHPFragment() {
		if (firstGHPFrag == null) {
			return new GraphBasedHPFragment<NodeType, EdgeType>(this);
		}
		final GraphBasedHPFragment<NodeType, EdgeType> ret = firstGHPFrag;
		firstGHPFrag = ret.next;
		return ret;
	}

	private final GSpanEdge<NodeType, EdgeType> nextGSpanEdge() {
		if (firstGSpanEdge == null) {
			return new GSpanEdge<NodeType, EdgeType>(this);
		}
		final GSpanEdge<NodeType, EdgeType> ret = firstGSpanEdge;
		firstGSpanEdge = ret.next;
		return ret;
	}

	private final GSpanExtension<NodeType, EdgeType> nextGSpanExtension() {
		if (firstGSpanExtension == null) {
			return new GSpanExtension<NodeType, EdgeType>(this);
		}
		final GSpanExtension<NodeType, EdgeType> ret = firstGSpanExtension;
		firstGSpanExtension = ret.next;
		return ret;
	}

	private final GSpanHPEmbedding_flat<NodeType, EdgeType> nextHPEmbedding() {
		if (firstHPEmbedding == null) {
			embeddings++;
			return new GSpanHPEmbedding_flat<NodeType, EdgeType>(this);
		}
		final GSpanHPEmbedding_flat<NodeType, EdgeType> ret = firstHPEmbedding;
		firstHPEmbedding = ret.next;
		ret.next = null;
		return ret;
	}

	private final GSpanHPEmbedding_hierarchical<NodeType, EdgeType> nextHPEmbedding_hierarchical() {
		if (firstHPEmbedding_hierarchical == null) {
			return new GSpanHPEmbedding_hierarchical<NodeType, EdgeType>(this);
		}
		final GSpanHPEmbedding_hierarchical<NodeType, EdgeType> ret = firstHPEmbedding_hierarchical;
		firstHPEmbedding_hierarchical = ret.next;
		ret.next = null;
		return ret;
	}

	private final MinExtension<NodeType, EdgeType> nextMinExtension() {
		if (firstMinExtension == null) {
			return new MinExtension<NodeType, EdgeType>(this);
		}
		final MinExtension<NodeType, EdgeType> ret = firstMinExtension;
		firstMinExtension = (MinExtension<NodeType, EdgeType>) ret.next;
		return ret;
	}

	/**
	 * stores the given object in the pool (if configured)
	 * 
	 * @param obj
	 */
	public final void push(
			final EmbeddingBasedHPFragment<NodeType, EdgeType> obj) {
		if (usePooling) {
			obj.next = firstEHPFrag;
			firstEHPFrag = obj;
		}
	}

	/**
	 * stores the given object in the pool (if configured)
	 * 
	 * @param obj
	 */
	public final void push(final GraphBasedHPFragment<NodeType, EdgeType> obj) {
		if (usePooling) {
			obj.next = firstGHPFrag;
			firstGHPFrag = obj;
		}
	}

	/**
	 * stores the given object in the pool (if configured)
	 * 
	 * @param obj
	 */
	public final void push(final GSpanEdge<NodeType, EdgeType> obj) {
		if (usePooling) {
			obj.next = firstGSpanEdge;
			firstGSpanEdge = obj;
		}
	}

	/**
	 * stores the given object in the pool (if configured)
	 * 
	 * @param first
	 * @param last
	 */
	public final void push(final GSpanEdge<NodeType, EdgeType> first,
			final GSpanEdge<NodeType, EdgeType> last) {
		if (usePooling) {
			last.next = firstGSpanEdge;
			firstGSpanEdge = first;
		}
	}

	/**
	 * stores the given object in the pool (if configured)
	 * 
	 * @param obj
	 */
	public final void push(final GSpanExtension<NodeType, EdgeType> obj) {
		if (usePooling) {
			obj.next = firstGSpanExtension;
			firstGSpanExtension = obj;
		}
	}

	/**
	 * stores the given object in the pool (if configured)
	 * 
	 * @param obj
	 */
	public final void push(final GSpanHPEmbedding_flat<NodeType, EdgeType> obj) {
		if (usePooling) {
			obj.next = firstHPEmbedding;
			firstHPEmbedding = obj;
		}
	}

	/**
	 * stores the given object in the pool (if configured)
	 * 
	 * @param obj
	 */
	public final void push(
			final GSpanHPEmbedding_hierarchical<NodeType, EdgeType> obj) {
		if (usePooling) {
			obj.next = firstHPEmbedding_hierarchical;
			firstHPEmbedding_hierarchical = obj;
		}
	}

	/**
	 * stores the given object in the pool (if configured)
	 * 
	 * @param obj
	 */
	public final void push(final MinExtension<NodeType, EdgeType> obj) {
		if (usePooling) {
			obj.next = firstMinExtension;
			firstMinExtension = obj;
		}
	}

	/**
	 * release the one and only ExtensionSet for reuse
	 * 
	 * @param set
	 */
	public final void push(final MinExtensionSet<NodeType, EdgeType> set) {
	}

	@Override
	public String toString() {
		return "GSpanEnvironment of thread " + threadIdx + "\nEmbeddingCount: "
				+ embeddings;
	}

}
