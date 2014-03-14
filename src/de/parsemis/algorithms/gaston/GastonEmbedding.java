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
import de.parsemis.miner.general.HPEmbedding;

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
public interface GastonEmbedding<NodeType, EdgeType> extends
		HPEmbedding<NodeType, EdgeType>,
		Comparable<GastonEmbedding<NodeType, EdgeType>> {

	public GastonEmbedding<NodeType, EdgeType> get();

	int getId();

	int getParentId();

	public HPGraph<NodeType, EdgeType> getSubGraph();

	public int getSubNode();

	public int getSuperNode();

	public boolean isInit();

	public void release(final ThreadEnvironment<NodeType, EdgeType> tenv);

	void setFrag(GastonFragment<NodeType, EdgeType> frag);

}
