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

package xmlbs;

/**
 * Interface for writing document structure descriptions.
 *
 * @author R.W. van 't Veer
 * @version $Revision: 1.4 $
 */
public interface DocumentStructure {
    /**
     * Determine if tag is known.
     * @param tag tag token to lookup
     * @return true if tag is known
     */
    boolean isKnownTag (TagToken tag);

    /**
     * Retain only known attributes.
     * <P><EM>TODO return number of modifications?</EM></P>
     * @param tag tag token to handle
     */
    void retainKnownAttributes (TagToken tag);

    /**
     * Determine if tag can be placed into other tag.
     * @param parent top tag
     * @param child sub tag
     * @return true when allowed
     */
    boolean canContain (TagToken parent, Token child);

    /**
     * Determine if tag can have children
     * @param tag tag to lookup
     * @return true when tag can no thave children
     */
    boolean isEndpoint (TagToken tag);
}
