/**
 * created Mar 26, 2007
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

import java.util.BitSet;

import de.parsemis.graph.HPGraph;
import de.parsemis.miner.chain.Extension;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.environment.ThreadEnvironment;
import de.parsemis.miner.general.AbstractFlatHPEmbedding;
import de.parsemis.miner.general.HPEmbedding;

/**
 * Implements the HPEmbedding interface for the GSpan algorithm.
 * <p>
 * It stores all mappings in a local array.
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
public class GSpanHPEmbedding_flat<NodeType, EdgeType>
		extends
		AbstractFlatHPEmbedding<NodeType, EdgeType, GSpanGraph<NodeType, EdgeType>>
		implements GSpanHPEmbedding<NodeType, EdgeType> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4172615715497009282L;

	public static int counter = 0;

	final static boolean equal(final BitSet a, final BitSet b) {
		final BitSet c = (BitSet) a.clone();
		c.xor(b);
		return c.cardinality() == 0;
	}

	/* final */transient GThreadEnvironment<NodeType, EdgeType> tenv;

	/** used for the pool */
	transient GSpanHPEmbedding_flat<NodeType, EdgeType> next;

	private final int threadIdx;

	/**
	 * creates a new empty GSpanEmbedding
	 * 
	 * @param tenv
	 */
	public GSpanHPEmbedding_flat(
			final GThreadEnvironment<NodeType, EdgeType> tenv) {
		counter++;
		this.threadIdx = tenv.threadIdx;
		this.tenv = tenv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.gSpan.GSpanHPEmbedding#extend(de.parsemis.gSpan.GSpanExtension,
	 *      int, int, de.parsemis.gSpan.GThreadEnvironment)
	 */
	@SuppressWarnings("unchecked")
	public HPEmbedding<NodeType, EdgeType> extend(
			final GSpanExtension<NodeType, EdgeType> ext, final int edge,
			final int superB) {

		// lazy embedding generation
		return new LazyExtendedEmbedding_flat(edge, ext, superB, this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPEmbedding#getExtension(int, int)
	 */
	public Extension<NodeType, EdgeType> getExtension(final int superNodeA,
			final int superEdge) {
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		final HPGraph<NodeType, EdgeType> graph = getDataBaseGraph()
				.toHPGraph();

		final int superNodeB = graph.getOtherNode(superEdge, superNodeA);
		final int subNodeA = getSubGraphNode(superNodeA);
		final int subNodeB = getSubGraphNode(superNodeB);

		if (subNodeB == HPGraph.NO_NODE) { // generate forward edge
			return tenv().getExtension(
					tenv().getEdge(subNodeA, getSubGraph().getNodeCount(),
							graph.getNodeLabelIndex(superNodeA, env),
							graph.getEdgeLabelIndex(superEdge, env),
							graph.getNodeLabelIndex(superNodeB, env),
							graph.getDirection(superEdge, superNodeA)), null);
		} else if (subNodeB < subNodeA) { // generate backward edge
			return tenv().getExtension(
					tenv().getEdge(subNodeA, subNodeB,
							graph.getNodeLabelIndex(superNodeA, env),
							graph.getEdgeLabelIndex(superEdge, env),
							graph.getNodeLabelIndex(superNodeB, env),
							graph.getDirection(superEdge, superNodeA)), null);
		} else {
			// no valid gSpan extension
			return null;
		}
	};

	final int[] getSuperNodes(final int newLength) {
		final int[] nsuperNodes = new int[newLength];
		assert nsuperNodes.length >= superNodes().length : "aaaaa";
		System.arraycopy(superNodes(), 0, nsuperNodes, 0, superNodes().length);
		return nsuperNodes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPEmbedding#mapExtension(de.parsemis.miner.Extension)
	 */
	public boolean mapExtension(final Extension<NodeType, EdgeType> ext) {
		return mapExtension(ext, freeEdges()) != HPGraph.NO_EDGE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPEmbedding#mapExtension(de.parsemis.miner.Extension,
	 *      java.util.BitSet)
	 */
	public int mapExtension(final Extension<NodeType, EdgeType> ext,
			final BitSet allowedEdges) {
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		final GSpanEdge<NodeType, EdgeType> ack = ((GSpanExtension<NodeType, EdgeType>) ext).edge;
		final HPGraph<NodeType, EdgeType> graph = getDataBaseGraph()
				.toHPGraph();

		final int superNodeA = this.getSuperGraphNode(ack.getNodeA());
		final int shallSubNodeB = (ack.isForward() ? HPGraph.NO_NODE : ack
				.getNodeB());
		final int shallSubLabelB = ack.getLabelB();
		final BitSet freeEdges = freeEdges();

		for (int i = graph.getDegree(superNodeA) - 1; i >= 0; --i) {
			// check all adjacent edges of nodeA
			final int edge = graph.getNodeEdge(superNodeA, i);
			if (freeEdges.get(edge) && allowedEdges.get(edge)) {
				// if edge is unused an allowed, check other node to match the
				// given extension
				final int nodeB = graph.getOtherNode(edge, superNodeA);
				if (ack.sameAs(graph, edge, superNodeA)
						&& (getSubGraphNode(nodeB) == shallSubNodeB)
						&& (graph.getNodeLabelIndex(nodeB, env) == shallSubLabelB)) {
					return edge;
				}
			}
		}
		return HPGraph.NO_EDGE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.gSpan.GSpanHPEmbedding#release(de.parsemis.utils.ThreadEnvironment)
	 */
	public void release(final ThreadEnvironment<NodeType, EdgeType> target) {
		if (target == tenv) {
			tenv.push(this);
		}
	}

	/**
	 * @param sub
	 * @param dbGraph
	 * @param superNodes
	 * @param freeEdges
	 * @return a (re-)initialized GSpanEmbedding
	 */
	GSpanHPEmbedding_flat<NodeType, EdgeType> set(
			final HPGraph<NodeType, EdgeType> sub,
			final GSpanGraph<NodeType, EdgeType> dbGraph,
			final int[] superNodes, final BitSet freeEdges) {
		super.set(dbGraph, sub, superNodes);
		freeEdges(freeEdges);
		return this;
	}

	/*
	 * reinitialize tenv after serialization to another machine
	 */
	final GThreadEnvironment<NodeType, EdgeType> tenv() {
		if (tenv == null) {
			return (GThreadEnvironment<NodeType, EdgeType>) LocalEnvironment
					.env(this).getThreadEnv(threadIdx);
		}
		return tenv;
	}
}
