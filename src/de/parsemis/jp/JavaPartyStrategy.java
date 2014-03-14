/**
 * created Nov 13, 2007
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
package de.parsemis.jp;

import static de.parsemis.miner.environment.Debug.INFO;
import static de.parsemis.miner.environment.Debug.VERBOSE;
import static de.parsemis.miner.environment.Debug.WARN;
import static de.parsemis.miner.environment.Debug.err;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import de.parsemis.algorithms.Algorithm;
import de.parsemis.miner.chain.SearchLatticeNode;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.environment.Statistics;
import de.parsemis.miner.general.Fragment;
import de.parsemis.strategy.LocalStack;
import de.parsemis.strategy.MiningStack;
import de.parsemis.strategy.StackList;
import de.parsemis.strategy.Strategy;
import de.parsemis.strategy.Worker;

/**
 * This class represents the distributed Strategy for the JavaParty environment
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
public class JavaPartyStrategy<NodeType, EdgeType> implements
		Strategy<NodeType, EdgeType> {
	private final RemoteHashSet<Fragment<NodeType, EdgeType>>[] answers;

	private final RemoteJPThread<NodeType, EdgeType>[] threads;

	private final Statistics stats;

	/**
	 * creates a new search for the given <code>settings</code>
	 * 
	 * @param threadCount
	 * @param stats
	 * 
	 */
	@SuppressWarnings("unchecked")
	public JavaPartyStrategy(final int threadCount, final Statistics stats) {
		this.stats = stats;
		answers = new RemoteHashSet[threadCount];
		threads = new RemoteJPThread[threadCount];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.strategy.Strategy#search(de.parsemis.miner.general.Algorithm)
	 */
	public Collection<Fragment<NodeType, EdgeType>> search(
			final Algorithm<NodeType, EdgeType> algo) {
		final StackList<NodeType, EdgeType> sl = new RemoteStackList<NodeType, EdgeType>();
		final long tmp2 = System.currentTimeMillis();
		if (VERBOSE) {
			err.print("create " + threads.length + " jobs (host " + Host.name()
					+ ")...");
		}
		final MiningStack<NodeType, EdgeType> mine = new LocalStack<NodeType, EdgeType>(
				0, sl, LocalEnvironment.env(this));
		for (final Iterator<SearchLatticeNode<NodeType, EdgeType>> it = algo
				.initialNodes(); it.hasNext();) {
			mine.push(it.next());
		}

		// initialize worker for the current thread
		final Collection<Fragment<NodeType, EdgeType>> ret = new ArrayList<Fragment<NodeType, EdgeType>>();
		final Worker<NodeType, EdgeType> me = new Worker<NodeType, EdgeType>(
				mine, ret, algo.getExtender(0), 0);

		// initialize worker for further threads
		for (int i = 1; i < answers.length; ++i) {
			answers[i] = /** @at 0 */
			new RemoteHashSet<Fragment<NodeType, EdgeType>>();

			threads[i] = /** @at i */
			new RemoteEnvironment<NodeType, EdgeType>(LocalEnvironment
					.env(this), i).createJPThread(algo, sl, answers[i]);
		}

		if (VERBOSE) {
			err.println("done (" + (System.currentTimeMillis() - tmp2)
					+ " ms)\n now starting jobs ");
		}
		if (INFO) {
			stats.distributedTime -= System.currentTimeMillis();
		}

		// start other workers
		for (int i = 1; i < threads.length; ++i) {
			threads[i].start();
		}

		// start local worker
		me.run();

		// wait for others
		for (int i = 1; i < threads.length; ++i) {
			try {
				threads[i].join();
			} catch (final InterruptedException ie) {
				if (WARN) {
					err.println(ie);
				}
			}
		}

		if (INFO) {
			stats.distributedTime += System.currentTimeMillis();
		}

		final long tmp = System.currentTimeMillis();
		if (VERBOSE) {
			System.out.print("merging... ");
		}

		// collect data
		for (int i = 1; i < answers.length; ++i) {
			ret.addAll(answers[i].getLocal());
		}

		if (VERBOSE) {
			System.out.println("done (" + (System.currentTimeMillis() - tmp)
					+ " ms)");
		}
		return ret;
	}

}
