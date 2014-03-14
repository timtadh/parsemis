/**
 * created Jul 6, 2006
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

import java.util.Collection;

import de.parsemis.miner.general.Fragment;

/**
 * This interface encapsulates the capability to filter fragments after the
 * search
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
public interface FragmentFilter<NodeType, EdgeType> {

	/**
	 * reduce the found set of fragments
	 * 
	 * @param frags
	 * @return the filtered collection of Fragments
	 */
	public Collection<Fragment<NodeType, EdgeType>> filter(
			Collection<Fragment<NodeType, EdgeType>> frags);

}
