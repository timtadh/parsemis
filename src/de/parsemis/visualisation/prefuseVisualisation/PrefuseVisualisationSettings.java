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
package de.parsemis.visualisation.prefuseVisualisation;

import prefuse.util.ColorLib;

/**
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 */
public class PrefuseVisualisationSettings {

	public static boolean setHighQuality = false;

	public static int nodeColor = ColorLib.rgb(200, 221, 242);

	public static int edgeColor = ColorLib.gray(100);

	public static int nodeTextColor = ColorLib.gray(80);

	/**
	 * new Settings
	 */
	public PrefuseVisualisationSettings() {
	}
}
