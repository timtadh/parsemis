/**
 * Created Jun 12, 2007
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
import java.util.Map;
import java.util.Set;

/**
 * This interface encapsulate the ability to read and write different label
 * types
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <LabelType>
 */
public interface GraphmlLabelParser<LabelType> extends LabelParser<LabelType> {

	/**
	 * @return a Set of all possible attribute names
	 */
	public Set<String> attributes();

	/**
	 * @param map
	 * @return the label represented by the given map
	 * @throws ParseException
	 */
	public LabelType parseML(Map<String, String> map) throws ParseException;

	/**
	 * @param label
	 * @return the Map representing the given label
	 */
	public Map<String, String> serializeML(LabelType label);

	/**
	 * compares and set the attributes for this Parser
	 * 
	 * @param foundAttributes
	 * @return <code>true</code>, if the attributes are possible, otherwise
	 *         <code>false</code>
	 */
	public boolean setAttributes(Set<String> foundAttributes);

}
