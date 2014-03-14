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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import de.parsemis.graph.Graph;
import de.parsemis.graph.GraphFactory;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.HPMutableGraph;
import de.parsemis.miner.chain.CanonicalPruningStep;
import de.parsemis.miner.chain.CompleteClosedStep;
import de.parsemis.miner.chain.EdgeCountStep;
import de.parsemis.miner.chain.EmbeddingBasedGenerationStep;
import de.parsemis.miner.chain.Extender;
import de.parsemis.miner.chain.FrequencyPruningStep;
import de.parsemis.miner.chain.GenerationPartialStep;
import de.parsemis.miner.chain.GenerationStep;
import de.parsemis.miner.chain.MiningStep;
import de.parsemis.miner.chain.NodeCountStep;
import de.parsemis.miner.chain.SearchLatticeNode;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.environment.Settings;
import de.parsemis.miner.filter.ClosedFilter;
import de.parsemis.miner.filter.GraphShrinkingFilter;
import de.parsemis.miner.filter.SingleRootedFilter;
import de.parsemis.miner.filter.ZaretskyFilter;
import de.parsemis.miner.general.DataBase;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.miner.general.Fragment;
import de.parsemis.utils.Generic;
import de.parsemis.utils.IntIterator;

/**
 * Creates a mining chain according to the gSpan algorithm, extended by
 * different options.
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
public class Algorithm<NodeType, EdgeType> implements
		de.parsemis.algorithms.Algorithm<NodeType, EdgeType>,
		Generic<NodeType, EdgeType> {

	/**
	 * Inner class to iterate over the initial edges
	 * 
	 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
	 * 
	 */
	private class MyIterator implements
			Iterator<SearchLatticeNode<NodeType, EdgeType>> {
		final Iterator<Map.Entry<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>>> entryit;

		final boolean del;

		Map.Entry<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> last = null;

		MyIterator(
				final Map<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> initials,
				final boolean del) {
			entryit = initials.entrySet().iterator();
			this.del = del;
		}

		public boolean hasNext() {
			return entryit.hasNext();
		}

		public SearchLatticeNode<NodeType, EdgeType> next() {
			last = entryit.next();
			return last.getValue();
		}

		public void remove() {
			if (del) {
				removeEdge(last.getValue());
			}
			entryit.remove();
		}
	}

	/**
	 * This class represents a Label that is used for the pseudo node required
	 * for unconnected fragemtn search in gSpan
	 * 
	 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
	 * 
	 */
	static class PseudoLabel implements Serializable {
		/**	 */
		private static final long serialVersionUID = -4215112903761599420L;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object o) {
			return (o instanceof PseudoLabel);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "pseudo";
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean keep;

	private transient/* final */Map<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> initials;;

	/**
	 * generates a new (GSpan) algorithm
	 */
	public Algorithm() {
		initials = new TreeMap<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Algorithm#getExtender(int)
	 */
	public Extender<NodeType, EdgeType> getExtender(final int threadIdx) {
		// configure mining chain
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		final GThreadEnvironment<NodeType, EdgeType> tenv = (GThreadEnvironment<NodeType, EdgeType>) env
				.getThreadEnv(threadIdx);
		final GSpanExtender<NodeType, EdgeType> extender = new GSpanExtender<NodeType, EdgeType>(
				tenv);
		// from last steps (filters after child computation) ...
		MiningStep<NodeType, EdgeType> curFirst = extender;
		GenerationStep<NodeType, EdgeType> gen;
		if (env.shrink) {
			final GraphShrinkingFilter<NodeType, EdgeType> gs = new GraphShrinkingFilter<NodeType, EdgeType>(
					curFirst);
			env.setFilter(gs);
			curFirst = gs;
		}
		if (env.zaretsky) {
			env.setFilter(new ZaretskyFilter<NodeType, EdgeType>());
		}
		if (env.closeGraph && env.embeddingBased) {
			env.setFilter(new ClosedFilter<NodeType, EdgeType>());
		}

		if (env.embeddingBased) {
			// ... over generation ...
			curFirst = gen = new EmbeddingBasedGenerationStep<NodeType, EdgeType>(
					curFirst);
			// .. to prefilters
			if (env.singleRooted) {
				curFirst = new SingleRootedFilter<NodeType, EdgeType>(curFirst);
			}
			if (!env.connectedFragments) {
				curFirst = new CompleteGlueNodeStep<NodeType, EdgeType>(
						curFirst);
			}
			curFirst = new FrequencyPruningStep<NodeType, EdgeType>(curFirst,
					env.minFreq, env.maxFreq);
			curFirst = new CanonicalPruningStep<NodeType, EdgeType>(curFirst);
			if (env.minNodeCount > 0 || env.maxNodeCount < Integer.MAX_VALUE) {
				curFirst = new NodeCountStep<NodeType, EdgeType>(curFirst,
						env.minNodeCount, env.maxNodeCount);
			}
			if (env.minEdgeCount > 0 || env.maxEdgeCount < Integer.MAX_VALUE) {
				curFirst = new EdgeCountStep<NodeType, EdgeType>(curFirst,
						env.minEdgeCount, env.maxEdgeCount);
			}
		} else {
			// ... over generation ...
			if (env.storeEmbeddings) {
				curFirst = gen = new EmbeddingBasedGenerationStep<NodeType, EdgeType>(
						curFirst);
			} else {
				curFirst = gen = new GSpanGeneration<NodeType, EdgeType>(
						curFirst, tenv);
			}
			// .. to prefilters
			if (env.singleRooted) {
				curFirst = new SingleRootedFilter<NodeType, EdgeType>(curFirst);
			}
			if (!env.connectedFragments) {
				curFirst = new CompleteGlueNodeStep<NodeType, EdgeType>(
						curFirst);
			}
			curFirst = new CanonicalPruningStep<NodeType, EdgeType>(curFirst);
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
		}

		// build generation chain
		GenerationPartialStep<NodeType, EdgeType> generationFirst = gen
				.getLast();
		if (env.miningFactory != null) {
			try {
				final GenerationPartialStep<NodeType, EdgeType> tmp = env.miningFactory
						.createGenerationPartialStep(generationFirst);
				generationFirst = tmp;
			} catch (final UnsupportedOperationException uo) {
			}
		}
		if (env.closeGraph && !env.embeddingBased) {
			generationFirst = new CompleteClosedStep<NodeType, EdgeType>(
					new GSpanBridgePruning<NodeType, EdgeType>(generationFirst,
							tenv));
			keep = true;
		}
		if (env.connectedFragments) {
			generationFirst = new RightMostExtension<NodeType, EdgeType>(
					generationFirst, tenv);
		} else {
			generationFirst = new UnconnectedExtension<NodeType, EdgeType>(
					generationFirst, tenv);
		}
		// insert generation chain
		gen.setFirst(generationFirst);

		// insert mining chain
		extender.setFirst(curFirst);
		return extender;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Algorithm#initialize(java.util.Collection,
	 *      de.parsemis.graph.GraphFactory, de.parsemis.Settings)
	 */
	@SuppressWarnings("unchecked")
	public Collection<Fragment<NodeType, EdgeType>> initialize(
			final Collection<Graph<NodeType, EdgeType>> graphs,
			final GraphFactory<NodeType, EdgeType> factory,
			final Settings<NodeType, EdgeType> settings) {
		final DataBase<NodeType, EdgeType> db = new DataBase<NodeType, EdgeType>(
				graphs, settings);
		initials.clear();

		// create label lists for unique numbering
		final ArrayList<NodeType> nodes = new ArrayList<NodeType>();
		final ArrayList<EdgeType> edges = new ArrayList<EdgeType>();
		NodeType nnil = settings.nnil;
		EdgeType enil = settings.enil;
		if (!settings.connectedFragments) {
			settings.minNodes++;
			if (nnil == null) {
				nnil = (NodeType) new PseudoLabel();
			}
			nodes.add(nnil);
			if (enil == null) {
				enil = (EdgeType) new PseudoLabel();
			}
			edges.add(enil);
		}
		nodes.addAll(db.frequentNodeLabels());
		edges.addAll(db.frequentEdgeLabels());

		// store settings in local environment
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.create(settings, graphs.size(), nodes, edges, nnil, enil,
						new GThreadEnvFactory<NodeType, EdgeType>());

		// generate GSpanGraphs and initial edges
		int i = 0;
		for (final Graph<NodeType, EdgeType> graph : graphs) {
			final GSpanGraph<NodeType, EdgeType> gg = new GSpanGraph<NodeType, EdgeType>(
					graph, i, settings.getFrequency(graph));
			env.setDataBaseGraph(i, gg);
			++i;
			gg.createInitials(initials);
		}

		// filter infrequent edges
		for (final Iterator<Map.Entry<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>>> eit = initials
				.entrySet().iterator(); eit.hasNext();) {
			final DFSCode<NodeType, EdgeType> code = eit.next().getValue();
			if (settings.minFreq.compareTo(code.frequency()) > 0) {
				removeEdge(code);
				eit.remove();
			}
		}

		final GThreadEnvironment<NodeType, EdgeType> tenv = (GThreadEnvironment<NodeType, EdgeType>) env
				.getThreadEnv(0);
		final Map<NodeType, Fragment<NodeType, EdgeType>> nodeFragments = new HashMap<NodeType, Fragment<NodeType, EdgeType>>();
		if (env.connectedFragments && env.minNodeCount <= 1) {
			// add single noded graphs
			for (final NodeType label : nodes) {
				final HPMutableGraph<NodeType, EdgeType> g = env.newHPGraph();
				g.addNodeIndex(label);
				nodeFragments.put(label, tenv.getHPFragment(g).toFragment());
			}
			// calculate frequency of single noded graphs
			for (int j = 0; j < env.graphCount(); j++) {
				final GSpanGraph<NodeType, EdgeType> graph = (GSpanGraph<NodeType, EdgeType>) env
						.getGraph(j);
				final HPGraph<NodeType, EdgeType> hpgraph = graph.toHPGraph();
				for (final IntIterator nit = hpgraph.nodeIndexIterator(); nit
						.hasNext();) {
					final int node = nit.next();
					final NodeType label = hpgraph.getNodeLabel(node);
					final Fragment<NodeType, EdgeType> frag = nodeFragments
							.get(label);
					if (frag != null) {
						final HPMutableGraph<NodeType, EdgeType> g = env
								.newHPGraph();
						g.addNodeIndex(label);
						if (env.storeEmbeddings) {
							frag
									.toHPFragment()
									.add(
											new GSpanEmbedding<NodeType, EdgeType>(
													tenv).set(g.toGraph(),
													graph, new int[] { node })
													.toHPEmbedding());
						} else {
							frag.toHPFragment().add(graph);
						}
					}
				}
			}
			// remove infrequent single noded graphs
			for (final Iterator<Map.Entry<NodeType, Fragment<NodeType, EdgeType>>> eit = nodeFragments
					.entrySet().iterator(); eit.hasNext();) {
				final Map.Entry<NodeType, Fragment<NodeType, EdgeType>> entry = eit
						.next();
				if (settings.minFreq.compareTo(entry.getValue().frequency()) > 0) {
					eit.remove();
				}
			}
			if (env.closeGraph) {
				// remove single noded graphs, with equal frequent supergraphs
				// (= initial edges)
				for (final DFSCode<NodeType, EdgeType> code : initials.values()) {
					final NodeType ta = env.getNodeLabel(code.getFirst()
							.getLabelA());
					final Fragment<NodeType, EdgeType> na = nodeFragments
							.get(ta);
					if (na != null
							&& na.frequency().compareTo(code.frequency()) == 0) {
						nodeFragments.remove(ta);
					}
					final NodeType tb = env.getNodeLabel(code.getFirst()
							.getLabelB());
					final Fragment<NodeType, EdgeType> nb = nodeFragments
							.get(tb);
					if (nb != null
							&& nb.frequency().compareTo(code.frequency()) == 0) {
						nodeFragments.remove(tb);
					}
				}
			}
		}
		// return frequent single noded fragments as expected fragments
		return nodeFragments.values();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Algorithm#initialNodes()
	 */
	public Iterator<SearchLatticeNode<NodeType, EdgeType>> initialNodes() {
		return new MyIterator(initials, !keep);
	}

	/**
	 * remove the initial edge of the given dfs <code>code</code> from all
	 * database graphs
	 * 
	 * @param code
	 * @return <code>true</code> if the edges are removed
	 */
	boolean removeEdge(final DFSCode<NodeType, EdgeType> code) {
		// if (LocalEnvironment.env(this).storeEmbeddings) return false;
		for (final Iterator<DataBaseGraph<NodeType, EdgeType>> it = code
				.toHPFragment().graphIterator(); it.hasNext();) {
			final GSpanGraph<NodeType, EdgeType> gg = (GSpanGraph<NodeType, EdgeType>) it
					.next();
			gg.removeAllOccurences(code.getFirst());
		}
		return true;
	}

}
