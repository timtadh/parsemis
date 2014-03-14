/**
 * Created on 12.01.2008
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

import java.util.Collection;
import java.util.Iterator;

import de.parsemis.graph.HPGraph;
import de.parsemis.miner.environment.ThreadEnvironment;

/**
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
final public class FragmentWrapper<NodeType, EdgeType> implements
		HPFragment<NodeType, EdgeType> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	final Fragment<NodeType, EdgeType> fragment;

	private transient Collection<HPEmbedding<NodeType, EdgeType>> mc;

	/**
	 * @param fragment
	 */
	public FragmentWrapper(final Fragment<NodeType, EdgeType> fragment) {
		this.fragment = fragment;
	}

	public void add(final DataBaseGraph<NodeType, EdgeType> graph)
			throws UnsupportedOperationException {
		this.fragment.add(graph);
	}

	public boolean add(final HPEmbedding<NodeType, EdgeType> o) {
		return this.fragment.add(o.toEmbedding());
	}

	public boolean addAll(
			final Collection<? extends HPEmbedding<NodeType, EdgeType>> c) {
		boolean ret = false;
		for (final HPEmbedding<NodeType, EdgeType> g : c) {
			ret |= add(g);
		}
		return ret;
	}

	public void clear() {
		this.fragment.clear();
	}

	public boolean contains(final Object o) {
		if (o instanceof HPEmbedding) {
			@SuppressWarnings("unchecked")
			final HPEmbedding<NodeType, EdgeType> emb = (HPEmbedding<NodeType, EdgeType>) o;
			return this.fragment.contains(emb.toEmbedding());
		}
		return false;
	}

	public boolean containsAll(final Collection<?> c) {
		boolean ret = true;
		for (final Object g : c) {
			ret &= contains(g);
		}
		return ret;
	}

	public HPFragment<NodeType, EdgeType> copy() {
		return this.fragment.copy().toHPFragment();
	}

	public void finalizeIt() {
		// TODO Auto-generated method stub

	}

	public Frequency frequency() {
		return this.fragment.frequency();
	}

	public Collection<HPEmbedding<NodeType, EdgeType>> getMaximalNonOverlappingSubSet() {
		if (mc == null) {
			mc = new Collection<HPEmbedding<NodeType, EdgeType>>() {

				public boolean add(final HPEmbedding<NodeType, EdgeType> arg0) {
					throw new UnsupportedOperationException(
							"add not allowed for that Wrapper");
				}

				public boolean addAll(
						final Collection<? extends HPEmbedding<NodeType, EdgeType>> arg0) {
					throw new UnsupportedOperationException(
							"addAll not allowed for that Wrapper");
				}

				public void clear() {
					throw new UnsupportedOperationException(
							"clear not allowed for that Wrapper");
				}

				@SuppressWarnings("unchecked")
				public boolean contains(final Object arg0) {
					final HPEmbedding<NodeType, EdgeType> emb = (HPEmbedding<NodeType, EdgeType>) arg0;
					return fragment.getMaximalNonOverlappingSubSet().contains(
							emb.toEmbedding());
				}

				@SuppressWarnings("unchecked")
				public boolean containsAll(final Collection<?> arg0) {
					for (final HPEmbedding<NodeType, EdgeType> e : (Collection<HPEmbedding<NodeType, EdgeType>>) arg0) {
						if (!fragment.getMaximalNonOverlappingSubSet()
								.contains(e.toEmbedding())) {
							return false;
						}
					}
					return true;
				}

				public boolean isEmpty() {
					return fragment.getMaximalNonOverlappingSubSet().isEmpty();
				}

				public Iterator<HPEmbedding<NodeType, EdgeType>> iterator() {
					return new Iterator<HPEmbedding<NodeType, EdgeType>>() {
						Iterator<Embedding<NodeType, EdgeType>> it = fragment
								.getMaximalNonOverlappingSubSet().iterator();

						public boolean hasNext() {
							return it.hasNext();
						}

						public HPEmbedding<NodeType, EdgeType> next() {
							return it.next().toHPEmbedding();
						}

						public void remove() {
							throw new UnsupportedOperationException(
									"remove not allowed for that Wrapper");
						}
					};
				}

				public boolean remove(final Object arg0) {
					throw new UnsupportedOperationException(
							"remove not allowed for that Wrapper");
				}

				public boolean removeAll(final Collection<?> arg0) {
					throw new UnsupportedOperationException(
							"removeAll not allowed for that Wrapper");
				}

				public boolean retainAll(final Collection<?> arg0) {
					throw new UnsupportedOperationException(
							"retainAll not allowed for that Wrapper");
				}

				public int size() {
					return fragment.getMaximalNonOverlappingSubSet().size();
				}

				@SuppressWarnings("unchecked")
				public Object[] toArray() {
					return toArray((HPEmbedding<NodeType, EdgeType>[]) new HPEmbedding[size()]);
				}

				@SuppressWarnings("unchecked")
				public <T> T[] toArray(final T[] arg0) {
					final HPEmbedding<NodeType, EdgeType>[] ret = (HPEmbedding<NodeType, EdgeType>[]) arg0;
					int i = 0;
					for (final HPEmbedding<NodeType, EdgeType> e : this) {
						ret[i++] = e;
					}
					return arg0;
				}

			};
		}
		return mc;
	}

	public Iterator<DataBaseGraph<NodeType, EdgeType>> graphIterator() {
		return this.fragment.graphIterator();
	}

	public boolean isEmpty() {
		return this.fragment.isEmpty();
	}

	public Iterator<HPEmbedding<NodeType, EdgeType>> iterator() {
		return new Iterator<HPEmbedding<NodeType, EdgeType>>() {
			Iterator<Embedding<NodeType, EdgeType>> it = fragment.iterator();

			public boolean hasNext() {
				return it.hasNext();
			}

			public HPEmbedding<NodeType, EdgeType> next() {
				return it.next().toHPEmbedding();
			}

			public void remove() {
				it.remove();
			}
		};
	}

	public void release(final ThreadEnvironment<NodeType, EdgeType> tenv) {
		// do nothing
	}

	public boolean remove(final Object o) {
		if (o instanceof HPEmbedding) {
			@SuppressWarnings("unchecked")
			final HPEmbedding<NodeType, EdgeType> emb = (HPEmbedding<NodeType, EdgeType>) o;
			return this.fragment.remove(emb.toEmbedding());
		}
		return false;
	}

	public boolean removeAll(final Collection<?> c) {
		boolean ret = false;
		for (final Object g : c) {
			ret |= remove(g);
		}
		return ret;
	}

	public boolean retainAll(final Collection<?> c) {
		throw new UnsupportedOperationException("retainAll is not supported");
	}

	public int size() {
		return this.fragment.size();
	}

	public Object[] toArray() {
		return toArray(new HPEmbedding[size()]);
	}

	public <T> T[] toArray(final T[] a) {
		@SuppressWarnings("unchecked")
		final HPEmbedding<NodeType, EdgeType> arr[] = (HPEmbedding<NodeType, EdgeType>[]) a;
		int pos = 0;
		for (final HPEmbedding<NodeType, EdgeType> emb : this) {
			arr[pos++] = emb;
		}
		return a;
	}

	public Fragment<NodeType, EdgeType> toFragment() {
		return this.fragment;
	}

	public HPGraph<NodeType, EdgeType> toHPGraph() {
		return this.fragment.toGraph().toHPGraph();
	}
}