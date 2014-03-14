/**
 * Created Jan 04, 2008
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
package de.parsemis.algorithms.gaston;

import de.parsemis.graph.HPGraph;
import de.parsemis.miner.environment.ThreadEnvironment;
import de.parsemis.miner.general.AbstractHierarchicalHPEmbedding;

/**
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class GastonEmbedding_hierarchical<NodeType, EdgeType>
		extends
		AbstractHierarchicalHPEmbedding<NodeType, EdgeType, GastonGraph<NodeType, EdgeType>>
		implements GastonEmbedding<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static int idc = 0;

	/** for order and fast joining like the original code */
	public final int id = idc++;

	private GastonFragment<NodeType, EdgeType> correspondingLeg;

	public GastonEmbedding_hierarchical(
			final GastonEmbedding_hierarchical<NodeType, EdgeType> parent,
			final int superNode) {
		super(parent, superNode);
		correspondingLeg = null;
	}

	public GastonEmbedding_hierarchical(
			final GastonGraph<NodeType, EdgeType> superGraph) {
		super(superGraph);
		correspondingLeg = null;
	}

	public int compareTo(final GastonEmbedding<NodeType, EdgeType> o) {
		if (getParentId() != o.getParentId()) {
			return getParentId() - o.getParentId();
		}
		return id - o.getId();
	}

	public GastonEmbedding<NodeType, EdgeType> get() {
		return this;
	}

	public int getId() {
		return id;
	}

	@SuppressWarnings("unchecked")
	public int getParentId() {
		return ((GastonEmbedding_hierarchical<NodeType, EdgeType>) super
				.getParent()).id;
	}

	@Override
	public HPGraph<NodeType, EdgeType> getSubGraph() {
		return correspondingLeg.subgraph;
	}

	@Override
	public int getSubNode() {
		return correspondingLeg.correspondingNode;
	}

	public boolean isInit() {
		return parent == null;
	}

	public void release(final ThreadEnvironment<NodeType, EdgeType> tenv) {

	}

	public void setFrag(final GastonFragment<NodeType, EdgeType> frag) {
		correspondingLeg = frag;
	}

}
