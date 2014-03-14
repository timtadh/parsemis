/**
 * created May 16, 2006
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
package de.parsemis.algorithms.gSpan;

import java.io.Serializable;

import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.HPMutableGraph;
import de.parsemis.graph.Node;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.utils.Cloneable;
import de.parsemis.utils.Generic;

/**
 * Represents the edge tuples used in gSpan to represent one edge in
 * the DFS-Code.
 * <p>
 * It can/will be stored in local object pool to avoid object generation/garbage
 * collection.
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
public class GSpanEdge<NodeType, EdgeType> implements
		Comparable<GSpanEdge<NodeType, EdgeType>>,
		Cloneable<GSpanEdge<NodeType, EdgeType>>, Generic<NodeType, EdgeType>,
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected transient GThreadEnvironment<NodeType, EdgeType> tenv;

	private int nodeA, nodeB;;

	private int labelA, labelB;

	private int edgeLabel, direction;

	/** the next edge in the DFS-Code (or the pool-list) */
	protected GSpanEdge<NodeType, EdgeType> next;

	/**
	 * creates a new edge tuple for the gSpan algorithm
	 * 
	 * @param tenv
	 */
	public GSpanEdge(final GThreadEnvironment<NodeType, EdgeType> tenv) {
		this.tenv = tenv;
	}

	/**
	 * adds this edge to the given <code>graph</code>
	 * 
	 * @param graph
	 */
	public final void addTo(final HPMutableGraph<NodeType, EdgeType> graph) {
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		if (graph.getNodeCount() == nodeA) {
			graph.addNodeIndex(env.getNodeLabel(labelA));
		}
		if (graph.getNodeCount() == nodeB) {
			graph.addNodeIndex(env.getNodeLabel(labelB));
		}
		graph
				.addEdgeIndex(nodeA, nodeB, env.getEdgeLabel(edgeLabel),
						direction);
	}

	@Override
	public GSpanEdge<NodeType, EdgeType> clone() {
		return new GSpanEdge<NodeType, EdgeType>(tenv).set(nodeA, nodeB,
				labelA, edgeLabel, labelB, direction);
	}

	/**
	 * @param tenv
	 * @return a new GSpanEdge in the given environment
	 */
	public GSpanEdge<NodeType, EdgeType> clone(
			final GThreadEnvironment<NodeType, EdgeType> tenv) {
		return tenv.getEdge(nodeA, nodeB, labelA, edgeLabel, labelB, direction);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(T)
	 */
	public int compareTo(final GSpanEdge<NodeType, EdgeType> arg0) {
		return compareTo(arg0, arg0.nodeB);
	}

	/**
	 * compares this edge with the given <code>other</code> one,
	 * 
	 * @param other
	 * @param nodeB
	 *            thid node is used as the second node for the other edge
	 * @return <0; 0; or >0
	 */
	public final int compareTo(final GSpanEdge<NodeType, EdgeType> other,
			final int nodeB) {
		if (this.nodeA == other.nodeA) {
			if (this.nodeB != nodeB) {
				return this.nodeB - nodeB;
			}
			if (this.direction != other.direction) {
				return other.direction - this.direction;
			}
			if (this.labelA != other.labelA) {
				return this.labelA - other.labelA;
			}
			if (this.edgeLabel != other.edgeLabel) {
				return this.edgeLabel - other.edgeLabel;
			}
			return this.labelB - other.labelB;
		} else { // TODO: das laesst sich bestimmt noch irgendwie schoener
			// schreiben
			if (this.nodeA < this.nodeB) {
				if (this.nodeB == other.nodeA) {
					return -1; // see paper
				} else {
					if (other.nodeA > this.nodeA) {
						if (other.nodeA > this.nodeB) {
							return -1;
						} else {
							return 1;
						}
					} else {
						if (this.nodeA >= nodeB) {
							return 1;
						} else {
							return -1;
						}
					}
				}
			} else if (other.nodeA < nodeB) {
				if (nodeB == this.nodeA) {
					return 1; // see paper
				} else {
					if (other.nodeA > this.nodeA) {
						if (other.nodeA >= this.nodeB) {
							return -1;
						} else {
							return 1;
						}
					} else {
						if (this.nodeA > nodeB) {
							return 1;
						} else {
							return -1;
						}
					}
				}
			} else { // compare two backwards edges with different nodeA
				return this.nodeA - other.nodeA;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof GSpanEdge && compareTo((GSpanEdge) obj) == 0;
	}

	/**
	 * calculates the edge of th given graph that corresponds to this gSpanEdge
	 * in the given "embedding"
	 * 
	 * @param graph
	 * @param ackNodes
	 * @return the calculated edge
	 */
	public final Edge<NodeType, EdgeType> getCorresponding(
			final Graph<NodeType, EdgeType> graph, final int[] ackNodes) {
		final Node<NodeType, EdgeType> nA = graph.getNode(ackNodes[nodeA]);
		final Node<NodeType, EdgeType> nB = graph.getNode(ackNodes[nodeB]);
		if (direction == Edge.INCOMING) {
			return graph.getEdge(nB, nA);
		} else {
			return graph.getEdge(nA, nB);
		}
	}

	/**
	 * calculates the edge of th given graph that corresponds to this gSpanEdge
	 * in the given "embedding"
	 * 
	 * @param graph
	 * @param ackNodes
	 * @return the calculated edge
	 */
	public final int getCorresponding(final HPGraph<NodeType, EdgeType> graph,
			final int[] ackNodes) {
		final int nA = ackNodes[nodeA];
		final int nB = ackNodes[nodeB];
		if (direction == Edge.INCOMING) {
			return graph.getEdge(nB, nA);
		} else {
			return graph.getEdge(nA, nB);
		}
	}

	/** @return the direction of the edge */
	public final int getDirection() {
		return direction;
	}

	/** @return the edge label index of the edge */
	public final int getEdgeLabel() {
		return edgeLabel;
	}

	/** @return the node label index of the first node of the edge */
	public final int getLabelA() {
		return labelA;
	}

	/** @return the node label index of the second node of the edge */
	public final int getLabelB() {
		return labelB;
	}

	/** @return the DFS-index of the first node of the edge */
	public final int getNodeA() {
		return nodeA;
	}

	/** @return the DFS-index of the second node of the edge */
	public final int getNodeB() {
		return nodeB;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return nodeA << 20 + nodeB << 16 + labelA << 12 + labelB << 8 + edgeLabel << 4 + direction;
	}

	/** @return if this edge is a forward edge */
	public final boolean isForward() {
		return nodeA < nodeB;
	}

	protected void release(final GThreadEnvironment<NodeType, EdgeType> target) {
		if (target == tenv) {
			target.push(this);
		}
	}

	/**
	 * stores this edge and all edges in the chain till the <code>last</code>
	 * edge in the given <code>target</code> environment, if possible
	 * 
	 * @param last
	 * @param target
	 */
	public void releaseAll(final GSpanEdge<NodeType, EdgeType> last,
			final GThreadEnvironment<NodeType, EdgeType> target) {
		if (target == tenv) {
			target.push(this, last);
		}
	}

	/**
	 * checks if this gSpanEdge has the same label and direction as the given
	 * edge
	 * 
	 * @param graph
	 * @param edge
	 * @param node
	 * @return <code>true</code>, if label and direction corresponds
	 */
	public final boolean sameAs(final HPGraph<NodeType, EdgeType> graph,
			final int edge, final int node) {
		return ((graph.getDirection(edge, node) == direction) && (graph
				.getEdgeLabelIndex(edge, LocalEnvironment.env(this)) == edgeLabel));
	}

	/**
	 * checks if this gSpanEdge is a represantation for the given graph edge
	 * 
	 * @param graph
	 * @param edge
	 * @return <code>true</code>, if the same
	 */
	public final boolean sameAs2(final HPGraph<NodeType, EdgeType> graph,
			final int edge) {
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		final int oe = graph.getEdgeLabelIndex(edge, env);
		if (oe != edgeLabel) {
			return false;
		}

		final int oa = graph.getNodeLabelIndex(graph.getNodeA(edge), env);
		final int ob = graph.getNodeLabelIndex(graph.getNodeB(edge), env);
		final int od = graph.getDirection(edge);
		if (od == Edge.UNDIRECTED && oa == labelA && ob == labelB) {
			return true;
		}
		return ((oa == labelA && ob == labelB && graph.getDirection(edge) == direction) || (ob == labelA
				&& oa == labelB && graph.getDirection(edge) == -direction));
	}

	/**
	 * checks if this gSpanEdge is a represantation for the given graph edge
	 * 
	 * @param orig
	 * @return <code>true</code>, if the same
	 */
	public final boolean sameAs3(final Edge<NodeType, EdgeType> orig) {
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		final int oe = env.getEdgeLabelIndex(orig.getLabel());
		if (oe != edgeLabel) {
			return false;
		}

		final int oa = env.getNodeLabelIndex(orig.getNodeA().getLabel());
		final int ob = env.getNodeLabelIndex(orig.getNodeB().getLabel());
		if (orig.getDirection() == Edge.UNDIRECTED && oa == labelA
				&& ob == labelB) {
			return true;
		}
		return ((oa == labelA && ob == labelB && orig.getDirection() == direction) || (ob == labelA
				&& oa == labelB && orig.getDirection() == -direction));
	}

	/**
	 * reinitializes the current edge
	 * 
	 * @param nodeA
	 * @param nodeB
	 * @param labelA
	 * @param edgeLabel
	 * @param labelB
	 * @param direction
	 * @return the reinitialized edge
	 */
	public GSpanEdge<NodeType, EdgeType> set(final int nodeA, final int nodeB,
			final int labelA, final int edgeLabel, final int labelB,
			final int direction) {
		this.nodeA = nodeA;
		this.nodeB = nodeB;
		this.edgeLabel = edgeLabel;
		this.direction = direction;
		if (direction == Edge.UNDIRECTED && nodeA == 0 && nodeB == 1
				&& labelA > labelB) {
			this.labelA = labelB;
			this.labelB = labelA;
		} else {
			this.labelA = labelA;
			this.labelB = labelB;
		}
		this.next = null;
		return this;
	}

	@Override
	public String toString() {
		return "(" + nodeA + " " + nodeB + ": " + labelA + " " + edgeLabel
				+ " " + labelB + " " + direction + ")";
	}

}
