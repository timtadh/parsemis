/**
 * created Nov 13, 2007
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
package de.parsemis.jp;

/**
 * This class is a small helper class to simple get acces to the local host name
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 */
public class Host {
	/**
	 * @return the name of the current host (or any other String)
	 */
	public final static String name() {
		// if (true) return "none";
		try {
			final java.net.InetAddress localMachine = java.net.InetAddress
					.getLocalHost();
			return localMachine.getHostName();
		} catch (final NumberFormatException ign) {
		} catch (final Exception ign) {
		}
		return "not found";
	}
}
