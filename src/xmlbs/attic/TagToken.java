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

import java.io.*;
import java.util.*;

import org.apache.regexp.*;

/**
 * A tag object.
 */
class TagToken implements Token
{
    int type;
    String tagName;
    Map attrs = new HashMap();

    final static int OPEN = 0;
    final static int CLOSE = 1;
    final static int EMPTY = 2;

    static RE closeRe, emptyRe;
    static RE nameRe;
    static RE attRe, valRe1, valRe2, valRe3;

    static
    {
	try
	{
	    closeRe = new RE("^\\s*/");
	    emptyRe = new RE("/\\s*$");

	    nameRe = new RE("([a-zA-Z0-9:]+)\\s*");

	    attRe = new RE("\\s([a-zA-Z_:][a-zA-Z0-9]*)\\s*=");
	    valRe1 = new RE("^\\s*=\\s*'([^']*)'");
	    valRe2 = new RE("^\\s*=\\s*\"([^\"]*)\"");
	    valRe3 = new RE("^\\s*=\\s*([^\\s]*)");
	}
	catch (RESyntaxException ex)
	{
	    throw new RuntimeException(ex.toString());
	}
    }

    public TagToken (String raw)
    {
	// determine tag type
	{
	    if (closeRe.match(raw)) type = CLOSE;
	    else if (emptyRe.match(raw)) type = EMPTY;
	    else type = OPEN;
	}

	// determine tag name
	{
	    nameRe.match(raw);
	    tagName = nameRe.getParen(1);
	}

	// collect attributes
	{
	    int pos = 0;
	    while (attRe.match(raw, pos))
	    {
		String attr = attRe.getParen(1);
		pos = attRe.getParenEnd(1);
		String valStr = raw.substring(pos);

		String val = null;
		RE valRe = null;
		if (valRe1.match(valStr)) valRe = valRe1;
		else if (valRe2.match(valStr)) valRe = valRe2;
		else if (valRe3.match(valStr)) valRe = valRe3;
		else break;
		val = valRe.getParen(1); // TODO handle entity and char refs
		pos += valRe.getParenEnd(1);

		attrs.put(attr, val);
	    }
	}
    }

    public TagToken (String tagName, Map attrs, int type)
    {
	this.tagName = tagName;
	this.attrs = attrs;
	this.type = type;
    }

    public TagToken emptyTag ()
    {
	TagToken tok = new TagToken(tagName, attrs, EMPTY);
	return tok;
    }

    public TagToken closeTag ()
    {
	TagToken tok = new TagToken(tagName, attrs, CLOSE);
	return tok;
    }

    /**
     * @return tag name
     */
    public String getName () { return tagName; }
    /**
     * @return true if this is a open tag
     */
    public boolean isOpenTag () { return type == OPEN; }
    /**
     * @return true if this is a close tag
     */
    public boolean isCloseTag () { return type == CLOSE; }
    /**
     * @return true if this is a empty tag
     */
    public boolean isEmptyTag () { return type == EMPTY; }
    /**
     * @return true if name of given tag matches
     */
    public boolean isSameTag (TagToken tag) { return tagName.equals(tag.getName()); }

    /**
     * @return proper string representation of this tag
     */
    public String toString ()
    {
	StringBuffer sb = new StringBuffer();

	sb.append('<');

	if (type == CLOSE)
	{
	    sb.append('/');
	    sb.append(tagName);
	    sb.append('>');

	    return sb.toString();
	}

	// else OPEN or EMPTY
	sb.append(tagName);

	if (attrs != null)
	{
	    List l = new Vector(attrs.keySet());
	    Collections.sort(l);
	    Iterator it = l.iterator();
	    while (it.hasNext())
	    {
		String attr = (String) it.next();
		String val = (String) attrs.get(attr);

		sb.append(' ');
		sb.append(attr);
		sb.append('=');
		sb.append('"');
		sb.append(TextToken.fixText(val));
		sb.append('"');
	    }
	}

	if (type == EMPTY) sb.append('/');
	sb.append('>');

	return sb.toString();
    }
}
