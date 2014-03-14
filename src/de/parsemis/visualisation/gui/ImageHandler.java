/**
 * Created on Apr 20, 2007
 *
 * @by Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 * Copyright 2007 Olga Urzova
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.visualisation.gui;

import javax.swing.ImageIcon;

/**
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 */
public class ImageHandler {
	/**
	 * Returns an ImageIcon, or null if the path was invalid.
	 * 
	 * @param path
	 * @return ImageIcon
	 */
	public static ImageIcon createImageIcon(final String path) {
		return new ImageIcon(path);

	}
}
