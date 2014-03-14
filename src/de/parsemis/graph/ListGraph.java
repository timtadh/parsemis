/**
 * created May 3, 2006
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.parsemis.parsers.LabelParser;

/**
 * List-based implementation of the MutableGraph interface.
 * <p>
 * It stores the required node and edge informations in different lists (ArrayList and arrays).
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
public class ListGraph<NodeType, EdgeType> implements
		MutableGraph<NodeType, EdgeType> {

	/**
	 * 
	 * This class is a factory to create new graphs of this type.
	 * 
	 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
	 * 
	 * @param <NodeType>
	 * @param <EdgeType>
	 */
	@SuppressWarnings("hiding")
	public final static class Factory<NodeType, EdgeType> implements
			GraphFactory<NodeType, EdgeType> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private final LabelParser<NodeType> nodeLabelParser;

		private final LabelParser<EdgeType> edgeLabelParser;

		public Factory(final LabelParser<NodeType> nodeLabelParser,
				final LabelParser<EdgeType> edgeLabelParser) {
			this.nodeLabelParser = nodeLabelParser;
			this.edgeLabelParser = edgeLabelParser;
		}

		public LabelParser<EdgeType> getEdgeLabelParser() {
			return edgeLabelParser;
		}

		public LabelParser<NodeType> getNodeLabelParser() {
			return nodeLabelParser;
		}

		public MutableGraph<NodeType, EdgeType> newGraph() {
			return new ListGraph<NodeType, EdgeType>();
		}

		public MutableGraph<NodeType, EdgeType> newGraph(final String name) {
			return new ListGraph<NodeType, EdgeType>(name);
		}
	}

	/**
	 * This class represents the edges objects of the ListGraph
	 * 
	 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
	 */
	private class MyEdge implements Edge<NodeType, EdgeType> {

		final ListGraph<NodeType, EdgeType> me;

		int idx;

		final int nodeA;

		final int nodeB;

		final int direction;

		EdgeType label;

		MyEdge(final ListGraph<NodeType, EdgeType> me, final int idx,
				final int nodeA, final int nodeB, final int direction,
				final EdgeType label) {
			this.me = me;
			this.idx = idx;
			this.nodeA = nodeA;
			this.nodeB = nodeB;
			this.direction = direction;
			this.label = label;
		}

		boolean consistent() {
			return idx < 0
					|| (nodeA >= 0 && nodeB >= 0
							&& nodes.get(nodeA).containsEdge(idx) && nodes.get(
							nodeB).containsEdge(idx));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object o) {
			MyEdge other;
			try {
				other = (MyEdge) o;
			} catch (final ClassCastException e) {
				return false;
			}
			if (other == null || other.me != me) {
				return false;
			}
			return other.idx == idx;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Edge#getDirection()
		 */
		public int getDirection() {
			return direction;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.parsemis.graph.Edge#getDirection(de.parsemis.graph.Node)
		 */
		public int getDirection(final Node<NodeType, EdgeType> node) {
			return (node.getIndex() == nodeA ? direction : -direction);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Edge#getGraph()
		 */
		public Graph<NodeType, EdgeType> getGraph() {
			return me;
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
			return label;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Edge#getNodeA()
		 */
		public Node<NodeType, EdgeType> getNodeA() {
			return nodes.get(nodeA);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Edge#getNodeB()
		 */
		public Node<NodeType, EdgeType> getNodeB() {
			return nodes.get(nodeB);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.parsemis.graph.Edge#getOtherNode(de.parsemis.graph.Node)
		 */
		public Node<NodeType, EdgeType> getOtherNode(
				final Node<NodeType, EdgeType> node) {
			if (node.getIndex() == nodeA) {
				return nodes.get(nodeB);
			}
			if (node.getIndex() == nodeB) {
				return nodes.get(nodeA);
			}
			return null;
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
			this.label = label;
		}
	}

	/**
	 * This class represents the nodes objects of the ListGraph
	 * 
	 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
	 */
	class MyNode implements Node<NodeType, EdgeType> {

		int idx;

		NodeType label;

		final int[] degrees;

		int[] myedges;

		MyNode(final ListGraph<NodeType, EdgeType> me, final int idx,
				final NodeType label) {
			this.idx = idx;
			this.label = label;
			degrees = new int[3];
			myedges = new int[DEFAULTSIZE];
		}

		final void addEdge(final int idx, final int direction) {
			assert idx >= 0;
			if (degrees[DEGREE] == myedges.length) {
				final int tmp[] = new int[(int) (myedges.length * 1.5)];
				System.arraycopy(myedges, 0, tmp, 0, myedges.length);
				myedges = tmp;
			}
			myedges[degrees[DEGREE]] = idx;
			degrees[DEGREE]++;
			if (direction == Edge.INCOMING) {
				degrees[INDEGREE]++;
			}
			if (direction == Edge.OUTGOING) {
				degrees[OUTDEGREE]++;
			}
		}

		private boolean checkEdges() {
			int in = 0;
			int out = 0;
			for (int i = 0; i < degrees[DEGREE]; i++) {
				final MyEdge e = edges.get(myedges[i]);
				assert e.idx >= 0 && (e.nodeA == idx || e.nodeB == idx);
				if (e.nodeA == idx) {
					assert nodes.get(e.nodeB).idx >= 0;
					if (e.direction == Edge.INCOMING) {
						in++;
					}
					if (e.direction == Edge.OUTGOING) {
						out++;
					}
				}
				if (e.nodeB == idx) {
					assert nodes.get(e.nodeA).idx >= 0;
					if (e.direction == Edge.INCOMING) {
						out++;
					}
					if (e.direction == Edge.OUTGOING) {
						in++;
					}
				}
			}
			assert degrees[INDEGREE] == in;
			assert degrees[OUTDEGREE] == out;
			return true;
		}

		boolean consistent() {
			return idx < 0 || (nodes.get(idx) == this && checkEdges());
		}

		boolean containsEdge(final int eidx) {
			for (int i = 0; i < degrees[DEGREE]; i++) {
				if (myedges[i] == eidx) {
					return true;
				}
			}
			return false;
		}

		final boolean delEdge(final int idx, final int direction) {
			assert idx >= 0;
			for (int i = degrees[DEGREE] - 1; i >= 0; --i) {
				if (myedges[i] == idx) {
					int j = i;
					--degrees[DEGREE];
					while (j < degrees[DEGREE]) {
						j++;
						myedges[j - 1] = myedges[j];
					}
					myedges[j] = 0;
					if (direction == Edge.INCOMING) {
						degrees[INDEGREE]--;
					}
					if (direction == Edge.OUTGOING) {
						degrees[OUTDEGREE]--;
					}
					return true;
				}
			}
			return false;
		}

		/*
		 * private final void deleteEdgePos(int pos, int direction){
		 * degrees[DEGREE]--; myedges[pos]=myedges[degrees[DEGREE]];
		 * myedges[degrees[DEGREE]]=0; if (direction==Edge.INCOMING)
		 * degrees[INDEGREE]--; if (direction==Edge.OUTGOING)
		 * degrees[OUTDEGREE]--; }
		 * 
		 * private final void deleteEdge(int idx, int direction){ int i=0; while
		 * (i<degrees[DEGREE]){ if (myedges[i]==idx) {
		 * deleteEdgePos(i,direction); return; } i++; } }
		 */

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Node#getEdges()
		 */
		public Iterator<Edge<NodeType, EdgeType>> edgeIterator() {
			return new Iterator<Edge<NodeType, EdgeType>>() {
				int pos = 0;

				public boolean hasNext() {
					return pos < degrees[DEGREE];
				}

				public Edge<NodeType, EdgeType> next() {
					if (!hasNext()) {
						throw new NoSuchElementException("No more elements");
					}
					return edges.get(myedges[pos++]);
				}

				public void remove() {
					/* me.deleteEdge(myedges[pos-1]); */
					throw new UnsupportedOperationException();
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
			MyNode other;
			try {
				other = (MyNode) o;
			} catch (final ClassCastException e) {
				return false;
			}
			if (other == null || other.getGraph() != ListGraph.this) {
				return false;
			}
			return other.idx == idx;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Node#getDegree()
		 */
		public int getDegree() {
			return degrees[DEGREE];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Node#getGraph()
		 */
		public Graph<NodeType, EdgeType> getGraph() {
			return ListGraph.this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Node#getInDegree()
		 */
		public int getInDegree() {
			return degrees[INDEGREE];
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
			return label;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Node#getOutDegree()
		 */
		public int getOutDegree() {
			return degrees[OUTDEGREE];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return idx ^ ListGraph.this.getID();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Node#getIncommingEdges()
		 */
		public Iterator<Edge<NodeType, EdgeType>> incommingEdgeIterator() {
			return new Iterator<Edge<NodeType, EdgeType>>() {
				int pos = 0;

				Edge<NodeType, EdgeType> next = null;

				public boolean hasNext() {
					while (next == null) {
						if (pos == degrees[DEGREE]) {
							return false;
						}
						next = edges.get(myedges[pos++]);
						if ((next.getNodeA().getIndex() == idx && next
								.getDirection() != Edge.INCOMING)
								|| (next.getNodeB().getIndex() == idx && next
										.getDirection() != Edge.OUTGOING)) {
							next = null;
						}
					}
					return true;
				}

				public Edge<NodeType, EdgeType> next() {
					if (!hasNext()) {
						throw new NoSuchElementException("No more elements");
					}
					final Edge<NodeType, EdgeType> ret = next;
					next = null;
					return ret;
				}

				public void remove() {
					throw new UnsupportedOperationException();
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
				int pos = 0;

				Edge<NodeType, EdgeType> next = null;

				public boolean hasNext() {
					while (next == null) {
						if (pos == degrees[DEGREE]) {
							return false;
						}
						next = edges.get(myedges[pos++]);
						if ((next.getNodeA().getIndex() == idx && next
								.getDirection() != Edge.OUTGOING)
								|| (next.getNodeB().getIndex() == idx && next
										.getDirection() != Edge.INCOMING)) {
							next = null;
						}
					}
					return true;
				}

				public Edge<NodeType, EdgeType> next() {
					if (!hasNext()) {
						throw new NoSuchElementException("No more elements");
					}
					final Edge<NodeType, EdgeType> ret = next;
					next = null;
					return ret;
				}

				public void remove() {
					throw new UnsupportedOperationException();
				};
			};
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see src.de.pargra.graph.Node#setLabel(NodeType)
		 */
		public void setLabel(final NodeType label) {
			this.label = label;
		}

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 9067733869699576796L;

	/** counter for the unique Graph ID */
	private static int ID = 0;

	private final static int DEGREE = 0, INDEGREE = 1, OUTDEGREE = 2,
			DEFAULTSIZE = 4;;

	private final int id;

	private final String name;

	private transient MutableGraphWrapper<NodeType, EdgeType> wrap = null;

	int nodeCount = 0;

	int emptyNode = -1;

	int edgeCount = 0;

	int emptyEdge = -1;

	ArrayList<MyNode> nodes;

	ArrayList<MyEdge> edges;

	/**
	 * creates an empty graph
	 */
	public ListGraph() {
		this("" + ID, ID++);
	}

//	/**
//	 * creates an empty graph with the given ID
//	 * 
//	 * @param id
//	 * @Deprecated
//	 */
//	@Deprecated
//	public ListGraph(final int id) {
//		this("" + ID, ID++);
//	}

	/**
	 * creates an empty graph with the given name
	 * 
	 * @param name
	 */
	public ListGraph(final String name) {
		this(name, ID++);
	}

	/**
	 * creates an empty graph with the given ID and the given name
	 * 
	 * @param name
	 * @param id
	 */
	private ListGraph(final String name, final int id) {
		this.name = name;
		this.id = id;
		nodes = new ArrayList<MyNode>();
		edges = new ArrayList<MyEdge>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see src.de.pargra.graph.MutableGraph#addEdge(src.de.pargra.graph.Node,
	 *      src.de.pargra.graph.Node, EdgeType, int)
	 */
	public Edge<NodeType, EdgeType> addEdge(
			final Node<NodeType, EdgeType> nodeA,
			final Node<NodeType, EdgeType> nodeB, final EdgeType label,
			int direction) {
		int idx;
		if (nodeA == nodeB) {
			assert false : "self edge";
			System.err.println("selfedge ignored!");
			return null;
		}
		final Edge<NodeType, EdgeType> e = getEdge(
				direction == Edge.INCOMING ? nodeB : nodeA,
				direction == Edge.INCOMING ? nodeA : nodeB);
		if (e != null) {
			assert false : "duplicated edge";
			System.err.println("duplicated edge ignored!");
			return e;
		}
		if (emptyEdge == -1) {
			idx = edges.size();
			final MyEdge ret = new MyEdge(this, idx, nodeA.getIndex(), nodeB
					.getIndex(), direction, label);
			((MyNode) nodeA).addEdge(idx, direction);
			((MyNode) nodeB).addEdge(idx, -direction);
			edges.add(idx, ret);
			edgeCount++;
			assert consistent();
			return ret;
		} else {
			idx = -emptyEdge - 2;
			emptyEdge = edges.get(idx).idx;
			final MyEdge ret = new MyEdge(this, idx, nodeA.getIndex(), nodeB
					.getIndex(), direction, label);
			((MyNode) nodeA).addEdge(idx, direction);
			((MyNode) nodeB).addEdge(idx, -direction);
			edges.set(idx, ret);
			edgeCount++;
			assert consistent();
			return ret;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see src.de.pargra.graph.MutableGraph#addNode(NodeType)
	 */
	public Node<NodeType, EdgeType> addNode(final NodeType label) {
		int idx;
		if (emptyNode == -1) {
			idx = nodes.size();
			final MyNode ret = new MyNode(this, idx, label);
			nodes.add(idx, ret);
			nodeCount++;
			assert consistent();
			return ret;
		} else {
			idx = -emptyNode - 2;
			emptyNode = nodes.get(idx).idx;
			final MyNode ret = new MyNode(this, idx, label);
			nodes.set(idx, ret);
			nodeCount++;
			assert consistent();
			return ret;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see src.de.pargra.graph.MutableGraph#addNodeAndEdge(src.de.pargra.graph.Node,
	 *      NodeType, EdgeType, int)
	 */
	public Node<NodeType, EdgeType> addNodeAndEdge(
			final Node<NodeType, EdgeType> nodeA, final NodeType nodeBLabel,
			final EdgeType edgeLabel, final int direction) {
		final Node<NodeType, EdgeType> nodeB = addNode(nodeBLabel);
		addEdge(nodeA, nodeB, edgeLabel, direction);
		return nodeB;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see src.de.pargra.utils.Cloneable#clone()
	 */
	@Override
	public Graph<NodeType, EdgeType> clone() {
		final ListGraph<NodeType, EdgeType> ret = new ListGraph<NodeType, EdgeType>();

		// TODO: ordentiches Clonen!!!
		for (final MyNode node : nodes) {
			ret.addNode(node.getLabel());
		}
		for (final MyEdge edge : edges) {
			ret.addEdge(ret.getNode(edge.nodeA), ret.getNode(edge.nodeB), edge
					.getLabel(), edge.getDirection());
		}

		return ret;
	}

	private boolean consistent() {
		int nc = 0;
		for (final MyNode n : nodes) {
			assert n.consistent();
			if (n.idx >= 0) {
				nc++;
			}
		}
		assert nc == nodeCount;
		for (int s = emptyNode; s < -1; s = nodes.get(-s - 2).idx) {
			nc++;
		}
		assert nc == nodes.size();

		int ec = 0;
		for (final MyEdge e : edges) {
			assert e.consistent();
			if (e.idx >= 0) {
				ec++;
			}
		}
		assert ec == edgeCount;
		for (int s = emptyEdge; s < -1; s = edges.get(-s - 2).idx) {
			ec++;
		}
		assert ec == edges.size();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see src.de.pargra.graph.Graph#edgeIterator()
	 */
	public Iterator<Edge<NodeType, EdgeType>> edgeIterator() {
		return new Iterator<Edge<NodeType, EdgeType>>() {
			int pos = 0;
			{
				while (pos < edges.size() && edges.get(pos).idx < 0) {
					pos++;
				}
			}

			public boolean hasNext() {
				return pos < edges.size();
			}

			public Edge<NodeType, EdgeType> next() {
				if (pos < edges.size()) {
					final int alt = pos++;
					while (pos < edges.size() && edges.get(pos).idx < 0) {
						pos++;
					}
					return edges.get(alt);
				} else {
					throw new NoSuchElementException();
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see src.de.pargra.graph.Graph#getEdge(int)
	 */
	public Edge<NodeType, EdgeType> getEdge(final int idx) {
		assert edges.get(idx).idx >= 0 : "Kante geloescht";
		return edges.get(idx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see src.de.pargra.graph.Graph#getEdge(src.de.pargra.graph.Node,
	 *      src.de.pargra.graph.Node)
	 */
	public Edge<NodeType, EdgeType> getEdge(
			final Node<NodeType, EdgeType> nodeA,
			final Node<NodeType, EdgeType> nodeB) {
		for (final Iterator<Edge<NodeType, EdgeType>> it = nodeA.edgeIterator(); it
				.hasNext();) {
			final Edge<NodeType, EdgeType> tmp = it.next();
			final Node<NodeType, EdgeType> tb = tmp.getNodeB();
			final Node<NodeType, EdgeType> ta = tmp.getNodeA();
			final int td = tmp.getDirection();

			final boolean b1 = (tb == nodeB && ta == nodeA && td != Edge.INCOMING);
			final boolean b2 = (tb == nodeA);
			final boolean b3 = (ta == nodeB);
			final boolean b4 = (td != Edge.OUTGOING);

			if (b1 || (b2 && b3 && b4)) {
				return tmp;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see src.de.pargra.graph.Graph#getEdgeCount()
	 */
	public int getEdgeCount() {
		return edgeCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see src.de.pargra.graph.Graph#getID()
	 */
	public int getID() {
		return id;
	}

	public int getMaxEdgeIndex() {
		return edges.size();
	}

	public int getMaxNodeIndex() {
		return nodes.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see src.de.pargra.graph.Graph#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see src.de.pargra.graph.Graph#getNode(int)
	 */
	public Node<NodeType, EdgeType> getNode(final int idx) {
		assert nodes.get(idx).idx >= 0 : "Knoten geloescht";
		return nodes.get(idx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see src.de.pargra.graph.Graph#getNodeA(src.de.pargra.graph.Edge)
	 */
	public Node<NodeType, EdgeType> getNodeA(final Edge<NodeType, EdgeType> edge) {
		return edge.getNodeA();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see src.de.pargra.graph.Graph#getNodeB(src.de.pargra.graph.Edge)
	 */
	public Node<NodeType, EdgeType> getNodeB(final Edge<NodeType, EdgeType> edge) {
		return edge.getNodeB();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see src.de.pargra.graph.Graph#getNodeCount()
	 */
	public int getNodeCount() {
		return nodeCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see src.de.pargra.graph.Graph#nodeIterator()
	 */
	public Iterator<Node<NodeType, EdgeType>> nodeIterator() {
		return new Iterator<Node<NodeType, EdgeType>>() {
			int pos = 0;
			{
				while (pos < nodes.size() && nodes.get(pos).idx < 0) {
					pos++;
				}
			}

			public boolean hasNext() {
				return pos < nodes.size();
			}

			public Node<NodeType, EdgeType> next() {
				if (pos < nodes.size()) {
					final int alt = pos++;
					while (pos < nodes.size() && nodes.get(pos).idx < 0) {
						pos++;
					}
					return nodes.get(alt);
				} else {
					throw new NoSuchElementException();
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.MutableGraph#removeEdge(de.parsemis.graph.Edge)
	 */
	public boolean removeEdge(final Edge<NodeType, EdgeType> edge) {
		final MyEdge me = (MyEdge) edge;
		final int tmp = me.idx;
		if (tmp < 0) {
			return false;
		}
		((MyNode) me.getNodeA()).delEdge(tmp, me.direction);
		((MyNode) me.getNodeB()).delEdge(tmp, -me.direction);
		me.idx = emptyEdge;
		emptyEdge = -tmp - 2;
		edgeCount--;
		assert consistent();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.MutableGraph#removeNode(de.parsemis.graph.Node)
	 */
	public boolean removeNode(final Node<NodeType, EdgeType> node) {
		final MyNode mn = (MyNode) node;
		final int tmp = mn.idx;
		if (tmp < 0) {
			return false;
		}
		for (int i = mn.degrees[DEGREE] - 1; i >= 0; --i) {
			if (!removeEdge(edges.get(mn.myedges[i]))) {
				assert consistent();
				return false;
			}
		}
		assert mn.degrees[DEGREE] == 0;
		mn.idx = emptyNode;
		emptyNode = -tmp - 2;
		nodeCount--;
		assert consistent();
		return true;
	}

	public HPGraph<NodeType, EdgeType> toHPGraph() {
		if (wrap == null) {
			wrap = new MutableGraphWrapper<NodeType, EdgeType>(this);
		}
		return wrap;
	}

}
