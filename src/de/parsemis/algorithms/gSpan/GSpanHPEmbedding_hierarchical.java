/**
 * created Mar 27, 2007
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
import java.util.Collection;

import de.parsemis.graph.HPGraph;
import de.parsemis.miner.chain.Extension;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.environment.ThreadEnvironment;
import de.parsemis.miner.general.AbstractHierarchicalHPEmbedding;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.miner.general.Embedding;
import de.parsemis.miner.general.Frequency;
import de.parsemis.miner.general.HPEmbedding;

/**
 * Implements the HPEmbedding interface for the GSpan algorithm.
 * <p>
 * It stores the last added mapping in a local field. It recieves each other
 * information of the parent embedding to avoid information duplication.
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
public class GSpanHPEmbedding_hierarchical<NodeType, EdgeType>
		extends
		AbstractHierarchicalHPEmbedding<NodeType, EdgeType, GSpanGraph<NodeType, EdgeType>>
		implements GSpanHPEmbedding<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 415471913577298943L;

	public static int counter = 0;

	/* final */transient GThreadEnvironment<NodeType, EdgeType> tenv;

	/** used for the pool */
	transient GSpanHPEmbedding_hierarchical<NodeType, EdgeType> next;

	private final int threadIdx;

	/**
	 * creates a new empty GSpanEmbedding
	 * 
	 * @param tenv
	 */
	public GSpanHPEmbedding_hierarchical(
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
	public HPEmbedding<NodeType, EdgeType> extend(
			final GSpanExtension<NodeType, EdgeType> ext, final int edge,
			final int superB) {
		// lazy embedding generation
		return new GSpanHPEmbedding<NodeType, EdgeType>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 693244997417300630L;

			transient GSpanHPEmbedding<NodeType, EdgeType> cache = null;

			public HPEmbedding<NodeType, EdgeType> extend(
					final GSpanExtension<NodeType, EdgeType> ext,
					final int edge, final int superB) {
				return get().extend(ext, edge, superB);
			}

			public boolean freeSuperEdge(final int superEdge) {
				return get().freeSuperEdge(superEdge);
			}

			public boolean freeSuperNode(final int superNode) {
				throw new UnsupportedOperationException("not implemented yet");
			}

			public void freeTransient() {
				// TODO Auto-generated method stub

			}

			public Frequency frequency() {
				return get().frequency();
			}

			// lazy embedding generation
			@SuppressWarnings("unchecked")
			private final GSpanHPEmbedding<NodeType, EdgeType> get() {
				if (cache == null) {
					final DFSCode<NodeType, EdgeType> code = (DFSCode<NodeType, EdgeType>) ext.frag;
					final BitSet freeEdges = (BitSet) GSpanHPEmbedding_hierarchical.this
							.freeEdges().clone();
					freeEdges.clear(edge);
					if (ext.edge.isForward()) {
						cache = tenv().getHPEmbedding(
								code,
								GSpanHPEmbedding_hierarchical.this
										.getDataBaseGraph(), superB,
								GSpanHPEmbedding_hierarchical.this, freeEdges);
					} else {
						cache = tenv()
								.getHPEmbedding(
										code,
										GSpanHPEmbedding_hierarchical.this
												.getDataBaseGraph(),
										getSuperNode(),
										(GSpanHPEmbedding_hierarchical<NodeType, EdgeType>) getParent(),
										freeEdges);
					}
				}
				return cache;
			}

			public DataBaseGraph<NodeType, EdgeType> getDataBaseGraph() {
				return GSpanHPEmbedding_hierarchical.this.getDataBaseGraph();
			}

			public Extension<NodeType, EdgeType> getExtension(
					final int superNode, final int superEdge) {
				return get().getExtension(superNode, superEdge);
			}

			public HPGraph<NodeType, EdgeType> getSubGraph() {
				return get().getSubGraph();
			}

			public int getSubGraphEdge(final int superEdge) {
				return get().getSubGraphEdge(superEdge);
			}

			public int getSubGraphNode(final int superNode) {
				return get().getSubGraphNode(superNode);
			}

			public HPGraph<NodeType, EdgeType> getSuperGraph() {
				return GSpanHPEmbedding_hierarchical.this.getSuperGraph();
			}

			public int getSuperGraphEdge(final int subEdge) {
				return get().getSuperGraphEdge(subEdge);
			}

			public int getSuperGraphNode(final int subNode) {
				return get().getSuperGraphNode(subNode);
			}

			public boolean mapExtension(final Extension<NodeType, EdgeType> ext) {
				return get().mapExtension(ext);
			}

			public int mapExtension(final Extension<NodeType, EdgeType> ext,
					final BitSet allowedEdges) {
				return get().mapExtension(ext, allowedEdges);
			}

			public boolean overlaps(
					final HPEmbedding<NodeType, EdgeType> other,
					final Collection<NodeType> ignore) {
				return get().overlaps(other, ignore);
			}

			public void release(
					final ThreadEnvironment<NodeType, EdgeType> target) {
				if (cache != null) {
					cache.release(target);
				}
			}

			public Embedding<NodeType, EdgeType> toEmbedding() {
				return get().toEmbedding();
			}

		};
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
	}

	@Override
	public int getSubNode() {
		return getSubGraph().getNodeCount() - 1;
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

		for (int i = graph.getDegree(superNodeA) - 1; i >= 0; --i) {
			// check all adjazent edges of nodeA
			final int edge = graph.getNodeEdge(superNodeA, i);
			if (freeEdges().get(edge) && allowedEdges.get(edge)) {
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

	public GSpanHPEmbedding_hierarchical<NodeType, EdgeType> set(
			final GSpanGraph<NodeType, EdgeType> dbGraph) {
		super.set(dbGraph, null);
		super.superNode = HPGraph.NO_NODE;
		return this;
	}

	public GSpanHPEmbedding_hierarchical<NodeType, EdgeType> set(
			final HPGraph<NodeType, EdgeType> sub, final int superNode,
			final GSpanHPEmbedding_hierarchical<NodeType, EdgeType> parent,
			final BitSet freeEdges) {
		super.set(sub, superNode, parent);
		super.freeEdges(freeEdges);
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
