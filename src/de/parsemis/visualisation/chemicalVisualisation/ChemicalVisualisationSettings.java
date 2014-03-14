/**
 * Created on Mar 30, 2005
 *
 * @by Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 * Copyright 2005 Olga Urzova
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.visualisation.chemicalVisualisation;

/**
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ChemicalVisualisationSettings {

	/**
	 * Die Groesse von dem Fenster setsen, in dem das Molekuel gezeichnet wird.
	 * Die Standardeinstellung ist 500
	 */
	public static int FrameSize = 0;

	/**
	 * Die Labels fuer die Kohlenstoffatome setzen
	 */
	public static boolean setCarbonLabels = false;

	/** */
	public static boolean setShortBonds = true;

	/** */
	public static boolean doColoredLabels = true;

	/** */
	public static String ColorSchemaFileName = "src/de/parsemis/visualisation/colorschema.props";

	/**
	 * Diese Variable auf true setzen, um das Molekuelbild in einer Datei zu
	 * speichern
	 */
	public static boolean doExportInRasterFormat = true;

	/**
	 * Wenn der Pfad fuer den Export nicht gesetzt ist, wird die Datei in dem
	 * Arbeitsverzeichnis gespeichert
	 */
	public static String exportPath = null;

	/**
	 * Den Name der Datei eingeben, sonst heisst es "newimage"
	 */
	public static String exportFileName = null;

	/**
	 * Die Endung(en) fuer die Datei(en) eingeben, sonst wird eine .png-Datei
	 * erzeugt
	 */
	public static String[] exportFileFormats = null;

	/**
	 * Die Breite des Bildes setzen. Die Standardeinstellung ist 500
	 */
	public static int imageWidth = 0;

	/**
	 * Die Hoehe des Bildes setzen. Die Standardeinstellung ist 500
	 */
	public static int imageHeight = 0;

}
