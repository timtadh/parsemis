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

/**
 * This class represents a remote version of the Collection interface.
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <Type>
 * 
 * @remote
 */
public abstract class RemoteCollection<Type> implements Collection<Type> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	public boolean addAll(final Collection<? extends Type> arg0) {
		boolean ret = true;
		for (final Type t : arg0) {
			ret = ret & add(t);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#clear()
	 */
	public void clear() {
		final Object[] vals = toArray();
		for (int i = vals.length - 1; i >= 0; --i) {
			remove(vals[i]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(final Collection<?> arg0) {
		boolean ret = true;
		for (final Object o : arg0) {
			ret = ret & contains(o);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#isEmpty()
	 */
	public boolean isEmpty() {
		return (size() == 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	public boolean removeAll(final Collection<?> arg0) {
		boolean ret = true;
		for (final Object o : arg0) {
			ret = ret & remove(o);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#size()
	 */
	public abstract int size();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray()
	 */
	public Object[] toArray() {
		return toArray(new Object[size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray(java.lang.Object[])
	 */
	public <T> T[] toArray(final T[] arg0) {
		int pos = 0;
		for (final T t : arg0) {
			arg0[pos++] = t;
		}
		return arg0;
	}

}
