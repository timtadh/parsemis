/**
 * created Nov 27, 2007
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
package de.parsemis.miner.general;

import java.util.BitSet;
import java.util.Collection;

import de.parsemis.graph.HPGraph;

/**
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 * @param <DB>
 *            the corresponding DataBaseGraph type
 */
public abstract class AbstractFlatHPEmbedding<NodeType, EdgeType, DB extends DataBaseGraph<NodeType, EdgeType>>
		extends AbstractHPEmbedding<NodeType, EdgeType, DB> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// stores to each subgraph node index (index of the array) the corresponding
	// supergraph node index (value)
	private int[] superNodes;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPEmbedding#freeSuperNode(int)
	 */
	@Override
	protected BitSet freeNodes() {
		if (freeNodes == null) {
			freeNodes = getDataBaseGraph().getNodes();
			for (final int node : superNodes) {
				freeNodes.clear(node);
			}
		}
		return freeNodes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPEmbedding#getSubGraphNode(int)
	 */
	public int getSubGraphNode(final int superNode) {
		for (int i = 0; i < superNodes.length; i++) {
			if (superNodes[i] == superNode) {
				return i;
			}
		}
		return HPGraph.NO_NODE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPEmbedding#getSuperGraphNode(int)
	 */
	public int getSuperGraphNode(final int subNode) {
		return superNodes[subNode];
	}

	private final boolean overlaps(final HPEmbedding<NodeType, EdgeType> other) {
		for (int i = 0; i < superNodes.length; ++i) {
			if (other.getSubGraphNode(superNodes[i]) != HPGraph.NO_NODE) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPEmbedding#overlaps(de.parsemis.graph.HPEmbedding,
	 *      java.util.Collection)
	 */
	public boolean overlaps(final HPEmbedding<NodeType, EdgeType> other,
			final Collection<NodeType> ignore) {
		if (other.getDataBaseGraph() != this.getDataBaseGraph()) {
			return false;
		}
		if (ignore == null) {
			return overlaps(other);
		}
		for (int i = 0; i < superNodes.length; ++i) {
			if (other.getSubGraphNode(superNodes[i]) != HPGraph.NO_NODE
					&& !ignore.contains(getSuperGraph().getNodeLabel(
							getSuperGraphNode(i)))) {
				return true;
			}
		}
		return false;
	}

	public AbstractFlatHPEmbedding<NodeType, EdgeType, DB> set(
			final DB dbGraph, final HPGraph<NodeType, EdgeType> subGraph,
			final int[] superNodes) {
		super.set(dbGraph.getIndex(), subGraph);
		this.superNodes = superNodes;
		return this;
	}

	public AbstractFlatHPEmbedding<NodeType, EdgeType, DB> set(
			final int dbGraphIdx, final HPGraph<NodeType, EdgeType> subGraph,
			final int[] superNodes) {
		super.set(dbGraphIdx, subGraph);
		this.superNodes = superNodes;
		return this;
	}

	protected final int[] superNodes() {
		return superNodes;
	}

}
