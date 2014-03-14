/**
 * created Sep 25, 2006
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
package de.parsemis.miner.filter;

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;

import de.parsemis.graph.Edge;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.Node;
import de.parsemis.miner.general.Embedding;
import de.parsemis.miner.general.Fragment;
import de.parsemis.miner.general.HPEmbedding;
import de.parsemis.utils.GraphUtils;
import de.parsemis.utils.IntIterator;

/**
 * This class provide a post-filter to filter out single sinked fragments as
 * used by Zaretsky.
 * 
 * These DAG-fragments have only one node without outgoing edges. An additional
 * constraint for the Zaretsky fragments is, that between each pair of nodes in
 * a fragment the following condition holds:
 * 
 * each path in the database graphs between the corresponding nodes has also to
 * be in the fragment
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
public class ZaretskyFilter<NodeType, EdgeType> implements
		FragmentFilter<NodeType, EdgeType> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.general.FragmentFilter#filter(java.util.Collection)
	 */
	public Collection<Fragment<NodeType, EdgeType>> filter(
			final Collection<Fragment<NodeType, EdgeType>> frags) {

		for (final Iterator<Fragment<NodeType, EdgeType>> fit = frags
				.iterator(); fit.hasNext();) {
			final Fragment<NodeType, EdgeType> ack = fit.next();
			final HPGraph<NodeType, EdgeType> graph = ack.toGraph()
					.toHPGraph();
			final int sink = getSingleSink(graph);
			if (sink < 0 || !GraphUtils.isConnected(graph)
					|| !willBeCreated(ack, ack.toGraph().getNode(sink))) {
				fit.remove();
			}
		}
		return frags;
	}

	/*
	 * detects the single sink (if available)
	 */
	private final int getSingleSink(final HPGraph<NodeType, EdgeType> graph) {
		if (graph.getNodeCount() < 1) {
			return -1;
		}
		int ret = -1;
		for (int ack = graph.getMaxNodeIndex() - 1; ack >= 0; ack--) {
			if (graph.isValidNode(ack) && graph.getOutDegree(ack) == 0) {
				if (ret != -1) {
					return -1;
				} else {
					ret = ack;
				}
			}
		}
		return ret;
	}

	/*
	 * checks, if there are unattended paths in the datebase
	 */
	private final boolean willBeCreated(final Fragment<NodeType, EdgeType> f,
			final Node<NodeType, EdgeType> sink) {
		for (final Embedding<NodeType, EdgeType> e : f) {
			// for each embeding (so also for each database graph)
			final HPEmbedding<NodeType, EdgeType> emb = e.toHPEmbedding();
			final HPGraph<NodeType, EdgeType> sub = emb.getSubGraph();
			final HPGraph<NodeType, EdgeType> sup = emb.getSuperGraph();
			boolean couldCreate = true;
			final BitSet todo = new BitSet(sup.getMaxNodeIndex());
			for (int n = sub.getMaxNodeIndex() - 1; couldCreate && n >= 0; --n) {
				// for each node in the subgraph detect pathes outside the
				// fragment, that returns to the fragments
				if (sub.isValidNode(n)) {
					final int sourceNode = emb.getSuperGraphNode(n);
					for (int i = sup.getDegree(sourceNode) - 1; couldCreate
							&& i >= 0; --i) {
						final int edge = sup.getNodeEdge(sourceNode, i);
						if (sup.getDirection(edge, sourceNode) == Edge.OUTGOING
								&& emb.freeSuperEdge(edge)) {
							// found start of a path that leaves the fragment,
							// so check if it will return
							todo.clear();
							for (int next = sup.getOtherNode(edge, sourceNode); couldCreate
									&& next > -1; next = todo.nextSetBit(0)) {
								for (final IntIterator oit = sup
										.getOutEdgeIndices(next); couldCreate
										&& oit.hasNext();) {
									final int neibor = sup.getOtherNode(oit
											.next(), next);
									if (emb.getSubGraphNode(neibor) > -1) {
										// current neibor is in side the
										// fragment, so the path returns
										couldCreate = false;
									} else {
										todo.set(neibor);
									}
								}
								todo.clear(next);
							}
						}
					}
				}
			}
			if (couldCreate) {
				return true;
			}
		}
		return false;
	}

}
