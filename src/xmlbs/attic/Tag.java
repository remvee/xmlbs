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
class Tag
{
    String raw;
    String tagName;
    Map attrs = new HashMap();
    int type;

    final static int OPEN = 0;
    final static int CLOSE = 1;
    final static int EMPTY = 2;
    final static int SPECIAL = 3;
    final static int DECLARATION = 4;

    final static int BUFFER_SIZE = 64*1024;

    static RE closeRe;
    static RE emptyRe;
    static RE specialRe;
    static RE declRe;

    static RE nameRe;

    static RE attRe;
    static RE valRe1;
    static RE valRe2;
    static RE valRe3;

    static
    {
	try
	{
	    closeRe = new RE("^\\s*/");
	    emptyRe = new RE("/\\s*$");
	    specialRe = new RE("^\\s*\\?");
	    declRe = new RE("^\\s*\\!");

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

    /**
     * Try to read a tag from a stream.  The '&lt;' character was
     * already read.
     * @param in stream to read from
     * @throws Exception when tag could not be read, this
     * possibly a stray '&lt;' character.
     */
    public Tag (InputStream in)
    throws IOException, Exception
    {
	// try to read full tag
	StringBuffer b = new StringBuffer();
	int c;
	in.mark(BUFFER_SIZE);
	while ((c = in.read()) != -1)
	{
	    if (c == '<') // illegal in attribute value TODO allow it?
	    {
		in.reset();
		// TODO signalling stray < is expensive!
		throw new Exception();
	    }
	    // TODO allow > to be in attribute value
	    if (c == '>') break;
	    b.append((char) c);
	}
	// TODO handle if c == -1
	raw = b.toString();

	// determine tag type
	{
	    if (closeRe.match(raw)) type = CLOSE;
	    else if (emptyRe.match(raw)) type = EMPTY;
	    else if (specialRe.match(raw)) type = SPECIAL; // TODO handle it!!
	    else if (declRe.match(raw)) type = DECLARATION; // TODO handle it!!
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

    /**
     * Create a new tag from given data.
     * @param tagName name of tag
     * @param attrs map of attributes
     * @param type one of <TT>CLOSE</TT>, <TT>EMPTY</TT>,
     * <TT>SPECIAL</TT>, <TT>DECLARATION</TT>.
     */
    public Tag (String tagName, Map attrs, int type)
    {
	this.tagName = tagName;
	this.attrs = attrs;
	this.type = type;
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
     * Get close version for this tag.
     */
    public Tag closeTag () { return new Tag(getName(), null, Tag.CLOSE); }
    /**
     * Get empty version of this tag.
     */
    public Tag emptyTag () { return new Tag(getName(), attrs, Tag.EMPTY); }

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
	else if (type == SPECIAL || type == DECLARATION) // TODO
	{
	    sb.append(raw);
	    sb.append('>');

	    return sb.toString();
	}

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
		sb.append(Text.fixText(val));
		sb.append('"');
	    }
	}

	if (type == EMPTY) sb.append('/');
	sb.append('>');

	return sb.toString();
    }

    /**
     * TODO this violates java rules!
     */
    public boolean equals (Object that)
    {
	if (that instanceof Tag)
	{
	    Tag t = (Tag) that;
	    if (t.type == type && t.getName().equals(tagName))
	    {
		return true;
	    }
	}
	return false;
    }

    /**
     * TODO this violates java rules!
     */
    public int hashCode ()
    {
	if (tagName != null) return tagName.hashCode();
	return 0;
    }
}
