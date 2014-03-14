/**
 * created May 24, 2006
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
package de.parsemis.parsers;

import java.io.Serializable;
import java.text.ParseException;

/**
 * This interface encapsulates the ability to read and write different label
 * types.
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <LabelType>
 *            the type of the parseable label
 */
public interface LabelParser<LabelType> extends Serializable {

	/**
	 * @param text
	 * @return the label represented by the given text
	 * @throws ParseException
	 */
	public LabelType parse(String text) throws ParseException;

	/**
	 * @param label
	 * @return the string representing the given label
	 */
	public String serialize(LabelType label);

}
