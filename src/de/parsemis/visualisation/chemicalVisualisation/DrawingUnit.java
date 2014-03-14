/**
 * Created on Jun 28, 2005
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

import java.util.HashSet;

/**
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 * Dieses Interface ermoeglicht es, alle Kreuzungselemente gleich zu behandeln.
 * 
 */
public interface DrawingUnit {

	/**
	 * @return groups
	 */
	public HashSet<Integer>[] getGroups();

	/**
	 * initialize groups
	 */
	public void initGroups();
}
