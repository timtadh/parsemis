/**
 * Created Nov 23, 2007
 * 
 * @by Sebastian Lenz (siselenz@stud.informatik.uni-erlangen.de)
 * 
 * Copyright 2007 Sebastian Lenz
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.parsers;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import de.parsemis.chemical.Atom;
import de.parsemis.chemical.Bond;
import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.GraphFactory;
import de.parsemis.graph.MutableGraph;
import de.parsemis.graph.Node;
import de.parsemis.miner.general.Fragment;

/**
 * This class implements a (simple and not complete) parser of the Graphml-file
 * format
 * 
 * @author Sebastian Lenz (siselenz@stud.informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */

public class GraphmlParser<NodeType, EdgeType> implements
		GraphParser<NodeType, EdgeType> {

	/*
	 * edge
	 */
	public class EdgeDesc {
		public String nodeA;

		public String nodeB;

		public Map<String, String> label;

		public boolean undirected = false;

		EdgeDesc(final String a, final String b, final Map<String, String> l) {
			nodeA = a;
			nodeB = b;
			label = l;
		}

		EdgeDesc(final String a, final String b, final Map<String, String> l,
				final boolean undir) {
			nodeA = a;
			nodeB = b;
			label = l;
			undirected = undir;
		}
	}

	public class GraphmlHandler extends DefaultHandler {

		// HashSet<String> graphID;
		String graphID;

		boolean gotNodeLabel;

		boolean gotEdgeLabel;

		String direction;

		String nodeID;

		String startNode;

		String endNode;

		String dataKey;

		// key ids
		Map<String, String> keyID;

		// label map
		Map<String, String> data;

		// nodes + edges
		Map<Map<String, Map<String, String>>, Collection<EdgeDesc>> graph;

		// nodes
		Map<String, Map<String, String>> nodes;

		// edges
		Collection<EdgeDesc> edges;

		// graphen = id + graph
		Map<String, Map<Map<String, Map<String, String>>, Collection<EdgeDesc>>> graphen;

		StringBuffer buf = new StringBuffer();

		public GraphmlHandler() {
			super();
			gotNodeLabel = false;
			gotEdgeLabel = false;
			nodes = new HashMap<String, Map<String, String>>();
			edges = new LinkedList<EdgeDesc>();
			graphen = new HashMap<String, Map<Map<String, Map<String, String>>, Collection<EdgeDesc>>>();
			graph = new HashMap<Map<String, Map<String, String>>, Collection<EdgeDesc>>();
			data = new HashMap<String, String>();
			keyID = new HashMap<String, String>();
			// graphID = new HashSet<String>();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(final char ch[], final int start,
				final int length) {
			buf.append(ch, start, length);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
		 */
		@Override
		public void endDocument() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
		 *      java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(final String uri, final String name,
				final String qName) {
			if ("graph".equals(name)) {
				// add nodes and edges to graph
				/*
				 * if (gotNodeLabel==false) { keyID.put("n", "x"); }
				 * 
				 * if (gotEdgeLabel==false) { keyID.put("e", "y"); }
				 */

				graph.put(nodes, edges);
				// System.out.println(nodes.hashCode());
				// System.out.println(graphen.containsKey(nodes));
				// System.out.println("Knoten: " + nodes.size() + " Kanten: " +
				// edges.size());
				graphen.put(graphID, graph);
				// System.out.println("Graphs: " + graphen.size());

			} else if ("node".equals(name)) {
				if (gotNodeLabel == false) {
					// System.out.println("kein label");
					data = new HashMap<String, String>();
					data.put("n", "a");
					// System.out.println("fake label: " + data.toString());
				}
				nodes.put(nodeID.trim(), data);
				// System.out.println("Nodelist: " + nodes.toString());

			} else if ("edge".equals(name)) {

				if (gotEdgeLabel == false) {
					// System.out.println("kein label");
					data = new HashMap<String, String>();
					data.put("e", "1");
				}

				if ("undirected".equals(direction)) {
					// undirected
					edges.add(new EdgeDesc(startNode.trim(), endNode.trim(),
							data, true));

				} else {
					// directed
					edges.add(new EdgeDesc(startNode.trim(), endNode.trim(),
							data));
				}

			} else if ("data".equals(name)) {

				data.put(dataKey, buf.toString().trim());
				buf.delete(0, buf.length());
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
		 */
		@Override
		public void startDocument() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
		 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(final String uri, final String name,
				final String qName, final Attributes atts) {
			if ("graph".equals(name)) {
				nodes = new HashMap<String, Map<String, String>>();
				edges = new LinkedList<EdgeDesc>();
				data = new HashMap<String, String>();
				graph = new HashMap<Map<String, Map<String, String>>, Collection<EdgeDesc>>();
				// name
				graphID = atts.getValue(0);
				// graphID.add(atts.getValue(0));
				// directed or undirected
				direction = atts.getValue(1);
				// System.out.println("Graphen(ID): " + graphID.size());
			} else if ("node".equals(name)) {
				// id
				data = new HashMap<String, String>();
				nodeID = atts.getValue(0);
			} else if ("edge".equals(name)) {
				// startnode
				data = new HashMap<String, String>();
				startNode = atts.getValue(0);
				// endnode
				endNode = atts.getValue(1);
				// edge label -> endElement
			} else if ("data".equals(name)) {
				gotEdgeLabel = true;
				gotNodeLabel = true;
				dataKey = atts.getValue(0);
				for (final Map.Entry<String, String> e : keyID.entrySet()) {
					if (e.getKey().equals(dataKey)) {
						dataKey = e.getValue();

					}
				}
			} else if ("key".equals(name)) {
				keyID.put(atts.getValue(0), atts.getValue(2));
			}
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final GraphParser<Atom, Bond> chemInstance() {
		return new GraphmlParser<Atom, Bond>(Atom.parser(), Bond.parser());
	}

	/**
	 * @return a default ML-parser for String labels
	 */
	public static final GraphParser<String, String> instance() {
		return new GraphmlParser<String, String>(new StringLabelParser(),
				new StringLabelParser());
	}

	private final LabelParser<NodeType> np;

	private final GraphmlLabelParser<NodeType> gnp;

	private final LabelParser<EdgeType> ep;

	private final GraphmlLabelParser<EdgeType> gep;

	/**
	 * creates a new parser for the given label types
	 * 
	 * @param np
	 * @param ep
	 */
	public GraphmlParser(final LabelParser<NodeType> np,
			final LabelParser<EdgeType> ep) {
		this.np = np;
		this.ep = ep;
		this.gnp = (np instanceof GraphmlLabelParser) ? (GraphmlLabelParser<NodeType>) np
				: null;
		this.gep = (ep instanceof GraphmlLabelParser) ? (GraphmlLabelParser<EdgeType>) ep
				: null;
	}

	public LabelParser<EdgeType> getEdgeParser() {
		return ep;
	}

	public LabelParser<NodeType> getNodeParser() {
		return np;
	}

	/*
	 * inputstream in -> input file called from utils.FileConverter
	 */

	public Collection<Graph<NodeType, EdgeType>> parse(final InputStream in,
			final GraphFactory<NodeType, EdgeType> factory)
			throws ParseException, IOException {

		try {
			return parseGraphs(in, factory);
		} catch (final SAXException e) {
			e.printStackTrace();
			throw new ParseException(e.toString(), -1);
		}
	}

	/*
	 * parses Graph<NodeType,EdgeType> to simple graphml only nodes, edges,
	 * labels and color
	 */

	/*
	 * not used yet -> no implementation
	 * 
	 */
	public Graph<NodeType, EdgeType> parse(final String input,
			final GraphFactory<NodeType, EdgeType> factory) {
		return null;
	}

	/*
	 * starts xml parser handler output -> mutablegraph
	 */
	private Collection<Graph<NodeType, EdgeType>> parseGraphs(
			final InputStream in, final GraphFactory<NodeType, EdgeType> factory)
			throws SAXException, IOException, ParseException {

		// handler
		final GraphmlHandler handler = new GraphmlHandler();
		// xml parser
		final XMLReader xr = XMLReaderFactory.createXMLReader();
		xr.setContentHandler(handler);
		xr.setErrorHandler(handler);

		xr.parse(new InputSource(in));

		final LinkedList<Graph<NodeType, EdgeType>> graphs = new LinkedList<Graph<NodeType, EdgeType>>();

		final Map<String, Node<NodeType, EdgeType>> nodes = new HashMap<String, Node<NodeType, EdgeType>>();

		MutableGraph<NodeType, EdgeType> g;
		// new graph
		// graphID -> name
		// MutableGraph<NodeType, EdgeType> g =
		// factory.newGraph(handler.graphID);
		// Iterator it = handler.graphID.iterator();

		// Set<String> nodeAtt=new HashSet<String>();
		// Set<String> edgeAtt=new HashSet<String>();

		for (final Map.Entry<String, Map<Map<String, Map<String, String>>, Collection<EdgeDesc>>> f : handler.graphen
				.entrySet()) {
			// System.out.println(handler.graphID.iterator().next());

			g = factory.newGraph(f.getKey());
			// g = factory.newGraph(it.next().toString());

			for (final Map.Entry<Map<String, Map<String, String>>, Collection<EdgeDesc>> val : f
					.getValue().entrySet()) {

				final Collection<EdgeDesc> helpedge = val.getValue();
				final Map<String, Map<String, String>> help = val.getKey();
				nodes.clear();

				if (gnp != null) {

					// special node
					// for (Map.Entry<Map<String, Map<String, String>>,
					// Collection<EdgeDesc>> f : handler.graphen.entrySet()) {
					// add nodes to graph
					// Map<String, Map<String, String>> help = f.getKey();
					for (final Map.Entry<String, Map<String, String>> e : help
							.entrySet()) {
						nodes.put(e.getKey(), g.addNode(gnp.parseML(e
								.getValue())));
					}
					// }
				} else {

					// node normal
					// add nodes to graph

					for (final Map.Entry<String, Map<String, String>> e : help
							.entrySet()) {
						for (final Map.Entry<String, String> muh : e.getValue()
								.entrySet()) {
							// if ("shrink.nodelabel.name".equals(muh.getKey())
							// || "NAME".equals(muh.getKey())) {
							nodes.put(e.getKey(), g.addNode(np.parse(muh
									.getValue())));
							// }
						}
					}
				}

				if (gep != null) {
					// special edge
					for (final EdgeDesc e : helpedge) {
						g
								.addEdge(nodes.get(e.nodeA),
										nodes.get(e.nodeB), gep
												.parseML(e.label),
										(e.undirected ? Edge.UNDIRECTED
												: Edge.OUTGOING));
					}
				} else {
					// edge normal
					for (final EdgeDesc e : helpedge) {
						for (final Map.Entry<String, String> muh : e.label
								.entrySet()) {
							// if ("weight".equals(muh.getKey())) {
							g.addEdge(nodes.get(e.nodeA), nodes.get(e.nodeB),
									ep.parse(muh.getValue()),
									(e.undirected ? Edge.UNDIRECTED
											: Edge.OUTGOING));
							// }
						}
					}
				}
				graphs.add(g);
			}
			if (gnp != null) {
				gnp.setAttributes(handler.nodes.values().iterator().next()
						.keySet());
			}
		}

		return graphs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see src.de.pargra.parsers.GraphParser#serialize(src.de.pargra.graph.Graph)
	 * 
	 * not used
	 */
	public String serialize(final Graph<NodeType, EdgeType> graph) {

		return serialize(graph, "transparent");
	}

	public String serialize(final Graph<NodeType, EdgeType> graph,
			final String bgcolor) {
		final StringBuffer buf = new StringBuffer(2048);
		/*
		 * data keys: nodes: string or int -> dn nodes: color -> color edges:
		 * int -> de
		 */

		// graphname and (un)directed
		buf.append("<graph id=\"" + graph.getName() + "\" edgedefault=");
		buf
				.append((graph.getEdgeCount() > 0
						&& graph.edgeIterator().next().getDirection() != Edge.UNDIRECTED ? "\""
						: "\"un")
						+ "directed\">\n");

		// special node
		if (gnp != null) {

			for (final Iterator<Node<NodeType, EdgeType>> nit = graph
					.nodeIterator(); nit.hasNext();) {

				final Node<NodeType, EdgeType> n = nit.next();
				buf.append("\t<node id=\"" + n.getIndex() + "\">");

				final Map<String, String> map = gnp.serializeML(n.getLabel());
				for (final Map.Entry<String, String> m : map.entrySet()) {
					buf.append("\t<data key=\"" + m.getKey() + "\">"
							+ m.getValue() + "</data>\n");
				}
				buf.append("</node>\n");
			}
		} else {
			// node normal
			for (final Iterator<Node<NodeType, EdgeType>> nit = graph
					.nodeIterator(); nit.hasNext();) {

				final Node<NodeType, EdgeType> n = nit.next();
				buf.append("\t<node id=\"" + n.getIndex() + "\">");
				buf.append("\t<data key=\"dn\">" + np.serialize(n.getLabel())
						+ "</data>\n");
				buf.append("\t<data key=\"color\">" + "</data>\n");
				buf.append("</node>\n");
			}
		}
		// special edge
		if (gep != null) {
			for (final Iterator<Edge<NodeType, EdgeType>> eit = graph
					.edgeIterator(); eit.hasNext();) {
				final Edge<NodeType, EdgeType> e = eit.next();
				final Map<String, String> map = gep.serializeML(e.getLabel());

				if (e.getDirection() == Edge.UNDIRECTED) {
					buf.append("<edge source=\"" + e.getNodeA().getIndex()
							+ "\" target=\"" + e.getNodeB().getIndex()
							+ "\">\n");
				} else if (e.getDirection() == Edge.OUTGOING) {
					buf.append("<edge source=\"" + e.getNodeA().getIndex()
							+ "\" target=\"" + e.getNodeB().getIndex()
							+ "\">\n");
				} else if (e.getDirection() == Edge.INCOMING) {
					buf.append("<edge source=\"" + e.getNodeB().getIndex()
							+ "\" target=\"" + e.getNodeA().getIndex()
							+ "\">\n");
				}

				for (final Map.Entry<String, String> m : map.entrySet()) {
					buf.append("\t<data key=\"" + m.getKey() + "\">"
							+ m.getValue() + "</data>\n");
				}

				buf.append("</edge>\n");
			}
		} else {
			// edge normal

			for (final Iterator<Edge<NodeType, EdgeType>> eit = graph
					.edgeIterator(); eit.hasNext();) {
				final Edge<NodeType, EdgeType> e = eit.next();

				if (e.getDirection() == Edge.UNDIRECTED) {
					buf.append("<edge source=\"" + e.getNodeA().getIndex()
							+ "\" target=\"" + e.getNodeB().getIndex()
							+ "\">\n");
				} else if (e.getDirection() == Edge.OUTGOING) {
					buf.append("<edge source=\"" + e.getNodeA().getIndex()
							+ "\" target=\"" + e.getNodeB().getIndex()
							+ "\">\n");
				} else if (e.getDirection() == Edge.INCOMING) {
					buf.append("<edge source=\"" + e.getNodeB().getIndex()
							+ "\" target=\"" + e.getNodeA().getIndex()
							+ "\">\n");
				}

				buf.append("\t<data key=\"de\">" + ep.serialize(e.getLabel())
						+ "</data>\n");
				buf.append("</edge>\n");
			}
		}

		buf.append("</graph>\n");

		return buf.toString();
	}

	/*
	 * just the xml-header
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphParser#serialize(java.io.OutputStream,
	 *      java.util.Collection)
	 * 
	 * 
	 */
	public void serialize(final OutputStream out,
			final Collection<Graph<NodeType, EdgeType>> graphs)
			throws IOException {

		final Iterator<Graph<NodeType, EdgeType>> git = graphs.iterator();

		final Graph<NodeType, EdgeType> g = git.next();
		// header
		out.write(writeHeader(g).getBytes());

		// if (git.hasNext()) {
		out.write(serialize(g, "transparent").getBytes());
		// }
		while (git.hasNext()) {
			out.write("\n".getBytes());
			out.write(serialize(git.next(), "transparent").getBytes());
		}

		out.write("</graphml>".getBytes());

	}

	/*
	 * handler-class for xml-parser
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphParser#serialize(java.io.OutputStream,
	 *      de.parsemis.miner.FragmentSet) called from utils.FileConverter not
	 *      used
	 */
	public void serializeFragments(final OutputStream out,
			final Collection<Fragment<NodeType, EdgeType>> frags)
			throws IOException {
		final BufferedOutputStream output = new BufferedOutputStream(out);

		// header
		output.write(writeHeader(frags.iterator().next().toGraph())
				.getBytes());

		for (final Fragment<NodeType, EdgeType> frag : frags) {
			output.write(serialize(frag.toGraph(), "transparent")
					.getBytes());
		}
		output.write("</graphml>".getBytes());

		output.flush();
	}

	private String writeHeader(final Graph<NodeType, EdgeType> graph) {
		final StringBuilder buf = new StringBuilder();

		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		buf
				.append("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n");
		buf.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
		buf
				.append("xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns\n");
		buf
				.append("http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n");

		// node type -> map
		if (gnp != null) {
			for (final String attr : gnp.attributes()) {
				buf.append("<key id=\"" + attr + "\" for=\"node\" attr.name=\""
						+ attr + "\" attr.type=\"string\"/>\n");
			}
			// node type -> standart
		} else {
			buf
					.append("<key id=\"dn\" for=\"node\" attr.name=\"name\" attr.type=\"string\"/>\n");
			buf
					.append("<key id=\"color\" for=\"node\" attr.name=\"color\" attr.type=\"string\"/>\n");
		}
		// edge type -> map
		if (gep != null) {
			for (final String attr : gep.attributes()) {
				buf.append("<key id=\"" + attr + "\" for=\"edge\" attr.name=\""
						+ attr + "\" attr.type=\"string\"/>\n");
			}
			// edge type -> standart
		} else {
			buf
					.append("<key id=\"de\" for=\"edge\" attr.name=\"weight\" attr.type=\"string\"/>\n");
		}
		return buf.toString();
	}
}
