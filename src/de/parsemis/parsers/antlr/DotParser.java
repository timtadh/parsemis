// $ANTLR 2.7.6 (2005-12-22): "Dot.g" -> "DotParser.java"$

package de.parsemis.parsers.antlr;
	
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;

/**
 */
public class DotParser extends antlr.LLkParser       implements DotLexerTokenTypes
 {

	String graphName = "";
	// forget between graphs
	private java.util.Map<String,String> nodeIDStr2LabelStr = new HashMap<String,String>();
	private java.util.Map nodeIDStr2AttrMap = new HashMap();
	// a linked list of EdgeDesc objects
	private java.util.Collection<EdgeDesc> edges = new LinkedList<EdgeDesc>();
	
	private boolean directed = false;
		
	/**
	 *
	 */
	public class EdgeDesc {
		public String nodeA;
		public String nodeB;
		public String label;
		public Map attributes;
		public boolean undirected=false;
		EdgeDesc(String a, String b, String l, Map attr) {
			nodeA = a;
			nodeB = b;
			label = l;
			attributes = attr;
		}
		EdgeDesc(String a, String b, String l, Map attr, boolean undir) {
			nodeA = a;
			nodeB = b;
			label = l;
			attributes = attr;
			undirected = undir;
		}
	}
	
	private String lastLabelAttr = null;		
	
	public Map<String,String> getNodeMap() {
		return nodeIDStr2LabelStr;
	}
	
	public Map getNodeAttrMap() {
		return nodeIDStr2AttrMap;
	}
	
	public Collection<EdgeDesc> getEdges() {
		return edges;
	}
	
	public String getName() {
		return graphName;
	}
	
	public boolean directed() {
		return directed;
	}

protected DotParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public DotParser(TokenBuffer tokenBuf) {
  this(tokenBuf,3);
}

protected DotParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public DotParser(TokenStream lexer) {
  this(lexer,3);
}

public DotParser(ParserSharedInputState state) {
  super(state,3);
  tokenNames = _tokenNames;
}

	public final void graph() throws RecognitionException, TokenStreamException {
		
		Token  n = null;
		
			graphName = "";
			
			nodeIDStr2LabelStr.clear();
			edges.clear();
		
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LITERAL_strict:
			{
				match(LITERAL_strict);
				break;
			}
			case LITERAL_graph:
			case LITERAL_digraph:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case LITERAL_graph:
			{
				match(LITERAL_graph);
				directed = false;
				break;
			}
			case LITERAL_digraph:
			{
				match(LITERAL_digraph);
				directed = true;
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case ID:
			{
				n = LT(1);
				match(ID);
				graphName=n.getText();
				break;
			}
			case LCB:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(LCB);
			stmt_list();
			match(RCB);
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_0);
		}
	}
	
	public final void stmt_list() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			int _cnt44=0;
			_loop44:
			do {
				if ((_tokenSet_1.member(LA(1)))) {
					stmt();
					{
					switch ( LA(1)) {
					case SEMICOLON:
					{
						match(SEMICOLON);
						break;
					}
					case RCB:
					case ID:
					case LITERAL_graph:
					case LITERAL_node:
					case LITERAL_edge:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
				}
				else {
					if ( _cnt44>=1 ) { break _loop44; } else {throw new NoViableAltException(LT(1), getFilename());}
				}
				
				_cnt44++;
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_2);
		}
	}
	
	public final void stmt() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			if ((LA(1)==ID) && (_tokenSet_3.member(LA(2)))) {
				node_stmt();
			}
			else if ((LA(1)==ID) && (LA(2)==ARROW||LA(2)==DASH)) {
				edge_stmt();
			}
			else if ((LA(1)==LITERAL_graph||LA(1)==LITERAL_node||LA(1)==LITERAL_edge)) {
				attr_stmt();
			}
			else if ((LA(1)==ID) && (LA(2)==EQUAL)) {
				match(ID);
				match(EQUAL);
				match(ID);
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_4);
		}
	}
	
	public final void node_stmt() throws RecognitionException, TokenStreamException {
		
		
			lastLabelAttr = null;
			String idStr = null;
			Map attrMap = null;
		
		
		try {      // for error handling
			idStr=node_id();
			{
			switch ( LA(1)) {
			case LSB:
			{
				attrMap=attr_list();
				break;
			}
			case RCB:
			case SEMICOLON:
			case ID:
			case LITERAL_graph:
			case LITERAL_node:
			case LITERAL_edge:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			
				String labelStr = (lastLabelAttr == null) ? idStr : lastLabelAttr;
				//System.out.println("Node " + idStr + " with label " + labelStr + ".");
				nodeIDStr2LabelStr.put(idStr, labelStr);
				if(attrMap != null) {
					nodeIDStr2AttrMap.put(idStr, attrMap);
				}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_4);
		}
	}
	
	public final void edge_stmt() throws RecognitionException, TokenStreamException {
		
		
			String lhsIDStr = null;
			LinkedList rhsNodes = null;
			LinkedList urhsNodes = null;
			lastLabelAttr = null;
			Map attrMap = null;
		
		
		try {      // for error handling
			{
			lhsIDStr=node_id();
			}
			{
			switch ( LA(1)) {
			case ARROW:
			{
				rhsNodes=edgeRHS();
				break;
			}
			case DASH:
			{
				urhsNodes=uedgeRHS();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case LSB:
			{
				attrMap=attr_list();
				break;
			}
			case RCB:
			case SEMICOLON:
			case ID:
			case LITERAL_graph:
			case LITERAL_node:
			case LITERAL_edge:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			
				String labelStr = (lastLabelAttr == null) ? "" : lastLabelAttr;
				
				// if a node is only mentioned in an edge, its label becomes its id.
				// if a node_stmt appears later, the label will be changed
				if(!nodeIDStr2LabelStr.containsKey(lhsIDStr)) {
					nodeIDStr2LabelStr.put(lhsIDStr, lhsIDStr);
				}
				
				while(rhsNodes!=null && !rhsNodes.isEmpty()) {
					String rhsIDStr = (String)rhsNodes.removeFirst();
					//System.out.print(" -> " + rhsIDStr);
					edges.add(new EdgeDesc(lhsIDStr, rhsIDStr, labelStr, attrMap));
					if(!nodeIDStr2LabelStr.containsKey(rhsIDStr)) {
						nodeIDStr2LabelStr.put(rhsIDStr, rhsIDStr);
					}
				
					lhsIDStr = rhsIDStr;   // if A -> B -> C, B is lhs in B-> C
				}
			
				while(urhsNodes!=null && !urhsNodes.isEmpty()) {
					String rhsIDStr = (String)urhsNodes.removeFirst();
					//System.out.print(" -> " + rhsIDStr);
					edges.add(new EdgeDesc(lhsIDStr, rhsIDStr, labelStr, attrMap, true));
					if(!nodeIDStr2LabelStr.containsKey(rhsIDStr)) {
						nodeIDStr2LabelStr.put(rhsIDStr, rhsIDStr);
					}
				
					lhsIDStr = rhsIDStr;   // if A -> B -> C, B is lhs in B-> C
				}
				
			
				//System.out.println();	
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_4);
		}
	}
	
	public final void attr_stmt() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LITERAL_graph:
			{
				match(LITERAL_graph);
				break;
			}
			case LITERAL_node:
			{
				match(LITERAL_node);
				break;
			}
			case LITERAL_edge:
			{
				match(LITERAL_edge);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			attr_list();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_4);
		}
	}
	
	public final Map  attr_list() throws RecognitionException, TokenStreamException {
		Map attrMap;
		
		
			attrMap = null;
			Map alistMap = null;
		
		
		try {      // for error handling
			match(LSB);
			{
			switch ( LA(1)) {
			case ID:
			{
				alistMap=a_list();
				break;
			}
			case RSB:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(RSB);
			{
			switch ( LA(1)) {
			case LSB:
			{
				attrMap=attr_list();
				break;
			}
			case RCB:
			case SEMICOLON:
			case ID:
			case LITERAL_graph:
			case LITERAL_node:
			case LITERAL_edge:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			
				if(attrMap == null) return alistMap;
				else if (alistMap!=null) {
					attrMap.putAll(alistMap);
				}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_4);
		}
		return attrMap;
	}
	
	public final Map  a_list() throws RecognitionException, TokenStreamException {
		Map attrMap;
		
		Token  l = null;
		Token  r = null;
		
			attrMap = null;
		
		
		try {      // for error handling
			l = LT(1);
			match(ID);
			{
			switch ( LA(1)) {
			case EQUAL:
			{
				match(EQUAL);
				r = LT(1);
				match(ID);
				
					if(l.getText().equals("label")) {
						lastLabelAttr = r.getText();
					}
				
				break;
			}
			case RSB:
			case COMMA:
			case ID:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case COMMA:
			{
				match(COMMA);
				break;
			}
			case RSB:
			case ID:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			switch ( LA(1)) {
			case ID:
			{
				attrMap=a_list();
				break;
			}
			case RSB:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			
				if(!l.getText().equals("label"))  { // labels are handled differently
					if(attrMap == null) attrMap = new TreeMap();
					attrMap.put(l.getText(), (r==null) ? null : r.getText());
				}
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_5);
		}
		return attrMap;
	}
	
	public final String  node_id() throws RecognitionException, TokenStreamException {
		String id;
		
		Token  i = null;
		
			id = null;
		
		
		try {      // for error handling
			i = LT(1);
			match(ID);
			
				id = i.getText();
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_6);
		}
		return id;
	}
	
	public final LinkedList  edgeRHS() throws RecognitionException, TokenStreamException {
		LinkedList nodes;
		
		
			nodes = null;
			String rhsIDStr;
			int rhsIDInt;
		
		
		try {      // for error handling
			match(ARROW);
			{
			rhsIDStr=node_id();
			}
			{
			switch ( LA(1)) {
			case ARROW:
			{
				nodes=edgeRHS();
				break;
			}
			case RCB:
			case LSB:
			case SEMICOLON:
			case ID:
			case LITERAL_graph:
			case LITERAL_node:
			case LITERAL_edge:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			
				if(nodes == null) nodes = new LinkedList();
				nodes.addFirst(rhsIDStr);
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_3);
		}
		return nodes;
	}
	
	public final LinkedList  uedgeRHS() throws RecognitionException, TokenStreamException {
		LinkedList nodes;
		
		
			nodes = null;
			String rhsIDStr;
			int rhsIDInt;
		
		
		try {      // for error handling
			match(DASH);
			{
			rhsIDStr=node_id();
			}
			{
			switch ( LA(1)) {
			case DASH:
			{
				nodes=uedgeRHS();
				break;
			}
			case RCB:
			case LSB:
			case SEMICOLON:
			case ID:
			case LITERAL_graph:
			case LITERAL_node:
			case LITERAL_edge:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			
				if(nodes == null) nodes = new LinkedList();
				nodes.addFirst(rhsIDStr);
			
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_3);
		}
		return nodes;
	}
	
	public final void compass_pt() throws RecognitionException, TokenStreamException {
		
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case LITERAL_n:
			{
				match(LITERAL_n);
				break;
			}
			case LITERAL_ne:
			{
				match(LITERAL_ne);
				break;
			}
			case LITERAL_e:
			{
				match(LITERAL_e);
				break;
			}
			case LITERAL_se:
			{
				match(LITERAL_se);
				break;
			}
			case LITERAL_s:
			{
				match(LITERAL_s);
				break;
			}
			case LITERAL_sw:
			{
				match(LITERAL_sw);
				break;
			}
			case LITERAL_w:
			{
				match(LITERAL_w);
				break;
			}
			case LITERAL_nw:
			{
				match(LITERAL_nw);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			recover(ex,_tokenSet_0);
		}
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"LCB",
		"RCB",
		"LSB",
		"RSB",
		"COMMA",
		"COLON",
		"SEMICOLON",
		"EQUAL",
		"ARROW",
		"DASH",
		"WHITESPACE",
		"CHARACTER",
		"DIGIT",
		"QUOTED_STRING",
		"ESCAPE",
		"NUMBER",
		"ID",
		"\"strict\"",
		"\"graph\"",
		"\"digraph\"",
		"\"node\"",
		"\"edge\"",
		"\"n\"",
		"\"ne\"",
		"\"e\"",
		"\"se\"",
		"\"s\"",
		"\"sw\"",
		"\"w\"",
		"\"nw\""
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 2L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 55574528L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 32L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 55575648L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 55575584L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 128L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 55587936L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	
	}
