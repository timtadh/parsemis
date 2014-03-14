/**
 * created Sep 30, 2006
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class is a collection that do not hold stored elements in memory, but
 * serialize it into a file
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <Type>
 *            that is stored in the Set
 */
public class FileSerializeCollection<Type extends Serializable> implements
		Collection<Type> {

	protected final File file;

	protected ObjectOutputStream oos;

	protected int size = 0;

	/**
	 * creates a new empty DumpSet
	 * 
	 * @param file
	 */
	public FileSerializeCollection(final File file) {
		assert (file != null) : "file == null";
		this.file = file;

		clear();
	}

	/**
	 * creates a new empty DumpSet
	 * 
	 * @param filename
	 */
	public FileSerializeCollection(final String filename) {
		this(new File(filename));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#add(E)
	 */
	public boolean add(final Type t) {
		try {
			oos.writeObject(t);
			size++;
			return true;
		} catch (final IOException io) {
			System.err.println(io + " " + t.toString());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	public boolean addAll(final Collection<? extends Type> c) {
		try {
			for (final Type t : c) {
				oos.writeObject(t);
			}
			size += c.size();
			return true;
		} catch (final IOException io) {
			System.err.println(io);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#clear()
	 */
	public void clear() {
		size = 0;
		try {
			if (oos != null) {
				oos.close();
			}
			oos = new ObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(file)));
		} catch (final IOException io) {
			System.err.println(io);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	public boolean contains(final Object o) {
		throw new UnsupportedOperationException(
				"contains not supported in DumpSet");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(final Collection<?> c) {
		throw new UnsupportedOperationException(
				"containsAll not supported in DumpSet");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#isEmpty()
	 */
	public boolean isEmpty() {
		return (size == 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#iterator()
	 */
	public Iterator<Type> iterator() {
		try {
			oos.flush();
			return new Iterator<Type>() {

				private Type next = null;

				private final ObjectInputStream ois = new ObjectInputStream(
						new BufferedInputStream(new FileInputStream(file)));

				@SuppressWarnings("unchecked")
				public boolean hasNext() {
					try {
						if (next == null) {
							next = (Type) ois.readObject();
						}
					} catch (final EOFException eof) {
						// do not report exception
					} catch (final Exception e) {
						System.err.println(e);
					}

					try {
						if (next == null) {
							ois.close();
						}
					} catch (final IOException io) {
						System.err.println(io);
					}

					return (next != null);
				}

				public Type next() {
					if (hasNext()) {
						final Type ret = next;
						next = null;
						return ret;
					}
					throw new NoSuchElementException("No more elements");
				}

				public void remove() {
					throw new UnsupportedOperationException(
							"remove not supported in DumpSet$iterator()");
				}
			};
		} catch (final IOException io) {
			System.err.println(io);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	public boolean remove(final Object o) {
		throw new UnsupportedOperationException(
				"remove not supported in DumpSet");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	public boolean removeAll(final Collection<?> c) {
		throw new UnsupportedOperationException(
				"removeAll not supported in DumpSet");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(final Collection<?> c) {
		throw new UnsupportedOperationException(
				"retainAll not supported in DumpSet");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#size()
	 */
	public int size() {
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray()
	 */
	public Object[] toArray() {
		return toArray(new Object[size]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray(T[])
	 */
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(final T[] arg0) {
		final Type[] arr = (Type[]) arg0;
		int i = -1;
		for (final Type v : this) {
			arr[++i] = v;
		}
		return arg0;
	}

}
