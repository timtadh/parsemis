/**
 * created 23.01.2008
 * 
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * Copyright 2008 Marc Woerlein
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
public abstract class AbstractHierarchicalHPEmbedding<NodeType, EdgeType, DB extends DataBaseGraph<NodeType, EdgeType>>
		extends AbstractHPEmbedding<NodeType, EdgeType, DB> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int INIT = -2;

	protected AbstractHierarchicalHPEmbedding<NodeType, EdgeType, DB> parent;

	protected int superNode;

	protected AbstractHierarchicalHPEmbedding() {

	}

	public AbstractHierarchicalHPEmbedding(
			final AbstractHierarchicalHPEmbedding<NodeType, EdgeType, DB> parent,
			final int superNode) {
		this.parent = parent;
		this.superNode = superNode;
		super.set(parent.databaseGraphIndex, null);
	}

	public AbstractHierarchicalHPEmbedding(final DB superGraph) {
		this.parent = null;
		this.superNode = INIT;
		super.set(superGraph.getIndex(), null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPEmbedding#freeSuperNode(int)
	 */
	@Override
	protected BitSet freeNodes() {
		if (freeNodes == null) {
			freeNodes = getDataBaseGraph().getNodes();
			for (AbstractHierarchicalHPEmbedding<NodeType, EdgeType, DB> ack = this; ack.superNode != INIT; ack = ack.parent) {
				freeNodes.clear(ack.superNode);
			}
		}
		return freeNodes;
	}

	public AbstractHierarchicalHPEmbedding<NodeType, EdgeType, DB> getParent() {
		return parent;
	}

	public int getSubGraphNode(final int superNode) {
		for (AbstractHierarchicalHPEmbedding<NodeType, EdgeType, DB> ack = this; ack.superNode != INIT; ack = ack.parent) {
			if (ack.superNode == superNode) {
				return ack.getSubNode();
			}
		}
		return HPGraph.NO_NODE;
	}

	abstract public int getSubNode();

	public int getSuperGraphNode(final int subNode) {
		for (AbstractHierarchicalHPEmbedding<NodeType, EdgeType, DB> ack = this; ack.superNode != INIT; ack = ack.parent) {
			if (ack.getSubNode() == subNode) {
				return ack.superNode;
			}
		}
		return HPGraph.NO_NODE;
	}

	public int getSuperNode() {
		return superNode;
	}

	private final boolean overlaps(final HPEmbedding<NodeType, EdgeType> other) {
		for (AbstractHierarchicalHPEmbedding<NodeType, EdgeType, DB> ack = this; ack.superNode != INIT; ack = ack.parent) {
			if (other.getSubGraphNode(ack.superNode) != HPGraph.NO_NODE) {
				return true;
			}
		}
		return false;
	}

	public boolean overlaps(final HPEmbedding<NodeType, EdgeType> other,
			final Collection<NodeType> ignore) {
		if (other.getDataBaseGraph() != this.getDataBaseGraph()) {
			return false;
		}
		if (ignore == null) {
			return overlaps(other);
		}
		for (AbstractHierarchicalHPEmbedding<NodeType, EdgeType, DB> ack = this; ack.superNode != INIT; ack = ack.parent) {
			if (other.getSubGraphNode(ack.superNode) != HPGraph.NO_NODE
					&& !ignore.contains(getSuperGraph().getNodeLabel(
							ack.superNode))) {
				return true;
			}
		}
		return false;
	}

	public AbstractHierarchicalHPEmbedding<NodeType, EdgeType, DB> set(
			final HPGraph<NodeType, EdgeType> sub, final int superNode,
			final AbstractHierarchicalHPEmbedding<NodeType, EdgeType, DB> parent) {
		super.set(parent.databaseGraphIndex, sub);
		this.superNode = superNode;
		this.parent = parent;
		return this;
	}

}