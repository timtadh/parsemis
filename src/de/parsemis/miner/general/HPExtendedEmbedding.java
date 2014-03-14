/**
 * Created on 04.01.2008
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

import de.parsemis.miner.chain.Extension;

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
public interface HPExtendedEmbedding<NodeType, EdgeType> extends
		HPEmbedding<NodeType, EdgeType> {
	/**
	 * @param superNode
	 *            the starting supergraph node for the extension
	 * @param superEdge
	 *            the supergraph edge the extension shall represent
	 * @return a corresponding extension to the given supergraph edge
	 */
	public Extension<NodeType, EdgeType> getExtension(final int superNode,
			final int superEdge);

	/**
	 * checks if the given Extension can be mapped to this embedding
	 * 
	 * @param ext
	 * @return <code>true</code>, if at least one supergraph edge is
	 *         represented by the given extension
	 */
	public boolean mapExtension(Extension<NodeType, EdgeType> ext);

	/**
	 * searches a edge of the given set the given Extension can be mapped to in
	 * this embedding
	 * 
	 * @param ext
	 * @param allowedEdges
	 * @return the found edge
	 */
	public int mapExtension(Extension<NodeType, EdgeType> ext,
			BitSet allowedEdges);

}
