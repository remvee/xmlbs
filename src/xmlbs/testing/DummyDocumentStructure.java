/*
 * xmlbs
 *
 * Copyright (C) 2002  R.W. van 't Veer
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 */

package xmlbs.testing;

import xmlbs.*;

/**
 * @author R.W. van 't Veer
 * @version $Revision: 1.1 $
 */
public class DummyDocumentStructure implements DocumentStructure {
    /**
     * Do nothing..
     * @param icase ignored parameter
     */
    public void setIgnoreCase (boolean icase) {
	// nop
    }

    /**
     * @return false
     */
    public boolean getIgnoreCase () {
	return false;
    }

    /**
     * Do nothing..
     * @param name ignored parameter
     * @return given name
     */
    public String getTagName (String name) {
	return name;
    }

    /**
     * Do nothing..
     * @param name ignored parameter
     * @param attr ignored parameter
     * @return given attr parameter
     */
    public String getTagAttribute (String name, String attr) {
	return attr;
    }

    /**
     * Do nothing..
     * @param tag ignored parameter
     * @return true
     */
    public boolean isKnownTag (TagToken tag) {
	return true;
    }

    /**
     * Do nothing..
     * @param tag ignored parameter
     */
    public void retainKnownAttributes (TagToken tag) {
	// nop
    }

    /**
     * Do nothing..
     * @param parent ignored parameter
     * @param child ignored parameter
     * @return true
     */
    public boolean canContain (TagToken parent, Token child) {
	return true;
    }
}
