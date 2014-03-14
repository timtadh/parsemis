/**
 * created: 18.02.2008
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
package de.parsemis.algorithms.gaston;

import java.util.Collection;

import de.parsemis.graph.HPGraph;
import de.parsemis.miner.environment.ThreadEnvironment;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.miner.general.Embedding;
import de.parsemis.miner.general.Frequency;
import de.parsemis.miner.general.HPEmbedding;

/**
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class LazyExtendedEmbedding_flat<NodeType, EdgeType> implements
		GastonEmbedding<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	GastonEmbedding_flat<NodeType, EdgeType> cache = null;
	final int superNode;

	int id;
	GastonFragment<NodeType, EdgeType> frag;

	boolean needsExtension = true;

	public LazyExtendedEmbedding_flat(
			final GastonEmbedding_flat<NodeType, EdgeType> parent,
			final int superNode, final int id) {
		this.cache = parent;
		this.superNode = superNode;
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final GastonEmbedding<NodeType, EdgeType> arg0) {
		return get().compareTo(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.HPEmbedding#freeSuperEdge(int)
	 */
	public boolean freeSuperEdge(final int superEdge) {
		return get().freeSuperEdge(superEdge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.HPEmbedding#freeSuperNode(int)
	 */
	public boolean freeSuperNode(final int superNode) {
		return get().freeSuperNode(superNode);
	}

	public void freeTransient() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.Frequented#frequency()
	 */
	public Frequency frequency() {
		return get().frequency();
	}

	public synchronized final GastonEmbedding<NodeType, EdgeType> get() {
		if (needsExtension) {
			needsExtension = false;

			int[] superNodes = cache.getSuperNodes();
			final int pid = cache.getId();
			final int dbIdx = cache.getDataBaseGraphIndex();

			final int subNode = frag.correspondingNode;
			if (subNode != HPGraph.NO_NODE) {
				if (superNodes.length <= subNode) {
					final int[] tmp = new int[subNode + 1];
					System.arraycopy(superNodes, 0, tmp, 0, superNodes.length);
					for (int i = superNodes.length; i < subNode; i++) {
						tmp[i] = HPGraph.NO_NODE;
					}
					superNodes = tmp;
				} else {
					superNodes = superNodes.clone();
				}
				superNodes[subNode] = superNode;
			}
			cache = new GastonEmbedding_flat<NodeType, EdgeType>(dbIdx,
					superNodes, frag, id);
			id = pid;
		}
		return cache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.HPEmbedding#getDataBaseGraph()
	 */
	public DataBaseGraph<NodeType, EdgeType> getDataBaseGraph() {
		return cache.getDataBaseGraph();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.algorithms.gaston.GastonEmbedding#getId()
	 */
	public synchronized int getId() {
		return needsExtension ? id : cache.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.algorithms.gaston.GastonEmbedding#getParentId()
	 */
	public synchronized int getParentId() {
		return needsExtension ? cache.getId() : id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.algorithms.gaston.GastonEmbedding#getSubGraph()
	 */
	public HPGraph<NodeType, EdgeType> getSubGraph() {
		return get().getSubGraph();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.HPEmbedding#getSubGraphEdge(int)
	 */
	public int getSubGraphEdge(final int superEdge) {
		return get().getSubGraphEdge(superEdge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.HPEmbedding#getSubGraphNode(int)
	 */
	public int getSubGraphNode(final int superNode) {
		return get().getSubGraphNode(superNode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.algorithms.gaston.GastonEmbedding#getSubNode()
	 */
	public int getSubNode() {
		return get().getSubNode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.HPEmbedding#getSuperGraph()
	 */
	public HPGraph<NodeType, EdgeType> getSuperGraph() {
		return cache.getSuperGraph();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.HPEmbedding#getSuperGraphEdge(int)
	 */
	public int getSuperGraphEdge(final int subEdge) {
		return get().getSuperGraphEdge(subEdge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.HPEmbedding#getSuperGraphNode(int)
	 */
	public int getSuperGraphNode(final int subNode) {
		return get().getSuperGraphNode(subNode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.algorithms.gaston.GastonEmbedding#getSuperNode()
	 */
	public int getSuperNode() {
		return superNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.algorithms.gaston.GastonEmbedding#isInit()
	 */
	public boolean isInit() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.HPEmbedding#overlaps(de.parsemis.miner.general.HPEmbedding,
	 *      java.util.Collection)
	 */
	public boolean overlaps(final HPEmbedding<NodeType, EdgeType> other,
			final Collection<NodeType> ignore) {
		return get().overlaps(other, ignore);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.algorithms.gaston.GastonEmbedding#release(de.parsemis.miner.environment.ThreadEnvironment)
	 */
	public void release(final ThreadEnvironment<NodeType, EdgeType> tenv) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.algorithms.gaston.GastonEmbedding#setFrag(de.parsemis.algorithms.gaston.GastonFragment)
	 */
	public void setFrag(final GastonFragment<NodeType, EdgeType> frag) {
		this.frag = frag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.HPEmbedding#toEmbedding()
	 */
	public Embedding<NodeType, EdgeType> toEmbedding() {
		return get().toEmbedding();
	}

}
