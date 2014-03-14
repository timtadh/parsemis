/**
 * created May 25, 2006
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
import java.util.Collection;
import java.util.Iterator;

import de.parsemis.graph.Edge;
import de.parsemis.graph.HPGraph;
import de.parsemis.miner.chain.Extension;
import de.parsemis.miner.chain.GenerationStep;
import de.parsemis.miner.chain.MiningStep;
import de.parsemis.miner.chain.SearchLatticeNode;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.miner.general.HPEmbedding;

/**
 * Contains the whole chain for possible children generation.
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
public class GSpanGeneration<NodeType, EdgeType> extends
		GenerationStep<NodeType, EdgeType> {
	private final Collection<HPEmbedding<NodeType, EdgeType>> embeddings;

	private final LocalEnvironment<NodeType, EdgeType> env;

	private final GThreadEnvironment<NodeType, EdgeType> tenv;

	/**
	 * creates a new embeddings searching GSpanGeneration
	 * 
	 * @param next
	 * @param tenv
	 */
	public GSpanGeneration(final MiningStep<NodeType, EdgeType> next,
			final GThreadEnvironment<NodeType, EdgeType> tenv) {
		super(next);
		this.env = LocalEnvironment.env(this);
		this.embeddings = new ArrayList<HPEmbedding<NodeType, EdgeType>>();
		// ArrayList for deterministic order
		this.tenv = tenv;
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
		reset();
		final DFSCode<NodeType, EdgeType> code = (DFSCode<NodeType, EdgeType>) node;
		final int[] ackNodes = tenv.getIntArray(code.toHPFragment().toHPGraph()
				.getNodeCount(), HPGraph.NO_NODE);
		// search for children in all required database graphs, therefor search
		// all embeddings
		for (final Iterator<DataBaseGraph<NodeType, EdgeType>> ggit = code
				.toHPFragment().graphIterator(); ggit.hasNext();) {
			searchEmbeddings((GSpanGraph<NodeType, EdgeType>) ggit.next(),
					code, ackNodes);
		}
		tenv.push(ackNodes);
		super.call(node, extensions);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.GenerationPartialStep#call(de.parsemis.miner.SearchLatticeNode,
	 *      de.parsemis.graph.Embedding)
	 */
	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node,
			final HPEmbedding<NodeType, EdgeType> embedding) {
		embeddings.add(embedding);
		super.call(node, embedding);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.GenerationPartialStep#reset()
	 */
	@Override
	public final void reset() {
		// delete old Embeddings;
		if (!env.storeEmbeddings) {
			for (final HPEmbedding<NodeType, EdgeType> emb : embeddings) {
				((GSpanHPEmbedding<NodeType, EdgeType>) emb).release(tenv);
			}
		}
		embeddings.clear();
		super.reset();
	}

	/* initializes the recursive search for embeddings */
	private final void searchEmbeddings(
			final GSpanGraph<NodeType, EdgeType> dbgraph,
			final DFSCode<NodeType, EdgeType> code, final int[] ackNodes) {
		// initialise embedding arrays
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		final HPGraph<NodeType, EdgeType> me = dbgraph.toHPGraph();
		final int[] usedNodes = tenv.getIntArray(me.getMaxNodeIndex(),
				ackNodes.length);
		final BitSet freeEdges = new BitSet(me.getMaxEdgeIndex() + 1);
		for (int edge = me.getMaxEdgeIndex() - 1; edge >= 0; --edge) {
			if (dbgraph.edgeExists(edge)) {
				freeEdges.set(edge);
			}
		}
		final GSpanEdge<NodeType, EdgeType> first = code.getFirst();

		for (int node = me.getMaxNodeIndex() - 1; node >= 0; --node) {
			// try all nodes as embedding start node
			if (me.isValidNode(node)
					&& me.getNodeLabelIndex(node, env) == first.getLabelA()) {
				ackNodes[0] = node;
				usedNodes[node] = 0;
				// recursive extension
				searchEmbeddings(dbgraph, first, code, ackNodes, usedNodes,
						freeEdges);
				usedNodes[node] = ackNodes.length;
			}

		}
	}

	/* recursiv extension to complete embeddings */
	private void searchEmbeddings(final GSpanGraph<NodeType, EdgeType> dbgraph,
			final GSpanEdge<NodeType, EdgeType> currentEdge,
			final DFSCode<NodeType, EdgeType> code, final int[] ackNodes,
			final int[] usedNodes, final BitSet freeEdges) {
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		final HPGraph<NodeType, EdgeType> me = dbgraph.toHPGraph();
		final int node = ackNodes[currentEdge.getNodeA()];
		final int cdir = currentEdge.getDirection();
		if (currentEdge.isForward()) {
			for (int i = me.getDegree(node) - 1; i >= 0; --i) {
				final int edge = me.getNodeEdge(node, i);// */
				if (freeEdges.get(edge) &&
				// (me.getDirection(edge,node)==cdir) &&
						// (me.getEdgeLabelIndex(edge,LocalEnvironment.env(this))==currentEdge.getEdgeLabel())
						currentEdge.sameAs(me, edge, node)) {
					final int oidx = me.getOtherNode(edge, node);
					// only unused, correct labeled edges
					if (usedNodes[oidx] == ackNodes.length
							&& (me.getNodeLabelIndex(me
									.getOtherNode(edge, node), env) == currentEdge
									.getLabelB())) {
						// only unused, correct labeled nodes
						final int old = usedNodes[oidx];
						ackNodes[currentEdge.getNodeB()] = oidx;
						usedNodes[oidx] = currentEdge.getNodeB();
						freeEdges.clear(edge);
						// If found, call first generation partial step
						if (currentEdge.next == null) {
							call(code, tenv.getHPEmbedding(code, dbgraph,
									ackNodes, freeEdges));
						} else {
							searchEmbeddings(dbgraph, currentEdge.next, code,
									ackNodes, usedNodes, freeEdges);
						}
						usedNodes[oidx] = old;
						freeEdges.set(edge);
					}

				}
			}
		} else { // backward Edge
			final int nB = ackNodes[currentEdge.getNodeB()];

			final int edge = (cdir == Edge.INCOMING ? me.getEdge(nB, node) : me
					.getEdge(node, nB));

			// check if corresponding edge exists and is rigth labeled
			if (edge != -1 && freeEdges.get(edge) &&
			// (me.getDirection(edge,node)==cdir) &&
					// (me.getEdgeLabelIndex(edge,LocalEnvironment.env(this))==currentEdge.getEdgeLabel())
					currentEdge.sameAs(me, edge, node)) {
				freeEdges.clear(edge);
				// If found, call first generation partial step
				if (currentEdge.next == null) {
					call(code, tenv.getHPEmbedding(code, dbgraph, ackNodes,
							freeEdges));
				} else {
					searchEmbeddings(dbgraph, currentEdge.next, code, ackNodes,
							usedNodes, freeEdges);
				}
				freeEdges.set(edge);
			}
		}
	}
}
