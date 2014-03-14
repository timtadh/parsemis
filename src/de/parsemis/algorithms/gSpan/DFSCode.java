/**
 * created May 16, 2006
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
import java.util.Collection;
import java.util.Iterator;

import de.parsemis.graph.HPGraph;
import de.parsemis.graph.HPMutableGraph;
import de.parsemis.miner.chain.Extension;
import de.parsemis.miner.chain.SearchLatticeNode;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.miner.general.Fragment;
import de.parsemis.miner.general.Frequency;
import de.parsemis.miner.general.HPEmbedding;
import de.parsemis.miner.general.HPFragment;
import de.parsemis.utils.Canonizable;
import de.parsemis.utils.Frequented;
import de.parsemis.utils.Generic;

/**
 * Implements the DFSCode that represents a subgraph during the
 * search.
 * <p>
 * It can/will be stored in local object pool to avoid object generation/garbage
 * collection.
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
public class DFSCode<NodeType, EdgeType> extends
		SearchLatticeNode<NodeType, EdgeType> implements
		Comparable<DFSCode<NodeType, EdgeType>>, Generic<NodeType, EdgeType>,
		Canonizable, Frequented {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected final static int UNUSED = -1;

	private GSpanEdge<NodeType, EdgeType> first, last;

	private int psize;

	private int threadIdx;

	private HPFragment<NodeType, EdgeType> me;

	private HPFragment<NodeType, EdgeType> finalMe;

	transient private GThreadEnvironment<NodeType, EdgeType> tenv;

	transient private ArrayList<GSpanEdge<NodeType, EdgeType>> parents;

	/** used for the object pool */
	// transient public DFSCode<NodeType,EdgeType> next;
	/**
	 * creates a new DFSCode
	 * 
	 * @param tenv
	 */
	public DFSCode(final GThreadEnvironment<NodeType, EdgeType> tenv) {
		this.threadIdx = tenv.threadIdx;
		this.tenv = tenv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.SearchLatticeNode#allEmbeddings()
	 */
	@Override
	public Collection<HPEmbedding<NodeType, EdgeType>> allEmbeddings() {
		return me;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(T)
	 */
	public int compareTo(final DFSCode<NodeType, EdgeType> arg0) {
		GSpanEdge<NodeType, EdgeType> ack1 = this.first;
		GSpanEdge<NodeType, EdgeType> ack2 = arg0.first;
		while (ack1 != null && ack2 != null && ack1.compareTo(ack2) == 0) {
			ack1 = ack1.next;
			ack2 = ack2.next;
		}
		if (ack1 == null) {
			if (ack2 == null) {
				return 0;
			} else {
				return -1;
			}
		} else if (ack2 == null) {
			return 1;
		} else {
			return ack1.compareTo(ack2);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof DFSCode && compareTo((DFSCode) obj) == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.SearchLatticeNode#extend(de.parsemis.miner.Extension)
	 */
	@Override
	public SearchLatticeNode<NodeType, EdgeType> extend(
			final Extension<NodeType, EdgeType> extension) {
		assert extension instanceof GSpanExtension : "DFSCode.extend(..) is just applicable for GSpanExtensions";
		final GSpanExtension<NodeType, EdgeType> ext = (GSpanExtension<NodeType, EdgeType>) extension;
		final GThreadEnvironment<NodeType, EdgeType> tenv = tenv();

		// clone current DFS-List
		final GSpanEdge<NodeType, EdgeType> nextFirst = first.clone(tenv);
		GSpanEdge<NodeType, EdgeType> nextLast = nextFirst;
		final HPGraph<NodeType, EdgeType> g = ext.getFragment().toHPGraph();
		final ArrayList<GSpanEdge<NodeType, EdgeType>> nextParents = new ArrayList<GSpanEdge<NodeType, EdgeType>>(
				g.getNodeCount());

		// generate parent map
		for (int i = g.getNodeCount(); i > 0; --i) {
			nextParents.add(null);
		}
		nextParents.set(nextFirst.getNodeA(), nextFirst);
		nextParents.set(nextFirst.getNodeB(), nextFirst);

		for (GSpanEdge<NodeType, EdgeType> ack = first.next; ack != null; ack = ack.next) {
			nextLast.next = ack.clone(tenv);
			nextLast = nextLast.next;
			if (nextLast.isForward()) {
				nextParents.set(nextLast.getNodeB(), nextLast);
			}
		}

		// append new edge
		nextLast.next = ext.edge;
		nextLast = ext.edge;
		if (nextLast.isForward()) {
			nextParents.set(nextLast.getNodeB(), nextLast);
		}

		// get "new" DFSCode object
		return tenv().getCode(ext.getFragment(), nextFirst, nextLast,
				nextParents);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.SearchLatticeNode#finalizeIt()
	 */
	@Override
	public void finalizeIt() {
		if (tenv != null) {
			first.releaseAll(last, tenv);
		}
		parents = null;
	}

	/** @return the frequency (finally) associated with this DFS-code */
	public final Frequency frequency() {
		return toHPFragment().frequency();
	}

	/*
	 * generates a single connected list of possible unused extensions of the
	 * nodeA/gNodeA
	 */
	private final MinExtension<NodeType, EdgeType> getExtensions(
			final int nodeA, final HPGraph<NodeType, EdgeType> graph,
			final int gNodeA, final int[] usedEdges, final int[] usedNodes) {
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		MinExtension<NodeType, EdgeType> last = null;
		for (int i = graph.getDegree(gNodeA) - 1; i >= 0; --i) {
			// for each adjazent edge of A
			final int edge = graph.getNodeEdge(gNodeA, i);
			final int gNodeB = graph.getOtherNode(edge, gNodeA);
			if (usedEdges[edge] == UNUSED) {
				// build extension for unused edges
				final MinExtension<NodeType, EdgeType> next = tenv()
						.getMinExtension(nodeA, usedNodes[gNodeB],
								graph.getNodeLabelIndex(gNodeA, env),
								graph.getEdgeLabelIndex(edge, env),
								graph.getNodeLabelIndex(gNodeB, env),
								graph.getDirection(edge, gNodeA), graph,
								gNodeA, edge, gNodeB);
				next.next = last;
				last = next;
			}
		}
		return last;
	}

	/** @return the initial GSpanEdge of this DFSCode */
	protected final GSpanEdge<NodeType, EdgeType> getFirst() {
		return first;
	}

	/** @return the last GSpanEdge of this DFSCode */
	protected final GSpanEdge<NodeType, EdgeType> getLast() {
		return last;
	}

	/**
	 * generates the parents array, if necessary
	 * 
	 * @param node
	 * @return the GSpanEdge that introduced the given node
	 */
	public GSpanEdge<NodeType, EdgeType> getParent(final int node) {
		if (parents == null) {
			parents = new ArrayList<GSpanEdge<NodeType, EdgeType>>(psize);
			for (int i = 0; i < psize; i++) {
				parents.add(null);
			}
			parents.set(0, first);
			for (GSpanEdge<NodeType, EdgeType> ack = first; ack != null; ack = ack.next) {
				if (ack.isForward()) {
					parents.set(ack.getNodeB(), ack);
				}
			}
		}
		assert parents.size() > node : this + " " + node;
		return parents.get(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.SearchLatticeNode#getThreadNumber()
	 */
	@Override
	public int getThreadNumber() {
		return threadIdx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return first == null ? 0 : first.hashCode();
	}

	/**
	 * helper function for the test of beeing canonical
	 * 
	 * search for a smaller first edge or starts recursive extension for equally
	 * ones
	 * 
	 * @param set
	 * @param ackNodes
	 * @param usedNodes
	 * @param usedEdges
	 * @return <code>false</code>, the part of the serach determines this
	 */
	private boolean isCan(final MinExtensionSet<NodeType, EdgeType> set,
			final int[] ackNodes, final int[] usedNodes, final int[] usedEdges) {
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		final HPGraph<NodeType, EdgeType> hp = me.toHPGraph();

		for (int node = hp.getMaxNodeIndex() - 1; node >= 0; --node) {
			// try each node as potential start node for a smaller DFSCode
			if (!hp.isValidNode(node)) {
				continue;
			}
			final int nodeLabelIndex = env.getNodeLabelIndex(hp
					.getNodeLabel(node));
			int foundEdge = -2;
			GSpanEdge<NodeType, EdgeType> ack = first;
			if (ack.getNodeA() == ack.getNodeB()) { // self edge
				final int edge = hp.getEdge(node, node);
				if (edge != -1) {
					if (nodeLabelIndex < ack.getLabelA()) {
						return false; // a smaller DFSCode is found
					}
					if (nodeLabelIndex == ack.getLabelA()) {
						// only DFSCodes will searched which starts same as this
						final int edgeIndex = edge;
						final int edgeLabelIndex = hp.getEdgeLabelIndex(edge,
								env);
						if (edgeLabelIndex < ack.getEdgeLabel()) {
							return false; // a smaller DFSCode is found
						}
						if (edgeLabelIndex == ack.getEdgeLabel()) {
							// equal starting edge found
							usedNodes[node] = 0;
							ackNodes[0] = node;
							usedEdges[edgeIndex] = 1;
							foundEdge = edgeIndex;
							ack = ack.next;
						}
					}
				}
			} else { // no self-edges
				if (hp.getEdge(node, node) != -1) {
					return false; // a smaller DFSCode is found
				}
				if (nodeLabelIndex <= ack.getLabelA()) {
					// only DFSCodes will searched which starts same as this
					// first edge will be detected by recursion
					usedNodes[node] = 0;
					ackNodes[0] = node;
					foundEdge = -1;
				}
			}
			if (foundEdge > -2 && ack != null) {// node is a start node
				final MinExtension<NodeType, EdgeType> exts = getExtensions(0,
						hp, node, usedEdges, usedNodes);
				set.addAll(exts);
				// recursiv extension to find a smaller DFSCode
				if (!isCan2(ack, set, 0, usedNodes, usedEdges, ackNodes, hp)) {
					// a smaller DFSCode is found
					set.removeAndFreeAll(exts);
					return false;
				}
				set.removeAndFreeAll(exts);
			}
			usedNodes[node] = -1;
			if (foundEdge > -1) {
				usedEdges[foundEdge] = -1;
			}
		}

		// no smaller is found
		return true;
	}

	/**
	 * helper function for the test of beeing canonical
	 * 
	 * extends current detected DFSCode and searches for new extensions
	 * 
	 * @param ackEdge
	 * @param set
	 * @param lastNode
	 * @param usedNodes
	 * @param usedEdges
	 * @param ackNodes
	 * @param graph
	 * @return <code>false</code>, the part of the serach determines this
	 */
	private boolean isCan2(final GSpanEdge<NodeType, EdgeType> ackEdge,
			final MinExtensionSet<NodeType, EdgeType> set, final int lastNode,
			final int[] usedNodes, final int[] usedEdges, final int[] ackNodes,
			final HPGraph<NodeType, EdgeType> graph) {
		final MinExtension<NodeType, EdgeType> first = set.forward;
		for (MinExtension<NodeType, EdgeType> ack = first; ack.compareTo(first) == 0; ack = ack.forward) {
			// for each extension that fits the first one

			// remove it from set
			final MinExtension<NodeType, EdgeType> next = set.unlink(ack);

			if (usedEdges[ack.gEdgei] != UNUSED) {
				// skip already used edges
				if (!isCan2(ackEdge, set, lastNode, usedNodes, usedEdges,
						ackNodes, graph)) {
					// smaller DFSCode found
					set.relink(ack, next);
					return false;
				}
			} else {
				if (ack.getNodeB() == UNUSED) { // forward edge
					final int tmp = ackEdge.compareTo(ack, lastNode + 1);
					if (tmp > 0) {
						// smaller DFSCode found
						set.relink(ack, next);
						return false;
					}
					if (ackEdge.next == null || tmp < 0) {
						// smaller DFSCode found
						set.relink(ack, next);
						return true;
					}
					// compute extensions from the new node
					usedNodes[ack.gNodeBi] = lastNode + 1;
					ackNodes[lastNode + 1] = ack.gNodeBi;
					usedEdges[ack.gEdgei] = 1;
					final MinExtension<NodeType, EdgeType> exts = getExtensions(
							lastNode + 1, graph, ack.gNodeBi, usedEdges,
							usedNodes);
					set.addAll(exts);
					// recursiv search
					if (!isCan2(ackEdge.next, set, lastNode + 1, usedNodes,
							usedEdges, ackNodes, graph)) {
						// smaller DFSCode found
						set.removeAndFreeAll(exts);
						set.relink(ack, next);
						return false;
					}
					set.removeAndFreeAll(exts);
					usedNodes[ack.gNodeBi] = UNUSED;
					ackNodes[lastNode + 1] = UNUSED;
					usedEdges[ack.gEdgei] = UNUSED;
				} else { // backward edge
					final int tmp = ackEdge.compareTo(ack, ack.getNodeB());
					if (tmp > 0) {
						// smaller DFSCode found
						set.relink(ack, next);
						return false;
					}
					if (ackEdge.next == null || tmp < 0) {
						// smaller DFSCode found
						set.relink(ack, next);
						return true;
					}
					// mark edge as used
					usedEdges[ack.gEdgei] = 1;
					// recursiv search
					if (!isCan2(ackEdge.next, set, lastNode, usedNodes,
							usedEdges, ackNodes, graph)) {
						// smaller DFSCode found
						set.relink(ack, next);
						return false;
					}
					usedEdges[ack.gEdgei] = UNUSED;
				}
			}
			set.relink(ack, next);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Canonizable#isCanonical()
	 */
	public boolean isCanonical() {
		// create and initilize embedding Arrays
		final int nc = me.toHPGraph().getNodeCount();
		final int ec = me.toHPGraph().getEdgeCount();
		final GThreadEnvironment<NodeType, EdgeType> tenv = tenv();
		final int[] ackNodes = tenv.getIntArray(nc, UNUSED);
		final int[] usedNodes = tenv.getIntArray(nc, UNUSED);
		final int[] usedEdges = tenv.getIntArray(ec, UNUSED);
		final MinExtensionSet<NodeType, EdgeType> set = tenv.getExtensionSet();

		final boolean ret = isCan(set, ackNodes, usedNodes, usedEdges);

		// release Arrays
		tenv.push(set);
		tenv.push(ackNodes);
		tenv.push(usedNodes);
		tenv.push(usedEdges);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.SearchLatticeNode#release()
	 */
	@Override
	public void release() {
		if (tenv != null && getLevel() > 1) {
			me.release(tenv);
			me = null;
			// tenv.push(this);
		}
	}

	/**
	 * initialisation do allow reusability
	 * 
	 * @param me
	 * @param first
	 * @param last
	 * @param parents
	 * @return a newly initialized DFSCode
	 */
	protected DFSCode<NodeType, EdgeType> set(
			final HPFragment<NodeType, EdgeType> me,
			final GSpanEdge<NodeType, EdgeType> first,
			final GSpanEdge<NodeType, EdgeType> last,
			final ArrayList<GSpanEdge<NodeType, EdgeType>> parents) {
		this.me = me;
		this.parents = parents;
		this.psize = parents.size();
		this.first = first;
		this.last = last;
		this.finalMe = null;
		setLevel(me.toHPGraph().getEdgeCount() - 1);
		store(true);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.SearchLatticeNode#setFinalEmbeddings(java.util.Collection)
	 */
	@Override
	public void setFinalEmbeddings(
			final Collection<HPEmbedding<NodeType, EdgeType>> embs) {
		finalMe = tenv().getHPFragment(me.toHPGraph());
		finalMe.addAll(embs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.SearchLatticeNode#setThreadNumber(int)
	 */
	@Override
	public void setThreadNumber(final int idx) {
		if (threadIdx != idx) {
			threadIdx = idx;
			tenv = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.SearchLatticeNode#store(java.util.Collection)
	 */
	@Override
	public void store(final Collection<Fragment<NodeType, EdgeType>> set) {
		set.add(toCleanFragment());
	}

	/*
	 * reinitialize tenv after serialization to another machine
	 */
	private final GThreadEnvironment<NodeType, EdgeType> tenv() {
		if (tenv == null) {
			return (GThreadEnvironment<NodeType, EdgeType>) LocalEnvironment
					.env(this).getThreadEnv(threadIdx);
		}
		return tenv;
	}

	/**
	 * removes the pseudo node from each fragment
	 * 
	 * @return a cleaned fragment
	 */
	private final Fragment<NodeType, EdgeType> toCleanFragment() {
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		final HPFragment<NodeType, EdgeType> hp = toHPFragment();

		// generate graph without the pseudo node
		final HPGraph<NodeType, EdgeType> old = hp.toHPGraph();
		final HPMutableGraph<NodeType, EdgeType> ng = env.newHPGraph();
		final int nodes[] = new int[old.getMaxNodeIndex()];
		// clone nodes
		for (int i = old.getMaxNodeIndex() - 1; i >= 0; --i) {
			if (old.isValidNode(i)
					&& (env.nnil == null || !env.nnil.equals(old
							.getNodeLabel(i)))) {
				nodes[i] = ng.addNodeIndex(old.getNodeLabel(i));
			} else {
				nodes[i] = HPGraph.NO_NODE;
			}
		}
		// clone edges
		for (int i = old.getMaxEdgeIndex() - 1; i >= 0; --i) {
			if (old.isValidEdge(i)
					&& (env.enil == null || !env.enil.equals(old
							.getEdgeLabel(i)))) {
				ng.addEdgeIndex(nodes[old.getNodeA(i)], nodes[old.getNodeB(i)],
						old.getEdgeLabel(i), old.getDirection(i));
			}
		}
		final Fragment<NodeType, EdgeType> ret = tenv().getFragment(
				ng.toGraph());

		// clone embeddings or graphset
		if (env.storeEmbeddings) {
			ret.addAll(hp.toFragment());
		} else {
			for (final Iterator<DataBaseGraph<NodeType, EdgeType>> dit = hp
					.graphIterator(); dit.hasNext();) {
				ret.add(dit.next());
			}
		}

		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.SearchLatticeNode#toHPFragment()
	 */
	@Override
	public HPFragment<NodeType, EdgeType> toHPFragment() {
		return (finalMe != null ? finalMe : me);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return LocalEnvironment.env(this).serializer.serialize(me.toHPGraph()
				.toGraph());
	}

}
