/**
 * created Jun 22, 2006
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
package de.parsemis.miner.general;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.Node;
import de.parsemis.miner.chain.MaxCliqueStep;
import de.parsemis.miner.environment.LocalEnvironment;

/**
 * This class is a simple Fragment implementation based on an ArrayList
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 * @deprecated
 */
@Deprecated
public class ArrayFragment<NodeType, EdgeType> extends
		ArrayList<Embedding<NodeType, EdgeType>> implements
		Fragment<NodeType, EdgeType> {
	final static long serialVersionUID = 15001;

	private transient Collection<Embedding<NodeType, EdgeType>> mc;

	transient HPFragment<NodeType, EdgeType> hp = null;

	/**
	 * creates an empty Fragment
	 */
	public ArrayFragment() {
		super();
	}

	/**
	 * creates a Fragment containing the given embeddings
	 * 
	 * @param arg0
	 */
	public ArrayFragment(final Collection<Embedding<NodeType, EdgeType>> arg0) {
		super(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#add(de.parsemis.miner.DataBaseGraph)
	 */
	public void add(final DataBaseGraph<NodeType, EdgeType> graph)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"add(DataBaseGraph) not available");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#copy()
	 */
	public Fragment<NodeType, EdgeType> copy() {
		return new ArrayFragment<NodeType, EdgeType>(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#embeddingToFragmentEdge(de.parsemis.graph.Embedding,
	 *      de.parsemis.graph.Edge)
	 */
	public Edge<NodeType, EdgeType> embeddingToFragmentEdge(
			final Embedding<NodeType, EdgeType> emb,
			final Edge<NodeType, EdgeType> embeddingEdge) {
		assert (contains(emb));
		return embeddingEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#embeddingToFragmentNode(de.parsemis.graph.Embedding,
	 *      de.parsemis.graph.Node)
	 */
	public Node<NodeType, EdgeType> embeddingToFragmentNode(
			final Embedding<NodeType, EdgeType> emb,
			final Node<NodeType, EdgeType> embeddingNode) {
		assert (contains(emb));
		return embeddingNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#fragmentToEmbeddingEdge(de.parsemis.graph.Embedding,
	 *      de.parsemis.graph.Edge)
	 */
	public Edge<NodeType, EdgeType> fragmentToEmbeddingEdge(
			final Embedding<NodeType, EdgeType> emb,
			final Edge<NodeType, EdgeType> fragmentEdge) {
		assert (contains(emb));
		return fragmentEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#fragmentToEmbeddingNode(de.parsemis.graph.Embedding,
	 *      de.parsemis.graph.Node)
	 */
	public Node<NodeType, EdgeType> fragmentToEmbeddingNode(
			final Embedding<NodeType, EdgeType> emb,
			final Node<NodeType, EdgeType> fragmentNode) {
		assert (contains(emb));
		return fragmentNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.Frequented#frequency()
	 */
	public Frequency frequency() {
		throw new UnsupportedOperationException("frequency() not available");
	}

	public Collection<Embedding<NodeType, EdgeType>> getEmbeddings() {
		return getMaximalNonOverlappingSubSet();
	}

	public Collection<Embedding<NodeType, EdgeType>> getMaximalNonOverlappingSubSet() {
		if (mc == null) {
			mc = MaxCliqueStep.findMaxClique(this,
					LocalEnvironment.env(this).ignoreNodes);
		}
		return mc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#toGraph()
	 */
	public Graph<NodeType, EdgeType> toGraph() {
		return get(0).getSubGraph();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#graphIterator()
	 */
	public Iterator<DataBaseGraph<NodeType, EdgeType>> graphIterator() {
		throw new UnsupportedOperationException("graphIterator() not available");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.Fragment#toHPFragment()
	 */
	public HPFragment<NodeType, EdgeType> toHPFragment() {
		if (hp == null) {
			hp = new FragmentWrapper<NodeType, EdgeType>(this);
		}
		return hp;
	}
}
