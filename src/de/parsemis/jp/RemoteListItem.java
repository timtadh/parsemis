/**
 * created Nov 13, 2007
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
package de.parsemis.jp;

import de.parsemis.utils.Generic;

/**
 * This class offers the requiered fields to be stored in a RemoteStackList.
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 * 
 * @remote
 */
public class RemoteListItem<NodeType, EdgeType> implements
		Generic<NodeType, EdgeType> {

	/** the next item in the double linked list */
	public RemoteListItem<NodeType, EdgeType> next;

	/** the previous item in the double linked list */
	public RemoteListItem<NodeType, EdgeType> prev;
}
