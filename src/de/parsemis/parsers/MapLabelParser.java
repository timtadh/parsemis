/**
 * Created Jun 13, 2007
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

import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * This class is a parser for multi-attributed labels stored as strings in a map
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public class MapLabelParser implements GraphmlLabelParser<Map<String, String>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// TODO: replace all whitespaces
	final char space;

	final char tab;

	final String sep;

	private Set<String> attributes = new HashSet<String>();

	public MapLabelParser() {
		this('_', '-', ":");
	}

	public MapLabelParser(final char space, final char tab, final String sep) {
		this.space = space;
		this.tab = tab;
		this.sep = sep;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphmlLabelParser#attributes()
	 */
	public Set<String> attributes() {
		return attributes;
	}

	private final String normalize(final String i) {
		return i.replace('\t', tab).replace(' ', space);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.LabelParser#parse(java.lang.String)
	 */
	public Map<String, String> parse(final String text) throws ParseException {
		final String[] fields = text.split(sep);
		final HashMap<String, String> ret = new HashMap<String, String>();
		for (int i = 0; i + 1 < fields.length; i += 2) {
			if (attributes.contains(fields[i])) {
				ret.put(rebuild(fields[i]), rebuild(fields[i + 1]));
			} else {
				throw new ParseException("invalid attribute \"" + fields[i]
						+ "\" found", -1);
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphmlLabelParser#parseML(java.util.Map)
	 */
	public Map<String, String> parseML(final Map<String, String> map)
			throws ParseException {
		if (true || map.keySet().equals(attributes)) {
			return map;
		}
		throw new ParseException("invalid attributes in map", -1);
	}

	private final String rebuild(final String i) {
		return i.replace(space, ' ').replace(tab, '\t');
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.LabelParser#serialize(java.lang.Object)
	 */
	public String serialize(final Map<String, String> label) {
		final StringBuilder b = new StringBuilder();
		for (final Entry<String, String> e : label.entrySet()) {
			b.append(normalize(e.getKey()));
			b.append(sep);
			b.append(normalize(e.getValue()));
			b.append(sep);
		}
		return b.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphmlLabelParser#serializeML(java.lang.Object)
	 */
	public Map<String, String> serializeML(final Map<String, String> label) {
		return label;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphmlLabelParser#setAttributes(java.util.Collection)
	 */
	public boolean setAttributes(final Set<String> foundAttributes) {
		this.attributes = foundAttributes;
		return true;
	}

}
