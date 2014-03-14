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

import java.awt.Color;
import java.text.ParseException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.parsemis.parsers.LabelParser;

/**
 * This class represents a chemical atom for the use as node label in molecular
 * graphs
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public class Atom implements Comparable<Atom> {

	/* the constant Atom labels */
	/** Hydrogen */
	public final static Atom Dummy = new Atom(0, "H", "Hydrogen", -1, false,
			Color.BLACK);

	/** Hydrogen */
	public final static Atom H = new Atom(1, "H", "Hydrogen", -1, false,
			Color.BLACK);

	/** Helium */
	public final static Atom He = new Atom(2, "He", "Helium", -1, false,
			Color.BLACK);

	/** Lithium */
	public final static Atom Li = new Atom(3, "Li", "Lithium", -1, false,
			Color.BLACK);

	/** Beryllium */
	public final static Atom Be = new Atom(4, "Be", "Beryllium", -1, false,
			Color.BLACK);

	/** Boron */
	public final static Atom B = new Atom(5, "B", "Boron", 3, true, Color.BLACK);

	/** Carbon */
	public final static Atom C = new Atom(6, "C", "Carbon", 4, true,
			Color.BLACK);

	/** Nitrogen */
	public final static Atom N = new Atom(7, "N", "Nitrogen", 3, true,
			Color.BLUE);

	/** Oxygen */
	public final static Atom O = new Atom(8, "O", "Oxigen", 2, true, Color.RED);

	/** Fluorine */
	public final static Atom F = new Atom(9, "F", "Fluorine", -1, false,
			new Color(255, 0, 255));

	/** Neon */
	public final static Atom Ne = new Atom(10, "Ne", "Neon", -1, false,
			Color.BLACK);

	/** Sodium */
	public final static Atom Na = new Atom(11, "Na", "Sodium", -1, false,
			Color.BLACK);

	/** Magnesium */
	public final static Atom Mg = new Atom(12, "Mg", "Magnesium", -1, false,
			Color.BLACK);

	/** Aluminium */
	public final static Atom Al = new Atom(13, "Al", "Aluminium", -1, false,
			Color.BLACK);

	/** Silicon */
	public final static Atom Si = new Atom(14, "Si", "Silicon", -1, false,
			Color.BLACK);

	/** Phosphorus */
	public final static Atom P = new Atom(15, "P", "Phosphorus", 3, true,
			Color.BLACK);

	/** Sulfur */
	public final static Atom S = new Atom(16, "S", "Sulfur", 2, true,
			new Color(240, 240, 0));

	/** Chlorine */
	public final static Atom Cl = new Atom(17, "Cl", "Chlorine", 1, true,
			new Color(0, 255, 0));

	/** Argon */
	public final static Atom Ar = new Atom(18, "Ar", "Argon", -1, false,
			Color.BLACK);

	/** Potassium */
	public final static Atom K = new Atom(19, "K", "Potassium", -1, false,
			Color.BLACK);

	/** Calcium */
	public final static Atom Ca = new Atom(20, "Ca", "Calcium", -1, false,
			Color.BLACK);

	/** Scandium */
	public final static Atom Sc = new Atom(21, "Sc", "Scandium", -1, false,
			Color.BLACK);

	/** Titanium */
	public final static Atom Ti = new Atom(22, "Ti", "Titanium", -1, false,
			Color.BLACK);

	/** Vanadium */
	public final static Atom V = new Atom(23, "V", "Vanadium", -1, false,
			Color.BLACK);

	/** Chromium */
	public final static Atom Cr = new Atom(24, "Cr", "Chromium", -1, false,
			Color.BLACK);

	/** Manganese */
	public final static Atom Mn = new Atom(25, "Mn", "Manganese", -1, false,
			Color.BLACK);

	/** Iron */
	public final static Atom Fe = new Atom(26, "Fe", "Iron", -1, false,
			Color.BLACK);

	/** Cobalt */
	public final static Atom Co = new Atom(27, "Co", "Cobalt", -1, false,
			Color.BLACK);

	/** Nickel */
	public final static Atom Ni = new Atom(28, "Ni", "Nickel", -1, false,
			Color.BLACK);

	/** Copper */
	public final static Atom Cu = new Atom(29, "Cu", "Copper", -1, false,
			Color.BLACK);

	/** Zinc */
	public final static Atom Zn = new Atom(30, "Zn", "Zinc", -1, false,
			Color.BLACK);

	/** Gallium */
	public final static Atom Ga = new Atom(31, "Ga", "Gallium", -1, false,
			Color.BLACK);

	/** Germanium */
	public final static Atom Ge = new Atom(32, "Ge", "Germanium", -1, false,
			Color.BLACK);

	/** Arsenic */
	public final static Atom As = new Atom(33, "As", "Arsenic", -1, false,
			Color.BLACK);

	/** Selenium */
	public final static Atom Se = new Atom(34, "Se", "Selenium", -1, false,
			Color.BLACK);

	/** Bromine */
	public final static Atom Br = new Atom(35, "Br", "Bromine", 1, true,
			new Color(255, 0, 255));

	/** Krypton */
	public final static Atom Kr = new Atom(36, "Kr", "Krypton", -1, false,
			Color.BLACK);

	/** Rubidium */
	public final static Atom Rb = new Atom(37, "Rb", "Rubidium", -1, false,
			Color.BLACK);

	/** Strontium */
	public final static Atom Sr = new Atom(38, "Sr", "Strontium", -1, false,
			Color.BLACK);

	/** Yttrium */
	public final static Atom Y = new Atom(39, "Y", "Yttrium", -1, false,
			Color.BLACK);

	/** Zirconium */
	public final static Atom Zr = new Atom(40, "Zr", "Zirconium", -1, false,
			Color.BLACK);

	/** Niobium */
	public final static Atom Nb = new Atom(41, "Nb", "Niobium", -1, false,
			Color.BLACK);

	/** Molybdenum */
	public final static Atom Mo = new Atom(42, "Mo", "Molybdenum", -1, false,
			Color.BLACK);

	/** Technetium */
	public final static Atom Tc = new Atom(43, "Tc", "Technetium", -1, false,
			Color.BLACK);

	/** Ruthenium */
	public final static Atom Ru = new Atom(44, "Ru", "Ruthenium", -1, false,
			Color.BLACK);

	/** Rhodium */
	public final static Atom Rh = new Atom(45, "Rh", "Rhodium", -1, false,
			Color.BLACK);

	/** Palladium */
	public final static Atom Pd = new Atom(46, "Pd", "Palladium", -1, false,
			Color.BLACK);

	/** Silver */
	public final static Atom Ag = new Atom(47, "Ag", "Silver", -1, false,
			Color.BLACK);

	/** Cadmium */
	public final static Atom Cd = new Atom(48, "Cd", "Cadmium", -1, false,
			Color.BLACK);

	/** Indium */
	public final static Atom In = new Atom(49, "In", "Indium", -1, false,
			Color.BLACK);

	/** Tin */
	public final static Atom Sn = new Atom(50, "Sn", "Tin", -1, false,
			Color.BLACK);

	/** Antimony */
	public final static Atom Sb = new Atom(51, "Sb", "Antimony", -1, false,
			Color.BLACK);

	/** Tellurium */
	public final static Atom Te = new Atom(52, "Te", "Tellurium", -1, false,
			Color.BLACK);

	/** Iodine */
	public final static Atom I = new Atom(53, "I", "Iodine", 1, true,
			Color.BLACK);

	/** Xenon */
	public final static Atom Xe = new Atom(54, "Xe", "Xenon", -1, false,
			Color.BLACK);

	/** Caesium */
	public final static Atom Cs = new Atom(55, "Cs", "Caesium", -1, false,
			Color.BLACK);

	/** Barium */
	public final static Atom Ba = new Atom(56, "Ba", "Barium", -1, false,
			Color.BLACK);

	/** Lanthanum */
	public final static Atom La = new Atom(57, "La", "Lanthanum", -1, false,
			Color.BLACK);

	/** Cerium */
	public final static Atom Ce = new Atom(58, "Ce", "Cerium", -1, false,
			Color.BLACK);

	/** Praseodymium */
	public final static Atom Pr = new Atom(59, "Pr", "Praseodymium", -1, false,
			Color.BLACK);

	/** Neodymium */
	public final static Atom Nd = new Atom(60, "Nd", "Neodymium", -1, false,
			Color.BLACK);

	/** Promethium */
	public final static Atom Pm = new Atom(61, "Pm", "Promethium", -1, false,
			Color.BLACK);

	/** Samarium */
	public final static Atom Sm = new Atom(62, "Sm", "Samarium", -1, false,
			Color.BLACK);

	/** Europium */
	public final static Atom Eu = new Atom(63, "Eu", "Europium", -1, false,
			Color.BLACK);

	/** Gadolinium */
	public final static Atom Gd = new Atom(64, "Gd", "Gadolinium", -1, false,
			Color.BLACK);

	/** Terbium */
	public final static Atom Tb = new Atom(65, "Tb", "Terbium", -1, false,
			Color.BLACK);

	/** Dysprosium */
	public final static Atom Dy = new Atom(66, "Dy", "Dysprosium", -1, false,
			Color.BLACK);

	/** Holmium */
	public final static Atom Ho = new Atom(67, "Ho", "Holmium", -1, false,
			Color.BLACK);

	/** Erbium */
	public final static Atom Er = new Atom(68, "Er", "Erbium", -1, false,
			Color.BLACK);

	/** Thulium */
	public final static Atom Tm = new Atom(69, "Tm", "Thulium", -1, false,
			Color.BLACK);

	/** Ytterbium */
	public final static Atom Yb = new Atom(70, "Yb", "Ytterbium", -1, false,
			Color.BLACK);

	/** Lutetium */
	public final static Atom Lu = new Atom(71, "Lu", "Lutetium", -1, false,
			Color.BLACK);

	/** Hafnium */
	public final static Atom Hf = new Atom(72, "Hf", "Hafnium", -1, false,
			Color.BLACK);

	/** Tantalum */
	public final static Atom Ta = new Atom(73, "Ta", "Tantalum", -1, false,
			Color.BLACK);

	/** Tungsten */
	public final static Atom W = new Atom(74, "W", "Tungsten", -1, false,
			Color.BLACK);

	/** Rhenium */
	public final static Atom Re = new Atom(75, "Re", "Rhenium", -1, false,
			Color.BLACK);

	/** Osmium */
	public final static Atom Os = new Atom(76, "Os", "Osmium", -1, false,
			Color.BLACK);

	/** Iridium */
	public final static Atom Ir = new Atom(77, "Ir", "Iridium", -1, false,
			Color.BLACK);

	/** Platinum */
	public final static Atom Pt = new Atom(78, "Pt", "Platinum", -1, false,
			Color.BLACK);

	/** Gold */
	public final static Atom Au = new Atom(79, "Au", "Gold", -1, false,
			Color.BLACK);

	/** Mercury */
	public final static Atom Hg = new Atom(80, "Hg", "Mercury", -1, false,
			Color.BLACK);

	/** Thallium */
	public final static Atom Tl = new Atom(81, "Tl", "Thallium", -1, false,
			Color.BLACK);

	/** Lead */
	public final static Atom Pb = new Atom(82, "Pb", "Lead", -1, false,
			Color.BLACK);

	/** Bismuth */
	public final static Atom Bi = new Atom(83, "Bi", "Bismuth", -1, false,
			Color.BLACK);

	/** Polonium */
	public final static Atom Po = new Atom(84, "Po", "Polonium", -1, false,
			Color.BLACK);

	/** Astatine */
	public final static Atom At = new Atom(85, "At", "Astatine", -1, false,
			Color.BLACK);

	/** Radon */
	public final static Atom Rn = new Atom(86, "Rn", "Radon", -1, false,
			Color.BLACK);

	/** Francium */
	public final static Atom Fr = new Atom(87, "Fr", "Francium", -1, false,
			Color.BLACK);

	/** Radium */
	public final static Atom Ra = new Atom(88, "Ra", "Radium", -1, false,
			Color.BLACK);

	/** Actinium */
	public final static Atom Ac = new Atom(89, "Ac", "Actinium", -1, false,
			Color.BLACK);

	/** Thorium */
	public final static Atom Th = new Atom(90, "Th", "Thorium", -1, false,
			Color.BLACK);

	/** Protactinium */
	public final static Atom Pa = new Atom(91, "Pa", "Protactinium", -1, false,
			Color.BLACK);

	/** Uranium */
	public final static Atom U = new Atom(92, "U", "Uranium", -1, false,
			Color.BLACK);

	/** Neptunium */
	public final static Atom Np = new Atom(93, "Np", "Neptunium", -1, false,
			Color.BLACK);

	/** Plutonium */
	public final static Atom Pu = new Atom(94, "Pu", "Plutonium", -1, false,
			Color.BLACK);

	/** Americium */
	public final static Atom Am = new Atom(95, "Am", "Americium", -1, false,
			Color.BLACK);

	/** Curium */
	public final static Atom Cm = new Atom(96, "Cm", "Curium", -1, false,
			Color.BLACK);

	/** Berkelium */
	public final static Atom Bk = new Atom(97, "Bk", "Berkelium", -1, false,
			Color.BLACK);

	/** Californium */
	public final static Atom Cf = new Atom(98, "Cf", "Californium", -1, false,
			Color.BLACK);

	/** Einsteinium */
	public final static Atom Es = new Atom(99, "Es", "Einsteinium", -1, false,
			Color.BLACK);

	/** Fermium */
	public final static Atom Fm = new Atom(100, "Fm", "Fermium", -1, false,
			Color.BLACK);

	/** Mendelevium */
	public final static Atom Md = new Atom(101, "Md", "Mendelevium", -1, false,
			Color.BLACK);

	/** Nobelium */
	public final static Atom No = new Atom(102, "No", "Nobelium", -1, false,
			Color.BLACK);

	/** Lawrencium */
	public final static Atom Lr = new Atom(103, "Lr", "Lawrencium", -1, false,
			Color.BLACK);

	/** Rutherfordium */
	public final static Atom Rf = new Atom(104, "Rf", "Rutherfordium", -1,
			false, Color.BLACK);

	/** Dubnium */
	public final static Atom Db = new Atom(105, "Db", "Dubnium", -1, false,
			Color.BLACK);

	/** Seaborgium */
	public final static Atom Sg = new Atom(106, "Sg", "Seaborgium", -1, false,
			Color.BLACK);

	/** Bohrium */
	public final static Atom Bh = new Atom(107, "Bh", "Bohrium", -1, false,
			Color.BLACK);

	/** Hassium */
	public final static Atom Hs = new Atom(108, "Hs", "Hassium", -1, false,
			Color.BLACK);

	/** Meitnerium */
	public final static Atom Mt = new Atom(109, "Mt", "Meitnerium", -1, false,
			Color.BLACK);

	/** Darmstadtium */
	// public final static Atom Ds =new Atom( 110 ,"Ds", "Darmstadtium", -1,
	// false, Color.BLACK);
	/** Ununnilium */
	public final static Atom Uun = new Atom(110, "Uun", "Ununnilium", -1,
			false, Color.BLACK);

	/** Roentgenium */
	// public final static Atom Rg =new Atom( 111 ,"Rg", "Roentgenium", -1,
	// false, Color.BLACK);
	/** Unununium */
	public final static Atom Uuu = new Atom(111, "Uuu", "Unununium", -1, false,
			Color.BLACK);

	/** Ununbium */
	public final static Atom Uub = new Atom(112, "Uub", "Ununbium", -1, false,
			Color.BLACK);

	/** Ununtrium */
	public final static Atom Uut = new Atom(113, "Uut", "Ununtrium", -1, false,
			Color.BLACK);

	/** Ununquadium */
	public final static Atom Uuq = new Atom(114, "Uuq", "Ununquadium", -1,
			false, Color.BLACK);

	/** Ununpentium */
	public final static Atom Uup = new Atom(115, "Uup", "Ununpentium", -1,
			false, Color.BLACK);

	/** Ununhexium */
	public final static Atom Uuh = new Atom(116, "Uuh", "Ununhexium", -1,
			false, Color.BLACK);

	/** Ununseptium */
	public final static Atom Uus = new Atom(117, "Uus", "Ununseptium", -1,
			false, Color.BLACK);

	/** Ununoctium */
	public final static Atom Uuo = new Atom(118, "Uuo", "Ununoctium", -1,
			false, Color.BLACK);

	public final static Atom[] atoms = { Dummy, H, He, Li, Be, B, C, N, O, F,
			Ne, Na, Mg, Al, Si, P, S, Cl, Ar, K, Ca, Sc, Ti, V, Cr, Mn, Fe, Co,
			Ni, Cu, Zn, Ga, Ge, As, Se, Br, Kr, Rb, Sr, Y, Zr, Nb, Mo, Tc, Ru,
			Rh, Pd, Ag, Cd, In, Sn, Sb, Te, I, Xe, Cs, Ba, La, Ce, Pr, Nd, Pm,
			Sm, Eu, Gd, Tb, Dy, Ho, Er, Tm, Yb, Lu, Hf, Ta, W, Re, Os, Ir, Pt,
			Hg, Tl, Pb, Bi, Po, At, Rn, Fr, Ra, Ac, Au, Th, Pa, U, Np, Pu, Am,
			Cm, Bk, Cf, Es, Fm, Md, No, Lr, Rf, Db, Sg, Bh, Hs, Mt, Uun, Uuu,
			Uub, Uut, Uuq, Uup, Uuh, Uus, Uuo };

	final static int maxCharge = atoms.length;

	private final static LabelParser<Atom> ap = new LabelParser<Atom>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.parsemis.parsers.LabelParser#parse(java.lang.String)
		 */
		public Atom parse(String text) throws ParseException {
			return Atom.parse(text);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.parsemis.parsers.LabelParser#serialize(LabelType)
		 */
		public String serialize(Atom label) {
			return label.symbol;
		}
	};

	/**
	 * @return an iterator over all known atoms
	 */
	public static Iterator<Atom> iterator() {
		return new Iterator<Atom>() {
			private int pos = 1;

			public boolean hasNext() {
				return pos < maxCharge;
			}

			public Atom next() {
				if (pos < maxCharge) {
					return atoms[pos++];
				} else {
					throw new NoSuchElementException();
				}
			}

			public void remove() {
				throw new UnsupportedOperationException("remove not suported!");
			}
		};
	}

	/**
	 * parse the given string to a known atom
	 * 
	 * @param text
	 *            the name or the symbol of the atom
	 * @return the corresponding atom
	 * @throws ParseException
	 */
	public final static Atom parse(final String text) throws ParseException {
		for (int i = 0; i < atoms.length; ++i) {
			if (text.equals(atoms[i].symbol) || text.equals(atoms[i].name)) {
				return atoms[i];
			}
		}
		if (text.equals("Ds") || text.equals("Darmstadtium")) {
			return Uun;
		}
		if (text.equals("Rg") || text.equals("Roentgenium")) {
			return Uuu;
		}
		throw new ParseException("Unknown atom: " + text, -1);
	}

	/**
	 * @return a LabelParser for atoms
	 */
	public static LabelParser<Atom> parser() {
		return ap;
	}

	private final String name;

	final String symbol;

	private final int nuclearCharge;

	private final int valences;

	private final boolean organic;

	private Color color;

	private Atom(final int nc, final String symbol, final String name,
			final int valences, final boolean organic, final Color def) {
		this.nuclearCharge = nc;
		this.symbol = symbol;
		this.valences = valences;
		this.organic = organic;
		this.name = name;
		color = def;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final Atom arg0) {
		return arg0.nuclearCharge - this.nuclearCharge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object arg0) {
		if (arg0 instanceof Atom) {
			return ((Atom) arg0).nuclearCharge == this.nuclearCharge;
		}
		return false;
	}

	/**
	 * @return the Color the atom shall be represented (default: Color.BLACK)
	 */
	public Color getColor() {
		return color;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return nuclearCharge;
	}

	/**
	 * @return <code>true</code>, if the atom is an organic atom
	 */
	public boolean isOrganic() {
		return organic;
	}

	/**
	 * @return the nucleare charge of the current atom
	 */
	public int nucleareCharge() {
		return this.nuclearCharge;
	}

	/**
	 * changes the display color for the given atom
	 * 
	 * @param color
	 */
	public void setColor(final Color color) {
		this.color = color;
	}

	/**
	 * @return the (short) symbol of the current atom
	 */
	public String symbol() {
		return symbol;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return symbol + "(" + nuclearCharge + ")";
	}

	/**
	 * @return the numbers of valence electorns for the current atom
	 */
	public int valenceElectrons() {
		return valences;
	}

}
