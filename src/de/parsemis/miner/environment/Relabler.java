/**
 * created May 17, 2006
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
package de.parsemis.miner.environment;

/**
 * This interface maps to each node and edge label corresponding unique indices.
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
public interface Relabler<NodeType, EdgeType> {

	/**
	 * @param idx
	 * @return the edge label with the given index
	 */
	public EdgeType getEdgeLabel(final int idx);

	/**
	 * @param edge
	 * @return the index of the given edge label
	 */
	public int getEdgeLabelIndex(final EdgeType edge);

	/**
	 * @param idx
	 * @return the node label with the given index
	 */
	public NodeType getNodeLabel(final int idx);

	/**
	 * @param node
	 * @return the index of the given node label
	 */
	public int getNodeLabelIndex(final NodeType node);

}
