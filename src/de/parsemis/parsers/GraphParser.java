/**
 * created May 2, 2006
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
package de.parsemis.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Collection;

import de.parsemis.graph.Graph;
import de.parsemis.graph.GraphFactory;
import de.parsemis.miner.general.Fragment;

/**
 * This interface declares the functionality to parse and serialize graphs
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
public interface GraphParser<NodeType, EdgeType> extends Serializable {

	/** @return the edge parser used */
	public LabelParser<EdgeType> getEdgeParser();

	/** @return the node parser used */
	public LabelParser<NodeType> getNodeParser();

	/**
	 * parses multiple graphs for the given input stream and creates a
	 * collection of the corresponding graphs
	 * 
	 * @param io
	 * @param factory
	 *            a factory that creates a corresponding empty graph
	 * @return a collection of the represented graphs
	 * @throws ParseException
	 * @throws IOException
	 */
	public Collection<Graph<NodeType, EdgeType>> parse(InputStream io,
			GraphFactory<NodeType, EdgeType> factory) throws ParseException,
			IOException;

	/**
	 * parses one graph from the given input string und creates the graph that
	 * is represented by it
	 * 
	 * @param input
	 * @param factory
	 *            a factory that creates a corresponding empty graph
	 * @return the corrsponding graph
	 * @throws ParseException
	 *             if the input string cannot be parsed correctly
	 */
	public Graph<NodeType, EdgeType> parse(String input,
			GraphFactory<NodeType, EdgeType> factory) throws ParseException;

	/**
	 * @param graph
	 * @return a serialized representation of the given graph
	 */
	public String serialize(Graph<NodeType, EdgeType> graph);

	/**
	 * writes all serialized representations of the given graphs to the output
	 * stream
	 * 
	 * @param out
	 * @param graphs
	 * @throws IOException
	 */
	public void serialize(OutputStream out,
			Collection<Graph<NodeType, EdgeType>> graphs) throws IOException;

	/**
	 * writes all serialized fragments to the output stream
	 * 
	 * @param out
	 * @param frags
	 * @throws IOException
	 */
	public void serializeFragments(OutputStream out,
			Collection<Fragment<NodeType, EdgeType>> frags) throws IOException;
}
