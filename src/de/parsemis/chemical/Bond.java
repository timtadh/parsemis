/**
 * created Jun 28, 2006
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
package de.parsemis.chemical;

import java.text.ParseException;

import de.parsemis.parsers.LabelParser;

/**
 * This class represents a chemical bond for the use as edge label in molecular
 * graphs
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public class Bond {

	/** single bond */
	public final static Bond singleB = new Bond(1, '-');

	/** double bond */
	public final static Bond doubleB = new Bond(2, '=');

	/** tripple bond */
	public final static Bond trippleB = new Bond(3, '#');

	/** aromatic bond */
	public final static Bond aromaticB = new Bond(4, ':');

	/** no bond */
	public final static Bond noBond = new Bond(-1, '\0');

	private final static LabelParser<Bond> bp = new LabelParser<Bond>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.parsemis.parsers.LabelParser#parse(java.lang.String)
		 */
		public Bond parse(String text) throws ParseException {
			if (text.equals("1")) {
				return Bond.singleB;
			} else if (text.equals("2")) {
				return Bond.doubleB;
			} else if (text.equals("3")) {
				return Bond.trippleB;
			} else if (text.equals("4")) {
				return Bond.aromaticB;
			} else {
				throw new ParseException("Unknown atom: " + text, -1);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.parsemis.parsers.LabelParser#serialize(LabelType)
		 */
		public String serialize(Bond label) {
			return label.toString();
		}
	};

	/**
	 * @return a LabelParser for bonds
	 */
	public static LabelParser<Bond> parser() {
		return bp;
	}

	private final int type;

	private final char symbol;

	private Bond(final int value, final char symbol) {
		this.type = value;
		this.symbol = symbol;
	}

	/**
	 * @return the number of bindings (1, 2, or 3, and for aromatic 4)
	 */
	public int bindings() {
		return type;
	}

	@Override
	public boolean equals(final Object arg0) {
		return (arg0 instanceof Bond && ((Bond) arg0).type == this.type);
	}

	@Override
	public int hashCode() {
		return type;
	}

	/**
	 * @return the (SLN-)symbol for the coresponding bond type
	 */
	public char symbol() {
		return symbol;
	}

	@Override
	public String toString() {
		return String.valueOf(type);
	}

}
