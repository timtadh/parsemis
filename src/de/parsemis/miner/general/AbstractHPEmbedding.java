/**
 * created Nov 27, 2007
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
package de.parsemis.miner.general;

import java.util.BitSet;

import de.parsemis.graph.Edge;
import de.parsemis.graph.HPGraph;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.utils.IntIterator;

/**
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 * @param <DB>
 *            the corresponding DataBaseGraph type
 */
public abstract class AbstractHPEmbedding<NodeType, EdgeType, DB extends DataBaseGraph<NodeType, EdgeType>>
		implements HPEmbedding<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private HPGraph<NodeType, EdgeType> hpSubGraph;

	int databaseGraphIndex;

	// unused supergraph nodes
	transient BitSet freeNodes = null;
	// unused supergraph edges
	transient BitSet freeEdges = null;

	// normal clone of current HPEmbedding
	private transient Embedding<NodeType, EdgeType> emb = null;

	public final BitSet freeEdges() {
		if (freeEdges == null) {
			freeEdges = getDataBaseGraph().getEdges();
			for (final IntIterator edges = getSubGraph().edgeIndexIterator(); edges
					.hasNext();) {
				freeEdges.clear(getSuperGraphEdge(edges.next()));
			}
		}
		return freeEdges;
	}

	protected final void freeEdges(final BitSet freeEdges) {
		this.freeEdges = freeEdges;
	}

	abstract protected BitSet freeNodes();

	protected final void freeNodes(final BitSet freeNodes) {
		this.freeNodes = freeNodes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPEmbedding#freeSuperEdge(int)
	 */
	public boolean freeSuperEdge(final int superEdge) {
		return freeEdges().get(superEdge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPEmbedding#freeSuperEdge(int)
	 */
	public boolean freeSuperNode(final int superNode) {
		return freeNodes().get(superNode);
	}

	public void freeTransient() {
		freeNodes = null;
		freeEdges = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.Frequented#frequency()
	 */
	public Frequency frequency() {
		return this.getDataBaseGraph().frequency();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPEmbedding#getDataBaseGraph()
	 */
	@SuppressWarnings("unchecked")
	public DB getDataBaseGraph() {
		return (DB) LocalEnvironment.env(this).getGraph(databaseGraphIndex);
	}

	public int getDataBaseGraphIndex() {
		return databaseGraphIndex;
	}

	public final BitSet getFreeEdges() {
		return (BitSet) freeEdges().clone();
	}

	public final BitSet getFreeNodes() {
		return (BitSet) freeNodes().clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPEmbedding#getSubGraph()
	 */
	public HPGraph<NodeType, EdgeType> getSubGraph() {
		return hpSubGraph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPEmbedding#getSubGraphEdge(int)
	 */
	public int getSubGraphEdge(final int superEdge) {
		if (freeSuperEdge(superEdge)) {
			return HPGraph.NO_EDGE;
		}
		final HPGraph<NodeType, EdgeType> superG = getSuperGraph();
		final int subNodeA = getSubGraphNode(superG.getNodeA(superEdge));
		final int subNodeB = getSubGraphNode(superG.getNodeB(superEdge));
		if (subNodeA == HPGraph.NO_NODE || subNodeB == HPGraph.NO_NODE) {
			return HPGraph.NO_EDGE;
		} else {
			return getSubGraph().getEdge(subNodeA, subNodeB);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPEmbedding#getSuperGraph()
	 */
	public HPGraph<NodeType, EdgeType> getSuperGraph() {
		return getDataBaseGraph().toHPGraph();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPEmbedding#getSuperGraphEdge(int)
	 */
	public int getSuperGraphEdge(final int subEdge) {
		final int na = getSuperGraphNode(hpSubGraph.getNodeA(subEdge));
		final int nb = getSuperGraphNode(hpSubGraph.getNodeB(subEdge));
		return hpSubGraph.getDirection(subEdge) == Edge.INCOMING ? getSuperGraph()
				.getEdge(nb, na)
				: getSuperGraph().getEdge(na, nb);
	}

	public AbstractHPEmbedding<NodeType, EdgeType, DB> set(final DB dbGraph,
			final HPGraph<NodeType, EdgeType> subGraph) {
		this.databaseGraphIndex = dbGraph.getIndex();
		this.hpSubGraph = subGraph;
		freeTransient();
		return this;
	}

	public AbstractHPEmbedding<NodeType, EdgeType, DB> set(
			final int dbGraphIdx, final HPGraph<NodeType, EdgeType> subGraph) {
		this.databaseGraphIndex = dbGraphIdx;
		this.hpSubGraph = subGraph;
		freeTransient();
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPEmbedding#toEmbedding()
	 */
	public Embedding<NodeType, EdgeType> toEmbedding() {
		if (emb == null) {
			emb = new HPEmbeddingWrapper<NodeType, EdgeType>(this);
		}
		return emb;
	}

	@Override
	public String toString() {
		final StringBuffer ret = new StringBuffer();
		ret.append(getSuperGraph().getName());
		ret.append("[0=" + getSuperGraphNode(0));
		for (int i = 1; i < getSubGraph().getMaxNodeIndex(); ++i) {
			ret.append(" ;" + i + "=" + getSuperGraphNode(i));
		}
		ret.append("]");
		return ret.toString();
	}
}
