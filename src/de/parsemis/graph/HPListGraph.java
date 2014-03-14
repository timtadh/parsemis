/**
 * created May 30, 2006
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

import java.util.BitSet;
import java.util.NoSuchElementException;

import de.parsemis.miner.environment.Debug;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.environment.Relabler;
import de.parsemis.parsers.LabelParser;
import de.parsemis.utils.GraphUtils;
import de.parsemis.utils.IntIterator;

/**
 * List-based implementation of the HPMutableGraph interface.
 * <p>
 * It stores the required node and edge informations in different int-arrays.
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
public class HPListGraph<NodeType, EdgeType> implements
		HPMutableGraph<NodeType, EdgeType> {

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
			return (MutableGraph<NodeType, EdgeType>) new HPListGraph<NodeType, EdgeType>()
					.toGraph();
		}

		public MutableGraph<NodeType, EdgeType> newGraph(final String name) {
			return (MutableGraph<NodeType, EdgeType>) new HPListGraph<NodeType, EdgeType>(
					name).toGraph();
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3527286763882922740L;

	private static int ID = 0;

	/** to mark a node or edge as deleted */
	static final Object DELETED = new Object();

	private static final int NODEA = 0, NODEB = 1, DIRECTION = 2,
			NEXTEMPTY = 0, EMPTYNODE = 0, EMPTYEDGE = 1, NODECOUNT = 2,
			EDGECOUNT = 3, MAXNODE = 4, MAXEDGE = 5, DEGREE = 0, INDEGREE = 1,
			OUTDEGREE = 2, FIRSTEDGE = 3, DEFAULTSIZE = 4, DEFAULTNODESIZE = 6;

	private static final double RESIZE_SCALE = 1.5;

	/** stores the name of the graph */
	private final String name;

	/** stores the ID of the graph */
	private final int id;

	/**
	 * stores the first deleted Edge/Node ([0/1]), the node/edge count([2/3]),
	 * and the max-node/edge index([4/5])
	 */
	final int[] status;

	/**
	 * stores for each node in the array[nodeIndex] the degree ([x][0]), the
	 * in/outdegree ([x][1/2]) and the connected edge [x][3..].
	 * 
	 * It also contains the single linked deleted list at [x][NEXTEMPTY] in each
	 * deleted node. -1 marks the end of this list.
	 */
	int[][] node_edges;

	/**
	 * stores the first node of each edge at [edgeIdx*3+NODEA], the second node
	 * at [edgeIdx*3+NODEB] and the direction in respect to nodeA at
	 * [edgeIdx*3+DIRECTION].
	 * 
	 * It also contains the single linked deleted list at [edgeIdx*3+NEXTEMPTY]
	 * in each deleted edge. -1 marks the end of the list.
	 */
	private int[] edge_nodes_and_direction;

	NodeType[] node_labels;

	EdgeType[] edge_labels;

	private transient Relabler<NodeType, EdgeType> lastNodeRelabler = null;

	private transient Relabler<NodeType, EdgeType> lastEdgeRelabler = null;

	private transient int[] node_lidx;

	private transient int[] edge_lidx;

	private transient BitSet edges;

	private transient BitSet nodes;

	private transient int[] partitions = null;

	private transient HPMutableWrapper<NodeType, EdgeType> wrap = null;

	/**
	 * creates an empty graph
	 */
	public HPListGraph() {
		this("" + ID, ID++);
	}

//	/**
//	 * creates an empty graph with the given ID
//	 * 
//	 * @param id
//	 * @Deprecated
//	 */
//	@Deprecated
//	public HPListGraph(final int id) {
//		this("" + id, id);
//	}

	/**
	 * creates an empty graph with the given name
	 * 
	 * @param name
	 */
	public HPListGraph(final String name) {
		this(name, ID++);
	}

	/**
	 * creates an empty graph with the given ID and the given name
	 * 
	 * @param name
	 * @param id
	 */
	private HPListGraph(final String name, final int id) {
		this(name, id, DEFAULTSIZE, DEFAULTSIZE);
		for (int i = 0; i < DEFAULTSIZE; ++i) {
			node_edges[i] = new int[DEFAULTNODESIZE];
		}
		status[EMPTYNODE] = -1;
		status[EMPTYEDGE] = -1;
	}

	@SuppressWarnings("unchecked")
	private HPListGraph(final String name, final int id, final int nodeSize,
			final int edgeSize) {
		this.name = name;
		this.id = id;
		node_edges = new int[nodeSize][];
		edge_nodes_and_direction = new int[edgeSize * 3];
		node_labels = (NodeType[]) new Object[nodeSize];
		edge_labels = (EdgeType[]) new Object[edgeSize];
		status = new int[MAXEDGE + 1];
	}

	@SuppressWarnings("unchecked")
	private final int _addEdge(final int na, final int nb,
			final EdgeType label, int direction) {
		int idx = status[EMPTYEDGE];
		if (idx < 0) {// no empty edge available
			idx = status[MAXEDGE]++;
			final int length = edge_labels.length;
			if (idx >= length) {// resize nodes
				final int newlength = (int) (RESIZE_SCALE * idx);
				System.arraycopy(edge_nodes_and_direction, 0,
						edge_nodes_and_direction = new int[3 * newlength], 0,
						3 * length);
				System.arraycopy(edge_labels, 0,
						edge_labels = ((EdgeType[]) new Object[newlength]), 0,
						length);
			}
		} else {
			status[EMPTYEDGE] = edge_nodes_and_direction[idx * 3 + NODEA];
		}
		edge_nodes_and_direction[idx * 3 + NODEA] = na;
		edge_nodes_and_direction[idx * 3 + NODEB] = nb;
		edge_nodes_and_direction[idx * 3 + DIRECTION] = direction;
		_addEdgeToNode(na, idx, direction);
		_addEdgeToNode(nb, idx, -direction);
		++status[EDGECOUNT];
		edge_labels[idx] = label;
		edgesChanged();
		return idx;
	}

	private final void _addEdgeToNode(final int ni, final int ei,
			final int direction) {
		int[] node = node_edges[ni];
		final int pos = ++node[DEGREE] + FIRSTEDGE - 1;
		final int length = node.length;
		if (pos >= length) { // resize array
			node_edges[ni] = new int[(int) (pos * RESIZE_SCALE)];
			System.arraycopy(node, 0, node = node_edges[ni], 0, length);
		}
		node[pos] = ei;
		if (direction == Edge.INCOMING) {
			++node[INDEGREE];
		}
		if (direction == Edge.OUTGOING) {
			++node[OUTDEGREE];
		}
	}

	@SuppressWarnings("unchecked")
	private final int _addNode(final NodeType label) {
		int idx = status[EMPTYNODE];
		if (idx < 0) {// no empty nodes available
			idx = status[MAXNODE];
			++status[MAXNODE];
			final int length = node_labels.length;
			if (idx >= length) {// resize nodes
				final int newlength = (int) (RESIZE_SCALE * idx);
				System.arraycopy(node_edges, 0,
						node_edges = new int[newlength][], 0, length);
				System.arraycopy(node_labels, 0,
						node_labels = ((NodeType[]) new Object[newlength]), 0,
						length);
				// initialize empty node_edges
				for (int i = length; i < newlength; ++i) {
					node_edges[i] = new int[DEFAULTNODESIZE];
				}
			}
		} else {
			status[EMPTYNODE] = node_edges[idx][NEXTEMPTY];
			node_edges[idx][DEGREE] = node_edges[idx][INDEGREE] = node_edges[idx][OUTDEGREE] = 0;
		}
		++status[NODECOUNT];
		node_labels[idx] = label;
		nodesChanged();
		assert node_edges[0] != null : dumpGraph("_addNode(" + label.toString()
				+ ") " + idx);
		return idx;
	}

	@SuppressWarnings("unchecked")
	final void _deleteEdge(final int ei) {
		_deleteEdgeFromNode(ei, edge_nodes_and_direction[ei * 3 + NODEA],
				edge_nodes_and_direction[ei * 3 + DIRECTION]);
		_deleteEdgeFromNode(ei, edge_nodes_and_direction[ei * 3 + NODEB],
				-edge_nodes_and_direction[ei * 3 + DIRECTION]);
		edge_nodes_and_direction[ei * 3 + NEXTEMPTY] = status[EMPTYEDGE];
		status[EMPTYEDGE] = ei;
		--status[EDGECOUNT];
		edge_labels[ei] = (EdgeType) DELETED;
		edgesChanged();
	}

	private final void _deleteEdgeFromNode(final int ei, final int ni,
			final int direction) {
		final int[] node = node_edges[ni];
		final int lastPos = --node[DEGREE] + FIRSTEDGE;
		if (direction == Edge.INCOMING) {
			--node[INDEGREE];
		}
		if (direction == Edge.OUTGOING) {
			--node[OUTDEGREE];
		}
		int i = lastPos;
		while (node[i] != ei) {
			--i;
		}
		node[i] = node[lastPos];
	}

	@SuppressWarnings("unchecked")
	final void _deleteNode(final int ni) {
		final int[] node = node_edges[ni];
		for (int eii = node[DEGREE] + FIRSTEDGE - 1; eii >= FIRSTEDGE; --eii) {
			_deleteEdge(node[eii]);
		}
		node[NEXTEMPTY] = status[EMPTYNODE];
		status[EMPTYNODE] = ni;
		node_labels[ni] = (NodeType) DELETED;
		nodesChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPMutableGraph#addEdgeIndex(int, int, null, int)
	 */
	public int addEdgeIndex(final int nodeAIdx, final int nodeBIdx,
			final EdgeType label, final int direction) {
		return _addEdge(nodeAIdx, nodeBIdx, label, direction);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPMutableGraph#addNodeAndEdgeIndex(int, null,
	 *      null, int)
	 */
	public int addNodeAndEdgeIndex(final int nodeAIdx,
			final NodeType nodeLabel, final EdgeType edgeLabel,
			final int direction) {
		final int nodeBIdx = addNodeIndex(nodeLabel);
		_addEdge(nodeAIdx, nodeBIdx, edgeLabel, direction);
		return nodeBIdx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPMutableGraph#addNodeIndex(null)
	 */
	public int addNodeIndex(final NodeType label) {
		return _addNode(label);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.Clonable#clone()
	 */
	@Override
	public HPGraph<NodeType, EdgeType> clone() {
		assert this.node_edges[0] != null : dumpGraph("clone(): original wrong");

		final int ns = node_labels.length;
		final int es = edge_labels.length;
		final HPListGraph<NodeType, EdgeType> ret = new HPListGraph<NodeType, EdgeType>(
				name, ID++, ns, es);
		System.arraycopy(this.status, 0, ret.status, 0, this.status.length);

		assert this.node_edges.length == ns : dumpGraph("clone(): node_edges.length wrong "
				+ this.node_edges.length + " " + ns);
		assert ret.node_edges.length == ns : ret
				.dumpGraph("clone(): node_edges.length wrong "
						+ ret.node_edges.length + " " + ns);

		for (int i = ns - 1; i >= 0; --i) {
			ret.node_edges[i] = new int[this.node_edges[i].length];
			System.arraycopy(this.node_edges[i], 0, ret.node_edges[i], 0,
					this.node_edges[i].length);
		}
		System.arraycopy(this.edge_nodes_and_direction, 0,
				ret.edge_nodes_and_direction, 0, 3 * es);
		for (int i = this.node_labels.length - 1; i >= 0; --i) {
			ret.node_labels[i] = this.node_labels[i];// clone??
		}
		for (int i = this.edge_labels.length - 1; i >= 0; --i) {
			ret.edge_labels[i] = this.edge_labels[i];// clone??
		}

		assert ret.node_edges[0] != null : dumpGraph("clone(): original")
				+ "\n" + ret.dumpGraph("clone(): clone");

		return ret;
	}

	private String dumpGraph(final String message) {
		final StringBuilder b = new StringBuilder();
		b.append("--- DUMP graph: " + this.name + "(" + this.id + ") ---");
		b.append(message);
		b.append("\nstatus    : ");
		Debug.dumpArray(status, b);
		b.append("\nedges     : " + edges);
		b.append("\nedge_d_a_n: ");
		Debug.dumpArray(edge_nodes_and_direction, b);
		b.append("\nnode_edges:\n");
		Debug.dumpArray(node_edges, b);
		b.append("\n--- END DUMP ---");
		return b.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#edgeIndexIterator()
	 */
	public IntIterator edgeIndexIterator() {
		return new IntIterator() {
			private final int max = status[MAXEDGE];

			private int next = -1, last = -1;

			public boolean hasNext() {
				if (next >= max) {
					return false;
				}
				if (next > 0) {
					return edge_labels[next] != DELETED;
				}
				for (next = last + 1; next < max
						&& edge_labels[next] == DELETED; ++next) {
					;// step to next valid edge
				}
				return (next < max);
			}

			public int next() {
				if (hasNext()) {
					last = next;
					next = -1;
					return last;
				}
				throw new NoSuchElementException();
			}

			public void remove() {
				_deleteEdge(last);
			};
		};
	}

	private final void edgesChanged() {
		edge_lidx = null;
		edges = null;
		partitions = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getDegree(int)
	 */
	public int getDegree(final int nodeIdx) {
		return node_edges[nodeIdx][DEGREE];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getDirection(int)
	 */
	public int getDirection(final int edgeIdx) {
		return edge_nodes_and_direction[edgeIdx * 3 + DIRECTION];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getDirection(int, int)
	 */
	public int getDirection(final int edgeIdx, final int nodeIdx) {
		if (edge_nodes_and_direction[edgeIdx * 3 + NODEA] == nodeIdx) {
			return edge_nodes_and_direction[edgeIdx * 3 + DIRECTION];
		}
		if (edge_nodes_and_direction[edgeIdx * 3 + NODEB] == nodeIdx) {
			return -edge_nodes_and_direction[edgeIdx * 3 + DIRECTION];
		}
		throw new IllegalArgumentException("node index " + nodeIdx
				+ " is invalid for the edge " + edgeIdx + "!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getEdge(int, int)
	 */
	public int getEdge(final int nodeAIdx, final int nodeBIdx) {
		final int[] nodeA = this.node_edges[nodeAIdx];
		for (int i = nodeA[DEGREE] + FIRSTEDGE - 1; i >= FIRSTEDGE; --i) {
			final int edgeIdx = nodeA[i];
			final int NA = this.edge_nodes_and_direction[edgeIdx * 3 + NODEA];
			final int NB = this.edge_nodes_and_direction[edgeIdx * 3 + NODEB];
			final int Dir = this.edge_nodes_and_direction[edgeIdx * 3
					+ DIRECTION];
			if ((NA == nodeAIdx && NB == nodeBIdx && Dir != Edge.INCOMING)
					|| (NA == nodeBIdx && NB == nodeAIdx && Dir != Edge.OUTGOING)) {
				return edgeIdx;
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getEdgeCount()
	 */
	public int getEdgeCount() {
		return status[EDGECOUNT];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getEdgeIndices(int)
	 */
	public IntIterator getEdgeIndices(final int nodeIdx) {
		return new IntIterator() {
			private final int[] node = node_edges[nodeIdx];

			private int pos = FIRSTEDGE;

			public boolean hasNext() {
				return pos < node[DEGREE] + FIRSTEDGE;
			}

			public int next() {
				if (hasNext()) {
					return node[pos++];
				}
				throw new NoSuchElementException();
			}

			public void remove() {
				_deleteEdge(node[pos - 1]);
				--pos;
			};
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getEdgeLabel(int)
	 */
	public EdgeType getEdgeLabel(final int edgeIdx) {
		return edge_labels[edgeIdx];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getEdgeLabelIndex(int,
	 *      de.parsemis.utils.Relabler)
	 */
	public int getEdgeLabelIndex(final int edgeIdx,
			final Relabler<NodeType, EdgeType> rel) {
		if (this.edge_lidx == null || lastEdgeRelabler != rel) {
			synchronized (this) {
				if (this.edge_lidx == null || lastEdgeRelabler != rel) {
					edge_lidx = new int[edge_labels.length];
					lastEdgeRelabler = rel;
				}
			}
		}
		// is just correct if you do not use different relabler!!
		if (this.edge_lidx[edgeIdx] == 0) {
			final int l = rel.getEdgeLabelIndex(edge_labels[edgeIdx]);
			edge_lidx[edgeIdx] = l + 2;
			return l;
		} else {
			return edge_lidx[edgeIdx] - 2;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getEdges()
	 */
	public BitSet getEdges() {
		if (edges == null) {
			synchronized (this) {
				if (edges == null) {
					edges = new BitSet(status[MAXEDGE]);
					for (int i = status[MAXEDGE] - 1; i >= 0; --i) {
						if (this.isValidEdge(i)) {
							edges.set(i);
						}
					}
				}
			}
		}
		return edges;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getID()
	 */
	public int getID() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getInDegree(int)
	 */
	public int getInDegree(final int nodeIdx) {
		return node_edges[nodeIdx][INDEGREE];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getInEdgeIndices(int)
	 */
	public IntIterator getInEdgeIndices(final int nodeIdx) {
		return new IntIterator() {
			private final int[] node = node_edges[nodeIdx];

			private int next = -1, last = FIRSTEDGE - 1;

			public boolean hasNext() {
				if (next >= node[DEGREE] + FIRSTEDGE) {
					return false;
				}
				if (next >= FIRSTEDGE) {
					return true;
				}
				for (next = last + 1; next < node[DEGREE] + FIRSTEDGE
						&& getDirection(node[next], nodeIdx) != Edge.INCOMING; ++next) {
					;// step to next incomming edge
				}
				return (next < node[DEGREE] + FIRSTEDGE);
			}

			public int next() {
				if (hasNext()) {
					last = next;
					next = -1;
					return node[last];
				}
				throw new NoSuchElementException();
			}

			public void remove() {
				_deleteEdge(node[last]);
				--last;
			};
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getMaxEdgeIndex()
	 */
	public int getMaxEdgeIndex() {
		return status[MAXEDGE];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getMaxNodeIndex()
	 */
	public int getMaxNodeIndex() {
		return status[MAXNODE];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getNodeA(int)
	 */
	public int getNodeA(final int edgeIdx) {
		return edge_nodes_and_direction[edgeIdx * 3 + NODEA];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getNodeB(int)
	 */
	public int getNodeB(final int edgeIdx) {
		return edge_nodes_and_direction[edgeIdx * 3 + NODEB];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getNodeCount()
	 */
	public int getNodeCount() {
		return status[NODECOUNT];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getNodeEdge(int, int)
	 */
	public int getNodeEdge(final int nodeIdx, final int pos) {
		return node_edges[nodeIdx][FIRSTEDGE + pos];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getNodeLabel(int)
	 */
	public NodeType getNodeLabel(final int nodeIdx) {
		return node_labels[nodeIdx];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getNodeLabelIndex(int,
	 *      de.parsemis.utils.Relabler)
	 */
	public int getNodeLabelIndex(final int nodeIdx,
			final Relabler<NodeType, EdgeType> rel) {
		if (this.node_lidx == null || lastNodeRelabler != rel) {
			synchronized (this) {
				if (this.node_lidx == null || lastNodeRelabler != rel) {
					node_lidx = new int[node_labels.length];
					lastNodeRelabler = rel;
				}
			}
		}
		// is just correct if you do not use different relabler!!
		if (this.node_lidx[nodeIdx] == 0) {
			final int l = rel.getNodeLabelIndex(node_labels[nodeIdx]);
			node_lidx[nodeIdx] = l + 2;
			return l;
		} else {
			return node_lidx[nodeIdx] - 2;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getNodeNeigbour(int, int)
	 */
	public int getNodeNeigbour(final int nodeIdx, final int pos) {
		return getOtherNode(getNodeEdge(nodeIdx, pos), nodeIdx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getNodePartions(de.parsemis.miner.environment.Relabler)
	 */
	public int[] getNodePartions(final Relabler<NodeType, EdgeType> rel) {
		if (partitions == null) {
			// use name to seperate partition compution from other locks
			synchronized (name) {
				if (partitions == null) {
					partitions = GraphUtils.computePartitions2(this, rel);
				}
			}
		}
		return partitions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getNodes()
	 */
	public BitSet getNodes() {
		if (nodes == null) {
			synchronized (this) {
				if (nodes == null) {
					nodes = new BitSet(status[MAXNODE]);
					for (int i = status[MAXNODE] - 1; i >= 0; --i) {
						if (this.isValidNode(i)) {
							nodes.set(i);
						}
					}
				}
			}
		}
		return nodes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getOtherNode(int, int)
	 */
	public int getOtherNode(final int edgeIdx, final int nodeIdx) {
		if (edge_nodes_and_direction[edgeIdx * 3 + NODEA] == nodeIdx) {
			return edge_nodes_and_direction[edgeIdx * 3 + NODEB];
		}
		if (edge_nodes_and_direction[edgeIdx * 3 + NODEB] == nodeIdx) {
			return edge_nodes_and_direction[edgeIdx * 3 + NODEA];
		}
		throw new IllegalArgumentException("node index " + nodeIdx
				+ " is invalid for the edge " + edgeIdx + "!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getOutDegree(int)
	 */
	public int getOutDegree(final int nodeIdx) {
		return node_edges[nodeIdx][OUTDEGREE];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#getOutEdgeIndices(int)
	 */
	public IntIterator getOutEdgeIndices(final int nodeIdx) {
		return new IntIterator() {
			private final int[] node = node_edges[nodeIdx];

			private int next = -1, last = FIRSTEDGE - 1;

			public boolean hasNext() {
				if (next >= node[DEGREE] + FIRSTEDGE) {
					return false;
				}
				if (next >= FIRSTEDGE) {
					return true;
				}
				for (next = last + 1; next < node[DEGREE] + FIRSTEDGE
						&& getDirection(node[next], nodeIdx) != Edge.OUTGOING; ++next) {
					;// step to next outgoing edge
				}
				return (next < node[DEGREE] + FIRSTEDGE);
			}

			public int next() {
				if (hasNext()) {
					last = next;
					next = -1;
					return node[last];
				}
				throw new NoSuchElementException();
			}

			public void remove() {
				_deleteEdge(node[last]);
				--last;
			};
		};
	}

	public boolean isValidEdge(final int edgeIdx) {
		return !(edgeIdx < 0 || edgeIdx >= status[MAXEDGE] || edge_labels[edgeIdx] == DELETED);
	}

	public boolean isValidNode(final int nodeIdx) {
		return !(nodeIdx < 0 || nodeIdx >= status[MAXNODE] || node_labels[nodeIdx] == DELETED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#nodeIndexIterator()
	 */
	public IntIterator nodeIndexIterator() {
		return new IntIterator() {
			private final int max = status[MAXNODE];

			private int next = -1, last = -1;

			public boolean hasNext() {
				if (next >= max) {
					return false;
				}
				if (next > 0) {
					return node_labels[next] != DELETED;
				}
				for (next = last + 1; next < max
						&& node_labels[next] == DELETED; ++next) {
					;// step to next valid node
				}
				return (next < max);
			}

			public int next() {
				if (hasNext()) {
					last = next;
					next = -1;
					return last;
				}
				throw new NoSuchElementException();
			}

			public void remove() {
				_deleteNode(last);
			};
		};
	}

	private final void nodesChanged() {
		node_lidx = null;
		nodes = null;
		partitions = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPMutableGraph#removeEdge(int)
	 */
	public boolean removeEdge(final int edgeIdx) {
		if (!this.isValidEdge(edgeIdx)) {
			return false;
		}
		this._deleteEdge(edgeIdx);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPMutableGraph#removeNode(int)
	 */
	public boolean removeNode(final int nodeIdx) {
		if (!this.isValidNode(nodeIdx)) {
			return false;
		}
		this._deleteNode(nodeIdx);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#setEdgeLabel(int, null)
	 */
	public void setEdgeLabel(final int edgeIdx, final EdgeType label) {
		edge_labels[edgeIdx] = label;
		edgesChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#setNodeLabel(int, null)
	 */
	public void setNodeLabel(final int nodeIdx, final NodeType label) {
		node_labels[nodeIdx] = label;
		nodesChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPGraph#toGraph()
	 */
	public Graph<NodeType, EdgeType> toGraph() {
		if (wrap == null) {
			synchronized (this) {
				if (wrap == null) {
					wrap = new HPMutableWrapper<NodeType, EdgeType>(this);
				}
			}
		}
		return wrap;
	}

	@Override
	@SuppressWarnings("unchecked")
	public String toString() {
		return LocalEnvironment.environ.serializer.serialize(this.toGraph());
	}
}
