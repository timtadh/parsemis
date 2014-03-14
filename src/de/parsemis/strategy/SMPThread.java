/**
 * Created on Jun 26, 2006
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
package de.parsemis.strategy;

import java.util.Collection;

import de.parsemis.algorithms.Algorithm;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.general.Fragment;
import de.parsemis.utils.Generic;

/**
 * This class represents a single thread for the multi-threaded paralellisation
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
public class SMPThread<NodeType, EdgeType> extends Thread implements
		Generic<NodeType, EdgeType> {
	private final int idx;

	private final Algorithm<NodeType, EdgeType> algo;

	private final StackList<NodeType, EdgeType> list;

	private final Collection<Fragment<NodeType, EdgeType>> answer;

	/**
	 * creates a new thread
	 * 
	 * @param idx
	 *            the index of the new thread
	 * @param algo
	 *            the algorithm that will be used
	 * @param list
	 *            the list of all stacks
	 * @param answer
	 *            the collection the found fragments will be stored in
	 */
	public SMPThread(final int idx, final Algorithm<NodeType, EdgeType> algo,
			final StackList<NodeType, EdgeType> list,
			final Collection<Fragment<NodeType, EdgeType>> answer) {
		this.idx = idx;
		this.algo = algo;
		this.list = list;
		this.answer = answer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		new Worker<NodeType, EdgeType>(new LocalStack<NodeType, EdgeType>(idx,
				list, LocalEnvironment.env(this)), answer, algo
				.getExtender(idx), idx).run();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#toString()
	 */
	@Override
	public String toString(){
		return "SMPThread-"+idx;
	}
	
	public int getIdx(){
		return idx;
	}

}
