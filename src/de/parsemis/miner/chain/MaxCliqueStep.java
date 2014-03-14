/**
 * created Jun 19, 2006
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
package de.parsemis.miner.chain;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;

import de.parsemis.graph.Edge;
import de.parsemis.graph.HPListGraph;
import de.parsemis.graph.HPMutableGraph;
import de.parsemis.miner.general.Embedding;
import de.parsemis.miner.general.HPEmbedding;
import de.parsemis.utils.MaxClique;

/**
 * 
 * This class is a mining step that reduces the set of all embeddings to a
 * non-overlapping one
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
public class MaxCliqueStep<NodeType, EdgeType> extends
		MiningStep<NodeType, EdgeType> {

	/**
	 * calculates the maximal non-overlapping subset of the given hp-embeddings
	 * 
	 * @param <NodeType>
	 * @param <EdgeType>
	 * @param <HPE>
	 * @param embeddings
	 * @param ignore
	 *            a set of NodeLabels that do not be used for collision
	 *            detection (can be null)
	 * @return a maximal non-overlapping subset
	 */
	@SuppressWarnings("unchecked")
	public final static <NodeType, EdgeType, HPE extends HPEmbedding<NodeType, EdgeType>> Collection<HPE> findHPMaxClique(
			final Collection<HPE> embeddings, final Collection<NodeType> ignore) {
		return findHPMaxClique(embeddings, ignore, new ArrayList<HPE>());
	}

	/**
	 * calculates the maximal non-overlapping subset of the given embeddings
	 * 
	 * @param <NodeType>
	 * @param <EdgeType>
	 * @param <HPE>
	 * @param embeddings
	 * @param ret
	 *            collection used to store the maximal non-overlapping subset of
	 *            embeddings
	 * @param ignore
	 *            a set of NodeLabels that do not be used for collision
	 *            detection (can be null)
	 * @return a maximal non-overlapping subset (ret)
	 */
	@SuppressWarnings("unchecked")
	public static <NodeType, EdgeType, HPE extends HPEmbedding<NodeType, EdgeType>> Collection<HPE> findHPMaxClique(
			final Collection<HPE> embeddings,
			final Collection<NodeType> ignore, final Collection<HPE> ret) {
		if (embeddings.size() <= 1) {
			ret.addAll(embeddings);
			return ret;
		}
		// build CollisionGraph
		final HPMutableGraph<HPE, Integer> c = new HPListGraph<HPE, Integer>();
		final HPE[] emb = embeddings.toArray((HPE[]) new HPEmbedding[embeddings
				.size()]);
		final int[] nodes = new int[emb.length];
		for (int i = 0; i < emb.length; ++i) {
			nodes[i] = c.addNodeIndex(emb[i]);
		}
		for (int i = 0; i < emb.length; ++i) {
			for (int j = i + 1; j < emb.length; ++j) {
				if (!emb[i].overlaps(emb[j], ignore)) {
					c.addEdgeIndex(nodes[i], nodes[j], 0, Edge.UNDIRECTED);
				}
			}
		}

		// calculate the maximum clique
		final BitSet clique = MaxClique.calculateHP(c, null);

		// return the found embeddings
		for (int node = clique.nextSetBit(0); node >= 0; node = clique
				.nextSetBit(node + 1)) {
			ret.add(c.getNodeLabel(node));
		}
		return ret;

	}

	/**
	 * calculates the maximal non-overlapping subset of the given embeddings
	 * 
	 * @param <NodeType>
	 * @param <EdgeType>
	 * @param embeddings
	 * @param ignore
	 *            a set of NodeLabels that do not be used for collision
	 *            detection (can be null)
	 * @return Fragment containing a maximal non-overlapping subset
	 * @deprecated
	 */
	/*
	 * @SuppressWarnings("unchecked") public static <NodeType, EdgeType>
	 * Fragment<NodeType, EdgeType> findMaxClique2( Collection<Embedding<NodeType,
	 * EdgeType>> embeddings, Collection<NodeType> ignore) {
	 * 
	 * Fragment<NodeType, EdgeType> ret = new ArrayFragment<NodeType,
	 * EdgeType>(); if (embeddings.size() == 1) { ret.addAll(embeddings); return
	 * ret; } // build CollisionGraph HPMutableGraph<Embedding<NodeType,
	 * EdgeType>, Integer> c = new HPListGraph<Embedding<NodeType, EdgeType>,
	 * Integer>(); Embedding<NodeType, EdgeType>[] emb = embeddings
	 * .toArray(new Embedding[embeddings.size()]); int[] nodes = new
	 * int[emb.length]; for (int i = 0; i < emb.length; ++i) nodes[i] =
	 * c.addNodeIndex(emb[i]); for (int i = 0; i < emb.length; ++i) for (int j =
	 * i + 1; j < emb.length; ++j) { if (!emb[i].overlaps(emb[j], ignore)) {
	 * c.addEdgeIndex(nodes[i], nodes[j], 0, Edge.UNDIRECTED); } } // calculate
	 * the maximum clique final BitSet clique = MaxClique.calculateHP(c, null); //
	 * return the found embeddings for (int node = clique.nextSetBit(0); node >=
	 * 0; node = clique .nextSetBit(node + 1)) ret.add(c.getNodeLabel(node));
	 * return ret; }
	 */

	/**
	 * calculates the maximal non-overlapping subset of the given embeddings
	 * 
	 * @param <NodeType>
	 * @param <EdgeType>
	 * @param embeddings
	 * @param ignore
	 *            a set of NodeLabels that do not be used for collision
	 *            detection (can be null)
	 * @return Fragment containing a maximal non-overlapping subset
	 */
	@SuppressWarnings("unchecked")
	public static <NodeType, EdgeType> Collection<Embedding<NodeType, EdgeType>> findMaxClique(
			final Collection<Embedding<NodeType, EdgeType>> embeddings,
			final Collection<NodeType> ignore) {

		if (embeddings.size() == 1) {
			return embeddings;
		}

		// build CollisionGraph
		final HPMutableGraph<Embedding<NodeType, EdgeType>, Integer> c = new HPListGraph<Embedding<NodeType, EdgeType>, Integer>();
		final Embedding<NodeType, EdgeType>[] emb = embeddings
				.toArray(new Embedding[embeddings.size()]);
		final int[] nodes = new int[emb.length];
		for (int i = 0; i < emb.length; ++i) {
			nodes[i] = c.addNodeIndex(emb[i]);
		}
		for (int i = 0; i < emb.length; ++i) {
			for (int j = i + 1; j < emb.length; ++j) {
				if (!emb[i].overlaps(emb[j], ignore)) {
					c.addEdgeIndex(nodes[i], nodes[j], 0, Edge.UNDIRECTED);
				}
			}
		}

		// calculate the maximum clique
		final BitSet clique = MaxClique.calculateHP(c, null);

		// return the found embeddings
		final Collection<Embedding<NodeType, EdgeType>> ret = new ArrayList<Embedding<NodeType, EdgeType>>(
				clique.cardinality());
		for (int node = clique.nextSetBit(0); node >= 0; node = clique
				.nextSetBit(node + 1)) {
			ret.add(c.getNodeLabel(node));
		}
		return ret;
	}

	private final Collection<NodeType> ignore;

	/**
	 * creates a new MaxCliqueStep
	 * 
	 * @param next
	 * @param ignore
	 */
	public MaxCliqueStep(final MiningStep<NodeType, EdgeType> next,
			final Collection<NodeType> ignore) {
		super(next);
		this.ignore = ignore;
	}

	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node,
			final Collection<Extension<NodeType, EdgeType>> extensions) {

		final Collection<HPEmbedding<NodeType, EdgeType>> ret = findHPMaxClique(
				node.allEmbeddings(), ignore);

		node.setFinalEmbeddings(ret);
		callNext(node, extensions);
	}

}
