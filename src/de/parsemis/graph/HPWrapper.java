/**
 * created May 31, 2006
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
package de.parsemis.graph;

import java.util.Iterator;

import de.parsemis.utils.IntIterator;

/**
 * A wrapper to use HPGraphs like normal Graphs.
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
class HPWrapper<NodeType, EdgeType> implements Graph<NodeType, EdgeType> {

	/**
	 * 
	 * This class is the wrapper edge object
	 * 
	 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
	 * 
	 * @param <NodeType>
	 * @param <EdgeType>
	 */
	@SuppressWarnings("hiding")
	protected final static class MyEdge<NodeType, EdgeType> implements
			Edge<NodeType, EdgeType> {

		final HPWrapper<NodeType, EdgeType> wrap;

		final HPGraph<NodeType, EdgeType> me;

		int idx;

		protected MyEdge(final HPWrapper<NodeType, EdgeType> w, final int idx) {
			this.wrap = w;
			this.me = w.master;
			this.idx = idx;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object o) {
			try {
				@SuppressWarnings("unchecked")
				final MyEdge<NodeType, EdgeType> other = (MyEdge<NodeType, EdgeType>) o;
				if (other == null || other.me != me) {
					return false;
				}
				return other.idx == idx;
			} catch (final ClassCastException e) {
				return false;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Edge#getDirection()
		 */
		public int getDirection() {
			return me.getDirection(idx);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.parsemis.graph.Edge#getDirection(de.parsemis.graph.Node)
		 */
		public int getDirection(final Node<NodeType, EdgeType> node) {
			return me.getDirection(idx, node.getIndex());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Edge#getGraph()
		 */
		public Graph<NodeType, EdgeType> getGraph() {
			return wrap;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Edge#getIndex()
		 */
		public int getIndex() {
			return idx;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Edge#getLabel()
		 */
		public EdgeType getLabel() {
			return me.getEdgeLabel(idx);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Edge#getNodeA()
		 */
		public Node<NodeType, EdgeType> getNodeA() {
			return wrap._getNode(me.getNodeA(idx));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Edge#getNodeB()
		 */
		public Node<NodeType, EdgeType> getNodeB() {
			return wrap._getNode(me.getNodeB(idx));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.parsemis.graph.Edge#getOtherNode(de.parsemis.graph.Node)
		 */
		public Node<NodeType, EdgeType> getOtherNode(
				final Node<NodeType, EdgeType> node) {
			return wrap._getNode(me.getOtherNode(idx, node.getIndex()));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return idx ^ me.getID();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Edge#setLabel(EdgeType)
		 */
		public void setLabel(final EdgeType label) {
			me.setEdgeLabel(idx, label);
		}
	}

	/**
	 * 
	 * This class is the wrapper node object
	 * 
	 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
	 * 
	 * @param <NodeType>
	 * @param <EdgeType>
	 */
	@SuppressWarnings("hiding")
	protected final static class MyNode<NodeType, EdgeType> implements
			Node<NodeType, EdgeType> {

		final HPWrapper<NodeType, EdgeType> wrap;

		final HPGraph<NodeType, EdgeType> me;

		int idx;

		protected MyNode(final HPWrapper<NodeType, EdgeType> w, final int idx) {
			this.wrap = w;
			this.me = w.master;
			this.idx = idx;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Node#getEdges()
		 */
		public Iterator<Edge<NodeType, EdgeType>> edgeIterator() {
			return new Iterator<Edge<NodeType, EdgeType>>() {
				private final IntIterator it = me.getEdgeIndices(idx);

				public boolean hasNext() {
					return it.hasNext();
				}

				public Edge<NodeType, EdgeType> next() {
					return wrap._getEdge(it.next());
				}

				public void remove() {
					it.remove();
				};
			};
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object o) {
			try {
				@SuppressWarnings("unchecked")
				final MyNode<NodeType, EdgeType> other = (MyNode<NodeType, EdgeType>) o;
				if (other == null || other.me != me) {
					return false;
				}
				return other.idx == idx;
			} catch (final ClassCastException e) {
				return false;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Node#getDegree()
		 */
		public final int getDegree() {
			return me.getDegree(idx);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Node#getGraph()
		 */
		public Graph<NodeType, EdgeType> getGraph() {
			return wrap;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Node#getInDegree()
		 */
		public int getInDegree() {
			return me.getInDegree(idx);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Node#getIndex()
		 */
		public int getIndex() {
			return idx;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Node#getLabel()
		 */
		public NodeType getLabel() {
			return me.getNodeLabel(idx);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Node#getOutDegree()
		 */
		public int getOutDegree() {
			return me.getOutDegree(idx);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return idx ^ me.getID();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Node#getIncommingEdges()
		 */
		public Iterator<Edge<NodeType, EdgeType>> incommingEdgeIterator() {
			return new Iterator<Edge<NodeType, EdgeType>>() {
				private final IntIterator it = me.getInEdgeIndices(idx);

				public boolean hasNext() {
					return it.hasNext();
				}

				public Edge<NodeType, EdgeType> next() {
					return wrap._getEdge(it.next());
				}

				public void remove() {
					it.remove();
				};
			};
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Node#getOutgoingEdges()
		 */
		public Iterator<Edge<NodeType, EdgeType>> outgoingEdgeIterator() {
			return new Iterator<Edge<NodeType, EdgeType>>() {
				private final IntIterator it = me.getOutEdgeIndices(idx);

				public boolean hasNext() {
					return it.hasNext();
				}

				public Edge<NodeType, EdgeType> next() {
					return wrap._getEdge(it.next());
				}

				public void remove() {
					it.remove();
				};
			};
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Node#setLabel(NodeType)
		 */
		public void setLabel(final NodeType label) {
			me.setNodeLabel(idx, label);
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6454424345555729527L;

	protected final HPGraph<NodeType, EdgeType> master;

	private transient Node<NodeType, EdgeType>[] nodes;

	private transient Edge<NodeType, EdgeType>[] edges;

	/**
	 * creates a wrapper for the given master graph
	 * 
	 * @param master
	 */
	HPWrapper(final HPGraph<NodeType, EdgeType> master) {
		this.master = master;
	}

	@SuppressWarnings("unchecked")
	final Edge<NodeType, EdgeType> _getEdge(final int idx) {
		if (edges == null || edges.length <= idx) {
			edges = new Edge[idx + 1];
		}
		if (edges[idx] == null) {
			edges[idx] = new MyEdge<NodeType, EdgeType>(this, idx);
		}
		return edges[idx];
	}

	@SuppressWarnings("unchecked")
	final Node<NodeType, EdgeType> _getNode(final int idx) {
		if (nodes == null || nodes.length <= idx) {
			nodes = new Node[idx + 1];
		}
		if (nodes[idx] == null) {
			nodes[idx] = new MyNode<NodeType, EdgeType>(this, idx);
		}
		return nodes[idx];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see src.de.pargra.utils.Cloneable#clone()
	 */
	@Override
	public Graph<NodeType, EdgeType> clone() {
		return new HPWrapper<NodeType, EdgeType>(master.clone());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Graph#edgeIterator()
	 */
	public Iterator<Edge<NodeType, EdgeType>> edgeIterator() {
		return new Iterator<Edge<NodeType, EdgeType>>() {
			private final IntIterator it = master.edgeIndexIterator();

			public boolean hasNext() {
				return it.hasNext();
			}

			@SuppressWarnings("synthetic-access")
			public Edge<NodeType, EdgeType> next() {
				return _getEdge(it.next());
			}

			public void remove() {
				it.remove();
			};
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Graph#getEdge(int)
	 */
	public Edge<NodeType, EdgeType> getEdge(final int idx) {
		return _getEdge(idx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Graph#getEdge(de.parsemis.graph.Node,
	 *      de.parsemis.graph.Node)
	 */
	public Edge<NodeType, EdgeType> getEdge(
			final Node<NodeType, EdgeType> nodeA,
			final Node<NodeType, EdgeType> nodeB) {
		final int edge = master.getEdge(nodeA.getIndex(), nodeB.getIndex());
		return (edge == -1 ? null : _getEdge(edge));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Graph#getEdgeCount()
	 */
	public int getEdgeCount() {
		return master.getEdgeCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Graph#getID()
	 */
	public int getID() {
		return master.getID();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Graph#getMaxEdgeIndex()
	 */
	public int getMaxEdgeIndex() {
		return master.getMaxEdgeIndex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Graph#getMaxNodeIndex()
	 */
	public int getMaxNodeIndex() {
		return master.getMaxNodeIndex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Graph#getName()
	 */
	public String getName() {
		return master.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Graph#getNode(int)
	 */
	public Node<NodeType, EdgeType> getNode(final int idx) {
		return _getNode(idx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Graph#getNodeA(de.parsemis.graph.Edge)
	 */
	public Node<NodeType, EdgeType> getNodeA(final Edge<NodeType, EdgeType> edge) {
		return _getNode(master.getNodeA(edge.getIndex()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Graph#getNodeB(de.parsemis.graph.Edge)
	 */
	public Node<NodeType, EdgeType> getNodeB(final Edge<NodeType, EdgeType> edge) {
		return _getNode(master.getNodeB(edge.getIndex()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Graph#getNodeCount()
	 */
	public int getNodeCount() {
		return master.getNodeCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Graph#nodeIterator()
	 */
	public Iterator<Node<NodeType, EdgeType>> nodeIterator() {
		return new Iterator<Node<NodeType, EdgeType>>() {
			private final IntIterator it = master.nodeIndexIterator();

			public boolean hasNext() {
				return it.hasNext();
			}

			public Node<NodeType, EdgeType> next() {
				return _getNode(it.next());
			}

			public void remove() {
				it.remove();
			};
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Graph#toHPGraph()
	 */
	public HPGraph<NodeType, EdgeType> toHPGraph() {
		return master;
	}
}
