/**
 * Created Jan 03, 2008
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

import static de.parsemis.miner.environment.Debug.INFO;
import static de.parsemis.miner.environment.Debug.VVERBOSE;
import static de.parsemis.miner.environment.Debug.out;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;

import de.parsemis.graph.Graph;
import de.parsemis.graph.GraphFactory;
import de.parsemis.graph.HPGraph;
import de.parsemis.jp.RemoteCounter;
import de.parsemis.miner.chain.DefaultExtender;
import de.parsemis.miner.chain.EdgeCountStep;
import de.parsemis.miner.chain.Extender;
import de.parsemis.miner.chain.FrequencyPruningStep;
import de.parsemis.miner.chain.MiningStep;
import de.parsemis.miner.chain.NodeCountStep;
import de.parsemis.miner.chain.SearchLatticeNode;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.environment.Settings;
import de.parsemis.miner.general.DataBase;
import de.parsemis.miner.general.Fragment;
import de.parsemis.utils.Generic;
import de.parsemis.utils.GraphSet;
import de.parsemis.utils.MutableInteger;

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
public class Algorithm<NodeType, EdgeType> implements
		de.parsemis.algorithms.Algorithm<NodeType, EdgeType>,
		Generic<NodeType, EdgeType>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	transient/* final */HashMap<NodeType, GastonPath<NodeType, EdgeType>> initials;

	private Collection<HPGraph<NodeType, EdgeType>> unique;

	public Extender<NodeType, EdgeType> getExtender(final int threadIdx) {
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		// GastonEnvironment<NodeType, EdgeType> tenv =
		// (GastonEnvironment<NodeType, EdgeType>) env
		// .getThreadEnv(threadIdx);
		final DefaultExtender<NodeType, EdgeType> extender = new DefaultExtender<NodeType, EdgeType>();

		// from last steps (filters after child computation) ...
		MiningStep<NodeType, EdgeType> curFirst = extender;
		// TODO: build mining chain
		curFirst = new GastonGeneration<NodeType, EdgeType>(curFirst);

		curFirst = new UniqueStep<NodeType, EdgeType>(curFirst, unique,
				env.stats);

		if (env.minNodeCount > 0 || env.maxNodeCount < Integer.MAX_VALUE) {
			curFirst = new NodeCountStep<NodeType, EdgeType>(curFirst,
					env.minNodeCount, env.maxNodeCount);
		}
		if (env.minEdgeCount > 0 || env.maxEdgeCount < Integer.MAX_VALUE) {
			curFirst = new EdgeCountStep<NodeType, EdgeType>(curFirst,
					env.minEdgeCount, env.maxEdgeCount);
		}
		curFirst = new FrequencyPruningStep<NodeType, EdgeType>(curFirst,
				env.minFreq, env.maxFreq);

		// insert mining chain
		extender.setFirst(curFirst);
		return extender;

	}

	public Collection<Fragment<NodeType, EdgeType>> initialize(
			final Collection<Graph<NodeType, EdgeType>> graphs,
			final GraphFactory<NodeType, EdgeType> factory,
			final Settings<NodeType, EdgeType> settings) {
		// generate local environment
		final DataBase<NodeType, EdgeType> db = new DataBase<NodeType, EdgeType>(
				graphs, settings);

		final SortedSet<NodeType> frequentNodes = db.frequentNodeLabels();
		final SortedSet<EdgeType> frequentEdges = db.frequentEdgeLabels();
		final ArrayList<NodeType> nodes = new ArrayList<NodeType>(frequentNodes);
		final GastonEnvironmentFactory<NodeType, EdgeType> envFac = new GastonEnvironmentFactory<NodeType, EdgeType>(
				frequentNodes.size(), frequentEdges.size(),
				settings.javaparty ? new RemoteCounter()
						: new MutableInteger(0));

		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.create(settings, graphs.size(), nodes,
						new ArrayList<EdgeType>(frequentEdges), envFac);
		final GastonEnvironment<NodeType, EdgeType> tenv = (GastonEnvironment<NodeType, EdgeType>) envFac
				.getNewEnvironment(0, env);

		if (INFO) {
			out.println("FREQUENT NODES:" + frequentNodes.size());
			out.println(frequentNodes);
			out.println("FREQUENT EDGES:" + frequentEdges.size());
			out.println(frequentEdges);
		}

		// create initial fragments
		initials = new HashMap<NodeType, GastonPath<NodeType, EdgeType>>();
		final Collection<Leg<NodeType, EdgeType>> siblings = new ArrayList<Leg<NodeType, EdgeType>>(
				frequentNodes.size());
		int i = 0;
		for (final Graph<NodeType, EdgeType> graph : graphs) {
			final GastonGraph<NodeType, EdgeType> dbg = new GastonGraph<NodeType, EdgeType>(
					graph.toHPGraph(), i, settings.getFrequency(graph));
			env.setDataBaseGraph(i++, dbg);
			dbg.createInitials(initials, nodes, siblings, tenv);
			if (graph.getMaxNodeIndex() > envFac.maxNodeIndex) {
				envFac.maxNodeIndex = graph.getMaxNodeIndex();
			}
		}
		if (!env.embeddingBased) {
			// filter graphBased infrequent initial nodes
			for (final Iterator<Map.Entry<NodeType, GastonPath<NodeType, EdgeType>>> eit = initials
					.entrySet().iterator(); eit.hasNext();) {
				final GastonNode<NodeType, EdgeType> code = eit.next()
						.getValue();
				if (settings.minFreq.compareTo(code.frequency()) > 0) {
					eit.remove();
					siblings.remove(code.getLeg());
				}

			}
		}

		if (VVERBOSE) {
			out.println("initial fragments:" + initials.size());
		}

		// create filter set for cyclic fragments
		unique = new GraphSet<NodeType, EdgeType>(env);

		final Collection<Fragment<NodeType, EdgeType>> expectedFragments = new HashSet<Fragment<NodeType, EdgeType>>();
		return expectedFragments;
	}

	public Iterator<SearchLatticeNode<NodeType, EdgeType>> initialNodes() {
		return new Iterator<SearchLatticeNode<NodeType, EdgeType>>() {
			Iterator<GastonPath<NodeType, EdgeType>> it = initials.values()
					.iterator();

			public boolean hasNext() {
				return it.hasNext();
			}

			public SearchLatticeNode<NodeType, EdgeType> next() {
				return it.next();
			}

			public void remove() {
				// throw new UnsupportedOperationException();
			}
		};
	}

}
