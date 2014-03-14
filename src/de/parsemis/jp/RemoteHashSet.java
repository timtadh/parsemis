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
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class wraps a local HashSet into the JavaParty environment.
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <Type>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * 
 * @resident
 */
public class RemoteHashSet<Type> extends RemoteCollection<Type> {

	final HashSet<Type> me = new HashSet<Type>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	public boolean add(final Type arg0) {
		return me.add(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	public boolean contains(final Object arg0) {
		return me.contains(arg0);
	}

	/**
	 * @return (a clone) of the local HashSet
	 */
	public HashSet<Type> getLocal() {
		return me;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@SuppressWarnings("unchecked")
	public Iterator<Type> iterator() {
		return new Iterator<Type>() {
			private final Type[] os = (Type[]) me.toArray();

			private int pos = 0;

			public boolean hasNext() {
				return pos < os.length;
			}

			public Type next() {
				if (hasNext()) {
					return os[pos++];
				}
				throw new NoSuchElementException();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	public boolean remove(final Object arg0) {
		return me.remove(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(final Collection<?> arg0) {
		return me.retainAll(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#size()
	 */
	@Override
	public int size() {
		return me.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.jp.RemoteCollection#toArray(T[])
	 */
	@Override
	public <T> T[] toArray(final T[] arg0) {
		return me.toArray(arg0);
	}
}
