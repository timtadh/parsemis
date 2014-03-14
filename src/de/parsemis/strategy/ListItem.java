/**
 * Created on Jun 26, 2006
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
package de.parsemis.strategy;

/**
 * This class represents an element of a double linked list
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <Type>
 *            Type that is stored in the list
 */
public class ListItem<Type> {

	/** the value of the ListItem */
	public Type elem;

	/** the next item in the double linked list */
	public ListItem<Type> next;

	/** the previous item in the double linked list */
	public ListItem<Type> prev;

	/**
	 * initialize the public fields;
	 */
	protected ListItem() {
		next = prev = null;
	}

}