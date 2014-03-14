/**
 * created May 25, 2006
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

import java.util.Collection;

import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.Node;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.miner.general.Embedding;
import de.parsemis.miner.general.EmbeddingWrapper;
import de.parsemis.miner.general.Frequency;
import de.parsemis.miner.general.HPEmbedding;

/**
 * Implements the embedding interface for the GSpan algorithm.
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
public class GSpanEmbedding<NodeType, EdgeType> implements
		Embedding<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1920495376552453967L;

	public static int counter = 0;

	private transient/* final */GThreadEnvironment<NodeType, EdgeType> tenv;

	private int idx;

	private transient GSpanGraph<NodeType, EdgeType> dbGraph;

	private Graph<NodeType, EdgeType> subGraph;

	// stores to each subgraph node index (index of the array) the corresponding
	// supergraph node index (value)
	private int[] superNodes;

	private final int threadIdx;

	private transient HPEmbedding<NodeType, EdgeType> hp;

	/**
	 * creates a new empty GSpanEmbedding
	 * 
	 * @param tenv
	 */
	public GSpanEmbedding(final GThreadEnvironment<NodeType, EdgeType> tenv) {
		counter++;
		this.threadIdx = tenv.threadIdx;
		this.tenv = tenv;
	}

	public Frequency frequency() {
		return this.getDataBaseGraph().frequency();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Embedding#getDataBaseGraph()
	 */
	public DataBaseGraph<NodeType, EdgeType> getDataBaseGraph() {
		if (dbGraph == null) {
			dbGraph = (GSpanGraph<NodeType, EdgeType>) LocalEnvironment.env(
					this).getGraph(idx);
		}
		return dbGraph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Embedding#getSubGraph()
	 */
	public Graph<NodeType, EdgeType> getSubGraph() {
		return subGraph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Embedding#getSubGraphEdge(de.parsemis.graph.Edge)
	 */
	public Edge<NodeType, EdgeType> getSubGraphEdge(
			final Edge<NodeType, EdgeType> superEdge) {
		final Node<NodeType, EdgeType> subNodeA = getSubGraphNode(superEdge
				.getNodeA());
		final Node<NodeType, EdgeType> subNodeB = getSubGraphNode(superEdge
				.getNodeB());
		if (subNodeA == null || subNodeB == null) {
			return null;
		} else {
			return getSubGraph().getEdge(subNodeA, subNodeB);
		}
	}

	private int getSubGraphNode(final int superNode) {
		for (int i = superNodes.length - 1; i >= 0; --i) {
			if (superNodes[i] == superNode) {
				return i;
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Embedding#getSubGraphNode(de.parsemis.graph.Node)
	 */
	public Node<NodeType, EdgeType> getSubGraphNode(
			final Node<NodeType, EdgeType> superNode) {
		final int superNodeIndex = superNode.getIndex();
		for (int i = superNodes.length - 1; i >= 0; --i) {
			if (superNodes[i] == superNodeIndex) {
				return getSubGraph().getNode(i);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Embedding#getSuperGraph()
	 */
	public Graph<NodeType, EdgeType> getSuperGraph() {
		return getDataBaseGraph().toGraph();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Embedding#getSuperGraphEdge(de.parsemis.graph.Edge)
	 */
	public Edge<NodeType, EdgeType> getSuperGraphEdge(
			final Edge<NodeType, EdgeType> subEdge) {
		final Node<NodeType, EdgeType> na = getSuperGraphNode(subEdge
				.getNodeA());
		final Node<NodeType, EdgeType> nb = getSuperGraphNode(subEdge
				.getNodeB());
		return subEdge.getDirection() == Edge.INCOMING ? getSuperGraph()
				.getEdge(nb, na) : getSuperGraph().getEdge(na, nb);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Embedding#getSuperGraphNode(de.parsemis.graph.Node)
	 */
	public Node<NodeType, EdgeType> getSuperGraphNode(
			final Node<NodeType, EdgeType> subNode) {
		return getSuperGraph().getNode(superNodes[subNode.getIndex()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Embedding#overlaps(de.parsemis.graph.Embedding,
	 *      java.util.Collection)
	 */
	public boolean overlaps(final Embedding<NodeType, EdgeType> other,
			final Collection<NodeType> ignore) {
		@SuppressWarnings("unchecked")
		final GSpanEmbedding<NodeType, EdgeType> emb = (GSpanEmbedding<NodeType, EdgeType>) other;
		if (other.getDataBaseGraph() != this.getDataBaseGraph()) {
			return false;
		}
		if (ignore == null) {
			return overlaps(emb);
		}
		for (int i = 0; i < superNodes.length; ++i) {
			if (emb.getSubGraphNode(superNodes[i]) != HPGraph.NO_NODE
					&& !ignore.contains(getSuperGraphNode(
							getSubGraph().getNode(i)).getLabel())) {
				return true;
			}
		}
		return false;
	}

	private final boolean overlaps(
			final GSpanEmbedding<NodeType, EdgeType> other) {
		for (int i = 0; i < superNodes.length; ++i) {
			if (other.getSubGraphNode(superNodes[i]) != HPGraph.NO_NODE) {
				return true;
			}
		}
		return false;
	}

	/**
	 * stores this embedding to the given <code>target</code> environment, if
	 * possible
	 * 
	 * @param target
	 */
	public void release(final GThreadEnvironment<NodeType, EdgeType> target) {
		// if (target==tenv) target.push(this);
	}

	/**
	 * @param code
	 * @param dbGraph
	 * @param superNodes
	 * @return a (re-)initialized GSpanEmbedding
	 */
	public GSpanEmbedding<NodeType, EdgeType> set(
			final DFSCode<NodeType, EdgeType> code,
			final GSpanGraph<NodeType, EdgeType> dbGraph, final int[] superNodes) {
		this.dbGraph = dbGraph;
		this.idx = dbGraph.getIndex();
		this.subGraph = code.toFragment().toGraph();
		this.superNodes = superNodes;
		return this;
	}

	/**
	 * @param sub
	 * @param dbGraph
	 * @param superNodes
	 * @return a (re-)initialized GSpanEmbedding
	 */
	public GSpanEmbedding<NodeType, EdgeType> set(
			final Graph<NodeType, EdgeType> sub,
			final GSpanGraph<NodeType, EdgeType> dbGraph, final int[] superNodes) {
		this.dbGraph = dbGraph;
		this.idx = dbGraph.getIndex();
		this.subGraph = sub;
		this.superNodes = superNodes;
		this.hp = null;
		return this;
	}

	/*
	 * reinitialize tenv after serialization to another machine
	 */
	@SuppressWarnings("unused")
	private final GThreadEnvironment<NodeType, EdgeType> tenv() {
		if (tenv == null) {
			return (GThreadEnvironment<NodeType, EdgeType>) LocalEnvironment
					.env(this).getThreadEnv(threadIdx);
		}
		return tenv;
	};

	public HPEmbedding<NodeType, EdgeType> toHPEmbedding() {
		if (hp == null) {
			hp = new EmbeddingWrapper<NodeType, EdgeType>(this);
		}
		return hp;
	}

	@Override
	public String toString() {
		String ret = getDataBaseGraph().toGraph().getName() + " {"
				+ this.superNodes[0];
		for (int i = 1; i < superNodes.length; ++i) {
			ret += ", " + superNodes[i];
		}
		return ret + "}";
	}
}
