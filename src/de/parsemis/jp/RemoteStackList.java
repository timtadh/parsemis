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

import java.util.Collection;
import java.util.Iterator;

import de.parsemis.strategy.MiningStack;
import de.parsemis.strategy.StackList;

/**
 * This class is the list of non-empty stacks for the JavaParty distribution
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
public class RemoteStackList<NodeType, EdgeType> extends
		RemoteListItem<NodeType, EdgeType> implements
		StackList<NodeType, EdgeType> {

	private int size = 0;

	/**
	 * creates a new empty List
	 */
	public RemoteStackList() {
		this.next = this.prev = this;
	}

	private final boolean _add(final RemoteListItem<NodeType, EdgeType> s) {
		if (s.next != null || s.prev != null) {
			return false;
		}
		s.prev = this.prev;
		s.next = this;
		this.prev.next = s;
		this.prev = s;
		size++;
		return true;
	}

	private final boolean _remove(final RemoteListItem<NodeType, EdgeType> s) {
		if (s.next == null || s.prev == null) {
			return false;
		}
		s.prev.next = s.next;
		s.next.prev = s.prev;
		s.prev = s.next = null;
		size--;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	public synchronized boolean add(final MiningStack<NodeType, EdgeType> o) {
		if (!(o instanceof RemoteStack)) {
			return false;
		}
		final boolean ret = _add((RemoteStack<NodeType, EdgeType>) o);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	public synchronized boolean addAll(
			final Collection<? extends MiningStack<NodeType, EdgeType>> arg0) {
		boolean ret = false;
		for (final MiningStack<NodeType, EdgeType> m : arg0) {
			ret |= add(m);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#clear()
	 */
	public synchronized void clear() {
		while (this.next != this) {
			_remove(this.next);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	public synchronized boolean contains(final Object o) {
		if (!(o instanceof RemoteStack)) {
			return false;
		}
		for (RemoteListItem<NodeType, EdgeType> ack = this.next; ack != this; ack = ack.next) {
			if (ack == o) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	public synchronized boolean containsAll(final Collection<?> arg0) {
		boolean ret = true;
		for (final Object m : arg0) {
			ret = ret & contains(m);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#isEmpty()
	 */
	public synchronized boolean isEmpty() {
		return size == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#iterator()
	 */
	public Iterator<MiningStack<NodeType, EdgeType>> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public synchronized boolean remove(final Object o) {
		if (!contains(o)) {
			return false;
		}
		final boolean ret = _remove((RemoteStack<NodeType, EdgeType>) o);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	public synchronized boolean removeAll(final Collection<?> arg0) {
		boolean ret = true;
		for (final Object m : arg0) {
			ret = ret & remove(m);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(final Collection<?> arg0) {
		throw new UnsupportedOperationException(
				"retainAll is not suported for RemoteStackList");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#size()
	 */
	public synchronized int size() {
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.strategy.StackList#split(de.parsemis.strategy.MiningStack)
	 */
	public synchronized boolean split(
			final MiningStack<NodeType, EdgeType> empty) {
		for (RemoteListItem<NodeType, EdgeType> ack = this.next; ack != this; ack = ack.next) {
			if (((RemoteStack<NodeType, EdgeType>) ack).split(empty)) {
				_remove(ack);
				_add(ack);
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray()
	 */
	public Object[] toArray() {
		return toArray(new RemoteStack[size]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray(T[])
	 */
	@SuppressWarnings("unchecked")
	public synchronized <T> T[] toArray(final T[] o) {
		int i = -1;
		for (RemoteListItem<NodeType, EdgeType> ack = this.next; ack != this; ack = ack.next) {
			o[++i] = (T) ack;
		}
		return o;
	}
}
