/**
 * created 21.06.2006
 *
 * @by Tobias Werth (sitowert@i2.informatik.uni-erlangen.de)
 *
 * Copyright 2006 Tobias Werth
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.algorithms.dagminer;

import java.util.Collection;
import java.util.LinkedList;

import de.parsemis.miner.chain.Extender;
import de.parsemis.miner.chain.Extension;
import de.parsemis.miner.chain.MiningStep;
import de.parsemis.miner.chain.SearchLatticeNode;

/**
 * @author Tobias Werth (sitowert@i2.informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class DAGmExtender<NodeType, EdgeType> extends
		MiningStep<NodeType, EdgeType> implements Extender<NodeType, EdgeType> {

	private Collection<SearchLatticeNode<NodeType, EdgeType>> children;
	private final Collection<Extension<NodeType, EdgeType>> dummy;
	private MiningStep<NodeType, EdgeType> first;

	// public static Graph g;
	// //static{try { g =new SimpleDirectedGraphParser(new
	// StringLabelParser(),new StringLabelParser()).parse("blub\nA 1 1 - -\n- B
	// - - 1\n- - B 1 1\n- - - C -\n- - - - C\n",new
	// ListGraph.Factory<String,String>()); }
	// static{try { g =new SimpleDirectedGraphParser(new StringLabelParser(),new
	// StringLabelParser()).parse("blub\n" +
	// // "X - - - - 1\n- B - - - 1\n- - B 1 1 -\n- - - C - -\n- - - - C -\n- -
	// - - - C\n",
	// // "A - - - 1 -\n- A 1 1 - -\n- - A - - 1\n- - - A 1 -\n- - - - A -\n- -
	// - - - A\n",
	//	
	// "C - - 1 - - -\n" +
	// "- A - - - - 1\n" +
	// "- - A - 1 1 -\n" +
	// "- - - G 1 1 1\n" +
	// "- - - - I - -\n" +
	// "- - - - - I -\n" +
	// "- - - - - - I",
	//			
	// new ListGraph.Factory<String,String>(new StringLabelParser(),new
	// StringLabelParser())); }
	// catch(Exception e) {System.err.println("exception while parsing static
	// graph: " + e);}
	// }

	// FIXME new extender
	public DAGmExtender() {
		super(null);
		dummy = new LinkedList<Extension<NodeType, EdgeType>>();
		first = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.MiningStep#call(de.parsemis.miner.SearchLatticeNode,
	 *      java.util.Collection)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node,
			final Collection<Extension<NodeType, EdgeType>> extensions) {
		for (final Extension<NodeType, EdgeType> ext : extensions) {
			children.add((SearchLatticeNode<NodeType, EdgeType>) ext);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Extender#getChildren(java.util.Collection)
	 */
	public Collection<SearchLatticeNode<NodeType, EdgeType>> getChildren(
			final Collection<SearchLatticeNode<NodeType, EdgeType>> nodes) {
		throw new UnsupportedOperationException("not available for DAGminer");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Extender#getChildren(de.parsemis.miner.SearchLatticeNode)
	 */
	public Collection<SearchLatticeNode<NodeType, EdgeType>> getChildren(
			final SearchLatticeNode<NodeType, EdgeType> node) {
		children = new LinkedList<SearchLatticeNode<NodeType, EdgeType>>();
		dummy.clear();
		first.call(node, dummy);
		return children;
	}

	protected final void setFirst(final MiningStep<NodeType, EdgeType> first) {
		this.first = first;
	}

}
