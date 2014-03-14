/**
 * Created on Jun 18, 2007
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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * diese Klasse kontrolliert die Groesse des Eingabefeldes bei einem Textfeld
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 */
class LimitedDoc extends PlainDocument {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int limit = 20;

	LimitedDoc(final int l) {
		super();
		limit = l;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.text.PlainDocument#insertString(int, java.lang.String,
	 *      javax.swing.text.AttributeSet)
	 */
	@Override
	public void insertString(final int offs, final String str,
			final AttributeSet a) throws BadLocationException {
		if (super.getLength() + str.length() < limit) {
			super.insertString(offs, str, a);
		} else {
			super.insertString(offs, str
					.substring(0, limit - super.getLength()), a);
		}
	}
}
