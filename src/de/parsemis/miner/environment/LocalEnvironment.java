/**
 * created May 19, 2006
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
package de.parsemis.miner.environment;

import static de.parsemis.miner.environment.Debug.VERBOSE;
import static de.parsemis.miner.environment.Debug.out;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import de.parsemis.graph.GraphFactory;
import de.parsemis.graph.HPMutableGraph;
import de.parsemis.graph.MutableGraph;
import de.parsemis.jp.RemoteGlobalEnvironment;
import de.parsemis.miner.chain.MiningStepFactory;
import de.parsemis.miner.filter.FragmentFilter;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.miner.general.Fragment;
import de.parsemis.miner.general.Frequency;
import de.parsemis.miner.general.IntFrequency;
import de.parsemis.parsers.GraphParser;
import de.parsemis.strategy.MiningStack;
import de.parsemis.utils.FileSerializeCollection;
import de.parsemis.utils.Generic;

/**
 * This class is for locally storing all final settings, the database and the
 * corresponding relabeling functions.
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
public class LocalEnvironment<NodeType, EdgeType> implements
		GraphEnvironment<NodeType, EdgeType>, Relabler<NodeType, EdgeType>,
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public transient static LocalEnvironment environ;

	private static long CLK_TICKS = 100;

	static {
		try {
			System.loadLibrary("java-time");
			CLK_TICKS = getClockTicks();
		} catch (final UnsatisfiedLinkError e) {
			if (VERBOSE) {
				System.err.println("ignored: " + e);
			}
		}
	}

	/**
	 * creates and sets the local environment
	 * 
	 * @param <NodeType>
	 * @param <EdgeType>
	 * @param settings
	 * @param nodeCount
	 * @param nodes
	 * @param edges
	 * @return the newly created environment
	 */
	public final static <NodeType, EdgeType> LocalEnvironment<NodeType, EdgeType> create(
			final Settings<NodeType, EdgeType> settings, final int nodeCount,
			final ArrayList<NodeType> nodes, final ArrayList<EdgeType> edges) {
		return create(settings, nodeCount, nodes, edges, null, null, null);
	}

	/**
	 * creates and sets the local environment
	 * 
	 * @param <NodeType>
	 * @param <EdgeType>
	 * @param settings
	 * @param nodeCount
	 * @param nodes
	 * @param edges
	 * @param nnil
	 * @param enil
	 * @param envFac
	 * @return the newly created environment
	 */
	public final static <NodeType, EdgeType> LocalEnvironment<NodeType, EdgeType> create(
			final Settings<NodeType, EdgeType> settings, final int nodeCount,
			final ArrayList<NodeType> nodes, final ArrayList<EdgeType> edges,
			final NodeType nnil, final EdgeType enil,
			final ThreadEnvironmentFactory<NodeType, EdgeType> envFac) {
		final LocalEnvironment<NodeType, EdgeType> n = new LocalEnvironment<NodeType, EdgeType>(
				settings, nodeCount, nodes, edges, nnil, enil, envFac);
		if (VERBOSE) {
			out.println(nodes);
		}
		if (VERBOSE) {
			out.println(edges);
		}
		environ = n;
		return n;
	}

	/**
	 * creates and sets the local environment
	 * 
	 * @param <NodeType>
	 * @param <EdgeType>
	 * @param settings
	 * @param nodeCount
	 * @param nodes
	 * @param edges
	 * @param envFac
	 * @return the newly created environment
	 */
	public final static <NodeType, EdgeType> LocalEnvironment<NodeType, EdgeType> create(
			final Settings<NodeType, EdgeType> settings, final int nodeCount,
			final ArrayList<NodeType> nodes, final ArrayList<EdgeType> edges,
			final ThreadEnvironmentFactory<NodeType, EdgeType> envFac) {
		return create(settings, nodeCount, nodes, edges, null, null, envFac);
	}

	/**
	 * @return the current CPU-time of the java process
	 */
	public static long currentCPUMillis() {
		try {
			return (getCPUtime() * 1000) / CLK_TICKS;
		} catch (final UnsatisfiedLinkError e) {
			return System.currentTimeMillis();
		}
	}

	/**
	 * @param <NodeType>
	 * @param <EdgeType>
	 * @param dummy
	 *            to determine generic types
	 * @return the current local environment
	 */
	@SuppressWarnings("unchecked")
	public final static <NodeType, EdgeType> LocalEnvironment<NodeType, EdgeType> env(
			final Generic<NodeType, EdgeType> dummy) {
		return environ;
	}

	private static native long getClockTicks();

	private static native long getCPUtime();

	/**
	 * sets the local environment to the given one used to initialize the local
	 * environments on remote machines of the JavaParty environment
	 * 
	 * @param env
	 * @param <NodeType>
	 *            the type of the node labels
	 * @param <EdgeType>
	 *            the type of the edge labels
	 * @return the newly set environment
	 */
	@SuppressWarnings("unchecked")
	public final static <NodeType, EdgeType> LocalEnvironment<NodeType, EdgeType> set(
			final LocalEnvironment<NodeType, EdgeType> env) {
		if (env.graphs == null) {
			env.graphs = new DataBaseGraph[env.graphCount];
		}
		if (env.threadEnvs == null) {
			env.threadEnvs = new ThreadEnvironment[env.threadCount];
		}
		if (env.stack == null) {
			env.stack = new MiningStack[env.threadCount];
		}
		LocalEnvironment.environ = env;
		return environ;
	}

	public final boolean embeddingBased;

	public final boolean connectedFragments;

	public final boolean storeEmbeddings;

	public final boolean storeHierarchicalEmbeddings;

	public final boolean findPathsOnly;

	public final boolean findTreesOnly;

	public final boolean shrink;

	public final boolean zaretsky;

	public final boolean singleRooted;

	public final boolean javaparty;

	public final boolean usePooling;

	public final boolean closeGraph;

	public final GraphParser<NodeType, EdgeType> parser;

	public final GraphParser<NodeType, EdgeType> serializer;

	public final String objectFileName;

	public final Frequency minFreq;

	public final Frequency maxFreq;

	public final int minNodeCount;

	public final int maxNodeCount;

	public final int minEdgeCount;

	public final int maxEdgeCount;

	public final NodeType nnil;

	public final EdgeType enil;

	public final Collection<NodeType> ignoreNodes;

	/** the minimal size of a stack to be splitable */
	public final int splitSize;

	/** the maximal depth of nodes transfered with a stack split */
	public final int maxSplitDepth;

	/** the maximal number of nodes transfered with a stack split */
	public final int maxSplitCount;

	public final GraphFactory<NodeType, EdgeType> factory;

	private final int graphCount;

	private transient DataBaseGraph<NodeType, EdgeType>[] graphs;

	private final ArrayList<NodeType> nodes;

	private final ArrayList<EdgeType> edges;

	public transient FragmentFilter<NodeType, EdgeType> filter = null;

	public MiningStepFactory<NodeType, EdgeType> miningFactory = null;

	private final GraphEnvironment<NodeType, EdgeType> global;

	public final Statistics stats;

	/** array of "all" stack on the local machine */
	transient public MiningStack<NodeType, EdgeType>[] stack;

	Collection<Fragment<NodeType, EdgeType>> returnSet = null;

	private final ThreadEnvironmentFactory<NodeType, EdgeType> tenvfac;

	private transient ThreadEnvironment<NodeType, EdgeType>[] threadEnvs;

	private final int threadCount;

	@SuppressWarnings("unchecked")
	private LocalEnvironment(final Settings<NodeType, EdgeType> settings,
			final int graphCount, final ArrayList<NodeType> nodes,
			final ArrayList<EdgeType> edges, final NodeType nnil,
			final EdgeType enil,
			final ThreadEnvironmentFactory<NodeType, EdgeType> envFac) {
		this.graphCount = graphCount;
		this.graphs = new DataBaseGraph[graphCount];
		this.nodes = nodes;
		this.edges = edges;
		this.embeddingBased = settings.embeddingBased;
		this.storeEmbeddings = settings.storeEmbeddings;
		this.storeHierarchicalEmbeddings = settings.storeHierarchicalEmbeddings;
		this.connectedFragments = settings.connectedFragments;
		this.closeGraph = settings.closeGraph;
		this.shrink = settings.shrink;
		this.zaretsky = settings.zaretsky;
		this.singleRooted = settings.singleRooted;
		this.javaparty = settings.javaparty;
		this.usePooling = settings.usePooling;
		this.factory = settings.factory;
		this.parser = settings.parser;
		this.serializer = settings.serializer;
		this.minFreq = settings.minFreq;
		this.maxFreq = settings.maxFreq;
		this.minNodeCount = settings.minNodes;
		this.maxNodeCount = settings.maxNodes;
		this.minEdgeCount = settings.minEdges;
		this.maxEdgeCount = settings.maxEdges;
		this.ignoreNodes = (nnil == null ? null : new HashSet<NodeType>());
		if (ignoreNodes != null) {
			ignoreNodes.add(nnil);
		}
		this.nnil = nnil;
		this.enil = enil;
		this.splitSize = settings.splitSize;
		this.maxSplitCount = settings.maxSplitCount;
		this.maxSplitDepth = settings.maxSplitDepth;
		this.tenvfac = envFac;
		this.objectFileName = settings.objectFileName;
		if (objectFileName != null) {
			returnSet = new FileSerializeCollection<Fragment<NodeType, EdgeType>>(
					objectFileName);
		}

		this.findPathsOnly = settings.pathsOnly;
		this.findTreesOnly = settings.treesOnly;

		this.threadCount = settings.threadCount;
		this.threadEnvs = new ThreadEnvironment[settings.threadCount];
		this.stack = new MiningStack[settings.threadCount];

		this.stats = settings.stats;
		this.miningFactory = settings.miningFactory;
		global = (javaparty ? /** @at 0 */
		new RemoteGlobalEnvironment() : null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.Relabler#getEdgeLabel(int)
	 */
	public EdgeType getEdgeLabel(final int idx) {
		return edges.get(idx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.Relabler#getEdgeLabelIndex(null)
	 */
	public int getEdgeLabelIndex(final EdgeType edge) {
		return edges.indexOf(edge);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.GraphEnvironment#getGraph(int)
	 */
	public DataBaseGraph<NodeType, EdgeType> getGraph(final int idx) {
		if (global != null && (graphs[idx] == null)) {
			graphs[idx] = global.getGraph(idx);
		}
		return graphs[idx];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.Relabler#getNodeLabel(int)
	 */
	public NodeType getNodeLabel(final int idx) {
		return nodes.get(idx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.Relabler#getNodeLabelIndex(null)
	 */
	public int getNodeLabelIndex(final NodeType node) {
		return nodes.indexOf(node);
	}

	public final Collection<Fragment<NodeType, EdgeType>> getReturnSet() {
		if (returnSet == null) {
			returnSet = new ArrayList<Fragment<NodeType, EdgeType>>();
		}
		return returnSet;
	}

	/**
	 * @param idx
	 * @return the ThreadEnvironment connected with the given index
	 */
	public ThreadEnvironment<NodeType, EdgeType> getThreadEnv(final int idx) {
			if (threadEnvs == null) {
		synchronized (this) {
			if (threadEnvs == null) {
				@SuppressWarnings("unchecked")
				final ThreadEnvironment<NodeType, EdgeType>[] t = new ThreadEnvironment[this.threadCount];
				threadEnvs = t;
			}
		}
			}
		synchronized (threadEnvs) {
			if (tenvfac == null) {
				return null;
			}
			if (threadEnvs[idx] == null) {
				final ThreadEnvironment<NodeType, EdgeType> tenv = tenvfac
						.getNewEnvironment(idx, this);
				threadEnvs[idx] = tenv;
				return tenv;
			} else {
				return threadEnvs[idx];
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.GraphEnvironment#graphCount()
	 */
	public int graphCount() {
		return graphCount;
	}

	/**
	 * @return a new frequency object
	 */
	public final Frequency newFrequency() {
		return new IntFrequency(0);
	}

	/**
	 * @return a new Graph according to the configured factory
	 */
	public final MutableGraph<NodeType, EdgeType> newGraph() {
		return factory.newGraph();
	}

	/**
	 * @return a new HPGraph according to the configured factory
	 */
	public final HPMutableGraph<NodeType, EdgeType> newHPGraph() {
		return (HPMutableGraph<NodeType, EdgeType>) (factory.newGraph()
				.toHPGraph());
	}

	/**
	 * insert a database graph at the given index
	 * 
	 * @param idx
	 * @param graph
	 */
	public final void setDataBaseGraph(final int idx,
			final DataBaseGraph<NodeType, EdgeType> graph) {
		graphs[idx] = graph;
	}

	/**
	 * adds a post mining filter
	 * 
	 * @param filter
	 * @return the previously set filter
	 */
	public FragmentFilter<NodeType, EdgeType> setFilter(
			final FragmentFilter<NodeType, EdgeType> filter) {
		final FragmentFilter<NodeType, EdgeType> ret = this.filter;
		this.filter = filter;
		return ret;
	}

	public final void setReturnSet(
			final Collection<Fragment<NodeType, EdgeType>> set) {
		returnSet = set;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LocalEnvironment: nodes=" + nodes + " edge=" + edges;
	}

}
