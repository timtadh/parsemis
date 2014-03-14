/**
 * created Sep 15, 2006
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
package de.parsemis.utils;

import java.util.ArrayList;
import java.util.Collection;

import de.parsemis.miner.general.Frequency;

/**
 * This class is a straight forward implementation of a frequented ArrayList
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <Type>
 *            the type that should be stored in the Collection
 */
public class FrequentedArrayList<Type extends Frequented> extends
		ArrayList<Type> implements FrequentedCollection<Type> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2585089970082786239L;

	private final Frequency freq;

	/**
	 * creates a new empty arraylist with the given frequency
	 * 
	 * @param freq
	 */
	public FrequentedArrayList(final Frequency freq) {
		this.freq = freq.clone();
		this.freq.sub(freq);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#add(Type)
	 */
	@Override
	public boolean add(final Type arg0) {
		if (super.add(arg0)) {
			freq.add(arg0.frequency());
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(final Collection<? extends Type> arg0) {
		boolean ret = false;
		for (final Type e : arg0) {
			ret |= add(e);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#clear()
	 */
	@Override
	public void clear() {
		super.clear();
		freq.sub(freq);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.Frequented#frequency()
	 */
	public Frequency frequency() {
		return freq;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean remove(final Object arg0) {
		if (super.remove(arg0)) {
			freq.sub(((Type) arg0).frequency());
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(final Collection<?> arg0) {
		boolean ret = false;
		for (final Object e : arg0) {
			ret |= remove(e);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(final Collection<?> arg0) {
		throw new UnsupportedOperationException(
				"retainAll not yet supported for FrequentedArrayList");
	}
}
