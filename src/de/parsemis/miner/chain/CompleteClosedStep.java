/**
 * created Aug 16, 2006
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

import java.util.Collection;
import java.util.TreeSet;

import de.parsemis.graph.HPGraph;
import de.parsemis.miner.general.HPEmbedding;
import de.parsemis.miner.general.HPExtendedEmbedding;

/**
 * This class detects as described in the CloseGraph algorithm closed or non
 * closed fragments during the search.
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
public class CompleteClosedStep<NodeType, EdgeType> extends
		GenerationPartialStep<NodeType, EdgeType> {

	private HPGraph<NodeType, EdgeType> cur, first;

	private Collection<Extension<NodeType, EdgeType>> curExtensions = new TreeSet<Extension<NodeType, EdgeType>>();

	private Collection<Extension<NodeType, EdgeType>> lastExtensions = new TreeSet<Extension<NodeType, EdgeType>>();

	/**
	 * creates a new complete closed pruning
	 * 
	 * @param next
	 */
	public CompleteClosedStep(final MiningStep<NodeType, EdgeType> next) {
		super(next);
	}

	/**
	 * adds all extensions of the given <code>embedding</code> to the set of
	 * interesting extensions
	 * 
	 * @param embedding
	 */
	private final void addExtensions(
			final HPExtendedEmbedding<NodeType, EdgeType> embedding) {
		final HPGraph<NodeType, EdgeType> fragment = embedding.getSubGraph();
		final HPGraph<NodeType, EdgeType> db = embedding.getSuperGraph();
		for (int nidx = fragment.getMaxNodeIndex() - 1; nidx >= 0; --nidx) {
			if (fragment.isValidNode(nidx)) {
				final int node = embedding.getSuperGraphNode(nidx);
				for (int e = db.getDegree(node) - 1; e >= 0; --e) {
					final int edge = db.getNodeEdge(node, e);
					if (embedding.freeSuperEdge(edge)) {
						final Extension<NodeType, EdgeType> ex = embedding
								.getExtension(node, edge);
						if (ex != null) {
							curExtensions.add(ex);
							// TODO: else pool.push(ex);
						}
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.MiningStep#call(de.parsemis.miner.SearchLatticeNode,
	 *      java.util.Collection)
	 */
	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node,
			final Collection<Extension<NodeType, EdgeType>> extensions) {
		if (curExtensions.size() != 0) {
			node.store(false);
		}
		callNext(node, extensions);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.GenerationPartialStep#call(de.parsemis.miner.SearchLatticeNode,
	 *      de.parsemis.graph.HPEmbedding)
	 */
	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node,
			final HPEmbedding<NodeType, EdgeType> emb) {
		final HPExtendedEmbedding<NodeType, EdgeType> embedding = (HPExtendedEmbedding<NodeType, EdgeType>) emb;
		final HPGraph<NodeType, EdgeType> db = embedding.getSuperGraph();
		if (first == null) {
			cur = first = db;
			curExtensions.clear();
		}
		if (first == db) {
			addExtensions(embedding);
		} else {
			if (cur != db) {
				cur = db;
				final Collection<Extension<NodeType, EdgeType>> tmp = lastExtensions;
				lastExtensions = curExtensions;
				curExtensions = tmp;
				curExtensions.clear();
			}
			checkExtensions(embedding);
		}
		callNext(node, embedding);
	}

	/**
	 * remove all interesting extensions that can not be mapped in the given
	 * embedding
	 * 
	 * @param embedding
	 */
	private final void checkExtensions(
			final HPExtendedEmbedding<NodeType, EdgeType> embedding) {
		for (final Extension<NodeType, EdgeType> ext : lastExtensions) {
			if (embedding.mapExtension(ext)) {
				curExtensions.add(ext);
				// TODO: else pool.push(ext);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.GenerationPartialStep#reset()
	 */
	@Override
	public void reset() {
		cur = first = null;
		resetNext();
	}

}
