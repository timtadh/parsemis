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

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import de.parsemis.graph.HPGraph;
import de.parsemis.graph.HPMutableGraph;
import de.parsemis.miner.chain.Extension;
import de.parsemis.miner.chain.GenerationPartialStep;
import de.parsemis.miner.chain.SearchLatticeNode;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.general.HPEmbedding;
import de.parsemis.miner.general.HPFragment;

/**
 * Represents the right most extension of gSpan.
 * <p>
 * For gSpan just backward edges from the last inserted node, or forward edges
 * staring in nodes of the right most path (path of forward edges between the
 * "root" node to the last inserted node) are relevant.
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
public class RightMostExtension<NodeType, EdgeType> extends
		GenerationPartialStep<NodeType, EdgeType> {

	public static int counter = 0;

	protected final GThreadEnvironment<NodeType, EdgeType> tenv;

	private final LocalEnvironment<NodeType, EdgeType> env;

	private final Map<GSpanEdge<NodeType, EdgeType>, GSpanExtension<NodeType, EdgeType>> children;

	/**
	 * creates a new pruning
	 * 
	 * @param next
	 *            the next step of the generation chain
	 * @param tenv
	 *            the environment used for releasing unused objects
	 */
	public RightMostExtension(
			final GenerationPartialStep<NodeType, EdgeType> next,
			final GThreadEnvironment<NodeType, EdgeType> tenv) {
		super(next);
		this.tenv = tenv;
		this.env = LocalEnvironment.env(this);
		this.children = new TreeMap<GSpanEdge<NodeType, EdgeType>, GSpanExtension<NodeType, EdgeType>>();
		// TODO: evtl schnellere vergleich der gEdges, aber das macht nicht viel
		// aus
	}

	/**
	 * includes the found extension to the corresponding fragment
	 * 
	 * @param gEdge
	 * @param emb
	 * @param code
	 * @param edge
	 * @param nodeB
	 */
	protected void add(final GSpanEdge<NodeType, EdgeType> gEdge,
			final GSpanHPEmbedding<NodeType, EdgeType> emb,
			final DFSCode<NodeType, EdgeType> code, final int edge,
			final int nodeB) {
		// search corresponding extension
		GSpanExtension<NodeType, EdgeType> ext = children.get(gEdge);
		if (env.storeEmbeddings) {
			if (ext == null) {
				// create new extension
				final HPMutableGraph<NodeType, EdgeType> ng = (HPMutableGraph<NodeType, EdgeType>) code
						.toHPFragment().toHPGraph().clone();
				// TODO: avoid clone??
				gEdge.addTo(ng);
				final HPFragment<NodeType, EdgeType> f = tenv.getHPFragment(ng);
				ext = tenv.getExtension(gEdge, f);
				ext.frag = code.extend(ext);
				children.put(gEdge, ext);
			} else {
				gEdge.release(tenv);
			}
			// store embedding in extension
			ext.getFragment().add(emb.extend(ext, edge, nodeB));
		} else {
			if (ext == null) {
				// create new extension
				final HPMutableGraph<NodeType, EdgeType> ng = (HPMutableGraph<NodeType, EdgeType>) emb
						.getSubGraph().clone();
				// TODO: avoid clone??
				gEdge.addTo(ng);
				final HPFragment<NodeType, EdgeType> f = tenv.getHPFragment(ng);
				ext = tenv.getExtension(gEdge, f);
				children.put(gEdge, ext);
			} else {
				gEdge.release(tenv);
			}
			// store database graph in extension
			ext.getFragment().add(emb.getDataBaseGraph());
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
		// just give YOUR extensions to the next step
		extensions.clear();
		extensions.addAll(children.values());
		callNext(node, extensions);
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
		counter++;
		extend((DFSCode<NodeType, EdgeType>) node,
				(GSpanHPEmbedding<NodeType, EdgeType>) embedding);
		callNext(node, embedding);
	}

	protected final void extend(final DFSCode<NodeType, EdgeType> code,
			final GSpanHPEmbedding<NodeType, EdgeType> emb) {
		final GSpanGraph<NodeType, EdgeType> dbg = (GSpanGraph<NodeType, EdgeType>) emb
				.getDataBaseGraph();
		final HPGraph<NodeType, EdgeType> sup = emb.getSuperGraph();
		final HPGraph<NodeType, EdgeType> sub = emb.getSubGraph();
		final int lastNode = emb.getSubGraph().getNodeCount() - 1;

		// find extensions of the last node;
		{
			final int nodeA = emb.getSuperGraphNode(lastNode);
			for (int i = sup.getDegree(nodeA) - 1; i >= 0; --i) {
				final int edge = sup.getNodeEdge(nodeA, i);
				if (!dbg.edgeExists(edge)) {
					continue;
				}

				if (emb.freeSuperEdge(edge)) {
					// adjacent edge currently unused
					final int oNode = sup.getOtherNode(edge, nodeA);
					final int fNode = emb.getSubGraphNode(oNode);
					final int oidx = (fNode == -1 ? lastNode + 1 : fNode);
					final GSpanEdge<NodeType, EdgeType> gEdge = tenv.getEdge(
							lastNode, oidx, sup.getNodeLabelIndex(nodeA, env),
							sup.getEdgeLabelIndex(edge, env), sup
									.getNodeLabelIndex(oNode, env), sup
									.getDirection(edge, nodeA));
					if ((code.getLast().compareTo(gEdge) < 0)
							&& (!(env.findPathsOnly || env.findTreesOnly) || gEdge
									.isForward())) {
						// possible extension found
						add(gEdge, emb, code, edge, oNode);
					} else {
						gEdge.release(tenv);
					}
				}
			}
		}

		// check if findPathsOnly and node 0 has even more than one edge
		if (!env.findPathsOnly || sub.getDegree(0) <= 1) {
			// if findPathsOnly then only extensions at node 0 are necessary
			int ackNode = (env.findPathsOnly ? 0 : lastNode);
			do {
				// find extension of the right most path
				final GSpanEdge<NodeType, EdgeType> ack = code
						.getParent(ackNode);
				ackNode = ack.getNodeA();
				final int nodeA = emb.getSuperGraphNode(ackNode);
				for (int i = sup.getDegree(nodeA) - 1; i >= 0; --i) {
					final int edge = sup.getNodeEdge(nodeA, i);
					if (!dbg.edgeExists(edge)) {
						continue;
					}
					final int oNode = sup.getOtherNode(edge, nodeA);
					if (emb.freeSuperEdge(edge)
							&& emb.getSubGraphNode(oNode) == -1) {
						// only forward eges are allowed
						add(tenv.getEdge(ackNode, lastNode + 1, sup
								.getNodeLabelIndex(nodeA, env), sup
								.getEdgeLabelIndex(edge, env), sup
								.getNodeLabelIndex(oNode, env), sup
								.getDirection(edge, nodeA)), emb, code, edge,
								oNode);
					}
				}
			} while (ackNode > 0);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.GenerationPartialStep#reset()
	 */
	@Override
	public void reset() {
		children.clear();
		resetNext();
	}

}
