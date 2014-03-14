/**
 * created May 17, 2006
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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.HPMutableGraph;
import de.parsemis.graph.Node;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.miner.general.Frequency;
import de.parsemis.miner.general.HPFragment;
import de.parsemis.utils.Generic;
import de.parsemis.utils.IntIterator;

/**
 * Creates a extended representation for a database graph.
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
public class GSpanGraph<NodeType, EdgeType> implements
		DataBaseGraph<NodeType, EdgeType>, Generic<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final Graph<NodeType, EdgeType> original;

	final HPGraph<NodeType, EdgeType> hp;

	final BitSet availableEdges;

	private final int idx;

	private final Frequency freq;

	private final int glue;

	/**
	 * creates a new GSpan graph
	 * 
	 * @param original
	 *            the original Graph this gSpan graph should represent
	 * @param idx
	 *            the index in the graph database
	 * @param freq
	 *            the frequency of this graph
	 */
	public GSpanGraph(final Graph<NodeType, EdgeType> original, final int idx,
			final Frequency freq) {
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		this.original = original;
		this.idx = idx;
		this.freq = freq;
		if (env.connectedFragments) {
			this.hp = original.toHPGraph();
			glue = HPGraph.NO_NODE;
		} else {
			final HPMutableGraph<NodeType, EdgeType> hp = (HPMutableGraph<NodeType, EdgeType>) original
					.toHPGraph().clone();
			this.hp = hp;
			glue = hp.addNodeIndex(env.nnil);
			for (int i = hp.getMaxNodeIndex() - 1; i >= 0; --i) {
				if (hp.isValidNode(i) && i != glue) {
					hp.addEdgeIndex(glue, i, env.enil, Edge.OUTGOING);
				}
			}
		}

		// mark frequent edges as available
		this.availableEdges = new BitSet(hp.getEdgeCount());
		for (int i = hp.getMaxEdgeIndex() - 1; i >= 0; --i) {
			if (hp.isValidEdge(i)) {
				if (hp.getEdgeLabelIndex(i, env) >= 0
						&& hp.getNodeLabelIndex(hp.getNodeA(i), env) >= 0
						&& hp.getNodeLabelIndex(hp.getNodeB(i), env) >= 0) {
					availableEdges.set(i);
				}
			}
		}
	}

	/* creates an initial embedding for the given (self) edge */
	private final GSpanHPEmbedding<NodeType, EdgeType> createEmbedding(
			final int nodeA, final int nodeB, final int edge,
			final DFSCode<NodeType, EdgeType> code) {
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		final GThreadEnvironment<NodeType, EdgeType> tenv = (GThreadEnvironment<NodeType, EdgeType>) env
				.getThreadEnv(0);
		int[] superNodes;
		if (nodeA == nodeB) {
			superNodes = tenv.getIntArray(1);
		} else {
			superNodes = tenv.getIntArray(2);
			superNodes[1] = nodeB;
		}
		superNodes[0] = nodeA;
		final BitSet freeEdges = (BitSet) availableEdges.clone();
		freeEdges.clear(edge);
		return tenv.getHPEmbedding(code, this, superNodes, freeEdges);
	}

	/**
	 * creates and inserts the initial DFS-Codes for each frequent edge of this
	 * graph
	 * 
	 * @param map
	 */
	public void createInitials(
			final Map<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> map) {
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		final GThreadEnvironment<NodeType, EdgeType> tenv = (GThreadEnvironment<NodeType, EdgeType>) env
				.getThreadEnv(0);
		if (env.connectedFragments) {
			// each frequent edge could be start of a DFSCode
			for (int edge = availableEdges.nextSetBit(0); edge >= 0; edge = availableEdges
					.nextSetBit(edge + 1)) {
				final int nodeA = hp.getNodeA(edge);
				final int nodeB = hp.getNodeB(edge);
				final GSpanEdge<NodeType, EdgeType> gedge = tenv.getEdge(0,
						(nodeA == nodeB ? 0 : 1), hp.getNodeLabelIndex(nodeA,
								env), hp.getEdgeLabelIndex(edge, env), hp
								.getNodeLabelIndex(nodeB, env), hp
								.getDirection(edge));

				// search fragment for current edge
				DFSCode<NodeType, EdgeType> code = map.get(gedge);
				HPFragment<NodeType, EdgeType> frag;
				if (code == null) {
					// create initial DFSCode
					final ArrayList<GSpanEdge<NodeType, EdgeType>> parents = new ArrayList<GSpanEdge<NodeType, EdgeType>>(
							2);
					parents.add(gedge);
					parents.add(gedge);
					final HPMutableGraph<NodeType, EdgeType> ng = env
							.newHPGraph();
					gedge.addTo(ng);
					frag = tenv.getHPFragment(ng);
					code = tenv.getCode(frag, gedge, gedge, parents);
					map.put(gedge, code);
				} else {
					frag = code.toHPFragment();
					gedge.release(tenv);
				}

				// add graph/embedding to fragment
				if (env.storeEmbeddings) {// create Embedding
					if (hp.getNodeLabelIndex(nodeA, env) == gedge.getLabelA()) {
						frag.add(createEmbedding(nodeA, nodeB, edge, code));
						if (nodeA != nodeB
								&& gedge.getDirection() == Edge.UNDIRECTED
								&& hp.getNodeLabelIndex(nodeA, env) == hp
										.getNodeLabelIndex(nodeB, env)) {
							frag.add(createEmbedding(nodeB, nodeA, edge, code));
						}
					} else {
						frag.add(createEmbedding(nodeB, nodeA, edge, code));
					}
				} else {
					frag.add(this);
				}
			}
		} else {
			// for unconnected search, just edges of the glue node are relevant
			// for initial edges
			for (int i = hp.getDegree(glue) - 1; i >= 0; --i) {
				final int nodeA = glue;
				final int edge = hp.getNodeEdge(glue, i);
				if (!availableEdges.get(edge)) {
					continue;
				}
				final int nodeB = hp.getOtherNode(edge, glue);
				final GSpanEdge<NodeType, EdgeType> gedge = tenv.getEdge(0,
						(nodeA == nodeB ? 0 : 1), hp.getNodeLabelIndex(nodeA,
								env), hp.getEdgeLabelIndex(edge, env), hp
								.getNodeLabelIndex(nodeB, env), hp
								.getDirection(edge));

				// search fragment for current edge
				DFSCode<NodeType, EdgeType> code = map.get(gedge);
				HPFragment<NodeType, EdgeType> frag;
				if (code == null) {// create initial DFSCode
					final ArrayList<GSpanEdge<NodeType, EdgeType>> parents = new ArrayList<GSpanEdge<NodeType, EdgeType>>(
							2);
					parents.add(gedge);
					parents.add(gedge);
					final HPMutableGraph<NodeType, EdgeType> ng = env
							.newHPGraph();
					gedge.addTo(ng);
					frag = tenv.getHPFragment(ng);
					code = tenv.getCode(frag, gedge, gedge, parents);
					map.put(gedge, code);
				} else {
					frag = code.toHPFragment();
					gedge.release(tenv);
				}

				// add graph/embedding to fragment
				if (env.storeEmbeddings) {// create Embedding
					final int[] superNodes = tenv.getIntArray(2);
					superNodes[1] = nodeB;
					superNodes[0] = nodeA;
					final BitSet freeEdges = (BitSet) availableEdges.clone();
					freeEdges.clear(edge);
					frag.add(tenv.getHPEmbedding(code, this, superNodes,
							freeEdges));
				} else {
					frag.add(this);
				}
			}
		}
	}

	/**
	 * @return the count of available edges
	 */
	public final int edgeCount() {
		return this.availableEdges.cardinality();
	}

	/**
	 * checks if the given edge is still a valid edge in the graph
	 * 
	 * @param edge
	 * @return <code>true</code>, if the given edge is frequent and not
	 *         deleted
	 */
	public final boolean edgeExists(final int edge) {
		return this.availableEdges.get(edge);
	}

	/**
	 * @return an iterator over all available Edges
	 */
	public final Iterator<Edge<NodeType, EdgeType>> edgeIterator() {
		return new Iterator<Edge<NodeType, EdgeType>>() {
			private int last = -1;

			private int next = availableEdges.nextSetBit(0);

			public boolean hasNext() {
				return next >= 0;
			}

			public Edge<NodeType, EdgeType> next() {
				last = next;
				next = availableEdges.nextSetBit(last + 1);
				return original.getEdge(last);
			}

			public void remove() {
				availableEdges.clear(last);
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.DataBaseGraph#frequency()
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
		return (BitSet) availableEdges.clone();
	}

	/**
	 * @param nodeIdx
	 *            the index of the node
	 * @return an iterator over all (available) edges (indices) connected to the
	 *         given node
	 */
	public final IntIterator getEdges(final int nodeIdx) {
		return new IntIterator() {
			private final IntIterator it = hp.getEdgeIndices(nodeIdx);

			private int next = -1, last = -1;

			public boolean hasNext() {
				if (next >= 0) {
					return true;
				}
				while (it.hasNext()) {
					if (availableEdges.get(next = it.next())) {
						return true;
					}
				}
				next = -1;
				return false;
			}

			public int next() {
				if (!hasNext()) {
					throw new NoSuchElementException("No more elements");
				}
				last = next;
				next = -1;
				return last;
			}

			public void remove() {
				availableEdges.clear(last);
			}
		};
	}

	/**
	 * @param node
	 * @return an iterator over all (available) edges connected to the given
	 *         node
	 */
	public final Iterator<Edge<NodeType, EdgeType>> getEdges(
			final Node<NodeType, EdgeType> node) {
		return new Iterator<Edge<NodeType, EdgeType>>() {
			private final Iterator<Edge<NodeType, EdgeType>> eit = node
					.edgeIterator();

			private Edge<NodeType, EdgeType> last = null;

			private Edge<NodeType, EdgeType> next = null;

			public boolean hasNext() {
				if (next != null) {
					return true;
				}
				while (eit.hasNext()) {
					next = eit.next();
					if (availableEdges.get(next.getIndex())) {
						return true;
					}
				}
				next = null;
				return false;
			}

			public Edge<NodeType, EdgeType> next() {
				if (!hasNext()) {
					throw new NoSuchElementException("No more elements");
				}
				last = next;
				next = null;
				return last;
			}

			public void remove() {
				availableEdges.clear(last.getIndex());
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.DataBaseGraph#getIndex()
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
		return (BitSet) hp.getNodes().clone();
	}

	/**
	 * remove all edges that are similar to the given GSpanEdge
	 * 
	 * @param orig
	 */
	public final void removeAllOccurences(
			final GSpanEdge<NodeType, EdgeType> orig) {
		for (int edge = availableEdges.nextSetBit(0); edge > -1; edge = availableEdges
				.nextSetBit(edge + 1)) {
			if (orig.sameAs2(this.hp, edge)) {
				availableEdges.clear(edge);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.DataBaseGraph#toGraph()
	 */
	public Graph<NodeType, EdgeType> toGraph() {
		return original;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.DataBaseGraph#toHPGraph()
	 */
	public HPGraph<NodeType, EdgeType> toHPGraph() {
		return hp;
	}

}
