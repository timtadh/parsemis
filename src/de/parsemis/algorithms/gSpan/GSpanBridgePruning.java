/**
 * Created on Aug 16, 2006
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
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import de.parsemis.graph.HPGraph;
import de.parsemis.miner.chain.Extension;
import de.parsemis.miner.chain.GenerationPartialStep;
import de.parsemis.miner.chain.SearchLatticeNode;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.miner.general.HPEmbedding;
import de.parsemis.utils.GraphUtils;

/**
 * Implements an improved pruning for closed graph mining.
 * <p>
 * It prunes perfect extensions that leads just to non closed fragments
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
public class GSpanBridgePruning<NodeType, EdgeType> extends
		GenerationPartialStep<NodeType, EdgeType> {

	private final GThreadEnvironment<NodeType, EdgeType> tenv;

	private boolean first = true;

	// store possible extensions included in all embeddings
	private SortedSet<GSpanExtension<NodeType, EdgeType>> curExtensions = new TreeSet<GSpanExtension<NodeType, EdgeType>>();

	private SortedSet<GSpanExtension<NodeType, EdgeType>> lastExtensions = new TreeSet<GSpanExtension<NodeType, EdgeType>>();

	// should be part of a Graph, but what is a bridge in a directed graph?
	private final BitSet[] bridges = new BitSet[LocalEnvironment.env(this)
			.graphCount()];

	/**
	 * creates a new pruning
	 * 
	 * @param next
	 *            the next step of the generation chain
	 * @param tenv
	 *            the environment used for releasing unused objects
	 */
	public GSpanBridgePruning(
			final GenerationPartialStep<NodeType, EdgeType> next,
			final GThreadEnvironment<NodeType, EdgeType> tenv) {
		super(next);
		this.tenv = tenv;
	}

	/**
	 * adds all extensions (bridges or backward edges) of the given
	 * <code>embedding</code> to the set of possible perfect extensions
	 * 
	 * @param embedding
	 */
	private final void addExtensions(
			final GSpanHPEmbedding<NodeType, EdgeType> embedding) {
		first = false;
		swap();

		final HPGraph<NodeType, EdgeType> fragment = embedding.getSubGraph();
		final HPGraph<NodeType, EdgeType> db = embedding.getSuperGraph();
		for (int nidx = fragment.getMaxNodeIndex() - 1; nidx >= 0; --nidx) {
			if (fragment.isValidNode(nidx)) {
				final int node = embedding.getSuperGraphNode(nidx);
				for (int e = db.getDegree(node) - 1; e >= 0; --e) {
					final int edge = db.getNodeEdge(node, e);
					if (embedding.freeSuperEdge(edge)) {
						final GSpanExtension<NodeType, EdgeType> ext = (GSpanExtension<NodeType, EdgeType>) embedding
								.getExtension(node, edge);
						if (ext != null) {
							if (!ext.edge.isForward()
									|| getBridges(embedding.getDataBaseGraph())
											.get(edge)) {
								curExtensions.add(ext);
							} else {
								ext.edge.release(tenv);
								ext.release(tenv);
							}
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
		if (curExtensions.size() > 0) {
			// Remove all extensions that are smaller than the first one that
			// can be mapped to each embedding. These extensions will also be
			// added to the fragment containing the first perfect embedding and
			// result in non-closed fragments
			final Extension<NodeType, EdgeType> first = curExtensions.first();
			for (final Iterator<Extension<NodeType, EdgeType>> all = extensions
					.iterator(); all.hasNext();) {
				if (first.compareTo(all.next()) < 0) {
					// pool.push(ack);
					all.remove();
				}
			}
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
			final HPEmbedding<NodeType, EdgeType> embedding) {
		// find extensions that are part of each embedding
		if (first) {
			addExtensions((GSpanHPEmbedding<NodeType, EdgeType>) embedding);
		} else {
			checkExtensions((GSpanHPEmbedding<NodeType, EdgeType>) embedding);
		}
		callNext(node, embedding);
	}

	/**
	 * remove all possible perfect extensions that can not be mapped (to a
	 * bridge or a backward edge) in the given <code>embedding</code>
	 * 
	 * @param embedding
	 */
	private final void checkExtensions(
			final GSpanHPEmbedding<NodeType, EdgeType> embedding) {
		swap();
		for (final Iterator<GSpanExtension<NodeType, EdgeType>> it = lastExtensions
				.iterator(); it.hasNext();) {
			final GSpanExtension<NodeType, EdgeType> ext = it.next();
			if ((!ext.edge.isForward() && embedding.mapExtension(ext))
					|| embedding.mapExtension(ext, getBridges(embedding
							.getDataBaseGraph())) != HPGraph.NO_EDGE) {
				curExtensions.add(ext);
				it.remove();
			}
		}
	}

	private final BitSet getBridges(final DataBaseGraph<NodeType, EdgeType> db) {
		if (bridges[db.getIndex()] == null) {
			bridges[db.getIndex()] = GraphUtils.getWeakBridges(db.toGraph()
					.toHPGraph());
		}
		return bridges[db.getIndex()];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.GenerationPartialStep#reset()
	 */
	@Override
	public void reset() {
		first = true;
		resetNext();
	}

	/**
	 * swaps and clears the internal sets of embeddings for a consistent reusing
	 * for the next embeddings
	 */
	private final void swap() {
		final SortedSet<GSpanExtension<NodeType, EdgeType>> tmp = lastExtensions;
		lastExtensions = curExtensions;
		curExtensions = tmp;
		for (final GSpanExtension<NodeType, EdgeType> ack : curExtensions) {
			ack.edge.release(tenv);
			ack.release(tenv);
		}
		curExtensions.clear();
	}

}
