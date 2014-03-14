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

import de.parsemis.miner.environment.ThreadEnvironment;
import de.parsemis.miner.general.DefaultFlatHPEmbedding;

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
public class GastonEmbedding_flat<NodeType, EdgeType>
		extends
		DefaultFlatHPEmbedding<NodeType, EdgeType, GastonGraph<NodeType, EdgeType>>
		implements GastonEmbedding<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** for order and fast joining like the original code */
	public final int id;

	private final GastonFragment<NodeType, EdgeType> correspondingLeg;

	public GastonEmbedding_flat(
			final GastonGraph<NodeType, EdgeType> superGraph, final int id) {
		super.set(superGraph, null, new int[0]);
		correspondingLeg = null;
		this.id = id;
	}

	public GastonEmbedding_flat(final int dbIdx, final int[] superNodes,
			final GastonFragment<NodeType, EdgeType> frag, final int id) {
		super.set(dbIdx, frag.subgraph, superNodes);
		correspondingLeg = frag;
		this.id = id;
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

	public int getParentId() {
		assert false : "should not happen";
		return -1;
	}

	public int getSubNode() {
		return correspondingLeg.correspondingNode;
	}

	public int getSuperNode() {
		return getSuperGraphNode(getSubNode());
	}

	int[] getSuperNodes() {
		return superNodes();
	}

	public boolean isInit() {
		return correspondingLeg == null;
	}

	@Override
	public void release(final ThreadEnvironment<NodeType, EdgeType> tenv) {

	}

	public void setFrag(final GastonFragment<NodeType, EdgeType> frag) {
		assert false : "should not happen";
	}

}
