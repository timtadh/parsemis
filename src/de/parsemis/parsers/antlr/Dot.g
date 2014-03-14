/*
 * @by Sebastian Seifert (Sebastian.Seifert@informatik.uni-erlangen.de)
 *
 * Copyright 2004, 2005 Sebastian Seifert, Thorsten Meinl
 * 
 * This file is part of parsemis.
 *
 * License:
 * 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */

/*
 * @author Sebastian Seifert (Sebastian.Seifert@informatik.uni-erlangen.de)
 * @author Thorsten Meinl (Thorsten.Meinl@informatik.uni-erlangen.de)
 */
header {
package de.parsemis.parsers.antlr;
	
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
}

/**
 */
class DotLexer extends Lexer;
options{
	k = 2;
	charVocabulary = '\3'..'\377'; // LATIN
}

LCB:			'{';
RCB:			'}';
LSB:			'[';
RSB:			']';
COMMA:			',';
COLON:			':';
SEMICOLON:		';';
EQUAL:			'=';

ARROW:			"->";
DASH:			"--";

WHITESPACE:	(' ' | '\n' | '\r' | '\t')+ { $setType(Token.SKIP); };

protected
CHARACTER:	('a'..'z' | 'A'..'Z' | '_');
protected
DIGIT:			('0'..'9');
protected
QUOTED_STRING: '"'! ( ESCAPE | ~('"'|'\\') )* '"'! ;   // ! removes the '"'
protected
ESCAPE:    '\\' ( 'n' { $setText("\n"); } | 'r' { $setText("\r"); } | 't' { $setText("\t"); } | '"' { $setText("\""); });
protected
//NUMBER:			('-')? ('.' (DIGIT)+ | (DIGIT)+ ('.' (DIGIT)*)?);
NUMBER:			('.' (DIGIT)+ | (DIGIT)+ ('.' (DIGIT)*)?);

ID:	(CHARACTER (CHARACTER | DIGIT)* | NUMBER | QUOTED_STRING);


/**
 */
class DotParser extends Parser;
options{
	 k = 3;
}

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
}


graph {
	graphName = "";
	
	nodeIDStr2LabelStr.clear();
	edges.clear();
} :
	("strict")? ("graph" {directed = false;} | "digraph" {directed = true;}) (n:ID { graphName=n.getText(); })? LCB stmt_list RCB;


//stmt_list: 	stmt (SEMICOLON)? (stmt_list)?;
stmt_list: 	(stmt (SEMICOLON)?)+;

stmt:				(node_stmt | edge_stmt | attr_stmt | ID EQUAL ID); // | subgraph);


attr_stmt:	("graph" | "node" | "edge") attr_list;

attr_list returns [Map attrMap] {
	attrMap = null;
	Map alistMap = null;
}:	LSB (alistMap=a_list)? RSB (attrMap=attr_list)? {
	if(attrMap == null) return alistMap;
	else if (alistMap!=null) {
		attrMap.putAll(alistMap);
	}
};

a_list returns [Map attrMap] {
	attrMap = null;
}:			l:ID (EQUAL r:ID {
	if(l.getText().equals("label")) {
		lastLabelAttr = r.getText();
	}
})? (COMMA)? (attrMap=a_list)? {
	if(!l.getText().equals("label"))  { // labels are handled differently
		if(attrMap == null) attrMap = new TreeMap();
		attrMap.put(l.getText(), (r==null) ? null : r.getText());
	}
};

edge_stmt {
	String lhsIDStr = null;
	LinkedList rhsNodes = null;
	LinkedList urhsNodes = null;
	lastLabelAttr = null;
	Map attrMap = null;
}:	(lhsIDStr=node_id /* | subgraph */) (rhsNodes=edgeRHS | urhsNodes=uedgeRHS) (attrMap=attr_list)?
{
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
};

// rhs koennen die form ... -- B -- C -- D haben; attribute gelten fuer alle!
uedgeRHS returns [LinkedList nodes] {
	nodes = null;
	String rhsIDStr;
	int rhsIDInt;
}
:	DASH (rhsIDStr=node_id /* | subgraph */) (nodes=uedgeRHS)? 
{
	if(nodes == null) nodes = new LinkedList();
	nodes.addFirst(rhsIDStr);
};


// rhs koennen die form ... -> B -> C -> D haben; attribute gelten fuer alle!
edgeRHS returns [LinkedList nodes] {
	nodes = null;
	String rhsIDStr;
	int rhsIDInt;
}
:	ARROW (rhsIDStr=node_id /* | subgraph */) (nodes=edgeRHS)? 
{
	if(nodes == null) nodes = new LinkedList();
	nodes.addFirst(rhsIDStr);
};


node_stmt {
	lastLabelAttr = null;
	String idStr = null;
	Map attrMap = null;
}:	idStr=node_id (attrMap=attr_list)? {
	String labelStr = (lastLabelAttr == null) ? idStr : lastLabelAttr;
	//System.out.println("Node " + idStr + " with label " + labelStr + ".");
	nodeIDStr2LabelStr.put(idStr, labelStr);
	if(attrMap != null) {
		nodeIDStr2AttrMap.put(idStr, attrMap);
	}
};

node_id returns [String id] {
	id = null;
}:	i:ID {
	id = i.getText();
}; //(port)?;


//port:				(COLON ID (COLON compass_pt)? | COLON compass_pt);

//subgraph:		(("subgraph" (ID)?)? LCB stmt_list RCB | "subgraph" ID);

compass_pt:	("n" | "ne" | "e" | "se" | "s" | "sw" | "w" | "nw");