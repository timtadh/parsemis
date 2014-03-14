/**
 * Created on 15.12.2007
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
package de.parsemis.algorithms.gSpan;

import java.util.BitSet;
import java.util.Collection;

import de.parsemis.graph.HPGraph;
import de.parsemis.miner.chain.Extension;
import de.parsemis.miner.environment.ThreadEnvironment;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.miner.general.Embedding;
import de.parsemis.miner.general.Frequency;
import de.parsemis.miner.general.HPEmbedding;

/**
 * Implements the HPEmbedding interface for the GSpan algorithm.
 * <p>
 * It generates the embedding just if it is necessary.
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
public class LazyExtendedEmbedding_flat<NodeType, EdgeType> implements
		GSpanHPEmbedding<NodeType, EdgeType> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6190290199714924146L;

	private final int edge;

	private final GSpanExtension<NodeType, EdgeType> ext;

	private final int superb;

	GSpanHPEmbedding_flat<NodeType, EdgeType> cache = null;

	boolean needsExtension = true;

	LazyExtendedEmbedding_flat(final int edge,
			final GSpanExtension<NodeType, EdgeType> ext, final int superb,
			final GSpanHPEmbedding_flat<NodeType, EdgeType> emb) {
		this.edge = edge;
		this.ext = ext;
		this.superb = superb;
		this.cache = emb;
	}

	public HPEmbedding<NodeType, EdgeType> extend(
			final GSpanExtension<NodeType, EdgeType> ext, final int edge,
			final int superB) {
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
		if (needsExtension) {
			needsExtension = false;
			final DFSCode<NodeType, EdgeType> code = (DFSCode<NodeType, EdgeType>) ext.frag;
			final int[] superNodes = cache.getSuperNodes(code.toHPFragment()
					.toHPGraph().getNodeCount());
			if (ext.edge.isForward()) {
				superNodes[ext.edge.getNodeB()] = superb;
			}
			final BitSet freeEdges = (BitSet) cache.getFreeEdges().clone();
			freeEdges.clear(edge);
			cache = (GSpanHPEmbedding_flat<NodeType, EdgeType>) cache.tenv()
					.getHPEmbedding(code, cache.getDataBaseGraph(), superNodes,
							freeEdges);
		}
		return cache;
	}

	public DataBaseGraph<NodeType, EdgeType> getDataBaseGraph() {
		return cache.getDataBaseGraph();
	}

	public Extension<NodeType, EdgeType> getExtension(final int superNode,
			final int superEdge) {
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
		return cache.getSuperGraph();
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

	public boolean overlaps(final HPEmbedding<NodeType, EdgeType> other,
			final Collection<NodeType> ignore) {
		return get().overlaps(other, ignore);
	}

	public void release(final ThreadEnvironment<NodeType, EdgeType> target) {
		if (!needsExtension) {
			cache.release(target);
			cache = null;
		}
	}

	public Embedding<NodeType, EdgeType> toEmbedding() {
		return get().toEmbedding();
	}
}