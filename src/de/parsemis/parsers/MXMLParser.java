/**
 * Created Sep 17, 2007
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
package de.parsemis.parsers;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.HPMutableGraph;
import de.parsemis.miner.general.Fragment;
import de.parsemis.utils.IntIterator;

/**
 * This class implements a (simple and not complete) parser of the mxml-file
 * format
 * 
 * @author Sebastian Lenz (siselenz@stud.informatik.uni-erlangen.de)
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */

public class MXMLParser<NodeType, EdgeType> implements
		GraphParser<NodeType, EdgeType> {

	// boolean graphtype = false;

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

	public class MXMLHandler extends DefaultHandler {

		final GraphFactory<NodeType, EdgeType> factory;

		final Collection<Graph<NodeType, EdgeType>> result;

		HashMap<String, String> currentNodeLabel = null;

		String currentTag = null;

		StringBuilder currentValue = new StringBuilder();

		HPMutableGraph<NodeType, EdgeType> currentGraph = null;

		public MXMLHandler(final GraphFactory<NodeType, EdgeType> factory,
				final Collection<Graph<NodeType, EdgeType>> result) {
			super();
			this.factory = factory;
			this.result = result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(final char ch[], final int start,
				final int length) {
			currentValue.append(ch, start, length);
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
			if ("AuditTrailEntry".equals(name)) { // finalize node
				try {
					currentGraph.addNodeIndex(gnp.parseML(currentNodeLabel));
				} catch (final ParseException e) {
					e.printStackTrace();
				}
			} else if ("ProcessInstance".equals(name)) { // finalize trace
				// graph
				result.add(currentGraph.toGraph());
				currentGraph = null;
			} else if ("Process".equals(name)) { // finalize mastergraph
			} else if (currentTag != null) { // search for closing one
				if (currentTag.equals(name)) {
					currentNodeLabel.put(currentTag, currentValue.toString()
							.trim());
					currentValue = new StringBuilder();
				} else {
					assert false : "unexpected clonsing tag: " + name
							+ "; expecting: " + currentTag;
				}
				currentTag = null;
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
			if ("AuditTrailEntry".equals(name)) { // new node
				currentNodeLabel = new HashMap<String, String>();
			} else if ("ProcessInstance".equals(name)) { // new trace graph
				currentGraph = (HPMutableGraph<NodeType, EdgeType>) factory
						.newGraph("TODO").toHPGraph();
			} else if ("Process".equals(name)) { // new mastergraph
			} else if (currentNodeLabel != null) {
				currentTag = name;
			}
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final GraphParser<Atom, Bond> chemInstance() {
		return new MXMLParser<Atom, Bond>(Atom.parser(), Bond.parser());
	}

	/**
	 * @return a default ML-parser for String labels
	 */
	public static final GraphParser<String, String> instance() {
		return new MXMLParser<String, String>(new StringLabelParser(),
				new StringLabelParser());
	}

	private final LabelParser<NodeType> np;

	final GraphmlLabelParser<NodeType> gnp;

	private final LabelParser<EdgeType> ep;

	private final GraphmlLabelParser<EdgeType> gep;

	/**
	 * creates a new parser for the given label types
	 * 
	 * @param np
	 * @param ep
	 */
	public MXMLParser(final LabelParser<NodeType> np,
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

		final Collection<Graph<NodeType, EdgeType>> ret = new ArrayList<Graph<NodeType, EdgeType>>();
		// handler
		final MXMLHandler handler = new MXMLHandler(factory, ret);
		// xml parser
		final XMLReader xr = XMLReaderFactory.createXMLReader();
		xr.setContentHandler(handler);
		xr.setErrorHandler(handler);

		xr.parse(new InputSource(in));

		return ret;
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

	public String serialize(final Graph<NodeType, EdgeType> graph2,
			final String bgcolor) {
		final StringBuffer buf = new StringBuffer(2048);
		final HPGraph<NodeType, EdgeType> graph = graph2.toHPGraph();
		/*
		 * data keys: nodes: string or int -> dn nodes: color -> color edges:
		 * int -> de
		 */

		// graphname and (un)directed
		buf.append("<graph id=\"" + graph.getName() + "\" edgedefault=");
		buf
				.append((graph.getEdgeCount() > 0
						&& graph.getDirection(graph.edgeIndexIterator().next()) != Edge.UNDIRECTED ? "\""
						: "\"un")
						+ "directed\">\n");

		for (final IntIterator nit = graph.nodeIndexIterator(); nit.hasNext();) {

			final int n = nit.next();
			buf.append("\t<node id=\"" + n + "\">");
			if (gnp != null) {
				// special node
				final Map<String, String> map = gnp.serializeML(graph
						.getNodeLabel(n));
				for (final Map.Entry<String, String> m : map.entrySet()) {
					buf.append("\t<data key=\"" + m.getKey() + "\">"
							+ m.getValue() + "</data>\n");
				}
			} else {
				// node normal
				buf.append("\t<data key=\"dn\">"
						+ np.serialize(graph.getNodeLabel(n)) + "</data>\n");
				buf.append("\t<data key=\"color\">" + "</data>\n");
			}
			buf.append("</node>\n");
		}

		for (final IntIterator eit = graph.edgeIndexIterator(); eit.hasNext();) {
			final int e = eit.next();

			switch (graph.getDirection(e)) {
			case Edge.UNDIRECTED:
			case Edge.OUTGOING:
				buf.append("<edge source=\"" + graph.getNodeA(e)
						+ "\" target=\"" + graph.getNodeB(e) + "\">\n");
				break;
			case Edge.INCOMING:
				buf.append("<edge source=\"" + graph.getNodeB(e)
						+ "\" target=\"" + graph.getNodeA(e) + "\">\n");
				break;
			}

			// special edge
			if (gep != null) {
				final Map<String, String> map = gep.serializeML(graph
						.getEdgeLabel(e));
				for (final Map.Entry<String, String> m : map.entrySet()) {
					buf.append("\t<data key=\"" + m.getKey() + "\">"
							+ m.getValue() + "</data>\n");
				}
			} else {
				// edge normal
				buf.append("\t<data key=\"de\">"
						+ ep.serialize(graph.getEdgeLabel(e)) + "</data>\n");
			}
			buf.append("</edge>\n");
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
