/**
 * created May 15, 2006
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

import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

import de.parsemis.algorithms.Algorithm;
import de.parsemis.chemical.Atom;
import de.parsemis.chemical.Bond;
import de.parsemis.graph.Graph;
import de.parsemis.graph.GraphFactory;
import de.parsemis.graph.HPListGraph;
import de.parsemis.jp.JavaPartyStrategy;
import de.parsemis.miner.chain.MiningStepFactory;
import de.parsemis.miner.general.Frequency;
import de.parsemis.miner.general.IntFrequency;
import de.parsemis.parsers.CccGraphParser;
import de.parsemis.parsers.DotGraphParser;
import de.parsemis.parsers.GraphParser;
import de.parsemis.parsers.GraphmlParser;
import de.parsemis.parsers.LabelParser;
import de.parsemis.parsers.LineGraphParser;
import de.parsemis.parsers.MXMLParser;
import de.parsemis.parsers.MapLabelParser;
import de.parsemis.parsers.SimpleDirectedGraphParser;
import de.parsemis.parsers.SimpleUndirectedGraphParser;
import de.parsemis.parsers.SmilesParser;
import de.parsemis.parsers.StringLabelParser;
import de.parsemis.parsers.SubdueParser;
import de.parsemis.strategy.BFSStrategy;
import de.parsemis.strategy.RecursiveStrategy;
import de.parsemis.strategy.Strategy;
import de.parsemis.strategy.ThreadedDFSStrategy;

/**
 * This class is for parsing and initialising the console parameters and mining
 * settings
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
public final class Settings<NodeType, EdgeType> {
	private final static String ALGORITHMS = "gspan|gaston|dagma"

	;

	@SuppressWarnings("unchecked")
	private final static <NodeType, EdgeType> Settings<NodeType, EdgeType> create(
			final GraphParser<NodeType, EdgeType> parser, final Settings set) {

		if (parser == null) {
			throw new RuntimeException(
					"No parser class given and file format unknown");
		}
		if (set.serializer == null) {
			set.serializer = parser;
		}
		if (set.minFreq == null && set.minProzent == 0) {
			throw new RuntimeException("No minimal frequency is given");
		}

		// set.factory=new
		// ListGraph.Factory<NodeType,EdgeType>(parser.getNodeParser(),parser.getEdgeParser());
		set.factory = new HPListGraph.Factory<NodeType, EdgeType>(parser
				.getNodeParser(), parser.getEdgeParser());
		if (set.algorithm == null) {
			set.algorithm = new de.parsemis.algorithms.gSpan.Algorithm<NodeType, EdgeType>();
		}
		if (set.distributionScheme.equals("threads")) {
			set.strategy = new ThreadedDFSStrategy(set.threadCount, set.stats);
			set.usePooling = false;
		} else if (set.distributionScheme.equals("threads_pooling")) {
			set.strategy = new ThreadedDFSStrategy(set.threadCount, set.stats);
			set.usePooling = true;
		} else if (set.distributionScheme.equals("javaparty")) {
			set.strategy = new JavaPartyStrategy(set.threadCount, set.stats);
			set.javaparty = true;
		} else if (set.distributionScheme.equals("local")) {
			set.strategy = new RecursiveStrategy();
		} else if (set.distributionScheme.equals("bfs")) {
			set.strategy = new BFSStrategy();
		} else if (set.distributionScheme.equals("visualisation")) {
			set.strategy = new RecursiveStrategy();
		} else {
			throw new RuntimeException("Unknown distribution scheme "
					+ set.distributionScheme);
		}

		// clone for setting generic Parameters
		return new Settings<NodeType, EdgeType>(set);

	}

	/**
	 * 
	 * @param args
	 * @return the (untyped) settings representing the given parameters, or
	 *         <code>null</code> if help is wished
	 */
	@SuppressWarnings("unchecked")
	public final static Settings parse(final String[] args) {
		return parse(args, null, null, null, null, null);
	}

	/**
	 * 
	 * @param args
	 * @param parser
	 * @return the settings representing the given parameters, or
	 *         <code>null</code> if help is wished
	 * @param <NodeType>
	 * @param <EdgeType>
	 */
	public final static <NodeType, EdgeType> Settings<NodeType, EdgeType> parse(
			final String[] args, final GraphParser<NodeType, EdgeType> parser) {
		return parse(args, parser, null, null, null, null);
	}

	/**
	 * 
	 * @param args
	 * @param parser
	 * @param serializer
	 * @return the settings representing the given parameters, or
	 *         <code>null</code> if help is wished
	 * @param <NodeType>
	 * @param <EdgeType>
	 */
	public final static <NodeType, EdgeType> Settings<NodeType, EdgeType> parse(
			final String[] args, final GraphParser<NodeType, EdgeType> parser,
			final GraphParser<NodeType, EdgeType> serializer) {
		return parse(args, parser, null, null, null, null);
	}

	public final static <NodeType, EdgeType> Settings<NodeType, EdgeType> parse(
			final String[] args, final GraphParser<NodeType, EdgeType> parser,
			final GraphParser<NodeType, EdgeType> serializer,
			final NodeType nnil, final EdgeType enil,
			final Collection<Graph<NodeType, EdgeType>> use) {
		final Settings<NodeType, EdgeType> set = new Settings<NodeType, EdgeType>();
		set.nnil = nnil;
		set.enil = enil;
		set.parser = parser;
		set.serializer = serializer;
		set.graphs = use;
		if (args.length == 0) {
			return null;
		}
		for (int i = 0; i < args.length; i++) {
			if (!parseOption(args[i], set)) {
				return null;
			}
		}

		return create(set.parser, set);
	}

	/**
	 * 
	 * @param args
	 * @param use
	 * @return the (untyped) settings representing the given parameters, or
	 *         <code>null</code> if help is wished
	 * @param <NodeType>
	 * @param <EdgeType>
	 */
	public final static <NodeType, EdgeType> Settings<NodeType, EdgeType> parse2(
			final String[] args, final Collection<Graph<NodeType, EdgeType>> use) {
		return parse(args, null, null, null, null, use);
	}

	@SuppressWarnings("unchecked")
	public final static <NodeType, EdgeType> GraphParser<NodeType, EdgeType> parseFileName(
			final String name, final LabelParser<NodeType> np,
			final LabelParser<EdgeType> ep) {
		if (name.endsWith(".dot") || name.endsWith(".dot.gz")) {
			if (np == null || ep == null) {
				return (GraphParser<NodeType, EdgeType>) DotGraphParser
						.instance();
			} else {
				return new DotGraphParser<NodeType, EdgeType>(np, ep);
			}
		} else if (name.endsWith(".graphml") || name.endsWith(".graphml.gz")
				|| name.endsWith(".gml") || name.endsWith(".gml.gz")) {
			if (np == null || ep == null) {
				return (GraphParser<NodeType, EdgeType>) GraphmlParser
						.instance();
			} else {
				return new GraphmlParser<NodeType, EdgeType>(np, ep);
			}
		} else if (name.endsWith(".mxml") || name.endsWith(".mxml.gz")) {
			if (np == null || ep == null) {
				return (GraphParser<NodeType, EdgeType>) new MXMLParser<Map<String, String>, String>(
						new MapLabelParser(), new StringLabelParser());
				// return MXMLParser.instance();
			} else {
				return new MXMLParser<NodeType, EdgeType>(np, ep);
			}
		} else if (name.endsWith(".shrink") || name.endsWith(".shrink.gz")) {
			if (np == null || ep == null) {
				return (GraphParser<NodeType, EdgeType>) new GraphmlParser<Map<String, String>, String>(
						new MapLabelParser(), new StringLabelParser());
			} else {
				return new GraphmlParser<NodeType, EdgeType>(np, ep);
			}
		} else if (name.endsWith(".cug") || name.endsWith(".cug.gz")) {
			if (np == null || ep == null) {
				return (GraphParser<NodeType, EdgeType>) new SimpleUndirectedGraphParser<Atom, Bond>(
						Atom.parser(), Bond.parser());
			} else {
				return new SimpleUndirectedGraphParser<NodeType, EdgeType>(np,
						ep);
			}
		} else if (name.endsWith(".sug") || name.endsWith(".sug.gz")) {
			if (np == null || ep == null) {
				return (GraphParser<NodeType, EdgeType>) SimpleUndirectedGraphParser
						.instance();
			} else {
				return new SimpleUndirectedGraphParser<NodeType, EdgeType>(np,
						ep);
			}
		} else if (name.endsWith(".sdg") || name.endsWith(".sdg.gz")) {
			if (np == null || ep == null) {
				return (GraphParser<NodeType, EdgeType>) SimpleDirectedGraphParser
						.instance();
			} else {
				return new SimpleDirectedGraphParser<NodeType, EdgeType>(np, ep);
			}
		} else if (name.endsWith(".edg") || name.endsWith(".edg.gz")) {
			if (np == null || ep == null) {
				return (GraphParser<NodeType, EdgeType>) new SimpleDirectedGraphParser<String, String>(
						new StringLabelParser(), new StringLabelParser());
			} else {
				return new SimpleDirectedGraphParser<NodeType, EdgeType>(np, ep);
			}
		} else if (name.endsWith(".eug") || name.endsWith(".eug.gz")) {
			if (np == null || ep == null) {
				return (GraphParser<NodeType, EdgeType>) new SimpleUndirectedGraphParser<String, String>(
						new StringLabelParser(), new StringLabelParser());
			} else {
				return new SimpleUndirectedGraphParser<NodeType, EdgeType>(np,
						ep);
			}
		} else if (name.endsWith(".g") || name.endsWith(".g.gz")) {
			if (np == null || ep == null) {
				return (GraphParser<NodeType, EdgeType>) SubdueParser
						.instance();
			} else {
				return new SubdueParser<NodeType, EdgeType>(np, ep);
			}
		} else if (name.endsWith(".ccc") || name.endsWith(".ccc.gz")) {
			if (np == null || ep == null) {
				return (GraphParser<NodeType, EdgeType>) CccGraphParser
						.instance();
			} else {
				return new CccGraphParser<NodeType, EdgeType>(np, ep);
			}
		} else if (name.endsWith(".smiles") || name.endsWith(".smiles.gz")) {
			return (GraphParser<NodeType, EdgeType>) new SmilesParser();
		} else if (name.endsWith(".lg") || name.endsWith(".lg.gz")) {
			if (np == null || ep == null) {
				return (GraphParser<NodeType, EdgeType>) new LineGraphParser<String, String>(
						new StringLabelParser(), new StringLabelParser());
			} else {
				return new LineGraphParser<NodeType, EdgeType>(np, ep);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private final static <NodeType, EdgeType> boolean parseOption(
			final String option, final Settings set) {
		final int index = option.indexOf('=');
		String[] temp = null;
		if (index >= 0) {
			final String[] tmp = { option.substring(0, index),
					option.substring(index + 1) };
			temp = tmp;
		} else {
			final String[] tmp = { option };
			temp = tmp;
		}

		if (temp[0].equals("--help")) {
			return false;

			// files
		} else if (temp[0].equals("--graphFile")) { // done
			set.inputFileName = temp[1];
			if (set.parser == null) {
				set.parser = parseFileName(set.inputFileName,
						(set.serializer == null ? null : set.serializer
								.getNodeParser()),
						(set.serializer == null ? null : set.serializer
								.getEdgeParser()));
			}
		} else if (temp[0].equals("--outputFile")) {// done
			set.outputFileName = temp[1];
			if (set.serializer == null) {
				set.serializer = parseFileName(
						set.outputFileName,
						(set.parser == null ? null : set.parser.getNodeParser()),
						(set.parser == null ? null : set.parser.getEdgeParser()));
			}
		} else if (temp[0].equals("--swapFile")) { // done
			set.objectFileName = temp[1];

			// constraints
		} else if (temp[0].equals("--minimumFrequency")) { // done
			final String[] freqs = temp[1].split(",");
			if (freqs.length == 1) {
				if (freqs[0].endsWith("%")) {
					set.minProzent = Float.parseFloat(freqs[0].substring(0,
							freqs[0].indexOf("%"))) / 100;
				} else {
					set.minFreq = new IntFrequency(Integer.parseInt(freqs[0]));
				}
			} else {
				throw new IllegalArgumentException(
						"Currently just single frequency are available");
			}
		} else if (temp[0].equals("--maximumFrequency")) { // done
			final String[] freqs = temp[1].split(",");
			if (freqs.length == 1) {
				if (freqs[0].endsWith("%")) {
					set.maxProzent = Float.parseFloat(freqs[0].substring(0,
							freqs[0].indexOf("%"))) / 100;
				} else {
					set.maxFreq = new IntFrequency(Integer.parseInt(freqs[0]));
				}
			} else {
				throw new IllegalArgumentException(
						"Currently just single frequency are available");
			}
		} else if (temp[0].equals("--minimumEdgeCount")) { // done
			set.minEdges = Integer.parseInt(temp[1]);
		} else if (temp[0].equals("--maximumEdgeCount")) { // done
			set.maxEdges = Integer.parseInt(temp[1]);
		} else if (temp[0].equals("--minimumNodeCount")) { // done
			set.minNodes = Integer.parseInt(temp[1]);
		} else if (temp[0].equals("--maximumNodeCount")) { // done
			set.maxNodes = Integer.parseInt(temp[1]);
		} else if (temp[0].equals("--findPathsOnly")) { // done
			set.pathsOnly = temp.length == 1
					|| Boolean.valueOf(temp[1]).booleanValue();
		} else if (temp[0].equals("--findTreesOnly")) { // done
			set.treesOnly = temp.length == 1
					|| Boolean.valueOf(temp[1]).booleanValue();
		} else if (temp[0].equals("--singleRooted")) { // done
			set.singleRooted = temp.length == 1
					|| Boolean.valueOf(temp[1]).booleanValue();
		} else if (temp[0].equals("--connectedFragments")) { // done
			set.connectedFragments = temp.length == 1
					|| Boolean.valueOf(temp[1]).booleanValue();

			// embedding
		} else if (temp[0].equals("--storeEmbeddings")) { // done
			set.storeEmbeddings = temp.length == 1
					|| Boolean.valueOf(temp[1]).booleanValue();
		} else if (temp[0].equals("--storeHierarchicalEmbeddings")) { // done
			set.storeHierarchicalEmbeddings = temp.length == 1
					|| Boolean.valueOf(temp[1]).booleanValue();
		} else if (temp[0].equals("--embeddingBased")) { // done
			set.embeddingBased = temp.length == 1
					|| Boolean.valueOf(temp[1]).booleanValue();

			// algorithm
		} else if (temp[0].equals("--algorithm")) { // done
			if (temp[1].equals("gspan")) {
				set.algorithm = new de.parsemis.algorithms.gSpan.Algorithm<NodeType, EdgeType>();
			} else if (temp[1].equals("gaston")) {
				set.algorithm = new de.parsemis.algorithms.gaston.Algorithm<NodeType, EdgeType>();
			} else if (temp[1].equals("dagma")) {
				set.algorithm = new de.parsemis.algorithms.dagminer.Algorithm<NodeType, EdgeType>();

			} else {
				throw new IllegalArgumentException(
						"currently supported algorithms are " + ALGORITHMS);
			}
		} else if (temp[0].equals("--zaretsky")) { // done
			set.zaretsky = temp.length == 1
					|| Boolean.valueOf(temp[1]).booleanValue();
		} else if (temp[0].equals("--subdue")) { // done
			set.shrink = temp.length == 1
					|| Boolean.valueOf(temp[1]).booleanValue();
		} else if (temp[0].equals("--closeGraph")) { // done
			set.closeGraph = temp.length == 1
					|| Boolean.valueOf(temp[1]).booleanValue();

			// parallel
		} else if (temp[0].equals("--threads")) { // done
			set.threadCount = Integer.parseInt(temp[1]);
		} else if (temp[0].equals("--distribution")) { // done
			set.distributionScheme = temp[1];

			// debug
		} else if (temp[0].equals("--debug")) {
			System.err
					.println("--debug is deprecated\nuse java option -D(quiet|info|[v][v]verbose) instead");
		} else if (temp[0].equals("--memoryStatistics")) {
			set.memoryStatistics = temp.length == 1
					|| Boolean.valueOf(temp[1]).booleanValue();

		} else if (temp[0].equals("--naturalOrdered")) {
			set.naturalOrderedNodeLabels = temp.length == 1
					|| temp[1].equals("nodes") || temp[1].equals("both");
			set.naturalOrderedEdgeLabels = temp.length == 1
					|| temp[1].equals("edges") || temp[1].equals("both");

		} else if (temp[0].equals("--reverseOrdered")) {
			set.reverseOrderedNodeLabels = temp.length == 1
					|| temp[1].equals("nodes") || temp[1].equals("both");
			set.reverseOrderedEdgeLabels = temp.length == 1
					|| temp[1].equals("edges") || temp[1].equals("both");
		} else {
			throw new IllegalArgumentException("Unknown option '" + temp[0]
					+ "'");
		}
		return true;
	}

	/**
	 * prints to the given PrintStream the usage informations
	 * 
	 * @param out
	 */
	public static void printUsage(final PrintStream out) {
		out.println("General options:");
		out.println("\t--graphFile=file");
		out.println("\t\tThe file from which the graphs should be read");
		out.println("\t--outputFile=file (optional)");
		out
				.println("\t\tThe file to which the found frequent subgraphs should be written (\'-\' for stdout)");
		out.println("\t--swapFile=file (optional)");
		out
				.println("\t\tA file to temporarly to swap out temporary unused objects");

		out.println("");
		out.println("\t--minimumFrequency=freq (integer or percentage)");
		out
				.println("\t\tThe minimum frequency a fragment must have to get reported");
		out
				.println("\t--maximumFrequency=freq (integer or percentage) (optional)");
		out
				.println("\t\tThe maximum frequency a fragment can have to get reported");
		out.println("\t--minimumNodeCount=int (optional; default: 0)");
		out
				.println("\t\tThe minimum size in nodes a fragment must have to get reported");
		out.println("\t--maximumNodeCount=int (optional; default: 0 = all)");
		out
				.println("\t\tThe maximum size in nodes a fragment can have to get reported");
		out.println("\t--minimumEdgeCount=int (optional; default: 0)");
		out
				.println("\t\tThe minimum size in edges a fragment must have to get reported");
		out.println("\t--maximumEdgeCount=int (optional; default: 0 = all)");
		out
				.println("\t\tThe maximum size in edges a fragment can have to get reported");

		out.println("");
		out.println("\t--findPathsOnly=true|false (optional; default: false)");
		out
				.println("\t\tSpecifies that only simple paths should be found (and no trees or arbitrary graphs)");
		out.println("\t--findTreesOnly=true|false (optional; default: false)");
		out
				.println("\t\tSpecifies that only (undirected) trees (graphs without cycles) should be found");
		out.println("\t--singleRooted=true|false (optional; default: false)");
		out
				.println("\t\tSpecifies for directed graph that only single rooted ones should be found");
		out
				.println("\t--connectedFragments=true|false (optional; default: true)");
		out
				.println("\t\tSpecifies that only connected fragments should be found");

		out.println("");
		out
				.println("\t--storeEmbeddings=true|false (optional; default: false)");
		out
				.println("\t\tSpecifies that for each fragment all embeddings should be stored");
		out
				.println("\t--storeHierarchicalEmbeddings=true|false (optional; default: false)");
		out
				.println("\t\tSpecifies that for each fragment all embeddings should be stored as a hierarchical structur");
		out.println("\t--embeddingBased=true|false (optional; default: false)");
		out
				.println("\t\tSpecifies that the frequency should be calculated embedding based or graph based");

		out.println("");
		out.println("\t--algorithm=" + ALGORITHMS
				+ " (optional; default: gspan)");
		out.println("\t\tSpecifies the mining algorithm to be used");
		out.println("\t--closeGraph=true|false (optional; default: false)");
		out
				.println("\t\tActivates fast closed mining as described for CloseGraph");
		out.println("\t--subdue=true|false (optional; default: false)");
		out.println("\t\tSpecifies fragment filtering as used in SubDue");
		out.println("\t--zaretsky=true|false (optional; default: false)");
		out
				.println("\t\tSpecifies fragment filtering to detect fragments as the algorithm of zaretsky");

		out.println("");
		out.println("Parallel options:");
		out
				.println("\t--distribution=local|threads|threads_np (optional; default: local)");
		out.println("\t\tThe scheme for distribution");
		out
				.println("\t\tlocal     : no distribution, whole serach is done in master thread");
		out.println("\t\tthreads   : distribution by local threads");
		out
				.println("\t\tthreads_np: distribution by local threads without pooling temporal objects");
		out.println("\t--threads=int (optional; default: 1)");
		out.println("\t\tThe number of working threads to be used");

		out.println("");
		out.println("Debug options:");
		out
				.println("\t--memoryStatistics=true|false (optional; default: false)");
		out
				.println("\t\tStarts debug thread for memory measurement (takes much time)");
		out.println("\t--visualize=true|false (optional; default: false)");
		out.println("\t\tRenders the database graphs");
		out
				.println("\t--naturalOrdered=none|edges|nodes|boths (optional; default: none)");
		out
				.println("\t\tDecides if node-/edge labels are order naturally or because of its frequency");
		out
				.println("\t--reverseOrdered=none|edges|nodes|boths (optional; default: none)");
		out.println("\t\tDecides if node-/edge labels order shall be inverted");
		out.println("\tJava option -D(quiet|info|[v][v]verbose)");
		out.println("\t\tFor no, informational, or verbose (debug) messages");
	}

	public Frequency minFreq;

	public Frequency maxFreq;

	public float maxProzent = -1;

	public float minProzent;

	public int maxNodes = Integer.MAX_VALUE;

	public int minNodes = 0;

	public int maxEdges = Integer.MAX_VALUE;

	public int minEdges = 0;

	public int threadCount = 1;

	/** the minimal size of a stack to be splitable */
	public int splitSize = 4;

	/** the maximal depth of nodes transfered with a stack split */
	public int maxSplitDepth = Integer.MAX_VALUE;

	/** the maximal number of nodes transfered with a stack split */
	public int maxSplitCount = Integer.MAX_VALUE;

	public boolean embeddingBased = false;

	public boolean pathsOnly = false;

	public boolean treesOnly = false;

	public boolean storeEmbeddings = false;

	public boolean storeHierarchicalEmbeddings = false;

	public boolean connectedFragments = true;

	public boolean memoryStatistics = false;

	public boolean shrink = false;

	public boolean zaretsky = false;

	public boolean javaparty = false;

	public boolean usePooling = false;

	public boolean closeGraph = false;

	public boolean singleRooted = false;

	public boolean naturalOrderedNodeLabels = false;

	public boolean naturalOrderedEdgeLabels = false;

	public boolean reverseOrderedNodeLabels = false;

	public boolean reverseOrderedEdgeLabels = false;

	public String inputFileName;

	public String outputFileName;

	public String objectFileName;

	private String distributionScheme = "local";

	public GraphFactory<NodeType, EdgeType> factory;

	public Algorithm<NodeType, EdgeType> algorithm;

	public Strategy<NodeType, EdgeType> strategy;

	public GraphParser<NodeType, EdgeType> parser;

	public GraphParser<NodeType, EdgeType> serializer;

	public Collection<Graph<NodeType, EdgeType>> graphs;

	public MiningStepFactory<NodeType, EdgeType> miningFactory;

	public final Statistics stats;

	public NodeType nnil;

	public EdgeType enil;

	public Settings() {
		stats = new Statistics();
	}

	@SuppressWarnings("unchecked")
	private Settings(final Settings set) {
		this.parser = set.parser;
		this.serializer = set.serializer;
		this.factory = set.factory;
		this.algorithm = set.algorithm;
		this.strategy = set.strategy;
		this.inputFileName = set.inputFileName;
		this.outputFileName = set.outputFileName;
		this.objectFileName = set.objectFileName;

		this.minProzent = set.minProzent;
		this.maxProzent = set.maxProzent;
		this.minFreq = set.minFreq;
		this.maxFreq = set.maxFreq;
		this.maxEdges = set.maxEdges;
		this.minEdges = set.minEdges;
		this.maxNodes = set.maxNodes;
		this.minNodes = set.minNodes;
		this.threadCount = set.threadCount;
		this.splitSize = set.splitSize;
		this.maxSplitCount = set.maxSplitCount;
		this.maxSplitDepth = set.maxSplitDepth;

		this.pathsOnly = set.pathsOnly;
		this.treesOnly = set.treesOnly;
		this.memoryStatistics = set.memoryStatistics;
		this.shrink = set.shrink;
		this.zaretsky = set.zaretsky;
		this.usePooling = set.usePooling;
		this.javaparty = set.javaparty;
		this.embeddingBased = set.embeddingBased || this.shrink;
		this.connectedFragments = set.connectedFragments;
		this.storeHierarchicalEmbeddings = set.storeHierarchicalEmbeddings;
		this.storeEmbeddings = set.storeEmbeddings || this.embeddingBased
				|| this.storeHierarchicalEmbeddings;
		this.closeGraph = set.closeGraph;
		this.singleRooted = set.singleRooted;
		this.naturalOrderedNodeLabels = set.naturalOrderedNodeLabels;
		this.naturalOrderedEdgeLabels = set.naturalOrderedEdgeLabels;
		this.reverseOrderedNodeLabels = set.reverseOrderedNodeLabels;
		this.reverseOrderedEdgeLabels = set.reverseOrderedEdgeLabels;

		this.miningFactory = set.miningFactory;
		this.nnil = (NodeType) set.nnil;
		this.enil = (EdgeType) set.enil;

		this.stats = set.stats;
	}

	/**
	 * STILL UNDER CONSTRUCTION
	 * 
	 * @param graph
	 * @return the frequency of the given graph
	 */
	public final Frequency getFrequency(final Graph<NodeType, EdgeType> graph) {
		return new IntFrequency(1); // TODO
	}

}
